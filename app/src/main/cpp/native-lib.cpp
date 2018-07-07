#include <jni.h>
#include <string>
#include <thread>
#include <unistd.h>
#include <cmath>
#include <random>

#include <android/log.h>


#define TAG "Java_hu_hexadecimal_nativetest_MainActivity"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)

const int array_size = 16384;
const int pos = 2040;
const int min = 100;
const int max = 5000;
int number = max + 1;

extern "C" JNIEXPORT jstring JNICALL Java_hu_hexadecimal_nativetest_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

extern "C" JNIEXPORT jlong JNICALL Java_hu_hexadecimal_nativetest_MainActivity_getDivisors(JNIEnv *env, jobject jobj, jlong number)
{
    int64_t divs = 1;
    int64_t num2 = (int64_t) number;
    int64_t upper_limit = (int64_t) sqrt(num2);
    for (int64_t i = 2; i <= upper_limit; i++) {
        if (num2%i==0) divs++;
    }
    LOGI("NATIVE - divs: %lu , number: %lu", divs, number);
    return divs*2;
}

extern "C" JNIEXPORT jint JNICALL Java_hu_hexadecimal_nativetest_MainActivity_generateRandom(JNIEnv *env, jobject jobj, jboolean which)
{
    if (which)
        number = min - 1;
    std::random_device rd;
    std::mt19937 generator(rd());
    std::uniform_int_distribution<int> uni(min, max);
    int * array = new int[array_size];
    for (int i = 0; i < array_size; i++)
        array[i] = uni(generator);

    return (jint) (array[pos] = number);

}

extern "C" JNIEXPORT jint JNICALL Java_hu_hexadecimal_nativetest_MainActivity_orderArray(JNIEnv *env, jobject jobj, jintArray jarray)
{
    jint * array = env->GetIntArrayElements(jarray, 0);
    for (int i = array_size-1; i >= 0; i--) {
        for (int j = 0; j < i; j++ ) {
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

    env->ReleaseIntArrayElements(jarray, array, 0);
    return found;
}

