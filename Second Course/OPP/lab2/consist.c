#include <stdio.h>
#include <malloc.h>
#include <math.h>
#include <stdlib.h>
#include <time.h>

#define eps 0.00001

double norming(double *u, unsigned int size) {
	double res = 0.0;
	for (unsigned int i = 0; i < size; ++i)
		res += u[i] * u[i];

	return sqrt(res);
}

int check_crit(double *r_n, double *b, unsigned int size) {
	double res = norming(r_n, size) / norming(b, size);
	if (res < eps)
		return 1;
	return 0;
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

void add(double *u, double *v, unsigned int size, double k) {
	for (unsigned int i = 0; i < size; ++i)
		u[i] += k * v[i];
}

void sub(double *u, double *v, unsigned int size, double k) {
	for (unsigned int i = 0; i < size; ++i)
		u[i] -= k * v[i];
}

void copy(double *u, double *v, unsigned int size) {
	for (unsigned int i = 0; i < size; ++i)
		u[i] = v[i];
}

void process(double *A, double *b, unsigned int size) {
	double *x = (double *) calloc(size, sizeof(double));
	double *r_old = (double *) malloc(size * sizeof(double));
	double *r_new = (double *) malloc(size * sizeof(double));
	double *z = (double *) malloc(size * sizeof(double));
	double *tmp = (double *) malloc(size * sizeof(double));
	double *Ax = (double *) calloc(size, sizeof(double));
	double *Az = (double *) calloc(size, sizeof(double));

	double alpha, betta;

	mult_matr_vect(A, x, size, Ax);
	copy(tmp, b, size);
	sub(tmp, Ax, size, 1.0);
	copy(r_old, tmp, size);
	copy(z, r_old, size);
	free(tmp);
	free(Ax);

	unsigned int iter = 0;
	while (!check_crit(r_old, b, size) && (iter < 50000)) {
		mult_matr_vect(A, z, size, Az);
		alpha = skalar(r_old, r_old, size) / skalar(Az, z, size);

		add(x, z, size, alpha);

		copy(r_new, r_old, size);
		sub(r_new, Az, size, alpha);

		betta = skalar(r_new, r_new, size) / skalar(r_old, r_old, size);

		copy(r_old, r_new, size);
		add(r_new, z, size, betta);
		copy(z, r_new, size);

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
	unsigned int N;
	N = atoi(argv[1]);

	struct timespec start, end;
	clock_gettime(CLOCK_MONOTONIC, &start);

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
