#!/bin/bash

if [ -z "$1" -o -z "$2" ]
then
    echo "Usage: $0 <CLASSES-DIRECTORY> <ARFF-OUTPUT-DIRECTORY>"
else

CONF=/home/alc/workspace/ALC/test/conf/myemo_large.conf
BIN=/home/alc/tools/opensmile-2.0-rc1/opensmile/SMILExtract

COUNT=0

# Get all sub directories to iterate through
DIRS=`find $1 -type d`

for d in $DIRS
do

 # Get all wav files in current sub directory
 FILES=${d%/}/*.wav
 c=`find ${d%/}/*.wav 2> /dev/null | wc -l`
 COUNT=`expr $COUNT + $c`
 if [ $c -gt 0 ]
 then
  echo "Processing $c files in sub directory $d"

  for f in $FILES
  do
   
   ARFF=${f:0:-4}.arff
   OUTP=`basename $ARFF`
   echo "Processing $f ..."
   $BIN -C $CONF -noconsoleoutput -I $f -O ${2%/}/$OUTP

  done

 fi

done

if [ $COUNT -gt 0 ]
then
 echo "FINISHED: Added $COUNT files to $2"
fi

fi
