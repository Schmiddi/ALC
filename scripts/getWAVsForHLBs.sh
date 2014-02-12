#!/bin/bash

# Copies all wav files that correspond to the hlb files of the specified directory into the hlb file directory.
if [ -z "$1" -o -z "$2" ]
then
	echo "Usage: $0 <HLB-DIRECTORY> <DESTINATION-DIRECTORY>"
else

FILES=${1%/}/*.hlb
DEST=${2%/}/

for f in $FILES
do	
	FILENAME=`basename $f`
	RESULT=`find /home/bas-alc/corpus/DATA/ -name ${FILENAME:0:-4}.wav`
	echo "Copying $RESULT to $DEST"
	
	if [ -n "$RESULT" ]
	then
		cp $RESULT $DEST
	fi

done

fi
