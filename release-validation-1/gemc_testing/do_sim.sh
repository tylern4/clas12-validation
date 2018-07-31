#!/bin/bash
source /jlab/2.2/ce/jlab.sh 2> /dev/null

/jlab/clas12Tags/4a.2.4/source/gemc clas12.gcard -USE_GUI=0 $@ 

