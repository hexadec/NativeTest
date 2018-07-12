package hu.hexadecimal.nativetest;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Arrays;
import java.util.Random;
import java.lang.Math;

public class MainActivity extends Activity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    public static String TAG = "NativeTest";

    /* Constants for arrays and random numbers */
    final int array_size = 32768;
    final int pos = 2040;
    final int min = 100;
    final int max = 25000;
    int number = max + 1;
    int[] array;

    /* Arrays for prime numbers */
    long[] arr0;
    long[] arr1;
    long[] arr2;
    long[] arr3;
    int pos0, pos1, pos2, pos3 = 0;

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
        new Thread(new Runnable() {
            @Override
            public void run() {
                /*Generating number to divide*/
                int r = new Random().nextInt(100000);
                final long DIVIDEND = 100000000000L + r;
                /*Maximum prime number to find*/
                final int MAX_PRIME = 1000000;

                Log.i(TAG, "Divisors - Starting up: native");
                long start_time = System.nanoTime();
                final long div_native_result = getDivisors(DIVIDEND);
                long end_time = System.nanoTime();
                final long div_native_time = end_time - start_time;

                Log.i(TAG, "Native done, starting java");
                start_time = System.nanoTime();
                final long div_java_result = divisorsJava(DIVIDEND);
                end_time = System.nanoTime();
                final long div_java_time = end_time - start_time;

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        divr.setText(Html.fromHtml("<b>Divisors </b>(" + DIVIDEND +")<br/>Native: " + String.format("%.1f", div_native_time / 1000000.0) + " ms\nJava: " + String.format("%.1f", div_java_time / 1000000.0) + " ms", Html.FROM_HTML_MODE_LEGACY));
                        Toast.makeText(MainActivity.this, div_native_result + "/" + div_java_result, Toast.LENGTH_SHORT).show();
                    }
                });

                Log.d(TAG, "Random - Starting up: native");
                start_time = System.nanoTime();
                final int random_native_result = generateRandom(false);
                end_time = System.nanoTime();
                final long random_native_time = end_time - start_time;

                Log.d(TAG, "Native done, starting java");
                start_time = System.nanoTime();
                final int random_java_result = generateRandomJava(false);
                end_time = System.nanoTime();
                final long random_java_time = end_time - start_time;

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        randomr.setText(Html.fromHtml("<b>Random integer generation: </b>(" + array_size + ")<br/>Native: " + String.format("%.1f", random_native_time / 1000000.0) + " ms\nJava: " + String.format("%.1f", random_java_time / 1000000.0)  + " ms", Html.FROM_HTML_MODE_LEGACY));
                        Toast.makeText(MainActivity.this, random_native_result + "/" + random_java_result, Toast.LENGTH_SHORT).show();
                    }
                });

                Log.d(TAG, "Arrays - Creating a copy for C++ code of the array");
                int[] array_native = new int[array_size];
                System.arraycopy(array, 0, array_native, 0, array_size);
                Log.d(TAG, "Arrays - Starting up: native");
                start_time = System.nanoTime();
                final int array_native_result = orderArray(array_native);
                end_time = System.nanoTime();
                final long array_native_time = end_time - start_time;

                Log.d(TAG, "Native done, starting java");
                start_time = System.nanoTime();
                final int array_java_result = orderArrayJava();
                end_time = System.nanoTime();
                final long array_java_time = end_time - start_time;

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        arrayr.setText(Html.fromHtml("<b>Arrays order: </b>(" + array_size + ")<br/>Native: " + String.format("%.1f", array_native_time / 1000000.0) + " ms\nJava: " + String.format("%.1f", array_java_time / 1000000.0) + " ms", Html.FROM_HTML_MODE_LEGACY));
                        Toast.makeText(MainActivity.this, array_native_result + "/" + array_java_result, Toast.LENGTH_SHORT).show();
                    }
                });


                Log.d(TAG, "Primes - Starting up: native");
                start_time = System.currentTimeMillis();
                final long[] prime_native_result = primesUntilX(MAX_PRIME);
                end_time = System.currentTimeMillis();
                final long prime_native_time = end_time - start_time;

                Log.e(TAG, "1. " + prime_native_result[0] + " Half. " +
                        prime_native_result[prime_native_result.length/2] + " Max. " +
                        prime_native_result[prime_native_result.length - 1]);

                Log.d(TAG, "Native done, starting java");
                start_time = System.currentTimeMillis();
                primesUntilXJava(MAX_PRIME);
                final int prime_java_result = pos0;
                end_time = System.currentTimeMillis();
                final long prime_java_time = end_time - start_time;

                Log.e(TAG, "1. " + arr0[0] + " Half. (" +
                                prime_java_result/2 + ") " +
                        arr0[prime_java_result/2] + " Max. " +
                        arr0[prime_java_result - 1]);

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        primer.setText(Html.fromHtml("<b>MultiThreading / Prime finding: </b>(" + MAX_PRIME + ")<br/>Native: " + prime_native_time + " ms\nJava: " + prime_java_time + " ms", Html.FROM_HTML_MODE_LEGACY));
                        Toast.makeText(MainActivity.this, prime_native_result.length + "/" + prime_java_result, Toast.LENGTH_SHORT).show();
                    }
                });
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

    public int generateRandomJava(boolean which) {
        if (which)
            number = min - 1;
        Random r = new Random();
        array = new int[array_size];
        for (int i = 0; i < array_size; i++)
            array[i] = r.nextInt(max-min)+min;
        return (array[pos] = max + 1);
    }

    public int orderArrayJava() {
        Arrays.sort(array);
        int found = -1;
        for (int i = 0; i < array_size; i++) {
            if (array[i] == number) {
                found = i;
                break;
            }
        }
        Log.i(TAG, "Array: " + array[0] + " - " + array[100] + " - " + array[array_size / 2]);
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
        int pos = 0;
        for (; from < to; from++) {
            long until = (long) Math.sqrt(from);
            long divs = 0;
            for (long i = 2; i <= until; i++) {
                if (from % i == 0) {
                    ++divs;
                    break;
                }
            }
            if (divs == 0) {
                in[pos++] = from;
            }
        }
        return pos;
    }

    public native long getDivisors(long number);
    public native int generateRandom(boolean which);
    public native int orderArray(int[] array);
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
