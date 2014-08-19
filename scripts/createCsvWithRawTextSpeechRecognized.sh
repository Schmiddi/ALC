#!/bin/bash

if [ -z "$1" -o -z "$2" -o -z "$3" ]
then
    echo "Usage: $0 <HYP-FILE> <CLASSES-DIRECTORY> <OUTPUT-CSV>"
else

COUNT=0
SUCC_COUNT=0

# Delete old csv file
rm -f $3

# Add column names as first row
echo "file,text,class,conf_score" > $3

# RegEx matching text, id, confidence score
re1="([^\(]*)"
re2="([A-Z|a-z|0-9|\_|\.]{1,})"
re3="([-|0-9|\.]{1,})"


exec<$1
while read line
do

    COUNT=`expr $COUNT + 1`
    
    # Get text
    if [[ $line =~ $re1 ]]
    then
        TEXT=${BASH_REMATCH[1]}
        echo "~~ Parsing entry #$COUNT ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
        echo "Text: $TEXT"
    else
        echo "No text match in line $COUNT"
        echo "Aborting"
        break 
    fi

    # Get ID
    text_l=`expr ${#TEXT} + 1`
    line_l=`expr ${#line} - $text_l`
    text_c=${line:text_l:line_l}
    if [[ $text_c =~ $re2 ]]
    then
        ID=${BASH_REMATCH[1]}
        echo "Id: $ID"
    else
        echo "No id match in line $COUNT"
        echo "Aborting"
        break 
    fi
   
    # Get confidence score
    id_l=`expr ${#ID} + 1`
    text_l=`expr ${#text_c} - $id_l`
    text_c=${text_c:id_l:text_l}
    if [[ $text_c =~ $re3 ]]
    then
        CONF_SCORE=${BASH_REMATCH[1]}
        echo "Conf score: $CONF_SCORE"
    else
        echo "No conf score match in line $COUNT"
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
        echo "Class: ALC"
    else
        if [ -f $search_file_nonalc ]
        then
            # found in nonalc dir
            CLASS=nonalc
            echo "Class: NONALC"
        else
            echo "Error in line $COUNT of $1."
            echo "$ID was not found in any class directory"
            echo "Aborting"
            break 
        fi
    fi

    # Add parsed data to CSV
    echo "Adding data to csv ..."
    CSV_LINE=${ID},${TEXT},${CLASS},${CONF_SCORE}
    echo ${CSV_LINE} >> $3
    SUCC_COUNT=`expr $SUCC_COUNT + 1`

done

if [ $SUCC_COUNT -gt 0 ]
then
 echo "FINISHED: Added $SUCC_COUNT files to $3"
fi

fi
