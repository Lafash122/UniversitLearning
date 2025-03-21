#include <stdio.h>
#include <stdlib.h>
#include <time.h>

int main(int argc, char** argv) {
    struct timespec start, end;
    int n = atoi(argv[1]);

    clock_gettime(CLOCK_MONOTONIC, &start);

    int* a = (int*)calloc(n, sizeof(int));
    int* b = (int*)calloc(n, sizeof(int));

    for (int i = 0; i < n; ++i) {
	a[i] = rand() % 10;
	b[i] = rand() % 10;
    }

    int s = 0;
    
    for (int i = 0; i < n; ++i)
        for (int j = 0; j < n; j++)
            s += a[i] * b[j];
    clock_gettime(CLOCK_MONOTONIC, &end);

    printf("Total summ: %d\nTime counting: %f\n", s, (end.tv_sec - start.tv_sec + 0.000000001 * (end.tv_nsec - start.tv_nsec)));

    free(a);
    free(b);

    return 0;
}
