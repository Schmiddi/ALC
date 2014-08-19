#!/bin/bash

if [ -z "$1" -o -z "$2" ]
then
    echo "Usage: $0 <WAV-INPUT-DIRECTORY> <WAV-OUTPUT-DIRECTORY>"
    echo "Takes all files in the input dir, converts them to 1-channel-audios and saves them to the output dir."
else
BIN=/usr/bin/sox

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
  echo "Processing $c files in directory $d"

  for f in $FILES
  do
    BF=`basename $f`
    OUTPUT_FILE=${2%/}/$BF
    echo "Processing $f ..."
    $BIN $f -c 1 $OUTPUT_FILE
    
  done
 fi

done

if [ $COUNT -gt 0 ]
then
 echo "FINISHED: Converted $COUNT files from $1 to $2"
fi

fi
