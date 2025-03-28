#include <stdio.h>
#include <malloc.h>
#include <math.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>

#define eps 0.00001

double norming(double *u, unsigned int size) {
	double res = 0.0;
	for (unsigned int i = 0; i < size; ++i)
		res += u[i] * u[i];

	return sqrt(res);
}

double skalar(double *u, double *v, unsigned int size) {
	double res = 0.0;
	for (unsigned int i = 0; i < size; ++i)
		res += u[i]*v[i];

	return res;
}

void fill(double *matrix, unsigned int size) {
	for (unsigned int i = 0; i < size * size; ++i) {
		unsigned int k = i / size;
		unsigned int j = i % size;

		if (j >= k) {
			double value = 2.0 * ((double) rand() / (double) RAND_MAX) - 1.0;
			if (k == j)
				value += (double) size / 64.0;

			matrix[k * size + j] = value;
			matrix[j * size + k] = value;
		}
	}
}

void fill_vect(double *u, unsigned int size) {
	for (unsigned int i = 0; i < size; ++i)
		u[i] = 200.0 * ((double) rand() / (double) RAND_MAX) - 100.0;
}

void mult_matr_vect(double *matrix, double *v, unsigned int size, double *res) {
	for (unsigned int i = 0; i < size; ++i) {
		res[i] = 0.0;
		for (unsigned int j = 0; j < size; ++j)
			res[i] += matrix[i * size + j] * v[j];
	}
}


void add(double *u, double *v, double *res, unsigned int size, double k) {
	for (unsigned int i = 0; i < size; ++i)
		res[i] = u[i] + k * v[i];
}

void sub(double *u, double *v, double *res, unsigned int size, double k) {
	for (unsigned int i = 0; i < size; ++i)
		res[i] = u[i] - k * v[i];
}

void process(double *A, double *b, unsigned int size) {
	double *x = (double *) calloc(size, sizeof(double));
	double *r_old = (double *) malloc(size * sizeof(double));
	double *r_new = (double *) malloc(size * sizeof(double));
	double *z = (double *) malloc(size * sizeof(double));
	double *Ax = (double *) calloc(size, sizeof(double));
	double *Az = (double *) calloc(size, sizeof(double));

	double alpha, betta;
	double cheker = eps * eps * skalar(b, b, size);

	mult_matr_vect(A, x, size, Ax);
	sub(b, Ax, r_old, size, 1.0);
	memcpy(z, r_old, size * sizeof(double));
	free(Ax);

	unsigned int iter = 0;
	while ((skalar(r_old, r_old, size) > cheker) && (iter < 50000)) {
		mult_matr_vect(A, z, size, Az);
		alpha = skalar(r_old, r_old, size) / skalar(Az, z, size);

		add(x, z, x, size, alpha);

		sub(r_old, Az, r_new, size, alpha);

		betta = skalar(r_new, r_new, size) / skalar(r_old, r_old, size);

		add(r_new, z, z, size, betta);
		memcpy(r_old, r_new, size * sizeof(double));

		iter++;
	}

	free(Az);
	free(r_old);
	free(r_new);
	free(z);
	free(x);
}

int main(int argc, char **argv) {
	if (argc < 2) {
		puts("Bad input: need 1 input argument");
		return 1;
	}

	struct timespec start, end;
	clock_gettime(CLOCK_MONOTONIC, &start);

	unsigned int N;
	N = atoi(argv[1]);

	double *b = (double *) malloc(N * sizeof(double));
	double *A = (double *) malloc(N * N * sizeof(double));;

	fill_vect(b, N);
	fill(A, N);

	process(A, b, N);

	clock_gettime(CLOCK_MONOTONIC, &end);
	printf("Time counting: %f\n", end.tv_sec - start.tv_sec + 0.000000001 * (end.tv_nsec - start.tv_nsec));

	free(b);
	free(A);

	return 0;
}
