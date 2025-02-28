#include <iostream>
#include <fstream>
#include <chrono>
#include <vector>
#include <random>
#include <string>

#define MIN_SIZE 1024 / sizeof(int)
#define MAX_SIZE 150 * 1024 * 1024 / sizeof(int)
#define K 6

std::string measuring(int* data, const int size) {
	unsigned long long i;
	int k;
	std::string str;
	for (k = 0, i = 0; i < size; i++)
		k = data[k];

	if (k == 2097152)
		std::cout << "Usage for \"warming\" cache" << std::endl;

	unsigned long long start = __builtin_ia32_rdtsc();
	for (k = 0, i = 0; i < size * K; i++)
		k = data[k];

	unsigned long long end = __builtin_ia32_rdtsc();
	if (k == 1337420)
		std::cout << "Usage for measuring time" << std::endl;

	str = std::to_string(((end - start) * 1.0) / (size * 1.0 * K));
	return str;
}

const std::string forward(int* data, const int size) {
	for (int i = 0; i < size - 1; i++)
		data[i] = i + 1;
	data[size - 1] = 0;
	return measuring(data, size);
}

const std::string backward(int* data, const int size) {
	data[0] = size - 1;
	for (int i = 1; i < size; i++)
		data[i] = i - 1;
	return measuring(data, size);
}

const std::string random(int* data, const int size) {
	std::random_device rd;
	std::mt19937 gen(rd());
	int* rd_arr = (int *) malloc(size * sizeof(int));
	for (int i = 0; i < size; ++i)
		rd_arr[i] = i;

	for (int i = size - 1; i > 0; --i) {
		std::uniform_int_distribution<> dis(0, i);
		int j = dis(gen);
		std::swap(rd_arr[i], rd_arr[j]);
	}

	for (int i = 0; i < size; ++i)
		data[rd_arr[i]] = rd_arr[(i + 1) % size];

	free(rd_arr);

	return measuring(data, size);
}

int main() {
	std::ofstream out;
	out.open("out.csv");

	if (out.is_open()) {
		const auto start_time = std::chrono::steady_clock::now();
		const size_t data_size = 10000;
		std::vector<double> overclock_arr(data_size, 0.3);

		out << "size;forward;back;random" << std::endl;

		while (std::chrono::steady_clock::now() - start_time < std::chrono::seconds(1))
			for (size_t i = 0; i < data_size; ++i)
				for (size_t j = 0; j < data_size; ++j)
					overclock_arr[i] = overclock_arr[i] * overclock_arr[i] + overclock_arr[j] * overclock_arr[j];

		if (overclock_arr[0] == 0)
			std::cout << "Cool" << std::endl;
		int* data = (int *) malloc(MAX_SIZE * sizeof(int));

		for (double size = MIN_SIZE; size <= MAX_SIZE; size *= 1.2) {
			out << (int)(size * sizeof(int)) << ";";
			out << forward(data, (int)size) << ";";
			out << backward(data, (int)size) << ";";
			out << random(data, (int)size) << std::endl;
		}

		free(data);
		out.close();
	}
	
	return 0;
}
