cmake_minimum_required (VERSION 3.8)
project (lab0b)

set(CMAKE_CXX_STANDARD 17)

add_library(lab0b
		filereader.h
		filereader.cpp
		filewritter.h
		filewritter.cpp)

add_executable (Main main.cpp)

target_link_libraries(Main PRIVATE lab0b)
