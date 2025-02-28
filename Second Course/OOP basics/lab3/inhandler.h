#ifndef INPUT_HANDLER
#define INPUT_HANDLER

#include <string>
#include <iostream>
#include <cstring>
#include <cstdlib>

class InputHandler {
private:
	std::string input;
	std::string output;
	int iters;
	bool mode;

public:
	bool handle(int argc, char** agrv);
	void get_help();
	const std::string& get_in() const;
	const std::string& get_out() const;
	const int get_iters() const;
	const bool get_mode() const;
};

#endif //INPUT_HANDLER