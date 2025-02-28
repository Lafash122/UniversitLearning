#include "printtup.h"
#include "csvpars.h"

#include <gtest/gtest.h>
#include <iostream>
#include <sstream>
#include <vector>
#include <string>


TEST(TuplePrinterTests, Print) {
	std::ostringstream stream;
	std::streambuf* old = std::cout.rdbuf(stream.rdbuf());

	std::tuple<int, std::string, char, bool> full(16, "Good kitty", 'O', true);
	std::tuple<> empty;

	std::cout << full << std::endl;
	ASSERT_EQ(stream.str(), "16; Good kitty; O; true\n");
	stream.str("");

	std::cout << empty << std::endl;
	ASSERT_EQ(stream.str(), "\n");
	stream.str("");

	std::cout << true << false << std::endl;
	ASSERT_EQ(stream.str(), "10\n");
	stream.str("");

	std::cout.rdbuf(old);
}

TEST(CSVParserTests, Default) {
	std::ifstream file("data.csv");
	std::vector<int> nums = { 0, 1, 2, 3 };
	std::vector<std::string> str = { "Start some text", "Hello,", "beautiful", "World!" };

	CSVParser<int, std::string> pars1(file);
	char ind = 0;
	for (const auto& rs : pars1) {
		ASSERT_EQ(std::get<0>(rs), nums[ind]);
		ASSERT_EQ(std::get<1>(rs), str[ind]);
		++ind;
	}

	CSVParser<int, std::string> pars2(file, 1);
	ind = 1;
	for (const auto& rs : pars2) {
		ASSERT_EQ(std::get<0>(rs), nums[ind]);
		ASSERT_EQ(std::get<1>(rs), str[ind]);
		++ind;
	}

	CSVParser<int, std::string> pars3(file, 2);
	ind = 2;
	for (const auto& rs : pars3) {
		ASSERT_EQ(std::get<0>(rs), nums[ind]);
		ASSERT_EQ(std::get<1>(rs), str[ind]);
		++ind;
	}

	CSVParser<int, std::string> pars4(file, 3);
	ind = 3;
	for (const auto& rs : pars4) {
		ASSERT_EQ(std::get<0>(rs), nums[ind]);
		ASSERT_EQ(std::get<1>(rs), str[ind]);
		++ind;
	}

	CSVParser<int, std::string> pars5(file, 4);
	for (const auto& rs : pars5)
		ASSERT_EQ(std::get<1>(rs), "");
}

TEST(CSVParserTests, Splitter) {
	std::ifstream file("spl.csv");
	std::vector<std::string> one = { "c", " 1b", " 1n" };
	std::vector<std::string> two = { "c,", "1b,", "1n" };
	std::vector<std::string> tre = { "c, ", "b, ", "n" };

	CSVParser<std::string, std::string, std::string> pars1(file, 0, ',');
	for (const auto& i : pars1) {
		ASSERT_EQ(std::get<0>(i), one[0]);
		ASSERT_EQ(std::get<1>(i), one[1]);
		ASSERT_EQ(std::get<2>(i), one[2]);
	}

	CSVParser<std::string, std::string, std::string> pars2(file, 0, ' ');
	for (const auto& i : pars2) {
		ASSERT_EQ(std::get<0>(i), two[0]);
		ASSERT_EQ(std::get<1>(i), two[1]);
		ASSERT_EQ(std::get<2>(i), two[2]);
	}

	CSVParser<std::string, std::string, std::string> pars3(file, 0, '1');
	for (const auto& i : pars3) {
		ASSERT_EQ(std::get<0>(i), tre[0]);
		ASSERT_EQ(std::get<1>(i), tre[1]);
		ASSERT_EQ(std::get<2>(i), tre[2]);
	}
}

TEST(CSVParserTests, Shield) {
	std::ifstream file("shld.csv");
	std::vector<std::string> one = { "+1", "+2", "+3" };
	std::vector<std::string> two = { "1; \"OOP Labs\"", "2; \"Big Call\"", "3; \"Some text\"" };

	char ind = 0;
	CSVParser<std::string, std::string> pars1(file, 0, ';');
	for (const auto& i : pars1) {
		ASSERT_EQ(std::get<0>(i), one[ind]);
		++ind;
	}

	ind = 0;
	CSVParser<std::string> pars2(file, 0, ';', '+');
	for (const auto& i : pars2) {
		ASSERT_EQ(std::get<0>(i), two[ind]);
		++ind;
	}
}