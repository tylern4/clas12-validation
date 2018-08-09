#!/usr/bin/env bash
mkdir -p validate
python sim.py -o $PWD/validate 2> /dev/null 1> /dev/null

evio2hipo out.hipo validate/*.evio

echo "out.hipo" validate/files.list

echo "set inputDir $PWD/validate" > cook.clara
echo "set outputDir $PWD" >> cook.clara
echo "set threads $(nproc)" >> cook.clara
echo "set javaMemory 4" >> cook.clara
echo "set fileList $PWD/validate/files.list" >> cook.clara
echo "run local" >> cook.clara
echo "exit" >> cook.clara
#clara-shell cook.clara


#mvn install && mvn exec:java -Dexec.mainClass="org.jlab.c12val.ParticleCounter"
