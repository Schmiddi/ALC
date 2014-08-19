#!/bin/bash

# Copies all wav files to the correct target class directory.
if [ -z "$1" -o -z "$2" -o -z "$3" ]
then
	echo "Usage: $0 <SOURCE-WAV-TOP-DIRECTORY> <DESTINATION-WAV-TOP-DIRECTORY> <MAPPING-CSV>"
else

FILES=`find $1 -type f -name '*.wav'`
DEST=${2%/}
REF=${3%/}

COUNT=0
COUNTA=0
COUNTN=0

# regex
reCLASS="[^,]+\,\s*(\w+)"

for f in $FILES
do	
	# Get file ID
    ID=`basename $f`
    ID=${ID:0:-4}
    echo "ID: $ID"

    # Look up class for file ID in mapping csv
    LOOKUP=`grep $ID $REF`
    if [ -n "$LOOKUP" ]
    then
        
        # Found id --> parse class
        if [[ $LOOKUP =~ $reCLASS ]]
        then
            CLASS=${BASH_REMATCH[1]}
            echo "Class is: $CLASS"
        else
            echo "Class not found in line $LOOKUP"
            echo "Exiting with error"
            exit 2
        fi

    else
        echo "ID $ID not found in $REF"
        echo "Exiting with error"
        exit 1
    fi

        COUNT=`expr $COUNT + 1`
        echo "Copying $COUNT. file: $f to $DEST/$CLASS"
        
        # Copy
        cp -u $f $DEST/$CLASS/
        
        if [ "$CLASS" == "alc" ]
        then
            COUNTA=`expr $COUNTA + 1`
        fi

        if [ "$CLASS" == "nonalc" ]
        then
            COUNTN=`expr $COUNTN + 1`
        fi

done

echo "Finished! Copied $COUNT files to $DEST"
echo "Copies for ALC:  $COUNTA"
echo "Copies for NALC: $COUNTN"

fi
