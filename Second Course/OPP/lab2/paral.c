#include <stdio.h>
#include <malloc.h>
#include <math.h>
#include <stdlib.h>
#include <mpi.h>

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

void mult_matr_vect(double *matrix, double *v, unsigned int l, unsigned int s, double *res) {
	for (unsigned int i = 0; i < s; ++i) {
		res[i] = 0.0;
		for (unsigned int j = 0; j < l; ++j)
			res[i] += matrix[i * l + j] * v[j];
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

int main(int argc, char **argv) {
	MPI_Init(&argc, &argv);

	int rank, proc_num;
	MPI_Comm_size(MPI_COMM_WORLD, &proc_num);
	MPI_Comm_rank(MPI_COMM_WORLD, &rank);

	if (argc < 2) {
		if (rank == 0)
			puts("Bad input: need 1 input argument");

		MPI_Finalize();
		return 1;
	}
	unsigned int N;
	N = atoi(argv[1]);

	double start = MPI_Wtime();

	unsigned int part_size = N / proc_num;

	double *b = (double *) malloc(N * sizeof(double));
	double *A = NULL;
	double *A_part = (double *) malloc(N * part_size * sizeof(double));

	double *x = (double *) calloc(N, sizeof(double));
	double *x_part = (double *) malloc(part_size * sizeof(double));
	double *r_part = (double *) malloc(part_size * sizeof(double));
	double *z_part = (double *) malloc (part_size * sizeof(double));
	double *rn_part = (double *) malloc(part_size * sizeof(double));
	double *r_old = (double *) malloc(N * sizeof(double));
	double *z = (double *) malloc(N * sizeof(double));
	double *Ax_part = (double *) malloc(part_size * sizeof(double));
	double *Az_part = (double *) malloc(part_size * sizeof(double));

	if (rank == 0) {
		fill_vect(b, N);
		A = (double *) malloc(N * N * sizeof(double));
		fill(A, N);
	}

	double alpha, betta;
	double r_sk = 0.0, rn_sk = 0.0, z_sk = 0.0;
	double r_sk_part, rn_sk_part, z_sk_part;

	MPI_Bcast(b, N, MPI_DOUBLE, 0, MPI_COMM_WORLD);
	MPI_Scatter(x, part_size, MPI_DOUBLE, x_part, part_size, MPI_DOUBLE, 0, MPI_COMM_WORLD);
	MPI_Scatter(A, N * part_size, MPI_DOUBLE, A_part, N * part_size, MPI_DOUBLE, 0, MPI_COMM_WORLD);
	
	mult_matr_vect(A_part, x, N, part_size, Ax_part);

	for (unsigned int i = 0; i < part_size; ++i) {
		r_part[i] = b[rank * part_size + i] - Ax_part[i];
		z_part[i] = r_part[i];
	}

	MPI_Allgather(z_part, part_size, MPI_DOUBLE, z, part_size, MPI_DOUBLE, MPI_COMM_WORLD);
	MPI_Allgather(r_part, part_size, MPI_DOUBLE, r_old, part_size, MPI_DOUBLE, MPI_COMM_WORLD);

	free(Ax_part);

	double b_norma = norming(b, N);
	unsigned int iter = 0;
	while ((norming(r_old, N) / b_norma >= eps) && (iter < 50000)) {
		mult_matr_vect(A_part, z, N, part_size, Az_part);

		r_sk_part = skalar(r_part, r_part, part_size);
		z_sk_part = skalar(Az_part, z_part, part_size);
		MPI_Allreduce(&r_sk_part, &r_sk, 1, MPI_DOUBLE, MPI_SUM, MPI_COMM_WORLD);
		MPI_Allreduce(&z_sk_part, &z_sk, 1, MPI_DOUBLE, MPI_SUM, MPI_COMM_WORLD);
		alpha = r_sk / z_sk;

		add(x_part, z_part, part_size, alpha);

		copy(rn_part, r_part, part_size);
		sub(rn_part, Az_part, part_size, alpha);

		rn_sk_part = skalar(rn_part, rn_part, part_size);
		r_sk_part = skalar(r_part, r_part, part_size);
		MPI_Allreduce(&rn_sk_part, &rn_sk, 1, MPI_DOUBLE, MPI_SUM, MPI_COMM_WORLD);
		MPI_Allreduce(&r_sk_part, &r_sk, 1, MPI_DOUBLE, MPI_SUM, MPI_COMM_WORLD);
		betta = rn_sk / r_sk;

		copy(r_part, rn_part, part_size);
		add(rn_part, z_part, part_size, betta);
		copy(z_part, rn_part, part_size);

		MPI_Allgather(z_part, part_size, MPI_DOUBLE, z, part_size, MPI_DOUBLE, MPI_COMM_WORLD);
		MPI_Allgather(r_part, part_size, MPI_DOUBLE, r_old, part_size, MPI_DOUBLE, MPI_COMM_WORLD);

		iter++;
	}

	MPI_Allgather(x_part, part_size, MPI_DOUBLE, x, part_size, MPI_DOUBLE, MPI_COMM_WORLD);
	if (rank == 0) {
		double end = MPI_Wtime();
		printf("Time counting: %f\n", end - start);

		free(A);
	}

	free(Az_part);
	free(rn_part);
	free(A_part);
	free(x_part);
	free(r_part);
	free(z_part);
	free(r_old);
	free(z);
	free(x);
	free(b);

	MPI_Finalize();

	return 0;
}
