1. Decode and cook raw files (see https://clasweb.jlab.org/wiki/index.php/CLAS12_KPP)

2. $coat/bin/run-groovy skim2trackEvents.groovy cooked1.hipo cooked2.hipo ... <br>
  - save output list to a txt file

3. javac -cp "$COAT/lib/services/*:$COAT/lib/clas/*:$COAT/lib/utils/*" SkimRaw2TrackEvents.java ; java -cp "$COAT/lib/services/*:$COAT/lib/clas/*:$COAT/lib/utils/*:." SkimRaw2TrackEvents <br>
  * make sure CLAS12DIR is pointing to coatjava

* requires http://central.maven.org/maven2/org/apache/commons/commons-lang3/3.4/commons-lang3-3.4.jar <br>
  * copy to $coat/lib/services/

