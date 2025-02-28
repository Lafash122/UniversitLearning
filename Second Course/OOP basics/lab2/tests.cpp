#include <gtest/gtest.h>
#include "bitarray.h"
#include <iostream>

TEST(BitArrayTests, EmptyConstructor) {
	BitArray test;
	ASSERT_EQ(test.size(), 0);
	ASSERT_TRUE(test.empty());
}

TEST(BitArrayTests, ValueConstructor) {
	ASSERT_THROW(BitArray test1(0, 15), std::invalid_argument);
	ASSERT_THROW(BitArray test2(4, 16), std::invalid_argument);

	BitArray test3(9, 201);
	ASSERT_EQ(test3.size(), 9);
	ASSERT_EQ(test3.bit_seq(), "0 11001001");

	BitArray test4(8, 112);
	ASSERT_EQ(test4.size(), 8);
	ASSERT_EQ(test4.bit_seq(), "01110000");
}

TEST(BitArrayTests, CopyConstructor) {
	BitArray orig(8, 125);
	BitArray test(orig);
	ASSERT_EQ(test.size(), orig.size());
	ASSERT_EQ(test.bit_seq(), orig.bit_seq());
}
 
TEST(BitArrayTests, Swap) {
	BitArray a(8, 125);
	BitArray b(9, 256);
	b.swap(a);
	ASSERT_EQ(b.size(), 8);
	ASSERT_EQ(b.bit_seq(), "01111101");
	ASSERT_EQ(a.size(), 9);
	ASSERT_EQ(a.bit_seq(), "1 00000000");
}

TEST(BitArrayTests, EqOperator) {
	BitArray orig(8, 125);
	BitArray test;
	test = orig;
	ASSERT_EQ(test.size(), 8);
	ASSERT_EQ(test.bit_seq(), "01111101");
}

TEST(BitArrayTests, Resize) {
	BitArray test(12, 1875);
	ASSERT_THROW(test.resize(-1, false), std::invalid_argument);

	test.resize(8);
	ASSERT_EQ(test.size(), 8);
	ASSERT_EQ(test.bit_seq(), "01010011");

	test.resize(12, false);
	ASSERT_EQ(test.size(), 12);
	ASSERT_EQ(test.bit_seq(), "0000 01010011");

	test.resize(14, true);
	ASSERT_EQ(test.size(), 14);
	ASSERT_EQ(test.bit_seq(), "110000 01010011");

	test.resize(16);
	ASSERT_EQ(test.size(), 16);
	ASSERT_EQ(test.bit_seq(), "00110000 01010011");
}

TEST(BitArrayTests, Clear) {
	BitArray test(8, 125);
	test.clear();
	ASSERT_EQ(test.size(), 0);
	ASSERT_EQ(test.bit_seq(), "");
}

TEST(BitArrayTests, PushBack) {
	BitArray test(7, 125);
	test.push_back(true);
	ASSERT_EQ(test.size(), 8);
	ASSERT_EQ(test.bit_seq(), "11111101");

	test.push_back(false);
	ASSERT_EQ(test.size(), 9);
	ASSERT_EQ(test.bit_seq(), "0 11111101");
}

TEST(BitArrayTests, ConEqOperator) {
	BitArray test1a(8, 125);
	BitArray test1b(9, 130);
	ASSERT_THROW(test1a &= test1b, std::range_error);

	BitArray test2(8, 125);
	test2 &= test2;
	ASSERT_EQ(test2.bit_seq(), "01111101");

	BitArray test3a(8, 125);
	BitArray test3b(8, 130);
	test3b &= test3a;
	ASSERT_EQ(test3b.bit_seq(), "00000000");
}

TEST(BitArrayTests, DisEqOperator) {
	BitArray test1a(8, 125);
	BitArray test1b(9, 130);
	ASSERT_THROW(test1a |= test1b, std::range_error);

	BitArray test2(8, 125);
	test2 |= test2;
	ASSERT_EQ(test2.bit_seq(), "01111101");

	BitArray test3a(8, 125);
	BitArray test3b(8, 130);
	test3b |= test3a;
	ASSERT_EQ(test3b.bit_seq(), "11111111");
}

