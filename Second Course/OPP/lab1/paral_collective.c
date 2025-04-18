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
    int s = 0, s_part = 0;
    int *b, *a;

    if (rank == 0) {
        a = (int*)calloc(n, sizeof(int));
        b = (int*)calloc(n, sizeof(int));

        for (int i = 0; i < n; ++i) {
	    a[i] = rand() % 10;
	    b[i] = rand() % 10;
        }
    }
    else
        b = (int*)calloc(n, sizeof(int));

    int* a_part = (int*)calloc(part_size, sizeof(int));

    MPI_Bcast(b, n, MPI_INT, 0, MPI_COMM_WORLD);

    MPI_Scatter(a, part_size, MPI_INT, a_part, part_size, MPI_INT, 0, MPI_COMM_WORLD);

    for (int i = 0; i < part_size; ++i)
        for (int j = 0; j < n; ++j)
            s_part += a_part[i] * b[j];

    MPI_Reduce(&s_part, &s, 1, MPI_INT, MPI_SUM, 0, MPI_COMM_WORLD);

    if (rank == 0) {
        double end = MPI_Wtime();
        printf("Total summ: %d\nTime counting: %f\n", s, (end - start));

        free(a);
    }
    free(a_part);
    free(b);

    MPI_Finalize();

    return 0;
}
