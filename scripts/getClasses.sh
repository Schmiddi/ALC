#!/bin/bash

if [ -z "$1" -o -z "$2" ]
then
    echo "Usage: $0 <CORPUS-ROOT-BLOCK-DIR> <OUTPUT-CSV>"
else

COUNT=0
SUCC_COUNT=0

COUNTA=0
COUNTN=0
COUNTI=0

# Delete old csv file
rm -f $2

## Add column names as first row
#echo "file,class" > $2

reID="\w+ (\w+)"
reAN="\w+ \w+ \w+ \w+ \w+ \w+ \w+ (a|na|cna)"
reBAC="[^\s]+ [^\s]+ [^\s]+ [^\s]+ [^\s]+ [^\s]+ [^\s]+ [^\s]+ [^\s]+ [^\s]+ [^\s]+ [^\s]+ [0-9]+\.[0-9]+ ([0-9]+\.[0-9]+)"

# iterate through all sub directories
FILES=`find $1 -type f -name '*.hlb'`

for sample in $FILES
do
    COUNT=`expr $COUNT + 1`
    echo "Processing #$COUNT"

    #get 4th line of sample
    echo "File to read from: $sample"
    line=`sed -n '4p' $sample`
    echo "Read line: $line"

    # get ID
    if [[ $line =~ $reID ]]
    then
        ID=${BASH_REMATCH[1]}
        echo "ID: $ID"
    else
        echo "ID not found for file $sample"
        echo "Exiting with error"
        exit 1
    fi

    # get A/NA
    if [[ $line =~ $reAN ]]
    then
        AN=${BASH_REMATCH[1]}
        echo "AN: $AN"
    else
        echo "A/N not found for file $sample"
        echo "Exiting with error"
        exit 2
    fi

    # get BAC
    if [[ $line =~ $reBAC ]]
    then
         BAC=${BASH_REMATCH[1]}
         echo "BAC: $BAC"
    else
         echo "BAC not found for file $sample"
         echo "Exiting with error"
         exit 3
    fi

    # get right class
    if [ 1 -eq `echo "0.0005 < ${BAC}" | bc` ]
    then
        CLASS=alc
        COUNTA=`expr $COUNTA + 1`
        echo "CLASS: $CLASS"
    else
        var2=$(awk 'BEGIN{ print "'$BAC'"<"'0.0005'" }')
        if [ 1 -eq `echo "0.0 < ${BAC}" | bc` ]
        then
            CLASS=nonalc
            COUNTI=`expr $COUNTI + 1`
            echo "CLASS: $CLASS"
        else
            CLASS=nonalc
            COUNTN=`expr $COUNTN + 1`
            echo "CLASS: $CLASS"
        fi

    fi


    # Add parsed data to CSV
    CSV_LINE=${ID},${CLASS}
    echo ${CSV_LINE} >> $2
    SUCC_COUNT=`expr $SUCC_COUNT + 1`

done

if [ $SUCC_COUNT -gt 0 ]
then
 echo "FINISHED: Added $SUCC_COUNT files to $2"
 echo "Found ALC instances:    $COUNTA"
 echo "Found NONALC instances: $COUNTN"
 echo "Found INTOXICATED instances: $COUNTI"
fi

fi
