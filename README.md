# NativeTest
A simple project to test the efficiency of C++ and native code using various aspects. 
This program is necessary to supply data for my EE.

> Do not gather test results created by the **debug** build variant in Android Studio!
> Due to various profiling and debug utilities, all run times *(especially native code)* are significantly slower
> than in **release** build variant, so use that!

> To provide a different kind of test environment, comment out usages of deleteCache()

> Please keep in mind that is project is intended for comparing the two languages rather than benchmarking devices!

This application neglects the result of the first runs of each type of test, but it may be completely unnecessary.
As resulting time is heavily affected by other processes and applications in the background, you may want to calculate an average of 
three to five runs, while keeping other applications closed. As some apps do less work without
an active network connection, keeping the device in airplane mode is advised.

#### Aspect 1.
Calculate how many divisors an arbitrarily long **long** number has.

#### Aspect 2.
Generate random integer in an arbitrary range using language built-in methods

#### Aspect 3.
Order these arrays using built-in functions.
Java:   Arrays.sort
C++:    std::sort (STL)

#### Aspect 4.
Find all primes smaller than a given number.
Do this using an arbitrary number of threads.

