#include "wavhandler.h"

WAVReader::WAVReader(std::string in) : in_name(in) {}

WAVHeader WAVReader::read_head() {
	input.open(in_name, std::ios::binary);
	if (!input.is_open())
		throw "The file cannot be opened";

	WAVHeader res;
	input.read(reinterpret_cast<char*>(&res), sizeof(res));

	if (res.format != SUP_FORMAT)
		throw "Not supported file format";
	if (res.audio_format != SUP_AUDIO)
		throw "Not supported audio format";
	if (res.num_channels != SUP_CHANNEL)
		throw "Not supported number of channels";
	if (res.depth != SUP_SIZE_SAMP)
		throw "Not supported depth of sample";
	if (res.frequency != SUP_FREQ)
		throw "Not supported frequncy";

	header = res;
	return res;
}

unsigned int WAVReader::count_samples() {
	if (!input.is_open())
		throw "The file cannot be opened";

	return header.data_size / (header.depth / BYTE);
}

int16_t WAVReader::get_sample() {
	if (!input.is_open())
		throw "The file cannot be opened";

	int16_t sample;
	input.read(reinterpret_cast<char*>(&sample), sizeof(sample));

	return sample;
}

void WAVReader::close_file() {
	input.close();
}

WAVWritter::WAVWritter(std::string out) : out_name(out) {}

void WAVWritter::write_head(WAVHeader header) {
	output.open(out_name, std::ios::binary);
	if (!output.is_open())
		throw "The file cannot be opened";

	output.write(reinterpret_cast<char*>(&header), sizeof(header));
}

void WAVWritter::write_sample(short sample) {
	if (!output.is_open())
		throw "The file cannot be opened";

	output.write(reinterpret_cast<char*>(&sample), sizeof(sample));
}

void WAVWritter::close_file() {
	output.close();
}