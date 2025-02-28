#include <stdio.h>
#include <mpi.h>
#include <stdlib.h>

int main(int argc, char** argv) {
    MPI_Init(&argc, &argv);

    int rank, size;
    MPI_Comm_size(MPI_COMM_WORLD, &size);
    MPI_Comm_rank(MPI_COMM_WORLD, &rank);

    if (argc != 2) {
        if (rank == 0)
            printf("The programm run ./prog.exe <num>\n");
        
        MPI_Finalize();
        return 1;
    }

    int n = atoi(argv[1]);

    double start = MPI_Wtime();

    int part_size = n / size;
    int rem = n % size;
    int s = 0;

    if (rank == 0) {
        int* a = (int*)calloc(n, sizeof(int));
        int* b = (int*)calloc(n, sizeof(int));

        for (int i = 0; i < n; ++i) {
	    a[i] = rand() % 10;
	    b[i] = rand() % 10;
        }

        for (int i = 1; i < size; ++i) {
            MPI_Send(b, n, MPI_INT, i, 0, MPI_COMM_WORLD);

            int indent = i * part_size;
            MPI_Send(a + indent, part_size, MPI_INT, i, 1, MPI_COMM_WORLD);
        }

        for (int i = 0; i < part_size; ++i)
	    for (int j = 0; j < n; j++)
		s += a[i] * b[j];

        free(a);
        free(b);
    }
    else {
        int* b = (int*)calloc(n, sizeof(int));
        MPI_Recv(b, n, MPI_INT, 0, 0, MPI_COMM_WORLD, MPI_STATUS_IGNORE);

        int* a_part = (int*)calloc(part_size, sizeof(int));
        MPI_Recv(a_part, part_size, MPI_INT, 0, 1, MPI_COMM_WORLD, MPI_STATUS_IGNORE);

        int s_part = 0;
        for (int i = 0; i < part_size; ++i)
            for (int j = 0; j < n; j++)
                s_part += a_part[i] * b[j];

        MPI_Send(&s_part, 1, MPI_INT, 0, 2, MPI_COMM_WORLD);

        free(a_part);
        free(b);
    }

    if (rank == 0) {
        for (int i = 1; i < size; ++i) {
            int tmp;
            MPI_Recv(&tmp, 1, MPI_INT, i, 2, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
            s += tmp;
        }

        double end = MPI_Wtime();
        printf("Total summ: %d\nTime counting: %f\n", s, (end - start));
    }

    MPI_Finalize();

    return 0;
}
