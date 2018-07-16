#include <jni.h>
#include <cmath>
#include <random>
#include <algorithm>
#include <fstream>
#include <cstdlib>
#include <thread>
#include <unistd.h>

#include <android/log.h>


#define TAG "Java_hu_hexadecimal_nativetest_MainActivity"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)

void calc(int32_t, int32_t, int32_t*, int32_t*);
int64_t pi(int64_t);

int32_t array_size = 8192;
const int pos = 2040;
const int min = 100;
const int max = 2048*2048;
int number = max + 1;
int32_t pmax = 0;


//Variables for primesUntilX
int32_t * l, *l1, *l2, *l3;
int32_t pos0,pos1,pos2,pos3 = 0; //uint32_t is not big enough to count all

void calc(int32_t from, int32_t to, int32_t * pos, int32_t * arr) {
    for (; from < to; from++) {
        int32_t c = sqrt(from);
        bool div = false;
        for (uint32_t t = 2; t <= c; t++) {
            if (from%t==0) {
                div = true;
                break;
            }
        }
        if (!div) {
            arr[(*pos)++] = from;
        }
    }
}

//Prime numbers until x, using approcc
int64_t pi(int64_t x) {
    if (x <= 100) return 40;
    double result = x/(log10(x) - 1) * 1.1;
    return (int64_t) ceil(result);
}

extern "C" JNIEXPORT jintArray JNICALL Java_hu_hexadecimal_nativetest_MainActivity_primesUntilX(JNIEnv *env, jobject jobj, jint x)
{
    pmax = (int32_t) x;
    pos0 = pos1 = pos2 = pos3 = 0;
    l = new int32_t[pi(pmax)];
    l1 = new int32_t[pi(pmax) / 3 * 2];
    l2 = new int32_t[pi(pmax) / 3 * 2];
    l3 = new int32_t[pi(pmax) / 2];
    std::thread t1(calc, 2, pmax/4, &pos0, l);
    std::thread t2(calc, pmax/4, pmax/2, &pos1, l1);
    std::thread t3(calc, pmax/2, (pmax/4)+(pmax/2), &pos2, l2);
    //calc4
    calc((pmax/4)+(pmax/2), pmax, &pos3, l3);
    t1.join();
    t2.join();
    t3.join();

    std::copy(l1, l1 + pos1, l + pos0);
    std::copy(l2, l2 + pos2, l + (pos0 += pos1));
    std::copy(l3, l3 + pos3, l + (pos0 += pos2));
    pos0 += pos3;

    delete [] l1;
    delete [] l2;
    delete [] l3;

    jintArray result;
    result = env->NewIntArray(pos0);
    env->SetIntArrayRegion(result, 0, pos0, l);
    delete [] l;
    return result;
}

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
    uint32_t upper_limit = (uint32_t) sqrt(num2);
    for (uint32_t i = 2; i <= upper_limit; i++) {
        if (num2%i==0) divs++;
    }
    LOGI("NATIVE - divs: %lu , number: %lu", divs, number);
    return divs*2;
}

extern "C" JNIEXPORT jint JNICALL Java_hu_hexadecimal_nativetest_MainActivity_generateRandom(JNIEnv *env, jobject jobj, jboolean which, jint size)
{
    if (which)
        number = min - 1;
    array_size = (int32_t) size;
    std::random_device rd;
    std::mt19937 generator(rd());
    std::uniform_int_distribution<int> uni(min, max);
    int * array = new int[array_size];
    for (int i = 0; i < array_size; i++)
        array[i] = uni(generator);

    return (jint) (array[pos] = number);

}

extern "C" JNIEXPORT jint JNICALL Java_hu_hexadecimal_nativetest_MainActivity_orderArray(JNIEnv *env, jobject jobj, jintArray jarray, jint size)
{
    jint * array = env->GetIntArrayElements(jarray, 0);

    array_size = (int32_t) size;
    std::sort(array, array + array_size);
    int found = -1;
    for (int i = 0; i < array_size; i++) {
        if (array[i] == number) {
            found = i;
            break;
        }
    }
    LOGI("NATIVE - array: 1. %i , 101. number: %i, half. %i", array[0], array[100], array[array_size / 2]);

    env->ReleaseIntArrayElements(jarray, array, 0);
    return found;
}

