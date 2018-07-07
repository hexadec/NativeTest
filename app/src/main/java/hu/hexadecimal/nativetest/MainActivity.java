package hu.hexadecimal.nativetest;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Random;

public class MainActivity extends Activity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    public static String TAG = "NativeTest";

    /* Constants for arrays and random numbers */
    final int array_size = 16384;
    final int pos = 2040;
    final int min = 100;
    final int max = 5000;
    int number = max + 1;
    int[] array;

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
        //Not using UI thread w/ long operations to avoid being killed
        new Thread(new Runnable() {
            @Override
            public void run() {
                long r = new Random().nextLong();
                long touse = 10000000000L + r % 100000;

                Log.i(TAG, "Divisors - Starting up: native");
                long start_time = System.nanoTime();
                final long div_native_result = getDivisors(touse);
                long end_time = System.nanoTime();
                final long div_native_time = end_time - start_time;

                Log.i(TAG, "Native done, starting java");
                start_time = System.nanoTime();
                final long div_java_result = divisorsJava(touse);
                end_time = System.nanoTime();
                final long div_java_time = end_time - start_time;

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        divr.setText(Html.fromHtml("<b>Divisors</b><br/>Native: " + div_native_time / 1000 + " μs\nJava: " + div_java_time / 1000 + " μs", Html.FROM_HTML_MODE_LEGACY));
                        Toast.makeText(MainActivity.this, div_native_result + "/" + div_java_result, Toast.LENGTH_SHORT).show();
                    }
                });

                Log.d(TAG, "Random - Starting up: native");
                start_time = System.currentTimeMillis();
                final int random_native_result = generateRandom(false);
                end_time = System.currentTimeMillis();
                final long random_native_time = end_time - start_time;

                Log.d(TAG, "Native done, starting java");
                start_time = System.currentTimeMillis();
                final int random_java_result = generateRandomJava(false);
                end_time = System.currentTimeMillis();
                final long random_java_time = end_time - start_time;

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        randomr.setText(Html.fromHtml("<b>Random integer generation: </b><br/>Native: " + random_native_time + " ms\nJava: " + random_java_time + " ms", Html.FROM_HTML_MODE_LEGACY));
                        Toast.makeText(MainActivity.this, random_native_result + "/" + random_java_result, Toast.LENGTH_SHORT).show();
                    }
                });

                Log.d(TAG, "Arrays - Creating a copy for C++ code of the array");
                int[] array_native = new int[array_size];
                System.arraycopy(array, 0, array_native, 0, array_size);
                Log.d(TAG, "Arrays - Starting up: native");
                start_time = System.currentTimeMillis();
                final int array_native_result = orderArray(array_native);
                end_time = System.currentTimeMillis();
                final long array_native_time = end_time - start_time;

                Log.d(TAG, "Native done, starting java");
                start_time = System.currentTimeMillis();
                final int array_java_result = orderArrayJava();
                end_time = System.currentTimeMillis();
                final long array_java_time = end_time - start_time;

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        arrayr.setText(Html.fromHtml("<b>Arrays order: </b><br/>Native: " + array_native_time + " ms\nJava: " + array_java_time + " ms", Html.FROM_HTML_MODE_LEGACY));
                        Toast.makeText(MainActivity.this, array_native_result + "/" + array_java_result, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).start();
    }

    public long divisorsJava(long random) {
        long divs = 1;
        long upper_limit = (long) Math.sqrt(random);
        for (int c = 2; c <= upper_limit; c++) {
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
        for (int i = array_size-1; i >= 0; i--) {
            for (int j = 0; j < i; j++) {
                if (array[i] < array[j]) {
                    int tmp = array[i];
                    array[i] = array[j];
                    array[j] = tmp;
                }
            }
        }
        int found = -1;
        for (int i = 0; i < array_size; i++) {
            if (array[i] == number) {
                found = i;
                break;
            }
        }

        return found;
    }

    public native long getDivisors(long number);
    public native int generateRandom(boolean which);
    public native int orderArray(int[] array);

    @Override
    protected void onDestroy() {
        File cache = MainActivity.this.getApplicationContext().getCacheDir();
        deleteFolder(cache);
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
