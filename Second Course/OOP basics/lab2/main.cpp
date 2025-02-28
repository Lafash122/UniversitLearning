#include <iostream>
#include "bitarray.h"

int main() {
	int bits;
	unsigned long long value3 = 255, value2;
	std::cin >> bits >> value2 /*>> value3*/;

	BitArray array1;
	BitArray array2(bits, value2);
	BitArray array3(bits, value3);

	std::cout << "1: " << array1.empty() << "\t2: " << array2.empty() << "\t3: " << array3.empty() << std::endl;
	std::cout << "1: " << array1.size() << "\t2: " << array2.size() << "\t3: " << array3.size() << std::endl << std::endl;

	//array1.swap(array3);
	//std::cout << "1: " << array1.empty() << "\t2: " << array2.empty() << "\t3: " << array3.empty() << std::endl;
	//std::cout << "1: " << array1.size() << "\t2: " << array2.size() << "\t3: " << array3.size() << std::endl << std::endl;

	//array1.resize(2, true);
	//std::cout << "1: " << array1.empty() << "\t2: " << array2.empty() << "\t3: " << array3.empty() << std::endl;
	//std::cout << "1: " << array1.size() << "\t2: " << array2.size() << "\t3: " << array3.size() << std::endl << std::endl;

	//array2.clear();
	//std::cout << "1: " << array1.empty() << "\t2: " << array2.empty() << "\t3: " << array3.empty() << std::endl;
	//std::cout << "1: " << array1.size() << "\t2: " << array2.size() << "\t3: " << array3.size() << std::endl << std::endl;

	//array2 = array1;
	//std::cout << "1: " << array1.empty() << "\t2: " << array2.empty() << "\t3: " << array3.empty() << std::endl;
	//std::cout << "1: " << array1.size() << "\t2: " << array2.size() << "\t3: " << array3.size() << std::endl << std::endl;

	//array1.push_back(0);
	//std::cout << "1: " << array1.empty() << "\t2: " << array2.empty() << "\t3: " << array3.empty() << std::endl;
	//std::cout << "1: " << array1.size() << "\t2: " << array2.size() << "\t3: " << array3.size() << std::endl << std::endl;

	//std::cout << "1: " << array1.any() << "\t2: " << array2.any() << std::endl;
	//
	//array1.reset();
	//std::cout << "1: " << array1.any() << " - " << array1.none() << "\t2: " << array2.any() << std::endl;

	//array1.set();
	//array2.reset();
	//std::cout << "1: " << array1.any() << " - " << array1.none() << "\t2: " << array2.none() << std::endl;

	//array2.set(1);
	//std::cout << "1: " << array1.any() << "\t2: " << array2.any() << " - " << array2.none() << std::endl;

	//array2.reset(1);
	//std::cout << "1: " << array1.any() << "\t2: " << array2.any() << " - " << array2.none() << std::endl;

	//array2.set(0, true);
	//std::cout << "1: " << array1.any() << "\t2: " << array2.any() << " - " << array2.none() << std::endl << std::endl;

	//array1.reset();
	//for (int i = 0; i < array1.size(); i++)
	//	std::cout << array1[i];

	//std::cout << std::endl;
	//array2 = ~array2;

	/*
	std::cout << "array2: ";
	for (int i = array2.size() - 1; i >= 0; i--) {
		std::cout << array2[i];
		if ((i > 0) && (i % 8 == 0))
			std::cout << " ";
	}

	std::cout << std::endl << "array3: ";
	for (int i = array3.size() - 1; i >= 0; i--) {
		std::cout << array3[i];
		if ((i > 0) && (i % 8 == 0))
			std::cout << " ";
	}

	std::cout << std::endl << "bit id: ";
	for (int i = array2.size() - 1; i >= 0; i--) {
		std::cout << i % 10;
		if ((i > 0) && (i % 8 == 0))
			std::cout << " ";
	}

	std::cout << std::endl << "byte:   ";
	for (int i = array2.size() - 1; i >= 0; i--) {
		std::cout << i / 8;
		if ((i > 0) && (i % 8 == 0))
			std::cout << " ";
	}

	std::cout << std::endl << array2.count() << std::endl;
	std::cout << array2.to_string() << std::endl;
	std::cout << "bytes: " << (bits + 7) / 8 << std::endl << std::endl;
	*/

	//array2 ^= array3;

	//for (int i = array2.size() - 1; i >= 0; i--) {
	//	std::cout << array2[i];
	//	if ((i > 0) && (i % 8 == 0))
	//		std::cout << " ";
	//}
	//std::cout << std::endl << array2.count() << std::endl;
	//std::cout << array2.to_string() << std::endl;

	/*
	std::cout << "array2: ";
	for (int i = 0; i < array2.size(); i++) {
		std::cout << array2[i];
		if ((i > 0) && (i % 8 == 7))
			std::cout << " ";
	}

	std::cout << std::endl << "bit id: ";
	for (int i = 0; i < array2.size(); i++) {
		std::cout << i % 10;
		if ((i > 0) && (i % 8 == 7))
			std::cout << " ";
	}

	std::cout << std::endl << "byte:   ";
	for (int i = 0; i < array2.size(); i++) {
		std::cout << i / 8;
		if ((i > 0) && (i % 8 == 7))
			std::cout << " ";
	}

	for (int i = 0; i < bits; i++) {
		array1 = array2 << i;
		std::cout << std::endl << "array1: ";
		for (int j = 0; j < array1.size(); j++) {
			std::cout << array1[j];
			if ((j > 0) && (j % 8 == 7))
				std::cout << " ";
		}
	}
	*/

	//std::cout << std::endl << "array3: ";
	//for (int i = array3.size() - 1; i >= 0; i--) {
	//	std::cout << array3[i];
	//	if ((i > 0) && (i % 8 == 0))
	//		std::cout << " ";
	//}

	//array1 = array2 ^ array3;
	//std::cout << std::endl << "array1: ";
	//for (int i = array1.size() - 1; i >= 0; i--) {
	//	std::cout << array1[i];
	//	if ((i > 0) && (i % 8 == 0))
	//		std::cout << " ";
	//}

	//std::cout << std::endl << (array1 == array2) << " - " << (array3 == array2) << std::endl;
	//std::cout << (array1 != array2) << " - " << (array3 != array2) << std::endl;+

	std::cout << "array2: " << array2.bit_seq() << std::endl;

	for (auto& i : array2) {
		array2[i] = true;
	}

	std::cout << "array2: " << array2.bit_seq() << std::endl;


	return 0;
}