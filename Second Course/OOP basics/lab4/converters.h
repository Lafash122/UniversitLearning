#ifndef CONVERTERS
#define CONVERTERS

#include <string>
#include <vector>
#include <stdexcept>
#include <cstdint>
#include "wavhandler.h"
#include "inhandler.h"

struct Command;

class Converter {
public:
	virtual ~Converter() = default;
	virtual void process(WAVReader &in, WAVWritter &out) = 0;
	virtual std::string get_desc() = 0;
};

class Muter : public Converter {
private:
	int start;
	int end;

public:
	Muter();
	Muter(std::vector<std::string> params);
	std::string get_desc() override;
	void process(WAVReader& in, WAVWritter& out) override;
};

class Mixer : public Converter {
private:
	int start;
	std::string addit_in;

public:
	Mixer();
	Mixer(std::vector<std::string> params);
	std::string get_desc() override;
	void process(WAVReader& in, WAVWritter& out) override;
};

class Inverser : public Converter {
private:
	int start;
	int end;
	uint16_t low = 1;

public:
	Inverser();
	Inverser(std::vector<std::string> params);
	std::string get_desc() override;
	void process(WAVReader& in, WAVWritter& out) override;
};

class Fabric {
private:
	std::vector<std::unique_ptr<Converter>> converters;
	
public:
	Fabric();
	std::unique_ptr<Converter> create_conv(Command& com);
	void add_conv(Command& com);
	void get_description();
	const std::vector<std::unique_ptr<Converter>>& get_conv() const;
};

#endif //CONVERTERS