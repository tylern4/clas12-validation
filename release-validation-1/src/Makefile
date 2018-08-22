ROOTLIBS	= $(shell root-config --libs)
CXX = g++
CXXFLAGS = -O3 -march=native -fPIC -w -g $(shell root-config --cflags)
TARGET = validate

MAINS = histogram.cpp main.cpp
MAIN = $(MAINS:%.cpp=%.o)

.PHONY: all clean

all:	$(TARGET)

$(MAIN): %.o : %.cpp
		$(CXX) $(CXXFLAGS) -c $< -o $@

$(TARGET): $(MAIN)
	$(CXX) $(MAIN) $(CXXFLAGS) $(ROOTLIBS) -o $(TARGET)


clean:
	-rm -f $(TARGET) $(MAIN)
