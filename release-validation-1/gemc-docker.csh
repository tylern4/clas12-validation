#!/bin/tcsh

setenv filenumber $1
setenv nevents $2

gemc clas12.gcard -INPUT_GEN_FILE="LUND, shared/trimmed_lund_$filenumber.dat" -USE_GUI=0 -N=$nevents -PRINT_EVENT=20 -RUNNO=11 -OUTPUT="evio, shared/gemcout_$filenumber.evio"
