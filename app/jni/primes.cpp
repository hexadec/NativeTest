#include <cstdio>
#include <cstdlib>
//#include <cmath>
#include <thread>
#include <unistd.h>


//int max = 5000000; //Must be greater than 10
int l[1024000];
int l1[768000];
int l2[768000];
int l3[512000];
int pos,pos1,pos2,pos3 = 0;
bool stop = false;

void status() {

    while(!stop) {
        printf("Found: %d\n", pos+pos1+pos2+pos3);
        sleep(3);
    }
}

void calc1(int from, int to) {
    for (int i = from; i < to; i++) {
        int c = i/2;
        int div = 0;
        for (int t = 1; t <= c; t++) {
            if (i%t==0) div++;
        }
        if (div==1) {
            l[pos] = i;

            pos++;
        }
    }
}

void calc2(int from, int to) {
    for (int i = from; i < to; i++) {
        int c = i/2;
        int div = 0;
        for (int t = 1; t <= c; t++) {
            if (i%t==0) div++;
        }
        if (div==1) {
            l1[pos1] = i;

            pos1++;
        }
    }
}

void calc3(int from, int to) {
    for (int i = from; i < to; i++) {
        int c = i/2;
        int div = 0;
        for (int t = 1; t <= c; t++) {
            if (i%t==0) div++;
        }
        if (div==1) {
            l2[pos2] = i;

            pos2++;
        }
    }
}

int main(int argc, char**argv) {
    if (argc < 3) {
        printf("Give the maximum number and the output\n");
        return -1;
    }
    int max = 20;
    if ((max = atoi(argv[1]))<=10) {
        printf("Wrong number!\nThe number has to be greater than 10!\n");
        return -1;
    }
    char *output = argv[2];
    std::thread t1(calc1, 2, max/4);
    std::thread t2(calc2, max/4, max/2);
    std::thread t3(calc3, max/2, (max/4)+(max/2));
    std::thread t4(status);
//calc4
    for (int i = (max/4)+(max/2); i < max; i++) {
        int c = i/2;
        int div = 0;
        for (int t = 1; t <= c; t++) {
            if (i%t==0) div++;
        }
        if (div==1) {
            l3[pos3] = i;

            pos3++;
        }
    }
    t1.join();
    t2.join();
    t3.join();
    t4.detach();
    printf("Found: %d\n", pos+pos1+pos2+pos3);
    printf("Merging arrays...\n");
    int ii = pos+pos1-1;
    int iii = 0;
    for (int i = pos; i <= ii; pos++) {
        l[i] = l1[iii];
//printf("%d-%d;",l[i],i);
        iii++;
        i++;
    }
    ii = pos+pos2-1;
    iii = 0;
    for (int i = pos-1; i < ii; pos++) {
        l[i+1] = l2[iii];
//printf("%d-%d;",l[i+1],i+1);
        iii++;
        i = pos;
    }
    ii = pos+pos3-1;
    iii = 0;
    for (int i = pos-1; i < ii; pos++) {
        l[i+1] = l3[iii];
//printf("%d-%d;",l[i+1],i+1);
        iii++;
        i = pos;
    }
    pos;
    printf("Ready, now write to file\n");
    FILE *file;
    if((file=fopen(output, "wb"))==NULL)
    {
        printf("Something went wrong reading %s\n",output);
        return -1;
    }
    else
    {

        for(int j = 0; j < pos; j+=2) {
            if (j+2>=pos) {
                if (j+1 >= pos) {
                    fprintf(file, "%d", l[j]);
                    break;
                } else {
                    fprintf(file, "%d;%d", l[j],l[j+1]);
                    break;
                }
                break;
            } else {
                fprintf(file, "%d;%d;", l[j],l[j+1]);
            }
        }
    }
    fclose(file);
    return 0;
}
