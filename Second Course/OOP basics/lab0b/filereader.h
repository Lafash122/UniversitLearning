#ifndef FILE_READER_H
#define FILE_READER_H

#include <string>
#include <fstream>
#include <iostream>
#include <map>
#include <algorithm>
#include <regex>

class FileReader {
private:
	std::ifstream in;
	int wrds_num = 0;
	std::map<std::string, int> frequency;

public:
	FileReader(std::string name);
	const std::map<std::string, int>& get_statistics() const;
	int get_words_number() const;
};

#endif //FILE_READER_H