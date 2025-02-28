#ifndef CSV_PARS
#define CSV_PARS

#include <iostream>
#include <fstream>
#include <sstream>
#include <stdexcept>
#include <string>
#include <tuple>


template <class... Args>
class CSVParser {
private:
    std::istream& input;
    char splitter;
    char shield;
    int curr_line = 0;

public:
    CSVParser(std::istream& file, int skip = 0, char spl = ';', char shld = '\"') : input(file), splitter(spl), shield(shld) {
        for (int i = 0; i < skip; i++) {
            std::string ignore;
            std::getline(file, ignore);
            ++curr_line;
        }
    }

    class Iterator {
    private:
        std::istream* input;
        char splitter;
        char shield;
        std::string line;
        int line_num;
        std::tuple<Args...> line_tuple;

        void parse_line() {
            std::istringstream stream(line);
            line_tuple = read<Args...>(stream);
        }

        template <typename T, typename... Ts>
        std::tuple<T, Ts...> read(std::istringstream& stream) const {
            T val;
            parse(stream, val);
            if constexpr (sizeof...(Ts) == 0)
                return std::make_tuple(val);
            else
                return std::tuple_cat(std::make_tuple(val), read<Ts...>(stream));
        }

        void parse(std::istringstream& stream, std::string& val) const {
            char let;
            val.clear();
            bool inside = false;

            while (stream.get(let)) {
                if (inside)
                    if (let == shield)
                        inside = false;
                    else
                        val += let;
                else
                    if (let == shield)
                        inside = true;
                    else if ((let == splitter) || (let == '\n'))
                        break;
                    else
                        val += let;

                if (stream.peek() == EOF)
                    break;
            }

            if (val.empty())
                throw std::runtime_error("Empty field in line " + std::to_string(line_num));
        }

        template<typename Ty>
        void parse(std::istringstream& stream, Ty& val) const {
            std::string tmp;
            parse(stream, tmp);
            std::istringstream(tmp) >> val;
            tmp.clear();
            if (stream.fail())
                throw std::runtime_error("Failed to convert value to type in line " + std::to_string(line_num));
        }

    public:
        Iterator(std::istream* file, int num, char spl, char shld) : input(file), line_num(num), splitter(spl), shield(shld) {
            ++(*this);
        }

        Iterator() : input(nullptr) {}

        bool operator!=(const Iterator& i) const {
            return (input != i.input);
        }

        Iterator& operator++() {
            if (input && std::getline(*input, line)) {
                ++line_num;
                try {
                    parse_line();
                }
                catch (const std::exception& err) {
                    throw std::runtime_error("Error in line " + std::to_string(line_num) + ": " + err.what());
                }
            }
            else
                input = nullptr;

            return *this;
        }

        std::tuple<Args...> operator*() const {
            return line_tuple;
        }
    };

    Iterator begin() {
        return Iterator(&input, curr_line, splitter, shield);
    }

    Iterator end() {
        return Iterator();
    }
};

#endif // CSV_PARS
