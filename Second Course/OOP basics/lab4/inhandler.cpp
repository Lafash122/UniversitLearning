#include "inhandler.h"

InputHandler::InputHandler(int argc, char** argv) : arg_cnt(argc), arg_vect(argv) {}

void InputHandler::get_help() {
	std::cout << "The Sound Processor program has the following converters:\n";
	Fabric f;
	f.get_description();
}

bool InputHandler::check() {
	if (arg_cnt < 2) {
		std::cerr << "The program should be running: soundproc [-h] [-c config.txt output.wav input1.wav [input2.wav ...]]\n";
		return false;
	}

	if (strcmp("-h", arg_vect[1]) == 0) {
		std::cout << "The program should be running: soundproc -c config.txt output.wav input1.wav [input2.wav ...]\n";
		get_help();
		return false;
	}

	if (strcmp("-c", arg_vect[1]) == 0) {
		if (arg_cnt < 5) {
			std::cerr << "Enter the config file name, then output file name and after input file names\n";
			return false;
		}

		config = arg_vect[2];
		output = arg_vect[3];
		for (int i = 4; i < arg_cnt; ++i)
			input.push_back(arg_vect[i]);
		
		return true;
	}

	std::cerr << "The program should be running: soundproc [-h] [-c ...]\n";
	return false;
}

const std::string& InputHandler::get_cfg() const {
	return config;
}

const std::string& InputHandler::get_out() const {
	return output;
}

const std::vector<std::string>& InputHandler::get_in() const {
	return input;
}

CfgHandler::CfgHandler(std::string in, std::vector<std::string> ins) : in_name(in), inputs(ins) {
	input.open(in_name);
}

std::string CfgHandler::make_add_in(std::string param) {
	if (param[0] != '$')
		return param;

	if (param.size() == 1)
		throw "Invalid name of link on the addition input stream";

	int link;
	try {
		link = std::stoi(param.erase(0, 1));
	}
	catch (const std::invalid_argument& e) {
		throw "invalid type of addition input stream link";
	}

	if ((link == 0) || (link > inputs.size()))
		throw "Invalid link on the addition input stream";

	return inputs[link - 1];
}

bool CfgHandler::handle() {
	if (!input.is_open()) {
		std::cerr << "Config file cannot be opened";
		return false;
	}

	std::string line;
	while (std::getline(input, line)) {
		if ((line.empty()) || (line[0] == '#'))
			continue;

		Command com;
		std::istringstream string(line);
		std::string tmp;

		string >> tmp;
		com.name = tmp;
		while (string >> tmp) {
			tmp = make_add_in(tmp);
			com.params.push_back(tmp);
		}

		comms.push_back(com);
	}

	return true;
}

std::vector<Command> CfgHandler::get_comms() {
	return comms;
}