#include "game.h"

Game::Game(UniverseParams params) : universe(params) {}

void Game::dump(std::string name) {
	std::ofstream out(name);
	if (!out.is_open())
		throw std::runtime_error("File could not be opened");

	out << "#Life 1.06\n";
	out << "#N " << universe.get_name() << "\n";
	out << "#R B";
	for (const auto& i : universe.get_rb())
		out << (char)(i + '0');
	out << "/S";

	for (const auto& i : universe.get_rs())
		out << (char)(i + '0');
	out << "\n#S " << universe.get_size() << "\n";

	std::vector<std::vector<bool>> field = universe.get_field();
	for (int i = 0; i < field.size(); ++i)
		for (int j = 0; j < field.size(); ++j)
			if (field[i][j])
				out << i << " " << j << "\n";

	out.close();
}

void Game::cnt_tick() {
	std::vector<std::vector<bool>> nfield;
	int size = universe.get_size();
	for (int i = 0; i < size; ++i) {
		std::vector<bool> row(size, false);
		nfield.push_back(row);
	}
	
	std::vector<char> rb = universe.get_rb();
	std::vector<char> rs = universe.get_rs();
	for (int i = 0; i < size; ++i)
		for (int j = 0; j < size; ++j) {
			int neigh = universe.cnt_neigh(i, j);
			if (!universe.get_field()[i][j] && (find(rb.begin(), rb.end(), neigh) != rb.end()))
				nfield[i][j] = true;
			else if (universe.get_field()[i][j] && (find(rs.begin(), rs.end(), neigh) != rs.end()))
				nfield[i][j] = true;
		}

	universe.update_field(nfield);
}

void Game::tick(int n) {
	for (int i = 0; i < n; ++i)
		cnt_tick();

	std::cout << "Iteration: " << n << "\nSize: " << universe.get_size() << "\n";
	universe.show_universe();
}

void Game::get_help() {
	std::cout << "In process you can use next commands:\n"
		"dump <file> - game will be saved in output file\n"
		"tick <n=1> (t <n=1>) - count <n> iterarions (default 1)\n"
		"exit - end game\n";
}

void Game::show_status() {
	universe.show_info();
	universe.show_universe();
}

Universe& Game::get_universe() {
	return universe;
}

std::ostream& operator<<(std::ostream& stream, const Universe& u) {
	stream << "Name: " << u.get_name() << "\n";

	stream << "Birth rule: ";
	for (const auto &i : u.get_rb())
		stream << (int)i;

	stream << "\nSurvive rule: ";
	for (const auto& i : u.get_rs())
		stream << (int)i;
	stream << "\nSize: " << u.get_size() << "\n";

	std::vector<std::vector<bool>> field = u.get_field();
	for (int i = 0; i < field.size(); ++i) {
		for (int j = 0; j < field.size(); ++j)
			if (field[i][j])
				stream << "o";
			else
				stream << ".";
		stream << "\n";
	}

	return stream;
}

std::istream& operator>>(std::istream& stream, Universe& u) {
	std::string line;
	int size = 0;
	bool n_s = false, rb_s = false, rs_s = false;
	while (std::getline(stream, line)) {
		if (line.substr(0, 5) == "Size:") {
			size = std::stoi(line.erase(0, 6));
			break;
		}

		else if (line.substr(0, 11) == "Birth rule:") {
			std::string data = line.erase(0, 12);
			std::vector<char> rb;
			for (size_t i = 0; i < data.size(); ++i)
				rb.push_back(data[i] - '0');
			u.set_brule(rb);
			rb_s = true;
		}

		else if (line.substr(0, 13) == "Survive rule:") {
			std::string data = line.erase(0, 14);
			std::vector<char> rs;
			for (size_t i = 0; i < data.size(); ++i)
				rs.push_back(data[i] - '0');
			u.set_brule(rs);
			rs_s = true;
		}

		else if (line.substr(0, 5) == "Name:") {
			u.set_name(line.erase(0, 6));
			n_s = true;
		}

		else
			continue;
	}

	if (!n_s)
		u.set_name("None");
	if (!rb_s) {
		std::vector<char> rb = { 3 };
		u.set_brule(rb);
	}
	if (!rs_s) {
		std::vector<char> rs = { 2, 3 };
		u.set_srule(rs);
	}

	std::vector<std::vector<bool>> f;
	for (int i = 0; i < size; ++i) {
		std::vector<bool> row(size, false);
		std::string tmp;
		stream >> tmp;
		for (int j = 0; j < size; ++j)
			if (tmp[j] == 'o')
				row[j] = true;
		f.push_back(row);
	}

	u.update_field(f);

	return stream;
}