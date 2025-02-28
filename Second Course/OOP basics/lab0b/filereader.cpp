#include "filereader.h"

FileReader::FileReader(std::string name) {
	in.open(name);
	if (!in.is_open())
		std::cerr << "The file cannot be opened" << std::endl;
	
	std::string line;
	std::regex word_el("\\b[A-Za-z0-9_][A-Za-z0-9_-]*");

	while (std::getline(in, line)) {
		std::sregex_iterator current_match(line.begin(), line.end(), word_el);
		std::sregex_iterator last_match;
		while (current_match != last_match) {
			std::smatch match = *current_match;
			std::string word = match.str();
			std::transform(word.begin(), word.end(), word.begin(), [](unsigned char c) { return std::tolower(c); });
			frequency[word]++;
			wrds_num++;

			current_match++;
		}
	}
}

const std::map<std::string, int>& FileReader::get_statistics() const{
	return frequency;
}

int FileReader::get_words_number() const {
	return wrds_num;
}