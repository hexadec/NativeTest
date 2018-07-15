package hu.hexadecimal.nativetest;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    /* Arrays for prime numbers */
    long[] arr0;
    long[] arr1;
    long[] arr2;
    long[] arr3;
    int pos0, pos1, pos2, pos3 = 0;

    /* How many different checks do we want? */
    static final int ITERATIONS = 5;

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
    long[] prime_native_result;
    int prime_java_result = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Trying to purge every optimization done on Java code...
        File homedir = MainActivity.this.getApplicationContext().getDataDir();
        deleteFolder(homedir);

        final TextView divr = findViewById(R.id.div_result);
        final TextView randomr = findViewById(R.id.random_result);
        final TextView arrayr = findViewById(R.id.array_result);
        final TextView primer = findViewById(R.id.prime_result);
        //Not using UI thread w/ long operations to avoid being killed
        final LinkedList<Results> allResult = new LinkedList<>();
        final Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                /*Generating number to divide*/
                int r = new Random().nextInt(100000);
                final long DIVIDEND = 10000000000L + r;
                /*Maximum prime number to find*/
                final int MAX_PRIME = 50000;

                Results res = new Results("Divisors", "dividend", "ms", "ms", -6);
                long div_secondary = DIVIDEND;
                for (int i = 0; i < ITERATIONS; i++) {
                    Log.i(TAG, "Divisors - Starting up: native");
                    start_time = System.nanoTime();
                    div_native_result = getDivisors(div_secondary);
                    end_time = System.nanoTime();
                    div_native_time = end_time - start_time;

                    Log.i(TAG, "Native done, starting java");
                    start_time = System.nanoTime();
                    div_java_result = divisorsJava(div_secondary);
                    end_time = System.nanoTime();
                    div_java_time = end_time - start_time;

                    res.addTriple(div_secondary, div_native_time, div_java_time);
                    div_secondary *= 10;
                }
                //TODO run 5 times with different dividend
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        divr.setText(Html.fromHtml("<b>Divisors </b>(" + DIVIDEND +")<br/>Native: " + String.format("%.1f", div_native_time / 1000000.0) + " ms\nJava: " + String.format("%.1f", div_java_time / 1000000.0) + " ms", Html.FROM_HTML_MODE_LEGACY));
                        Toast.makeText(MainActivity.this, div_native_result + "/" + div_java_result, Toast.LENGTH_SHORT).show();
                    }
                });
                allResult.add(res);

                res = new Results("Random", "Array-size", "ms", "ms", -6);
                Results res2 = new Results("Array-order", "Array-size", "ms", "ms", -6);
                for (int i = 0; i < ITERATIONS; i++) {
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

                    res.addTriple(array_size, random_native_time, random_java_time);

                    Log.d(TAG, "Arrays - Creating a copy for C++ code of the array");
                    int[] array_native = new int[array_size];
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

                    res2.addTriple(array_size, array_native_time, array_java_time);

                    array_size *= 2;
                }
                allResult.add(res);
                allResult.add(res2);

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        randomr.setText(Html.fromHtml("<b>Random integer generation: </b>(" + array_size + ")<br/>Native: " + String.format("%.1f", random_native_time / 1000000.0) + " ms\nJava: " + String.format("%.1f", random_java_time / 1000000.0)  + " ms", Html.FROM_HTML_MODE_LEGACY));
                        Toast.makeText(MainActivity.this, random_native_result + "/" + random_java_result, Toast.LENGTH_SHORT).show();
                    }
                });

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        arrayr.setText(Html.fromHtml("<b>Arrays order: </b>(" + array_size + ")<br/>Native: " + String.format("%.1f", array_native_time / 1000000.0) + " ms\nJava: " + String.format("%.1f", array_java_time / 1000000.0) + " ms", Html.FROM_HTML_MODE_LEGACY));
                        Toast.makeText(MainActivity.this, array_native_result + "/" + array_java_result, Toast.LENGTH_SHORT).show();
                    }
                });

                res = new Results("Primes", "Max prime", "ms", "ms", 0);
                long prime_secondary = MAX_PRIME;
                for (int i = 0; i < ITERATIONS - 1; i++) {
                    Log.d(TAG, "Primes - Starting up: native");
                    start_time = System.currentTimeMillis();
                    prime_native_result = primesUntilX(prime_secondary);
                    end_time = System.currentTimeMillis();
                    prime_native_time = end_time - start_time;

                    Log.e(TAG, "1. " + prime_native_result[0] + " Half. " +
                            prime_native_result[prime_native_result.length / 2] + " Max. " +
                            prime_native_result[prime_native_result.length - 1]);

                    Log.d(TAG, "Native done, starting java");
                    start_time = System.currentTimeMillis();
                    primesUntilXJava(prime_secondary);
                    prime_java_result = pos0;
                    end_time = System.currentTimeMillis();
                    prime_java_time = end_time - start_time;

                    Log.e(TAG, "1. " + arr0[0] + " Half. (" +
                            prime_java_result / 2 + ") " +
                            arr0[prime_java_result / 2] + " Max. " +
                            arr0[prime_java_result - 1]);

                    res.addTriple(prime_secondary, prime_native_time, prime_java_time);
                    prime_secondary *= 5;
                }
                allResult.add(res);

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        primer.setText(Html.fromHtml("<b>MultiThreading / Prime finding: </b>(" + MAX_PRIME + ")<br/>Native: " + prime_native_time + " ms\nJava: " + prime_java_time + " ms", Html.FROM_HTML_MODE_LEGACY));
                        Toast.makeText(MainActivity.this, prime_native_result.length + "/" + prime_java_result, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        t.start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    t.join();
                } catch (Exception e) {
                    e.printStackTrace();
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
                    }
                }
                Log.i(TAG, "Files written: " + allResult.size());
            }
        }).start();
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
        array = new int[size];
        for (int i = 0; i < size; i++)
            array[i] = r.nextInt(max-min)+min;
        return (array[pos] = max + 1);
    }

    public int orderArrayJava(int size) {
        Arrays.sort(array);
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

    public void primesUntilXJava(final long x) {
        int max = 0;
        if (x <= 100) max = 40;
        else {
            double result = x / (Math.log10(x) - 1) * 1.1;
            max = (int) Math.ceil(result);
        }
        arr0 = new long[max];
        arr1 = new long[max / 3 * 2];
        arr2 = new long[max / 3 * 2];
        arr3 = new long[max / 2];
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
        System.arraycopy(arr2, 0, arr0, pos0 + pos1, pos2);
        System.arraycopy(arr3, 0, arr0, pos0 + pos1 + pos2, pos3);
        pos0 += pos1 + pos2 + pos3;
    }

    public int calculate(long from, long to, long[] in) {
        long pos = 0;
        for (; from < to; from++) {
            long until = (long) Math.sqrt(from);
            boolean divs = false;
            for (long i = 2; i <= until; i++) {
                if (from % i == 0) {
                    divs = true;
                    break;
                }
            }
            if (!divs) {
                in[(int)pos++] = from;
            }
        }
        return (int) pos;
    }

    public native long getDivisors(long number);
    public native int generateRandom(boolean which, int size);
    public native int orderArray(int[] array, int size);
    public native long[] primesUntilX(long x);

    @Override
    protected void onDestroy() {
        File cache = MainActivity.this.getApplicationContext().getCacheDir();
        deleteFolder(cache);
        System.exit(0);
        super.onDestroy();
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
        File cache = MainActivity.this.getApplicationContext().getCacheDir();
        deleteFolder(cache);
        System.exit(0);
        super.onBackPressed();
    }
}
