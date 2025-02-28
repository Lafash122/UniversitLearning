cmake_minimum_required (VERSION 3.14)
project ("gay_life")

set(CMAKE_CXX_STANDARD 14)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

include(FetchContent)
FetchContent_Declare(
	googletest
	URL https://github.com/google/googletest/archive/03597a01ee50ed33e9dfd640b249b4be3799d395.zip
)

set(gtest_force_shared_crt ON CACHE BOOL "" FORCE)
FetchContent_MakeAvailable(googletest)

add_library(libs game.h game.cpp inhandler.h inhandler.cpp universe.h universe.cpp)

add_executable (game main.cpp)

target_link_libraries(game PRIVATE libs)

enable_testing()

add_executable(tests game.cpp inhandler.cpp tests.cpp universe.cpp)

target_link_libraries(tests GTest::gtest_main)

include(GoogleTest)
gtest_discover_tests(tests)
