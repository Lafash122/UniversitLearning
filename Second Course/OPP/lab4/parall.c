#include <stdio.h>
#include <stdlib.h>
#include <mpi.h>

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
	MPI_Init(&argc, &argv);

	int rank, proc_num;
	MPI_Comm_size(MPI_COMM_WORLD, &proc_num);
	MPI_Comm_rank(MPI_COMM_WORLD, &rank);

	if (argc < 3) {
		if (rank == 0)
			puts("Bad input: need 2 input argument: sizes of grid");

		MPI_Finalize();
		return 1;
	}

	unsigned int p1 = atoi(argv[1]);
	unsigned int p2 = atoi(argv[2]);

	if (proc_num != (p1 * p2)) {
		if (rank == 0)
			puts("Bad input: sizes of grid does not match the number of processes");

		MPI_Finalize();
		return 1;

	}

	double start = MPI_Wtime();

	int grid_dim[2] = {p1, p2};
	int grid_cy[2] = {0, 0};
	MPI_Comm grid_matrix_comm;
	MPI_Cart_create(MPI_COMM_WORLD, 2,  grid_dim, grid_cy, 0, &grid_matrix_comm);

	int row_dim[2] = {0, 1};
	MPI_Comm row_matrix_comm;
	MPI_Cart_sub(grid_matrix_comm, row_dim, &row_matrix_comm);

	int col_dim[2] = {1, 0};
	MPI_Comm col_matrix_comm;
	MPI_Cart_sub(grid_matrix_comm, col_dim, &col_matrix_comm);

	int proc_coords[2];
	MPI_Cart_coords(grid_matrix_comm, rank, 2, proc_coords);

	double *A, *B, *C;

	if (rank == 0) {
		A = (double *) malloc(C_ROWS * MIDDLE_SIZE * sizeof(double));
		B = (double *) malloc(MIDDLE_SIZE * C_COLS * sizeof(double));
		C = (double *) calloc(C_ROWS * C_COLS, sizeof(double));
		fill(A, C_ROWS, MIDDLE_SIZE);
		fill(B, MIDDLE_SIZE, C_COLS);
	}

	double *A_part = malloc(C_ROWS / p1 * MIDDLE_SIZE * sizeof(double));
	if (proc_coords[1] == 0) {
		MPI_Scatter(A, C_ROWS / p1 * MIDDLE_SIZE, MPI_DOUBLE, A_part, C_ROWS / p1 * MIDDLE_SIZE, MPI_DOUBLE, 0, col_matrix_comm);
	} //3

	double *B_part = malloc(MIDDLE_SIZE * C_COLS / p2 * sizeof(double));
	MPI_Datatype B_cols;
	MPI_Type_vector(MIDDLE_SIZE, C_COLS / p2, C_COLS, MPI_DOUBLE, &B_cols);
	MPI_Type_commit(&B_cols); //4

	if ((proc_coords[0] == 0) && (proc_coords[1] == 0)) {
		for (unsigned int i = 1; i < p2; ++i)
			MPI_Send(B + i * C_COLS / p2, 1, B_cols, i, 0, row_matrix_comm);

		for (unsigned int i = 0; i < MIDDLE_SIZE; ++i)
			for (unsigned int j = 0; j < C_COLS / p2; ++j)
				B_part[i * C_COLS / p2 + j] = B[i * C_COLS + j];
	}
	else if (proc_coords[0] == 0) {
		MPI_Recv(B_part, MIDDLE_SIZE * C_COLS / p2, MPI_DOUBLE, 0, 0, row_matrix_comm, MPI_STATUS_IGNORE);
	} //5

	MPI_Bcast(A_part, C_ROWS / p1 * MIDDLE_SIZE, MPI_DOUBLE, 0, row_matrix_comm); //6

	MPI_Bcast(B_part, MIDDLE_SIZE * C_COLS / p2, MPI_DOUBLE, 0, col_matrix_comm); //7

	double *C_part = (double *) calloc(C_ROWS / p1 * C_COLS / p2, sizeof(double));
	mult(A_part, B_part, C_part, C_ROWS / p1, C_COLS / p2); //8

	MPI_Datatype C_cell;
	MPI_Type_vector(C_ROWS / p1, C_COLS / p2, C_COLS, MPI_DOUBLE, &C_cell);
	MPI_Type_commit(&C_cell);

	int offset[proc_num];
	for (unsigned int i = 0; i < proc_num; ++i) {
		MPI_Cart_coords(grid_matrix_comm, i, 2, proc_coords);
		offset[i] = proc_coords[0] * C_ROWS / p1 * C_COLS + proc_coords[1] * C_COLS / p2;
	}

	if (rank == 0) {
		for (unsigned int i = 0; i < C_ROWS / p1; ++i)
			for (unsigned int j = 0; j < C_COLS / p2; ++j)
				C[i * C_COLS + j] = C_part[i * C_COLS / p2 + j];

		for (unsigned int i = 1; i < proc_num; ++i)
			MPI_Recv(C + offset[i], 1, C_cell, i, 2, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
	}
	else 
		MPI_Send(C_part, C_ROWS / p1 * C_COLS / p2, MPI_DOUBLE, 0, 2, MPI_COMM_WORLD);

	MPI_Type_free(&B_cols);
	MPI_Type_free(&C_cell);
	MPI_Comm_free(&col_matrix_comm);
	MPI_Comm_free(&row_matrix_comm);
	MPI_Comm_free(&grid_matrix_comm);

	if (rank == 0) {
		double end = MPI_Wtime();
		printf("Time counting: %f; first el: %3.3f\n", end - start, C[0]);

		free(A);
		free(B);
		free(C);
	}

	free(A_part);
	free(B_part);
	free(C_part);

	MPI_Finalize();

	return 0;
}