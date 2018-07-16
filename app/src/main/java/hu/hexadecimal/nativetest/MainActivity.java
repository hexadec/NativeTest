package hu.hexadecimal.nativetest;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;
import java.lang.Math;

public class MainActivity extends Activity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    public static String TAG = "NativeTest";

    /* Constants for arrays and random numbers */
    int array_size = 8192;
    final int pos = 2040;
    final int min = 100;
    final int max = 2048*2048;
    int number = max + 1;
    int[] array;
    int[] array_native;

    /* Arrays for prime numbers */
    int[] arr0;
    int[] arr1;
    int[] arr2;
    int[] arr3;
    int pos0, pos1, pos2, pos3 = 0;

    /* How many different checks do we want? */
    static final int ITERATIONS = 5;

    /* How many times run each test */
    static final int RUNS = 4;
    int CURRENT_RUN_ID = 0;

    /* How many experiments do we have? */
    static final int EXPERIMENTS = 4;

    /* Times */
    long start_time, end_time = 0;
    long div_native_time, div_java_time = 0;
    long random_native_time, random_java_time = 0;
    long array_native_time, array_java_time = 0;
    long prime_native_time, prime_java_time = 0;

    /* Results */
    long div_native_result, div_java_result = 0;
    int random_native_result, random_java_result = 0;
    int array_native_result, array_java_result = 0;
    int[] prime_native_result;
    int prime_java_result = 0;

    /* Variables */
    long DIVIDEND = 1000;
    final int MAX_PRIME = 20000;
    TextView divr, randomr, arrayr, primer;
    LinkedList<Results> allResult;
    Thread t;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Trying to purge every optimization done on Java code...
        deleteCache();

        /*Generating number to divide*/
        int r = new Random().nextInt(100000);
        DIVIDEND = 1000000000L + r;

        divr = findViewById(R.id.div_result);
        randomr = findViewById(R.id.random_result);
        arrayr = findViewById(R.id.array_result);
        primer = findViewById(R.id.prime_result);
        //-1 is not necessary as ITERATIONS start from -1!
        array = new int[( array_size * ((int)Math.pow(2, ITERATIONS)))];
        array_native = new int[array.length];
        Log.e(TAG, "-----------"+ array_size);
        //Not using UI thread w/ long operations to avoid being killed
        allResult = new LinkedList<>();
        final Thread runMore = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < RUNS; i++) {
                    try {
                        if (t != null && t.isAlive()) {
                            t.join();
                            //Restore array size to original
                            // +1 since there is a *2 at the end of each run (even the last)
                            array_size /= Math.pow(2, ITERATIONS + 1);
                        }
                        CURRENT_RUN_ID = i;
                        t = new Thread(toRun);
                        t.start();
                        Log.w(TAG, "--------------- RUN TIMES --------: " + i);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Fail in runMore");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "Failed to run experiment multiple times...", Toast.LENGTH_LONG).show();
                            }
                        });
                        break;
                    } catch (IllegalThreadStateException ie) {
                        ie.printStackTrace();
                        Log.e(TAG, "Fail in runMore - illegal thread state...");
                    }
                }
            }
        });
        runMore.start();

        Thread save = new Thread(new Runnable() {
            @Override
            public void run() {
                SystemClock.sleep(5000);
                try {
                    runMore.join();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (allResult.size() % EXPERIMENTS != 0) {
                    Log.e(TAG, "There is an incorrect number of EXPERIMENTS!");
                    return;
                }
                for(Results r : allResult) {
                    File f = new File(Environment.getExternalStorageDirectory() + "/Results-" + r.getName() + ".csv");
                    try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                            new FileOutputStream(f), "UTF-8"))) {
                        writer.write(r.toCSV(1));
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Cannot save results! -- " + r.getName());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this,
                                        "Cannot save files, please enable Storage permission in settings", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
                Log.w(TAG, "Files written: " + allResult.size());
            }
        });
        //Suppose the user knows that it is considered stupid to disallow data saving...
        if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
        save.start();
    }


    public long divisorsJava(long random) {
        long divs = 1;
        long upper_limit = (long) Math.sqrt(random);
        for (long c = 2; c <= upper_limit; c++) {
            if (random % c == 0) divs++;
        }
        return divs*2;
    }

    public int generateRandomJava(boolean which, int size) {
        if (which)
            number = min - 1;
        Random r = new Random();
        for (int i = 0; i < size; i++)
            array[i] = r.nextInt(max-min)+min;
        return (array[pos] = max + 1);
    }

    public int orderArrayJava(int size) {
        Arrays.sort(array, 0, size);
        int found = -1;
        for (int i = 0; i < size; i++) {
            if (array[i] == number) {
                found = i;
                break;
            }
        }
        Log.i(TAG, "Array: " + array[0] + " - " + array[100] + " - " + array[size / 2]);
        return found;
    }

    public void primesUntilXJava(final int x) {
        int max = 0;
        if (x <= 100) max = 40;
        else {
            double result = x / (Math.log10(x) - 1) * 1.1;
            max = (int) Math.ceil(result);
        }
        arr0 = new int[max];
        arr1 = new int[max / 3 * 2];
        arr2 = new int[max / 3 * 2];
        arr3 = new int[max / 2];
        Thread t0 = new Thread(new Runnable() {
            @Override
            public void run() {
                pos0 = calculate(2, x / 4, arr0);
            }
        });
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                pos1 = calculate(x / 4, x / 2, arr1);
            }
        });
        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                pos2 = calculate(x / 2, x / 4 * 3, arr2);
            }
        });
        t0.start();
        t1.start();
        t2.start();
        pos3 = calculate(x/ 4 * 3, x, arr3);
        try {
            t0.join();
            t1.join();
            t2.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.arraycopy(arr1, 0, arr0, pos0, pos1);
        arr1 = null;
        System.arraycopy(arr2, 0, arr0, pos0 + pos1, pos2);
        arr2 = null;
        System.arraycopy(arr3, 0, arr0, pos0 + pos1 + pos2, pos3);
        arr3 = null;
        pos0 += pos1 + pos2 + pos3;
    }

    public int calculate(int from, int to, int[] in) {
        int pos = 0;
        for (; from < to; from++) {
            int until = (int) Math.sqrt(from);
            boolean divs = false;
            for (long i = 2; i <= until; i++) {
                if (from % i == 0) {
                    divs = true;
                    break;
                }
            }
            if (!divs) {
                in[pos++] = from;
            }
        }
        return pos;
    }

    public native long getDivisors(long number);
    public native int generateRandom(boolean which, int size);
    public native int orderArray(int[] array, int size);
    public native int[] primesUntilX(int x);

    @Override
    protected void onDestroy() {
        deleteCache();
        System.exit(0);
        super.onDestroy();
    }

    private void deleteCache() {
        File cache = MainActivity.this.getApplicationContext().getCacheDir();
        File ccache = MainActivity.this.getApplicationContext().getCodeCacheDir();
        deleteFolder(cache);
        deleteFolder(ccache);
    }

    private void deleteFolder(File folder) {
        if (folder.isDirectory()) {
            for (File sub : folder.listFiles()) {
                deleteFolder(sub);
            }
        }

    }


    @Override
    public void onBackPressed() {
        deleteCache();
        System.exit(0);
        super.onBackPressed();
    }

    private final Runnable toRun = new Runnable() {
        @Override
        public void run() {

            Results res = new Results("Divisors", "dividend", "ms", "ms", -6);
            long div_secondary = DIVIDEND;
            int experiment_id = 0;
            for (int i = -1; i < ITERATIONS; i++) {
                Log.d(TAG, "Divisors - Starting up: native");
                start_time = System.nanoTime();
                div_native_result = getDivisors(div_secondary);
                end_time = System.nanoTime();
                div_native_time = end_time - start_time;

                Log.d(TAG, "Native done, starting java");
                start_time = System.nanoTime();
                div_java_result = divisorsJava(div_secondary);
                end_time = System.nanoTime();
                div_java_time = end_time - start_time;

                if (i >= 0) res.addTriple(div_secondary, div_native_time, div_java_time);
                div_secondary *= 10;
                deleteCache();
            }
            //TODO run 5 times with different dividend
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    divr.setText(Html.fromHtml("<b>Divisors </b>(" + DIVIDEND +")<br/>Native: " + String.format("%.1f", div_native_time / 1000000.0) + " ms\nJava: " + String.format("%.1f", div_java_time / 1000000.0) + " ms"));
                    Toast.makeText(MainActivity.this, div_native_result + "/" + div_java_result, Toast.LENGTH_SHORT).show();
                }
            });
            if (CURRENT_RUN_ID == 0) {
                allResult.addLast(res);
            } else {
                res = Results.average(allResult.get(experiment_id), res, 1 + CURRENT_RUN_ID);
                allResult.remove(experiment_id);
                allResult.add(experiment_id++, res);
            }

            res = new Results("Random", "Array-size", "ms", "ms", -6);
            Results res2 = new Results("Array-order", "Array-size", "ms", "ms", -6);
            for (int i = -1; i < ITERATIONS; i++) {
                Log.d(TAG, "Random - Starting up: native");
                start_time = System.nanoTime();
                random_native_result = generateRandom(false, array_size);
                end_time = System.nanoTime();
                random_native_time = end_time - start_time;

                Log.d(TAG, "Native done, starting java");
                start_time = System.nanoTime();
                random_java_result = generateRandomJava(false, array_size);
                end_time = System.nanoTime();
                random_java_time = end_time - start_time;

                if (i >= 0) res.addTriple(array_size, random_native_time, random_java_time);

                Log.d(TAG, "Arrays - Creating a copy for C++ code of the array");
                //int[] array_native = new int[array_size];
                System.arraycopy(array, 0, array_native, 0, array_size);
                Log.d(TAG, "Arrays - Starting up: native");
                start_time = System.nanoTime();
                array_native_result = orderArray(array_native, array_size);
                end_time = System.nanoTime();
                array_native_time = end_time - start_time;

                Log.d(TAG, "Native done, starting java");
                start_time = System.nanoTime();
                array_java_result = orderArrayJava(array_size);
                end_time = System.nanoTime();
                array_java_time = end_time - start_time;

                if (i >= 0) res2.addTriple(array_size, array_native_time, array_java_time);

                array_size *= 2;
                //array = null;
                deleteCache();
            }
            if (CURRENT_RUN_ID == 0) {
                allResult.addLast(res);
            } else {
                res = Results.average(allResult.get(experiment_id), res, 1 + CURRENT_RUN_ID);
                allResult.remove(experiment_id);
                allResult.add(experiment_id++, res);
            }
            if (CURRENT_RUN_ID == 0) {
                allResult.addLast(res2);
            } else {
                res2 = Results.average(allResult.get(experiment_id), res2, 1 + CURRENT_RUN_ID);
                allResult.remove(experiment_id);
                allResult.add(experiment_id++, res2);
            }

            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    randomr.setText(Html.fromHtml("<b>Random integer generation: </b>(" + array_size + ")<br/>Native: " + String.format("%.1f", random_native_time / 1000000.0) + " ms\nJava: " + String.format("%.1f", random_java_time / 1000000.0)  + " ms"));
                    Toast.makeText(MainActivity.this, random_native_result + "/" + random_java_result, Toast.LENGTH_SHORT).show();
                }
            });

            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    arrayr.setText(Html.fromHtml("<b>Arrays order: </b>(" + array_size + ")<br/>Native: " + String.format("%.1f", array_native_time / 1000000.0) + " ms\nJava: " + String.format("%.1f", array_java_time / 1000000.0) + " ms"));
                    Toast.makeText(MainActivity.this, array_native_result + "/" + array_java_result, Toast.LENGTH_SHORT).show();
                }
            });

            res = new Results("Primes", "Max prime", "ms", "ms", 0);
            int prime_secondary = MAX_PRIME;
            for (int i = -1; i < ITERATIONS - 1; i++) {
                Log.d(TAG, "Primes - Starting up: native");
                start_time = System.currentTimeMillis();
                prime_native_result = primesUntilX(prime_secondary);
                end_time = System.currentTimeMillis();
                prime_native_time = end_time - start_time;

                Log.i(TAG, "1. " + prime_native_result[0] + " Half. " +
                        prime_native_result[prime_native_result.length / 2] + " Max. " +
                        prime_native_result[prime_native_result.length - 1]);

                Log.d(TAG, "Native done, starting java");
                start_time = System.currentTimeMillis();
                primesUntilXJava(prime_secondary);
                prime_java_result = pos0;
                end_time = System.currentTimeMillis();
                prime_java_time = end_time - start_time;

                Log.i(TAG, "1. " + arr0[0] + " Half. (" +
                        prime_java_result / 2 + ") " +
                        arr0[prime_java_result / 2] + " Max. " +
                        arr0[prime_java_result - 1]);

                if (i >= 0) res.addTriple(prime_secondary, prime_native_time, prime_java_time);
                prime_secondary *= 5;
                deleteCache();
            }
            if (CURRENT_RUN_ID == 0) {
                allResult.addLast(res);
            } else {
                res = Results.average(allResult.get(experiment_id), res, 1 + CURRENT_RUN_ID);
                allResult.remove(experiment_id);
                allResult.add(experiment_id++, res);
            }

            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    primer.setText(Html.fromHtml("<b>MultiThreading / Prime finding: </b>(" + MAX_PRIME + ")<br/>Native: " + prime_native_time + " ms\nJava: " + prime_java_time + " ms"));
                    Toast.makeText(MainActivity.this, prime_native_result.length + "/" + prime_java_result, Toast.LENGTH_SHORT).show();
                }
            });
        }
    };
}
