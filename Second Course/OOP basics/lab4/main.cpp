#include <iostream>
#include <cstdio>
#include "inhandler.h"
#include "wavhandler.h"
#include "converters.h"


int main(int argc, char** argv) {
	InputHandler h(argc, argv);
	if (!h.check())
		return 1;

	CfgHandler ch(h.get_cfg(), h.get_in());
	if (!ch.handle())
		return 1;

	Fabric f;
	for (auto& i : ch.get_comms()) {
		f.add_conv(i);
	}

	if (ch.get_comms().size() % 2 == 0) {
		WAVReader r(h.get_in()[0]);
		WAVWritter w("tmp.wav");
		f.get_conv()[0]->process(r, w);
		for (auto j = 1; j < f.get_conv().size(); ++j) {
			if (j % 2 == 1) {
				WAVReader t("tmp.wav");
				WAVWritter w(h.get_out());
				f.get_conv()[j]->process(t, w);
			}
			else {
				WAVReader r(h.get_out());
				WAVWritter w("tmp.wav");
				f.get_conv()[j]->process(r, w);
			}
		}
	}
	else {
		WAVReader r(h.get_in()[0]);
		WAVWritter w(h.get_out());
		f.get_conv()[0]->process(r, w);
		for (auto j = 1; j < f.get_conv().size(); ++j) {
			if (j % 2 == 0) {
				WAVReader t("tmp.wav");
				WAVWritter w(h.get_out());
				f.get_conv()[j]->process(t, w);
			}
			else {
				WAVReader r(h.get_out());
				WAVWritter w("tmp.wav");
				f.get_conv()[j]->process(r, w);
			}
		}
	}

	std::remove("tmp.wav");

	return 0;
}