#ifndef INPUT_HANDLER
#define INPUT_HANDLER

#include <string>
#include <vector>
#include <fstream>
#include <iostream>
#include <sstream>
#include <cstring>
#include "converters.h"


struct Command {
	std::string name;
	std::vector<std::string> params;
};

class InputHandler {
private:
	int arg_cnt;
	char** arg_vect;
	std::string output;
	std::string config;
	std::vector<std::string> input;

public:
	InputHandler(int agrc, char** argv);
	bool check();
	void get_help();
	const std::string& get_cfg() const;
	const std::string& get_out() const;
	const std::vector<std::string>& get_in() const;
};

class CfgHandler {
private:
	std::vector<Command> comms;
	std::string in_name;
	std::ifstream input;
	std::vector<std::string> inputs;
	std::string make_add_in(std::string param);

public:
	CfgHandler(std::string in, std::vector<std::string> ins);
	bool handle();
	std::vector<Command> get_comms();
};

#endif //INPUT_HANDLER