/************************************************************************/
/*  Created by Nick Tyler*/
/*	University Of South Carolina*/
/************************************************************************/

// Only My Includes. All others in main.h
#include "colors.hpp"
#include "main.hpp"

using namespace std;

int main(int argc, char **argv) {
  if (argc == 2) {
    std::string infilename = argv[1];
    datahandeler(infilename, "out.root");
  } else if (argc == 3) {
    std::string infilename = argv[1];
    std::string outfilename = argv[2];
    datahandeler(infilename, outfilename);
    std::cerr << RESET << std::endl;
  } else {
    std::cerr << RED << "Error: \n";
    std::cerr << BOLDRED << "\tNeed input file and output file\n";
    std::cerr << RESET << "Usage:\n\t";
    std::cerr << BOLDWHITE << argv[0] << " infile.root outfile.root\n\n";
    std::cerr << RESET << std::endl;
  }

  return 0;
}
