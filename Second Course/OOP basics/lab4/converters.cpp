#include "converters.h"

Muter::Muter() = default;

Muter::Muter(std::vector<std::string> params) {
	if (params.size() != 2 && (params.size() != 0))
		throw "Invalid number of parameters for Muter";

	start = 0;
	end = INT_MAX;
	if (params.size() == 2) {
		int st, en;
		try {
			st = std::stoi(params[0]);
			en = std::stoi(params[1]);
		}
		catch (const std::invalid_argument& e) {
			throw "Invalid type of time interval parameters";
		}

		if (st > en)
			throw "Invalid time interval parameters";

		start = st;
		end = en;
	}
}

std::string Muter::get_desc() {
	return "Mute Converter (aka Muter) - mute sounds\nCalling: mute <start time> <end time>\n"
		"Params: integers; start and end time of processing interval (seconds)\n\n";
}

void Muter::process(WAVReader& in, WAVWritter& out) {
	WAVHeader head = in.read_head();
	out.write_head(head);

	for (unsigned int i = 0; i < in.count_samples(); ++i) {
		int16_t sample = in.get_sample();
		if ((i >= (start * head.frequency)) && (i <= (end * head.frequency)))
			sample = 0;

		out.write_sample(sample);
	}

	in.close_file();
	out.close_file();
}

Mixer::Mixer() = default;

Mixer::Mixer(std::vector<std::string> params) {
	if ((params.size() != 2) && (params.size() != 1))
		throw "Invalid number of parameters for Mixer";

	start = 0;
	if (params.size() == 2) {
		int st;
		try {
			st = std::stoi(params[1]);
		}
		catch (const std::invalid_argument& e) {
			throw "Invalid type of time interval parameters";
		}

		start = st;
	}

	addit_in = params[0];
}

std::string Mixer::get_desc() {
	return "Mix Converter (aka Mixer) - mix two sounds\nCalling: mix $<addition stream> <start time>\n"
		"Params: $<addition stream> - number of addition input file (integer), <start time> - time when mixing starts (seconds, integer)\n\n";
}

void Mixer::process(WAVReader& in, WAVWritter& out) {
	WAVReader addit(addit_in);
	WAVHeader add_head = addit.read_head();
	WAVHeader in_head = in.read_head();
	out.write_head(in_head);

	unsigned int size = 0;
	for (unsigned int i = 0; i < in.count_samples(); ++i) {
		int16_t sample = in.get_sample();
		if ((i >= (start * in_head.frequency)) && (size < addit.count_samples())) {
			int16_t add_sample = addit.get_sample();
			int16_t new_sample = (add_sample / 2 + sample / 2);
			out.write_sample(new_sample);
			++size;
		}
		else
			out.write_sample(sample);
	}

	in.close_file();
	addit.close_file();
	out.close_file();
}

Inverser::Inverser() = default;

Inverser::Inverser(std::vector<std::string> params) {
	if ((params.size() != 3) && (params.size() != 2) && (params.size() != 0))
		throw "Invalid number of parameters for Invreser";

	start = 0;
	end = INT_MAX;
	if (params.size() >= 2) {
		int st, en;
		try {
			st = std::stoi(params[0]);
			en = std::stoi(params[1]);
		}
		catch (const std::invalid_argument& e) {
			throw "Invalid type of time interval parameters";
		}

		if (st > en)
			throw "Invalid time interval parameters";

		start = st;
		end = en;

		if (params.size() == 3) {
			int sn;
			try {
				sn = std::stoi(params[2]);
			}
			catch (const std::invalid_argument& e) {
				throw "Invalid type of time interval parameters";
			}
			low = sn;
		}
	}
}

std::string Inverser::get_desc() {
	return "Inverse Consverter (aka Inverser) - abs inverse sounds\nCalling: inverse <start time> <end time> <low volume>\n"
		"Params: start and end time of processing interval (seconds) - integers, <low volume> - make sounse more quite - short int (default 1)\n\n";
}

void Inverser::process(WAVReader& in, WAVWritter& out) {
	WAVHeader head = in.read_head();
	out.write_head(head);

	for (unsigned int i = 0; i < in.count_samples(); ++i) {
		int16_t sample = in.get_sample();
		if ((i >= (start * head.frequency)) && (i <= (end * head.frequency))) {
			if (sample >= 0)
				sample = (SHRT_MAX - sample) / low;
			else
				sample = (SHRT_MIN - sample) / low;
		}

		out.write_sample(sample);
	}

	in.close_file();
	out.close_file();
}

Fabric::Fabric() {}

std::unique_ptr<Converter> Fabric::create_conv(Command& com) {
	if (com.name == "mute")
		return std::make_unique<Muter>(com.params);
	else if (com.name == "mix")
		return std::make_unique<Mixer>(com.params);
	else if (com.name == "inverse")
		return std::make_unique<Inverser>(com.params);
	else
		throw "Invalid command";
}

void Fabric::add_conv(Command& com) {
	auto conv = create_conv(com);
	converters.push_back(std::move(conv));
}

void Fabric::get_description() {
	Muter mu;
	Mixer mi;
	Inverser i;

	std::cout << mu.get_desc();
	std::cout << mi.get_desc();
	std::cout << i.get_desc();
}

const std::vector<std::unique_ptr<Converter>>& Fabric::get_conv() const {
	return converters;
}