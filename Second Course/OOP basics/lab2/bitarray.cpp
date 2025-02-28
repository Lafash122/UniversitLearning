#include "bitarray.h"

#define BYTE 8

BitArray::Wrapper::Wrapper(int bit_pos, BitArray* array) : bit_pos(bit_pos), array(array) {}

BitArray::Wrapper& BitArray::Wrapper::operator=(bool value) {
	(*array).set(bit_pos, value);

	return *this;
}

BitArray::Wrapper::operator bool() const {
	return (*array).bit_arr[bit_pos / BYTE] & (1 << (bit_pos % BYTE));
}

BitArray::Wrapper BitArray::operator[](int index) {
	if ((index >= (*this).size()) || (index < 0))
		throw std::invalid_argument("There is no such bit's position in the bit array");

	return Wrapper(index, this);
}

BitArray::Iterator::Iterator(int bit_pos, BitArray* array) : bit_pos(bit_pos), array(array) {}

BitArray::Iterator::~Iterator() = default;

BitArray::Wrapper BitArray::Iterator::operator*() const {
	if ((bit_pos >= (*array).size()) || (bit_pos < 0))
		throw std::invalid_argument("There is no such bit's position in the bit array");

	return BitArray::Wrapper(bit_pos, array);
}

BitArray::Iterator& BitArray::Iterator::operator++() {
	bit_pos++;
	return *this;
}

BitArray::Iterator& BitArray::Iterator::operator--() {
	bit_pos--;
	return *this;
}

bool BitArray::Iterator::operator==(const Iterator& i) const {
	return (bit_pos == i.bit_pos);
}

bool BitArray::Iterator::operator!=(const Iterator& i) const {
	return (bit_pos != i.bit_pos);
}

BitArray::Iterator BitArray::begin() {
	return Iterator(0, this);
}

BitArray::Iterator BitArray::end() {
	return Iterator((*this).size(), this);
}

BitArray::BitArray() : num_bits(0) {}
BitArray::~BitArray() = default;

BitArray::BitArray(int num_bits, unsigned long long value) : num_bits(num_bits) {
	int byte_size = (num_bits + 7) / BYTE;
	bit_arr.resize(byte_size, 0);

	unsigned long long remainder = value;
	for (int i = 0; (i < num_bits) && (i < sizeof(value) * BYTE); i++) {
		int bit_pos = i % BYTE;
		int byte = i / BYTE;
		int bit = remainder % 2;
		remainder /= 2;
		bit_arr[byte] |= (bit << bit_pos);
	}
	if (remainder)
		throw std::invalid_argument("There are not enough bits to write a value");
}

BitArray::BitArray(const BitArray& b) : bit_arr(b.bit_arr), num_bits(b.num_bits) {}

void BitArray::swap(BitArray& b) {
	std::swap(b.bit_arr, bit_arr);
	std::swap(b.num_bits, num_bits);
}

BitArray& BitArray::operator=(const BitArray& b) {
	if (this != &b) {
		num_bits = b.num_bits;
		bit_arr = b.bit_arr;
	}
	return *this;
}

void BitArray::resize(int num_bits, bool value) {
	if (num_bits < 0)
		throw std::invalid_argument("The number of bits must be positive");

	int bytes = (num_bits + 7) / BYTE;
	int tmp = (*this).num_bits;
	bit_arr.resize(bytes, 0);
	(*this).num_bits = num_bits;

	if ((num_bits > tmp) && value)
		for (int i = tmp; i < num_bits; i++)
			(*this).set(i);
}

void BitArray::clear() {
	bit_arr.clear();
	num_bits = 0;
}

void BitArray::push_back(bool bit) {
	resize(num_bits + 1, bit);
	set(num_bits - 1, bit);
}

BitArray& BitArray::operator&=(const BitArray& b) {
	if (num_bits != b.num_bits)
		throw std::range_error("The sizes of the bit arrays must be the same");

	if (this != &b) {
		int bytes = (num_bits + 7) / BYTE;
		for (int byte = 0; byte < bytes; byte++)
			bit_arr[byte] &= b.bit_arr[byte];
	}

	return *this;
}

BitArray& BitArray::operator|=(const BitArray& b) {
	if (num_bits != b.num_bits)
		throw std::range_error("The sizes of the bit arrays must be the same");

	if (this != &b) {
		int bytes = (num_bits + 7) / BYTE;
		for (int byte = 0; byte < bytes; byte++)
			bit_arr[byte] |= b.bit_arr[byte];
	}

	return *this;
}

BitArray& BitArray::operator^=(const BitArray& b) {
	if (num_bits != b.num_bits)
		throw std::range_error("The sizes of the bit arrays must be the same");

	if (this != &b) {
		int bytes = (num_bits + 7) / BYTE;
		for (int byte = 0; byte < bytes; byte++)
			bit_arr[byte] ^= b.bit_arr[byte];
	}

	return *this;
}

