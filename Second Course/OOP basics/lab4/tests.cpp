#include <gtest/gtest.h>
#include <iostream>
#include <sstream>
#include "inhandler.h"

TEST(InHandlersTests, InHandler) {
	std::ostringstream stream;
	std::streambuf* old = std::cout.rdbuf(stream.rdbuf());

	const char* v1[] = { "soundproc.exe" };
	const char* v2[] = { "soundproc.exe", "-h" };
	const char* v3[] = { "soundproc.exe", "-c" };
	const char* v4[] = { "soundproc.exe", "-c", "cfg", "out", "in1", "in2" };
	const char* v5[] = { "soundproc.exe", "52" };

	InputHandler i1(1, const_cast<char**>(v1));
	ASSERT_FALSE(i1.check());

	InputHandler i2(2, const_cast<char**>(v2));
	ASSERT_FALSE(i2.check());

	InputHandler i3(2, const_cast<char**>(v3));
	ASSERT_FALSE(i3.check());

	InputHandler i4(6, const_cast<char**>(v4));
	ASSERT_TRUE(i4.check());
	ASSERT_EQ(i4.get_cfg(), "cfg");
	ASSERT_EQ(i4.get_out(), "out");
	ASSERT_EQ(i4.get_in()[0], "in1");
	ASSERT_EQ(i4.get_in()[1], "in2");

	InputHandler i5(2, const_cast<char**>(v5));
	ASSERT_FALSE(i5.check());

	stream.str("");
	std::cout.rdbuf(old);
}

TEST(InHandlersTests, CFGHandler) {
	const char* v[] = { "soundproc.exe", "-c", "cfg", "out", "in1", "in2" };
	InputHandler i(6, const_cast<char**>(v));
	i.check();

	CfgHandler h("tstcfg.txt", i.get_in());
	ASSERT_TRUE(h.handle());
	ASSERT_EQ((h.get_comms()[0]).name, "inverse");
	ASSERT_EQ((h.get_comms()[1]).name, "mute");
	ASSERT_EQ((h.get_comms()[2]).params[0], "0");
	ASSERT_EQ((h.get_comms()[3]).params[1], "16");
	ASSERT_EQ((h.get_comms()[4]).name, "mix");
	ASSERT_EQ((h.get_comms()[5]).params[0], "in1");
	ASSERT_EQ((h.get_comms()[5]).params[1], "500");
}

TEST(WavHandlerTests, GetInfo) {
	WAVReader r("input.wav");
	ASSERT_NO_THROW(r.read_head());
}