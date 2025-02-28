#ifndef BIT_ARRAY_H
#define BIT_ARRAY_H

#include <iostream>
#include <vector>
#include <stdexcept>

class BitArray {
private:
	std::vector<char> bit_arr;
	int num_bits;

public:
	class Wrapper {
	private:
		int bit_pos;
		BitArray* array;

	public:
		Wrapper(int bit_pos, BitArray* array);
		Wrapper& operator=(bool value);
		operator bool() const;
	};
	Wrapper operator[](int index);

	class Iterator {
	private:
		int bit_pos;
		BitArray* array;

	public:
		Iterator(int bit_pos, BitArray* array);
		~Iterator();

		Wrapper operator*() const;
		Iterator& operator++();
		Iterator& operator--();
		bool operator==(const Iterator& w) const;
		bool operator!=(const Iterator& w) const;
	};
	Iterator begin();
	Iterator end();

	BitArray();
	~BitArray();
	explicit BitArray(int num_bits, unsigned long long value = 0);
	BitArray(const BitArray& b);

	void swap(BitArray& b);
	BitArray& operator=(const BitArray& b);

	void resize(int num_bits, bool value = false);
	void clear();
	void push_back(bool bit);

	BitArray& operator&=(const BitArray& b);
	BitArray& operator|=(const BitArray& b);
	BitArray& operator^=(const BitArray& b);

	BitArray& operator<<=(int bits);
	BitArray& operator>>=(int bits);
	BitArray operator<<(int bits) const;
	BitArray operator>>(int bits) const;

	BitArray& set(int index, bool value = true);
	BitArray& set();

	BitArray& reset(int index);
	BitArray& reset();

	bool any() const;
	bool none() const;

	BitArray operator~() const;

	int count() const;

	bool operator[](int index) const;

	int size() const;
	bool empty() const;

	std::string to_string() const;
	std::string bit_seq() const;
};

bool operator==(const BitArray& a, const BitArray& b);
bool operator!=(const BitArray& a, const BitArray& b);

BitArray operator&(const BitArray& a, const BitArray& b);
BitArray operator|(const BitArray& a, const BitArray& b);
BitArray operator^(const BitArray& a, const BitArray& b);

#endif //BIT_ARRAY_H