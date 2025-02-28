#ifndef GAME
#define GAME

#include <iostream>
#include <fstream>
#include <string>
#include <vector>
#include "universe.h"

class Game {
private:
	Universe universe;

public:
	Game(UniverseParams params);
	void dump(std::string name);
	void cnt_tick();
	void tick(int n);
	void get_help();
	void show_status();
	Universe& get_universe();

	friend std::ostream& operator<<(std::ostream& stream, const Universe& u);
	friend std::istream& operator>>(std::istream& stream, Universe& u);
};


#endif //GAME
