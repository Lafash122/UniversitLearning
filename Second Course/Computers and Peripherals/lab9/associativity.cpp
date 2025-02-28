#include <iostream>
#include <fstream>
#include <chrono>
#include <vector>
#include <cmath>
#include <climits>
#include <string>

#define MAX_FRAGS 32
#define OFFSET 8 * 1024 * 1024 / sizeof(int)
#define SIZE_FRAG 393 * 1024 / sizeof(int)
#define SIZE MAX_FRAGS * OFFSET

void make_data(int* data, int frags) {
	int i = 0, j = 1;
	while (i < SIZE_FRAG) {
		for (j = 1; j < frags; j++)
			data[i + (j - 1) * (OFFSET)] = i + j * (OFFSET);
		data[i + (j - 1) * (OFFSET)] = i + 1;
		++i;
	}
	data[i - 1 + (j - 1) * (OFFSET)] = 0;
}

const std::string measuring(int* data, int frags) {
	make_data(data, frags);
	int i;
	int k;
	unsigned long long min_time = ULLONG_MAX;
	std::string res;

	for (int iters = 0; iters < 6; ++iters) {
		unsigned long long start = __builtin_ia32_rdtsc();
		for (k = 0, i = 0; i < SIZE; ++i)
			k = data[k];
		unsigned long long end = __builtin_ia32_rdtsc();

		if (k == 15122024)
			std::cout << "Usage for measuring time" << std::endl;

		unsigned long long time = end - start;
		min_time = std::min(min_time, time);
	}

	res = std::to_string(min_time / (unsigned long long)(SIZE));
	return res;
}

int main() {
	std::ofstream out("out.csv");
	if (out.is_open()) {
		const auto start_time = std::chrono::steady_clock::now();
		const size_t data_size = 10000;
		std::vector<double> overclock_arr(data_size, 0.1);

		out << "fragments;time" << std::endl;

		while ((std::chrono::steady_clock::now() - start_time) < std::chrono::seconds(2))
			for (size_t i = 0; i < data_size; ++i)
				for (size_t j = 0; j < data_size; ++j)
					overclock_arr[i] = std::sqrt(overclock_arr[i] * overclock_arr[i] + overclock_arr[j] * overclock_arr[j]);

		if (overclock_arr[0] == 0)
			std::cout << "Cool" << std::endl;
		int* data = (int*) malloc(SIZE * sizeof(int));

		for (int fragments = 1; fragments <= MAX_FRAGS; ++fragments) {
			out << fragments << ";";
			out << measuring(data, fragments) << std::endl;
		}

		free(data);
		out.close();
	}

	return 0;
}