BitArray& BitArray::operator<<=(int bits) {
	if (bits >= num_bits) {
		reset();
		return *this;
	}

	int bytes = (num_bits + 7) / BYTE;
	int f_byte = bits / BYTE;
	int f_bit = bits % BYTE;

	for (int i = bytes - 1; i >= 0; i--) {
		if ((i - f_byte) >= 0)
			bit_arr[i] = (static_cast<unsigned char>(bit_arr[i - f_byte]) << f_bit);
		else
			bit_arr[i] = 0;
		if ((i - f_byte - 1) >= 0)
			bit_arr[i] |= (static_cast<unsigned char>(bit_arr[i - f_byte - 1]) >> (BYTE - f_bit));
	}

	return *this;
}

BitArray& BitArray::operator>>=(int bits) {
	if (bits >= num_bits) {
		reset();
		return *this;
	}

	int bytes = (num_bits + 7) / BYTE;
	int f_byte = bits / BYTE;
	int f_bit = bits % BYTE;

	for (int i = 0; i < bytes; i++) {
		if ((i + f_byte) < bytes)
			bit_arr[i] = (static_cast<unsigned char>(bit_arr[i + f_byte]) >> f_bit);
		else
			bit_arr[i] = 0;
		if ((i + f_byte + 1) < bytes)
			bit_arr[i] |= (static_cast<unsigned char>(bit_arr[i + f_byte + 1]) << (BYTE - f_bit));
	}

	return *this;
}

BitArray BitArray::operator<<(int bits) const {
	BitArray tmp(*this);
	return (tmp <<= bits);
}

BitArray BitArray::operator>>(int bits) const {
	BitArray tmp(*this);
	return (tmp >>= bits);
}

BitArray& BitArray::set(int index, bool value) {
	if ((index < 0) || (index >= num_bits))
		throw std::invalid_argument("There is no such index in the bit array");

	int bit_pos = index % BYTE;
	int byte = index / BYTE;
	if (value)
		bit_arr[byte] |= (value << bit_pos);
	else
		bit_arr[byte] &= (~(1 << bit_pos));

	return *this;
}

BitArray& BitArray::set() {
	std::fill(bit_arr.begin(), bit_arr.end(), 255);
	return *this;
}

BitArray& BitArray::reset(int index) {
	return set(index, false);
}

BitArray& BitArray::reset() {
	std::fill(bit_arr.begin(), bit_arr.end(), 0);
	return *this;
}

bool BitArray::any() const {
	for (auto& byte : bit_arr)
		if (byte != 0)
			return true;

	return false;
}

bool BitArray::none() const {
	return !any();
}

BitArray BitArray::operator~() const {
	BitArray tmp(*this);

	for (auto& byte : tmp.bit_arr)
		byte = ~byte;
	return tmp;
}

int BitArray::count() const {
	int cnt = 0;
	for (int i = 0; i < num_bits; i++)
		if ((*this)[i])
			cnt++;

	return cnt;
}

bool BitArray::operator[](int index) const {
	if ((index < 0) || (index >= num_bits))
		throw std::invalid_argument("There is no such index in the bit array");

	int bit_pos = index % BYTE;
	int byte = index / BYTE;
	char tmp = bit_arr[byte];
	return (tmp & (1 << bit_pos));
}

int BitArray::size() const {
	return num_bits;
}

bool BitArray::empty() const {
	return (num_bits == 0);
}

std::string BitArray::to_string() const {
	std::string string;
	for (auto& byte : bit_arr)
		string = byte + string;

	return string;
}

std::string BitArray::bit_seq() const {
	std::string sequence;
	for (int i = num_bits - 1; i >= 0; i--) {
		if ((*this)[i])
			sequence += '1';
		else
			sequence += '0';
		if ((i > 0) && (i % 8 == 0))
			sequence += ' ';
	}


	return sequence;
}

bool operator==(const BitArray& a, const BitArray& b) {
	if (a.size() != b.size())
		return false;

	for (int i = 0; i < a.size(); i++)
		if (a[i] != b[i])
			return false;

	return true;
}

bool operator!=(const BitArray& a, const BitArray& b) {
	return !(operator==(a, b));
}

BitArray operator&(const BitArray& a, const BitArray& b) {
	BitArray tmp(a);
	return (tmp &= b);
}

BitArray operator|(const BitArray& a, const BitArray& b) {
	BitArray tmp(a);
	return (tmp |= b);
}

BitArray operator^(const BitArray& a, const BitArray& b) {
	BitArray tmp(a);
	return (tmp ^= b);
}