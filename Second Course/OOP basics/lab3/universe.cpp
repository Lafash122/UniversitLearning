#include "universe.h"

Universe::Universe(UniverseParams prms) : params(prms) {}

char Universe::cnt_neigh(int x, int y) {
	char res = 0;
	for (int i = -1; i < 2; ++i)
		for (int j = -1; j < 2; ++j) {
			int t_x, t_y;
			if (x + i == -1)
				t_x = params.size - 1;
			else if (x + i == params.size)
				t_x = 0;
			else
				t_x = x + i;

			if (y + j == -1)
				t_y = params.size - 1;
			else if (y + j == params.size)
				t_y = 0;
			else
				t_y = y + j;

			if ((t_x != x) || (t_y != y))
				res += params.field[t_x][t_y];
		}

	return res;
}

void Universe::update_field(std::vector<std::vector<bool>> f) {
	params.field = f;
	params.size = f.size();
}

void Universe::show_info() {
	std::cout << "Name: " << params.name;
	std::cout << "\nBirh rule: ";
	for (const auto& i : params.rule_birth)
		std::cout << (int)i << " ";
	std::cout << "\nSurvive rule: ";
	for (const auto& i : params.rule_surve)
		std::cout << (int)i << " ";
	std::cout << "\nSize: " << params.size << "\n";
}

void Universe::show_universe() {
	for (int i = 0; i < params.size; ++i) {
		for (int j = 0; j < params.size; ++j)
			if (params.field[i][j])
				std::cout << "o";
			else
				std::cout << ".";
		std::cout << "\n";
	}
}

void Universe::set_name(std::string name) {
	params.name = name;
}

void Universe::set_brule(std::vector<char> rb) {
	params.rule_birth = rb;
}

void Universe::set_srule(std::vector<char> rs) {
	params.rule_birth = rs;
}

int Universe::get_size() const {
	return params.size;
}

const std::string& Universe::get_name() const {
	return params.name;
}

const std::vector<char>& Universe::get_rb() const {
	return params.rule_birth;
}

const std::vector<char>& Universe::get_rs() const {
	return params.rule_surve;
}

const std::vector<std::vector<bool>>& Universe::get_field() const {
	return params.field;
}

UniverseHandler::UniverseHandler(std::string fname) : filename(fname) {
	in.open(filename);
}

bool UniverseHandler::check_label() {
	std::string lable;
	std::getline(in, lable);
	if (lable == "#Life 1.06")
		return true;

	return false;
}

std::vector<std::vector<bool>> UniverseHandler::read_cells(int size) {
	std::vector<std::vector<bool>> field;
	for (int i = 0; i < size; ++i) {
		std::vector<bool> row(size, false);
		field.push_back(row);
	}

	in.clear();
	in.seekg(0, std::ios::beg);
	std::string tmp;
	while (std::getline(in, tmp))
		if (tmp.find("#") == std::string::npos) {
			try {
				int x = std::stoi(tmp.substr(0, tmp.find(" ")));
				int y = std::stoi(tmp.substr(tmp.find(" ") + 1));
				field[x][y] = true;
			}
			catch (const std::invalid_argument& e) {
				throw std::runtime_error("Invalid position of cell: not integer");
			}
			catch (const std::out_of_range& e) {
				throw std::runtime_error("Invalid position of cell: could not be set");
			}
		}

	return field;
}

UniverseParams UniverseHandler::handle() {
	if (!check_label()) {
		in.close();
		throw std::runtime_error("Invalid Universe file format");
	}

	UniverseParams params;
	std::string tmp;
	int line_num = 2;

	while (std::getline(in, tmp)) {
		if (line_num == 2)
			if ((tmp.substr(0, 2) != "#N") || (tmp.size() <= 3)) {
				std::cerr << "Universe name could not be found - will be set default\n";
				params.name = "New Universe";
			}
			else
				params.name = tmp.substr(3);

		else if (line_num == 3)
			if ((tmp.substr(0, 2) != "#R") || (tmp.size() <= 3))
				std::cerr << "Universe rules could not be found - will be set default\n";
			else {
				if ((tmp.find("B") != 3) || (tmp.find("B") == (tmp.size() - 1))) {
					std::cerr << "Invalid birth rules - will be set default\n";
					continue;
				}
				for (char i = 4; i < tmp.find("/"); ++i)
					if (std::find(params.rule_birth.begin(), params.rule_birth.end(), tmp[i] - '0') == params.rule_birth.end())
						params.rule_birth.push_back(tmp[i] - '0');

				if ((tmp.find("S") != (tmp.find("/") + 1)) || (tmp.find("S") == (tmp.size() - 1))) {
					std::cerr << "Invalid survive rules - will be set default\n";
					continue;
				}
				for (char i = tmp.find("S") + 1; i < tmp.size(); ++i)
					if (std::find(params.rule_surve.begin(), params.rule_surve.end(), tmp[i] - '0') == params.rule_surve.end())
						params.rule_surve.push_back(tmp[i] - '0');
			}

		else if (line_num == 4)
			if ((tmp.substr(0, 2) != "#S") || (tmp.size() <= 3)) {
				std::cerr << "Universe size could not be found - will be set default\n";
				params.size = 12;
			}
			else {
				try {
					params.size = std::stoi(tmp.substr(3));
				}
				catch (const std::invalid_argument& e) {
					throw std::runtime_error("Invalid size");
				}
			}
		++line_num;
	}

	if (params.rule_birth.empty())
		params.rule_birth = { 3 };
	if (params.rule_surve.empty())
		params.rule_surve = { 2, 3 };
	params.field = read_cells(params.size);

	in.close();

	return params;
}