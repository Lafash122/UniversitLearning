cmake_minimum_required (VERSION 3.14)
project ("temps")

set(CMAKE_CXX_STANDARD 14)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

include(FetchContent)
FetchContent_Declare(
  googletest
  URL https://github.com/google/googletest/archive/03597a01ee50ed33e9dfd640b249b4be3799d395.zip
)

set(gtest_force_shared_crt ON CACHE BOOL "" FORCE)
FetchContent_MakeAvailable(googletest)

add_library (libs lib.cpp csvpars.h printtup.h)

add_executable (temps main.cpp)

target_link_libraries(temps PRIVATE libs)

enable_testing()

add_executable(tests lib.cpp tests.cpp)

target_link_libraries(tests GTest::gtest_main)

include(GoogleTest)
gtest_discover_tests(tests)