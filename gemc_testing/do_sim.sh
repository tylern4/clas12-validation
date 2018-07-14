#!/bin/bash
source /jlab/2.2/ce/jlab.sh

/jlab/clas12Tags/4a.2.4/source/gemc clas12.gcard -USE_GUI=0 -RUNNO=11 -N=1000 -OUTPUT="evio, out.evio"
