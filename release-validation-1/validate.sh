#!/usr/bin/env bash
mkdir -p validate
python sim.py -o validate

echo "evio2hipo"
cd validate
evio2hipo -r 11 -t -1.0 -s -1.0 -o out.hipo *.evio

echo "out.hipo" > files.list

echo "set inputDir $PWD" > cook.clara
echo "set outputDir $PWD" >> cook.clara
echo "set threads $(nproc)" >> cook.clara
echo "set javaMemory 4" >> cook.clara
echo "set fileList $PWD/files.list" >> cook.clara
echo "run local" >> cook.clara
echo "exit" >> cook.clara
echo "CLARA"
../run-clara-validate cook.clara

cd ..
cp validate/out_out.hipo .
mvn install && mvn exec:java -Dexec.mainClass="org.jlab.c12val.ParticleCounter"
