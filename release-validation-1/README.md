## GEMC
* On Fedora or similar Linux versions, start Docker and run the GEMC image:
```
sudo systemctl start docker
sudo docker run -it -v $PWD/:/jlab/workdir/shared --rm jeffersonlab/clas12tags:4a.2.4 tcsh
```
* From the docker container, run GEMC:
```
gemc clas12.gcard -INPUT_GEN_FILE="LUND, shared/11gev_sidis_500.dat" -USE_GUI=0 -N=500 -PRINT_EVENT=20 -RUNNO=11 -OUTPUT="evio, shared/out.evio"
```
* Tip: divide up the lund file and run several instances of GEMC:
```
awk -v min=0 -v max=250 '{ if(NF == 10) i++; if(i > min && i <= max) print $0 }' 11gev_sidis_500.dat > 11gev_sidis_250a.dat
awk -v min=250 -v max=500 '{ if(NF == 10) i++; if(i > min && i <= max) print $0 }' 11gev_sidis_500.dat > 11gev_sidis_250b.dat
```

## CLARA and COATJAVA
* Get CLARA (comes with COATJAVA):
```
wget --no-check-certificate https://claraweb.jlab.org/clara/_downloads/install-claracre-clas.sh
chmod +x install-claracre-clas.sh
setenv CLARA_HOME $PWD/myclara/
setenv COATJAVA $CLARA_HOME/plugins/clas12/
./install-claracre-clas.sh -v 5b.6.1
```
* Convert evio to hipo
```
$COATJAVA/bin/evio2hipo -r 11 -t -1.0 -s -1.0 -o out.hipo out.evio
```

## Reconstruction
* Run the following commands to set up and run the reconstruction with CLARA (modify parameters as needed):
```
echo "set inputDir $PWD/" > cook.clara
echo "set outputDir $PWD/" >> cook.clara
echo "set threads 4" >> cook.clara
echo "set javaMemory 2" >> cook.clara
echo "set session s_cook" >> cook.clara
echo "set description d_cook" >> cook.clara
ls out.hipo > files.list
echo "set fileList $PWD/files.list" >> cook.clara
echo "run local" >> cook.clara
echo "exit" >> cook.clara
$CLARA_HOME/bin/clara-shell cook.clara
```

