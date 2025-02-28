#include "filewritter.h"

FileWritter::FileWritter(std::string name) {
	out.open(name);
}

void FileWritter::write(const std::map<std::string, int>& frequency, int wrds_num) {
	if (!out.is_open())
		std::cerr << "The file could not be opened" << std::endl;

	std::list<const std::pair<const std::string, int>*> sorted_frequency;
	for (const auto& j : frequency)
		sorted_frequency.push_back(&j);

	sorted_frequency.sort([](const auto * prev, const auto * next) {
		if ((*prev).second != (*next).second)
			return (*prev).second > (*next).second;
		return (*prev).first < (*next).first;
		});

	for (auto& i : sorted_frequency) {
		double statistic = double ((*i).second) / wrds_num;
		out << (*i).first << ";" << (*i).second << ";" << statistic << std::endl;
	}

	out.close();
}