cmake_minimum_required(VERSION 3.26)
project(lab0a)

set(CMAKE_CXX_STANDARD 17)

add_library(lab0a
        module1.cpp
        module1.h
        module2.cpp
        module2.h
        module3.cpp
        module3.h)

add_executable(Main main.cpp)

target_link_libraries(Main PRIVATE lab0a)