TEST(BitArrayTests, XOREqOperator) {
	BitArray test1a(8, 125);
	BitArray test1b(9, 130);
	ASSERT_THROW(test1a ^= test1b, std::range_error);

	BitArray test2(8, 125);
	test2 ^= test2;
	ASSERT_EQ(test2.bit_seq(), "01111101");

	BitArray test3a(8, 125);
	BitArray test3b(8, 254);
	test3b ^= test3a;
	ASSERT_EQ(test3b.bit_seq(), "10000011");
}

TEST(BitArrayTests, LeftShiftEqOperator) {
	BitArray test1(9, 125);
	test1 <<= 16;
	ASSERT_EQ(test1.bit_seq(), "0 00000000");

	BitArray test2(12, 1875);
	test2 <<= 8;
	ASSERT_EQ(test2.bit_seq(), "0011 00000000");

	BitArray test3(12, 1875);
	test3 <<= 10;
	ASSERT_EQ(test3.bit_seq(), "1100 00000000");

	BitArray test4(12, 1875);
	test4 <<= 1;
	ASSERT_EQ(test4.bit_seq(), "1110 10100110");

	BitArray test5(8, 125);
	test5 <<= 0;
	ASSERT_EQ(test5.bit_seq(), "01111101");
}

TEST(BitArrayTests, RightShiftEqOperator) {
	BitArray test1(9, 125);
	test1 >>= 16;
	ASSERT_EQ(test1.bit_seq(), "0 00000000");

	BitArray test2(12, 1884);
	test2 >>= 8;
	ASSERT_EQ(test2.bit_seq(), "0000 00000111");

	BitArray test3(12, 1884);
	test3 >>= 10;
	ASSERT_EQ(test3.bit_seq(), "0000 00000001");

	BitArray test4(12, 1884);
	test4 >>= 2;
	ASSERT_EQ(test4.bit_seq(), "0001 11010111");

	BitArray test5(8, 125);
	test5 >>= 0;
	ASSERT_EQ(test5.bit_seq(), "01111101");
}

TEST(BitArrayTests, LeftShiftOperator) {
	BitArray test(8, 125);
	ASSERT_EQ((test << 4).bit_seq(), "11010000");
}

TEST(BitArrayTests, RightShiftOperator) {
	BitArray test(8, 125);
	ASSERT_EQ((test >> 4).bit_seq(), "00000111");
}

TEST(BitArrayTests, SetElement) {
	BitArray test(12, 1875);
	ASSERT_THROW(test.set(16), std::invalid_argument);
	ASSERT_THROW(test.set(-1), std::invalid_argument);

	test.set(11);
	ASSERT_EQ(test.bit_seq(), "1111 01010011");

	test.set(11, false);
	ASSERT_EQ(test.bit_seq(), "0111 01010011");

	test.set(2, false);
	ASSERT_EQ(test.bit_seq(), "0111 01010011");

	test.set(1, true);
	ASSERT_EQ(test.bit_seq(), "0111 01010011");
}

TEST(BitArrayTests, Set) {
	BitArray test(12, 1875);
	test.set();
	ASSERT_EQ(test.bit_seq(), "1111 11111111");
}

TEST(BitArrayTests, ResetElement) {
	BitArray test(12, 1875);
	test.reset(8);
	ASSERT_EQ(test.bit_seq(), "0110 01010011");
}

TEST(BitArrayTests, Reset) {
	BitArray test(12, 1875);
	test.reset();
	ASSERT_EQ(test.bit_seq(), "0000 00000000");
}

TEST(BitArrayTests, Any) {
	BitArray test(12, 1875);
	ASSERT_TRUE(test.any());

	test.reset();
	ASSERT_FALSE(test.any());
}

TEST(BitArrayTests, None) {
	BitArray test(12, 1875);
	ASSERT_FALSE(test.none());

	test.reset();
	ASSERT_TRUE(test.none());
}

