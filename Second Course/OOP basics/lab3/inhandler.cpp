#include "inhandler.h"

bool InputHandler::handle(int argc, char** argv) {
	if (argc == 1) {
		mode = false;
		return true;
	}

	if (strcmp(argv[1], "-h") == 0) {
		get_help();
		return false;
	}

	if ((argc < 4) || (argc > 6)) {
		std::cerr << "Invalid input parameters\n";
		return false;
	}

	for (int i = 0; i < argc; ++i) {
		if (strcmp(argv[i], "-i") == 0)
			if (i + 1 < argc) {
				std::string tmp = argv[++i];
				try {
					iters = std::stoi(tmp);
					if (iters < 0) {
						std::cerr << "Number of iterarions must be positive\n";
						return false;
					}
				}
				catch (const std::invalid_argument& e) {
					std::cerr << "Invalid number of iterations: " << tmp << std::endl;
					return false;
				}
			}
			else {
				std::cerr << "Expected number of iterations\n";
				return false;
			}

		else if (strncmp(argv[i], "--iterations=", 13) == 0) {
			std::string tmp = argv[i];
			try {
				iters = std::stoi(tmp.erase(0, 13));
				if (iters < 0) {
					std::cerr << "Number of iterarions must be positive\n";
					return false;
				}
			}
			catch (const std::invalid_argument& e) {
				std::cerr << "Invalid number of iterations: " << tmp << std::endl;
				return false;
			}
		}

		else if (strcmp(argv[i], "-o") == 0) {
			if (i + 1 < argc)
				output = argv[++i];
			else {
				std::cerr << "Expected output file name\n";
				return false;
			}

			if ((output == "-i") || (output.substr(0, 13) == "--iterations=")) {
				std::cerr << "Invalid output file name\n";
				return false;
			}
		}

		else if (strncmp(argv[i], "--output=", 9) == 0) {
			std::string tmp = argv[i];
			tmp.erase(0, 9);
			if (tmp == "") {
				std::cerr << "Empty output file name\n";
				return false;
			}
			else
				output = tmp;
		}

		else
			input = argv[i];
	}

	mode = true;
	return true;
}

void InputHandler::get_help() {
	std::cout << "Input File Name is set: <filename>\n"
		"Number of Iterations is set: <-i x> or <--iterations=x>\n"
		"Output File Name is set: <-o filename> or <--output=filename>\n";
}

const std::string& InputHandler::get_in() const {
	return input;
}

const std::string& InputHandler::get_out() const {
	return output;
}

const int InputHandler::get_iters() const {
	return iters;
}

const bool InputHandler::get_mode() const{
	return mode;
}