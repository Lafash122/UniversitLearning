#ifndef FILE_WRITTER_H
#define FILE_WRITTER_H

#include <string>
#include <fstream>
#include <iostream>
#include <map>
#include <list>
#include <algorithm>

class FileWritter {
private:
	std::ofstream out;

public:
	FileWritter(std::string name);
	void write(const std::map<std::string, int>& frequency, int wrds_num);
};

#endif //FILE_WRITTER_H