TEST(BitArrayTests, InverseOperator) {
	BitArray test(8, 195);
	ASSERT_EQ((~test).bit_seq(), "00111100");
}

TEST(BitArrayTests, Count) {
	BitArray test1;
	ASSERT_EQ(test1.count(), 0);

	BitArray test2(8, 0);
	ASSERT_EQ(test2.count(), 0);

	test2.set();
	ASSERT_EQ(test2.count(), 8);

	BitArray test3(8, 125);
	ASSERT_EQ(test3.count(), 6);
}

TEST(BitArrayTests, AccessOperator) {
	BitArray test(9, 381);
	ASSERT_EQ(test[0], true);
	ASSERT_EQ(test[1], false);
	ASSERT_EQ(test[2], true);
	ASSERT_EQ(test[3], true);
	ASSERT_EQ(test[4], true);
	ASSERT_EQ(test[5], true);
	ASSERT_EQ(test[6], true);
	ASSERT_EQ(test[7], false);
	ASSERT_EQ(test[8], true);
	ASSERT_THROW(test[9], std::invalid_argument);
}

TEST(BitArrayTests, Size) {
	BitArray test(8, 125);
	ASSERT_EQ(test.size(), 8);
}

TEST(BitArrayTests, Empty) {
	BitArray test(8, 125);
	ASSERT_FALSE(test.empty());

	test.clear();
	ASSERT_TRUE(test.empty());
}

TEST(BitArrayTests, ToString) {
	BitArray test(48, 88482574266222);
	ASSERT_EQ(test.to_string(), "Python");
}

TEST(BitArrayTests, BitSequence) {
	BitArray test(14, 1875);
	ASSERT_EQ(test.bit_seq(), "000111 01010011");
}

TEST(BitArrayTests, DoubleEqOperator) {
	BitArray test1a(8, 125);
	BitArray test1b(9, 125);
	ASSERT_FALSE(test1a == test1b);

	BitArray test2a(8, 125);
	BitArray test2b(8, 126);
	ASSERT_FALSE(test2a == test2b);

	BitArray test3a(8, 125);
	BitArray test3b(8, 125);
	ASSERT_TRUE(test3a == test3b);
}

TEST(BitArrayTests, NotEqOperator) {
	BitArray test1a(8, 125);
	BitArray test1b(9, 125);
	ASSERT_TRUE(test1a != test1b);

	BitArray test2a(8, 125);
	BitArray test2b(8, 126);
	ASSERT_TRUE(test2a != test2b);

	BitArray test3a(8, 125);
	BitArray test3b(8, 125);
	ASSERT_FALSE(test3a != test3b);
}

TEST(BitArrayTests, ConOperator) {
	BitArray test1(8, 125);
	BitArray test2(9, 125);
	ASSERT_THROW(test1 & test2, std::range_error);

	BitArray test3(8, 77);
	ASSERT_EQ((test1 & test3).bit_seq(), "01001101");
}

TEST(BitArrayTests, DisOperator) {
	BitArray test1(8, 79);
	BitArray test2(9, 125);
	ASSERT_THROW(test1 | test2, std::range_error);

	BitArray test3(8, 77);
	ASSERT_EQ((test1 | test3).bit_seq(), "01001111");
}

TEST(BitArrayTests, XOROperator) {
	BitArray test1(8, 79);
	BitArray test2(9, 125);
	ASSERT_THROW(test1 ^ test2, std::range_error);

	BitArray test3(8, 89);
	ASSERT_EQ((test1 ^ test3).bit_seq(), "00010110");
}

TEST(IteratorTests, AccessAndEqOperators) {
	BitArray test(12, 1875);
	for (int i = 0; i < test.size(); i++)
		if (i % 2 == 0)
			test[i] = true;
		else
			test[i] = false;
	ASSERT_EQ(test.bit_seq(), "0101 01010101");
}

TEST(IteratorTests, Auto) {
	BitArray test(12, 4095);
	for (auto& i : test)
		ASSERT_TRUE(i);

	test.reset();
	for (auto& i : test)
		ASSERT_FALSE(i);
}