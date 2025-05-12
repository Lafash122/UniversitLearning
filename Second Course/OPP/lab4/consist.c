#include <stdio.h>
#include <stdlib.h>
#include <time.h>

#define MIDDLE_SIZE 2500
#define C_ROWS 3600
#define C_COLS 4800

void fill(double *matrix, unsigned int rows, unsigned int cols) {
	for (unsigned int i = 0; i < rows * cols; ++i)
		matrix[i] = 2.0 * ((double) rand() / (double) RAND_MAX) - 1.0;
}

void mult(double *m_a, double *m_b, double *res, unsigned int rows, unsigned int cols) {
	for (unsigned int i = 0; i < rows; ++i)
		for (unsigned int j = 0; j < cols; ++j)
			for (unsigned int k = 0; k < MIDDLE_SIZE; ++k)
				res[i * cols + j] += m_a[i * MIDDLE_SIZE + k] * m_b[k * cols + j];
}

int main(int argc, char** argv) {
	struct timespec start, end;
	clock_gettime(CLOCK_MONOTONIC, &start);

	double *A = (double *) malloc(C_ROWS * MIDDLE_SIZE * sizeof(double));
	double *B = (double *) malloc(MIDDLE_SIZE * C_COLS * sizeof(double));
	double *C = (double *) calloc(C_ROWS * C_COLS, sizeof(double));
	fill(A, C_ROWS, MIDDLE_SIZE);
	fill(B, MIDDLE_SIZE, C_COLS);

	mult(A, B, C, C_ROWS, C_COLS);

	clock_gettime(CLOCK_MONOTONIC, &end);
	printf("Time counting: %f; first el: %3.3f\n", (end.tv_sec - start.tv_sec + 0.000000001 * (end.tv_nsec - start.tv_nsec)), C[0]);

	free(A);
	free(B);
	free(C);

	return 0;
}