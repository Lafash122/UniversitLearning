#ifndef PRINT_TUPLE
#define PRINT_TUPLE

#include <iostream>
#include <tuple>

template <std::size_t Size, typename... Args>
void print_Tuple(std::ostream& os, const std::tuple<Args...>& tuple) {
    if constexpr (Size < sizeof...(Args)) {
        if (Size > 0)
            os << "; ";

        os << std::get<Size>(tuple);
        print_Tuple<Size + 1>(os, tuple);
    }

    return;
}

template<typename... Args>
std::ostream& operator<<(std::ostream& os, const std::tuple<Args...>& tuple) {
    os << std::boolalpha;
    print_Tuple<0>(os, tuple);
    os << std::noboolalpha;

    return os;
}

#endif // PRINT_TUPLE