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
