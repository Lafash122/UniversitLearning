#include <stdio.h>
#include <malloc.h>
#include <math.h>
#include <stdlib.h>

#define eps 0.00001

typedef struct {
	double *elements;
} Matrix;

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

Matrix *create(unsigned int size) {
	Matrix *matrix = malloc(sizeof(Matrix));
	matrix->elements = calloc(size * size, sizeof(double));
	if (!matrix->elements) {
		free(matrix);
		return NULL;
	}

	return matrix;
}

void fill(Matrix *matrix, unsigned int size) {
	for (unsigned int i = 0; i < size * size; ++i) {
		unsigned int k = i / size;
		unsigned int j = i % size;

		if (j >= k) {
			double value = 2.0 * ((double) rand() / (double) RAND_MAX) - 1.0;
			if (k == j)
				value += (double) size;

			matrix->elements[k * size + j] = value;
			matrix->elements[j * size + k] = value;		
		}
	}
}

void fill_vect(double *u, unsigned int size) {
	for (unsigned int i = 0; i < size; ++i)
		u[i] = 2.0 * ((double) rand() / (double) RAND_MAX) - 1.0;
}

Matrix *make(unsigned int size) {
	Matrix *matrix = create(size);
	fill(matrix, size);
	return matrix;
}

void clear(Matrix *matrix) {
	if (matrix) {
		free(matrix->elements);
		free(matrix);
	}
}

void mult_matr_vect(Matrix *matrix, double *v, unsigned int size, double *res) {
	for (unsigned int i = 0; i < size; ++i) {
		res[i] = 0.0;
		for (unsigned int j = 0; j < size; ++j)
			res[i] += matrix->elements[i * size + j] * v[j];
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

double *mult_vect_num(double *u, double k, unsigned int size) {
	double *res = (double *) calloc(size, sizeof(double));
	for (unsigned int i = 0; i < size; ++i)
		res[i] = u[i] * k;

	return res;
}

void copy(double *u, double *v, unsigned int size) {
	for (unsigned int i = 0; i < size; ++i)
		u[i] = v[i];
}

void process(Matrix *A, double *b, unsigned int size) {
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

		//printf("Iterations counter: %d\n", iter);
		iter++;
	}

	printf("Iterations counter: %d\n", iter);
	//printf("Vector x:\n");
	//for (unsigned int i = 0; i < size; ++i)
	//	printf("%6.3f ", x[i]);
	//printf("\n");

	free(Az);
	free(r_old);
	free(r_new);
	free(z);
	free(x);
}

void print_matr(Matrix *matrix, unsigned int size) {
	printf("Matrix A:\n");
	for (unsigned int i = 0; i < size; ++i) {
		for (unsigned int j = 0; j < size; ++j)
			printf("%6.3f ", matrix->elements[i * size + j]);
		printf("\n");
	}
}

int main(int argc, char **argv) {
	if (argc < 2) {
		puts("bad input: need 1 input argument");
		return 1;
	}
	unsigned int N;
	N = atoi(argv[1]);

	double *b = (double *) calloc(N, sizeof(double));

	fill_vect(b, N);

	Matrix *A = make(N);

	process(A, b, N);
	//print_matr(A, N);

	//printf("Vector b:\n");
	//for (unsigned int i = 0; i < N; ++i)
	//	printf("%6.3f ", b[i]);
	//printf("\n");

	char bukva = getchar();

	free(b);
	clear(A);

	return 0;
}
