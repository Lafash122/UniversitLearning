#include <stdio.h>
#include <malloc.h>
#include <math.h>
#include <stdlib.h>
#include <string.h>
#include <omp.h>

#define eps 0.00001

double skalar(double *u, double *v, unsigned int size) {
  double res = 0.0;
	#pragma omp parallel for reduction(+:res)
	for (unsigned int i = 0; i < size; ++i)
		res += u[i] * v[i];

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
	#pragma omp for
	for (unsigned int i = 0; i < size; ++i) {
		res[i] = 0.0;
		for (unsigned int j = 0; j < size; ++j)
			res[i] += matrix[i * size + j] * v[j];
	}
}


void add(double *u, double *v, double *res, unsigned int size, double k) {
	#pragma omp for
	for (unsigned int i = 0; i < size; ++i)
		res[i] = u[i] + k * v[i];
}

void sub(double *u, double *v, double *res, unsigned int size, double k) {
	#pragma omp for
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
	double cheker, sk_r_n, sk_b, sk_r, sk_z;
	unsigned int iter = 0;

	#pragma omp parallel shared(alpha, betta, cheker, sk_r_n, sk_b, sk_r, sk_z, iter)
	{
		mult_matr_vect(A, x, size, Ax);
		sub(b, Ax, r_old, size, 1.0);
		#pragma omp for
		for (unsigned int i = 0; i < size; ++i)
			z[i] = r_old[i];

		#pragma omp single
		{
			sk_b = 0.0;
		}
		#pragma omp for reduction(+:sk_b)
		for (unsigned int i = 0; i < size; ++i)
			sk_b += b[i] * b[i];
		#pragma omp single
		{
			cheker = eps * eps * sk_b;
		}

		#pragma omp single
		{
			sk_r = 0.0;
		}
		#pragma omp for reduction(+:sk_r)
		for (unsigned int i = 0; i < size; ++i)
			sk_r += r_old[i] * r_old[i];

		while ((sk_r > cheker) && (iter < 50000)) {
			mult_matr_vect(A, z, size, Az);
			#pragma omp single
			{
				sk_z = 0.0;
			}

			#pragma omp for reduction(+:sk_z)
			for (unsigned int i = 0; i < size; ++i)
				sk_z += Az[i] * z[i];

			#pragma omp single
			{
				alpha = sk_r / sk_z;
			}

			add(x, z, x, size, alpha);

			sub(r_old, Az, r_new, size, alpha);

			#pragma omp single
			{
				sk_r_n = 0.0;
			}

			#pragma omp for reduction(+:sk_r_n)
			for (unsigned int i = 0; i < size; ++i)
				sk_r_n += r_new[i] * r_new[i];

			#pragma omp single
			{
				betta = sk_r_n / sk_r;
			}

			add(r_new, z, z, size, betta);

			#pragma omp for
			for (unsigned int i = 0; i < size; ++i)
				r_old[i] = r_new[i];

			#pragma omp single
			{
				sk_r = sk_r_n;
				iter++;
			}
		}
	}

	free(Ax);
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

	double start = omp_get_wtime();

	double *b = (double *) malloc(N * sizeof(double));
	double *A = (double *) malloc(N * N * sizeof(double));;

	fill_vect(b, N);
	fill(A, N);

	process(A, b, N);

	double end = omp_get_wtime();
	printf("Time counting: %f\n", end - start);

	free(b);
	free(A);

	return 0;
}
