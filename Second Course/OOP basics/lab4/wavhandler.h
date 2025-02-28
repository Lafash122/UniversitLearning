#ifndef WAV_HANDLER
#define WAV_HANDLER

#include <string>
#include <fstream>
#include <iostream>
#include <cstdint>
#include <stdexcept>

#define BYTE 8
#define SUP_FORMAT 1163280727 //WAVE
#define SUP_AUDIO 1 //PCM
#define SUP_CHANNEL 1
#define SUP_SIZE_SAMP 16
#define SUP_FREQ 44100

struct WAVHeader {
	uint32_t start_label;
	uint32_t file_size;
	uint32_t format;

	uint32_t fmt_label;
	uint32_t chunck_size;
	uint16_t audio_format;
	uint16_t num_channels;
	uint32_t frequency;
	uint32_t bps;
	uint16_t align;
	uint16_t depth;

	uint32_t data_label;
	uint32_t data_size;
};

class WAVReader {
private:
	std::string in_name;
	std::ifstream input;
	WAVHeader header;

public:
	WAVReader(std::string in);
	WAVHeader read_head();
	unsigned int count_samples();
	int16_t get_sample();
	void close_file();
};

class WAVWritter {
private:
	std::string out_name;
	std::ofstream output;

public:
	WAVWritter(std::string out);
	void write_head(WAVHeader header);
	void write_sample(short sample);
	void close_file();
};

#endif //WAV_HANDLER