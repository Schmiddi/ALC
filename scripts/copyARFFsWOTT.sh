#!/bin/bash

# Copies all arrf files that belong to the WOTT folder.
if [ -z "$1" -o -z "$2" -o -z "$3" ]
then
	echo "Usage: $0 <SOURCE-ARFF-DIRECTORY> <DESTINATION-ARFF-DIRECTORY> <REFERENCE-ARFF-DIRECTORY>"
else

FILES=${1%/}/*.arff
DEST=${2%/}
REF=${3%/}

COUNT=0

for f in $FILES
do	
	FILENAME=`basename $f`
	SEARCHFILE=$REF/$FILENAME
    if [ -f $SEARCHFILE ]
    then
        COUNT=`expr $COUNT + 1`
        echo "Copying $COUNT. file: $f"
        cp $f $DEST/$FILENAME
        #echo "INFO: cp $f $DEST/$FILENAME"
    fi
    	
done

echo "Finished! Copied $COUNT files to $DEST"
fi
