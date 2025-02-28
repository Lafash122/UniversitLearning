#include <iostream>
#include <string>
#include "inhandler.h"
#include "universe.h"
#include "game.h"


int main(int argc, char** argv) {
	InputHandler i;
	if (!i.handle(argc, argv)) {
		std::cout << "Ough";
		return 1;
	}
	
	if (i.get_mode()) {
		UniverseHandler uf(i.get_in());
		UniverseParams pf = uf.handle();
		Game gf(pf);
		gf.tick(i.get_iters());
		gf.dump(i.get_out());

		return 0;
	}

	UniverseHandler u("default.txt");
	std::cout << "ok";

	UniverseParams p = u.handle();
	Game g(p);

	std::string command;
	int iter = 0;
	std::cout << "Iteration: 0\n";
	g.show_status();
	while (true) {
		std::getline(std::cin, command);
		if (command == "exit")
			break;

		else if ((command.substr(0, 4) == "tick") || (command.substr(0, 2) == "t ")) {
			try {
				int n = std::stoi(command.substr(command.find(" ")));
				g.tick(n);
				iter += n;
				std::cout << "Iteration: " << iter << "\n";
				g.show_status();
			}
			catch (const std::invalid_argument& e) {
				throw std::runtime_error("Invalid tick - not integer");
			}
		}

		else if ((command.substr(0, 4) == "dump") && (command.size() > 5)) {
			std::string filename = command.substr(5);
			g.dump(filename);
		}

		else if (command == "help")
			g.get_help();
		else
			std::cerr << "Invalid command name: type <help>\n";
	}

	return 0;
}