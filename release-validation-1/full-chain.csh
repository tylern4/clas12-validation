#!/bin/csh -f

./cleanup.csh

setenv gemcversion 4a.2.4
setenv coatversion 5b.6.1

setenv lundfile 11gev_sidis_10000.dat
setenv nevents 10000

setenv ngemccores 2 # might not be safe to go above 2-3
setenv nclaracores `getconf _NPROCESSORS_ONLN`


### gemc/docker ###
setenv eventsPerCore `echo "$nevents / $ngemccores" | bc`
setenv ngemccoresMinus1 `echo "$ngemccores - 1" | bc`

foreach N (`seq 0 $ngemccoresMinus1`)

  setenv min `echo "$N * $eventsPerCore" | bc` 
  setenv max `echo "($N + 1) * $eventsPerCore" | bc`

  echo "" > temp.txt # start with empty file
  awk -v min=$min -v max=$max '{ if(NF == 10) i++; if(i > min && i <= max) print $0 }' $lundfile >> temp.txt
  tail -n +2 temp.txt > trimmed_lund_$N.dat # remove first blank line and give real filename
  rm temp.txt

  docker run -it --detach --name gemc_$N -v $PWD/:/jlab/workdir/shared --rm jeffersonlab/clas12tags:$gemcversion tcsh -c "shared/gemc-docker.csh $N $eventsPerCore"

  echo $N

end 

# wait for docker containers to finish before moving on:
setenv loopcount 0
while(`docker ps | wc -l` > 1)
  setenv loopcount `echo "$loopcount + 1" | bc`
  sleep 4
  if($loopcount % 10 == 0) then
    echo "gemc/docker still running..."
  endif
end
sleep 1
### end gemc/docker ###


### coat/clara ###
wget --no-check-certificate https://claraweb.jlab.org/clara/_downloads/install-claracre-clas.sh
chmod +x install-claracre-clas.sh
setenv CLARA_HOME $PWD/myclara/
setenv COATJAVA $CLARA_HOME/plugins/clas12/
./install-claracre-clas.sh -v $coatversion

foreach N (`seq 0 $ngemccoresMinus1`)

  $COATJAVA/bin/evio2hipo -r 11 -t -1.0 -s -1.0 -o gemcout_$N.hipo gemcout_$N.evio

end

$COATJAVA/bin/hipo-utils -merge -o gemcout.hipo gemcout_*.hipo
rm gemcout_*.evio gemcout_*hipo trimmed_lund_*.dat

echo "set inputDir $PWD/" > cook.clara
echo "set outputDir $PWD/" >> cook.clara
echo "set threads $nclaracores" >> cook.clara
echo "set javaMemory 2" >> cook.clara
echo "set session s_cook" >> cook.clara
echo "set description d_cook" >> cook.clara
ls gemcout.hipo > files.list
echo "set fileList $PWD/files.list" >> cook.clara
echo "run local" >> cook.clara
echo "exit" >> cook.clara
$CLARA_HOME/bin/clara-shell cook.clara
### end coat/clara ###


### analysis ###
javac -cp "$COATJAVA/lib/services/*:$COATJAVA/lib/clas/*:$COATJAVA/lib/utils/*" src/main/java/org/jlab/c12val/ParticleCounter.java
java -Xmx1536m -Xms1024m -cp "$COATJAVA/lib/services/*:$COATJAVA/lib/clas/*:$COATJAVA/lib/utils/*:src/main/java/" org.jlab.c12val.ParticleCounter

if(!(-e trackingPics)) then
  mkdir trackingPics
endif
javac -cp "$COATJAVA/lib/services/*:$COATJAVA/lib/clas/*:$COATJAVA/lib/utils/*:lib/*" src/main/java/org/jlab/c12val/TrackingTest.java
java -Xmx1536m -Xms1024m -DCLAS12DIR="$COATJAVA" -DINPUTFILE="gemcout" -DRESULTS="trackingPics" -cp "$COATJAVA/lib/services/*:$COATJAVA/lib/clas/*:$COATJAVA/lib/utils/*:lib/*:src/main/java/" org.jlab.c12val.TrackingTest
### end analysis ###

