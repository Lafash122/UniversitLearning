#include "filereader.h"
#include "filewritter.h"
#include <iostream>

int main(int argc, char **argv) {
	if (argc != 3) {
		std::cout << "bad input" << std::endl;
		return 0;
	}

	FileReader input(argv[1]);
	FileWritter output(argv[2]);
	output.write(input.get_statistics(), input.get_words_number());

	std::cout << "Work done" << std::endl;

	return 0;
}