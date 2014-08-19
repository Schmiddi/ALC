#!/bin/bash

if [ -z "$1" -o -z "$2" -o -z "$3" ]
then
    echo "Usage: $0 <TBL-FILE> <CLASSES-DIRECTORY> <OUTPUT-CSV>"
else

COUNT=0
SUCC_COUNT=0

COUNTA=0
COUNTN=0

# Delete old csv file
rm -f $3

# Add column names as first row
echo "file,class" > $3

reID="[A-Z0-9]{1,}\/[A-Z0-9]{1,}\/([0-9a-z_]{1,})"
 
exec<$1
while read line
do
     
   COUNT=`expr $COUNT + 1`
      
   # Get ID
   if [[ $line =~ $reID ]]
   then
     ID=${BASH_REMATCH[1]}
     echo "~~ Parsing entry #$COUNT ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
     echo "ID: $ID"
   else
     echo "No ID match in line $COUNT"
     echo "Aborting"
     break
   fi

    # Get class
    search_file_alc=${2%/}/alc/$ID.wav
    search_file_nonalc=${2%/}/nonalc/$ID.wav
    if [ -f $search_file_alc ]
    then
        # found in alc dir
        CLASS=alc
        COUNTA=`expr $COUNTA + 1`
        echo "Class: ALC"
    else
        if [ -f $search_file_nonalc ]
        then
            # found in nonalc dir
            CLASS=nonalc
            COUNTN=`expr $COUNTN + 1`
            echo "Class: NONALC"
        else
            echo "Error in line $COUNT of $1."
            echo "$ID was not found in any class directory"
            echo "Aborting"
            break 
        fi
    fi

    # Add parsed data to CSV
    CSV_LINE=${ID},${CLASS}
    echo ${CSV_LINE} >> $3
    SUCC_COUNT=`expr $SUCC_COUNT + 1`

done

if [ $SUCC_COUNT -gt 0 ]
then
 echo "FINISHED: Added $SUCC_COUNT files to $3"
 echo "Found ALC instances:    $COUNTA"
 echo "Found NONALC instances: $COUNTN"
fi

fi
