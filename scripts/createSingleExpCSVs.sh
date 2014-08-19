#!/bin/bash

if [ -z "$1" -o -z "$2" ]
then
    echo "Usage: $0 <SOURCE-OUTPUT-CSV> <EXP-OUTPUT-CSV-DIR>"
else


# regex
reID="([^\s|\,]+)"
reTEXT="[^,]\,+([^,]+)"
reEXPID="[0-9]{7}([0-9]{3})"
reCLASS="[^,]+\,[^,]+\,(\w+)"
reFirstLine="(file)"
rePARSEEXPID="0?0?([1-9][0-9]?)"

COUNT=1

# Get all sub directories to iterate through
DIRS=`find $2 -type d`

# Iterate through all EXP dirs
for d in $DIRS
do
 #delete CSV
 rm -f $d/output.csv

 # Create new empty CSV - Add column names as first row
 echo "Creating new CSV in $d"
 echo "file,text,class" > $d/output.csv

done


## Start iterating through output.csv ($1)

exec<$1
while read line
do

if [[ $line =~ $reFirstLine ]]
then
    echo "Skipping first line: $line"
else

 # Get ID from current line
 if [[ $line =~ $reID ]]
 then
     ID_S=${BASH_REMATCH[1]}

     # Get EXP ID
     if [[ $ID_S =~ $reEXPID ]]
     then
      EXPID=${BASH_REMATCH[1]}
      EXPID2=$EXPID
     else
      echo "EXP ID not found for line $line"
      exit 50
     fi

     echo "----------------------------------"
     echo "#$COUNT"
     echo "--------------"
     echo "ID:     $ID_S"
     echo "EXP ID: $EXPID"
 else
     echo "ID not found for line $line"
     echo "Exiting with error"
     exit 1
 fi

 # Get text from current line
 if [[ $line =~ $reTEXT ]]
     then
        TEXT=${BASH_REMATCH[1]}
        echo "Text: $TEXT"

	if [[ $TEXT == "alc" ]]
	then
	    echo "Error"
	    exit 99
	fi

     else
        echo "TEXT not found in line $line"
        echo "Exiting with error"
        exit 3
     fi


 # Get class from current line
 if [[ $line =~ $reCLASS ]]
     then
         CLASS=${BASH_REMATCH[1]}
         echo "Class: $CLASS"
     else
         echo "Correct class not found in line $line"
         echo "Exiting with error"
         exit 4
     fi

     # Get correct EXPID, depending on class
     case $EXPID in
023)
	if [ "$CLASS" == "nonalc" ]
	then
		EXPID="007"
	fi ;;

024)
        if [ "$CLASS" == "nonalc" ]
        then
                EXPID="017"
        fi ;;

026)
        if [ "$CLASS" == "nonalc" ]
        then
                EXPID="006"
        fi ;;

029)
        if [ "$CLASS" == "nonalc" ]
        then
                EXPID="009"
        fi ;;

030)
        if [ "$CLASS" == "nonalc" ]
        then
                EXPID="005"
        fi ;;

031)
        if [ "$CLASS" == "nonalc" ]
        then
                EXPID="011"
        fi ;;

032)
        if [ "$CLASS" == "nonalc" ]
        then
                EXPID="003"
        fi ;;

034)
        if [ "$CLASS" == "nonalc" ]
        then
                EXPID="002"
        fi ;;

036)
        if [ "$CLASS" == "nonalc" ]
        then
                EXPID="016"
        fi ;;

038)
        if [ "$CLASS" == "nonalc" ]
        then
                EXPID="018"
        fi ;;

041)
        if [ "$CLASS" == "nonalc" ]
        then
                EXPID="021"
        fi ;;

042)
        if [ "$CLASS" == "nonalc" ]
        then
                EXPID="022"
        fi ;;

046)
        if [ "$CLASS" == "nonalc" ]
        then
                EXPID="025"
        fi ;;

048)
        if [ "$CLASS" == "nonalc" ]
        then
                EXPID="027"
        fi ;;

049)
        if [ "$CLASS" == "nonalc" ]
        then
                EXPID="028"
        fi ;;

050)
        if [ "$CLASS" == "nonalc" ]
        then
                EXPID="029"
        fi ;;

051)
        if [ "$CLASS" == "nonalc" ]
        then
                EXPID="024"
        fi ;;

055)
        if [ "$CLASS" == "nonalc" ]
        then
                EXPID="026"
        fi ;;

059)
        if [ "$CLASS" == "nonalc" ]
        then
                EXPID="023"
        fi ;;

060)
        if [ "$CLASS" == "nonalc" ]
        then
                EXPID="030"
        fi ;;

002)
        if [ "$CLASS" == "nonalc" ]
        then
		# Skip this experiment later
                EXPID="099"
        fi ;;

003)
        if [ "$CLASS" == "nonalc" ]
        then
                # Skip this experiment later
                EXPID="099"
        fi ;;

004)
        if [ "$CLASS" == "nonalc" ]
        then
                # Skip this experiment later
                EXPID="099"
        fi ;;

005)
        if [ "$CLASS" == "nonalc" ]
        then
                # Skip this experiment later
                EXPID="099"
        fi ;;

006)
        if [ "$CLASS" == "nonalc" ]
        then
                # Skip this experiment later
                EXPID="099"
        fi ;;

007)
        if [ "$CLASS" == "nonalc" ]
        then
                # Skip this experiment later
                EXPID="099"
        fi ;;

009)
        if [ "$CLASS" == "nonalc" ]
        then
                # Skip this experiment later
                EXPID="099"
        fi ;;

011)
        if [ "$CLASS" == "nonalc" ]
        then
                # Skip this experiment later
                EXPID="099"
        fi ;;

016)
        if [ "$CLASS" == "nonalc" ]
        then
                # Skip this experiment later
                EXPID="099"
        fi ;;

017)
        if [ "$CLASS" == "nonalc" ]
        then
                # Skip this experiment later
                EXPID="099"
        fi ;;

018)
        if [ "$CLASS" == "nonalc" ]
        then
                # Skip this experiment later
                EXPID="099"
        fi ;;

021)
        if [ "$CLASS" == "nonalc" ]
        then
                # Skip this experiment later
                EXPID="099"
        fi ;;

022)
        if [ "$CLASS" == "nonalc" ]
        then
                # Skip this experiment later
                EXPID="099"
        fi ;;

025)
        if [ "$CLASS" == "nonalc" ]
        then
                # Skip this experiment later
                EXPID="099"
        fi ;;

027)
        if [ "$CLASS" == "nonalc" ]
        then
                # Skip this experiment later
                EXPID="099"
        fi ;;

028)
        if [ "$CLASS" == "nonalc" ]
        then
                # Skip this experiment later
                EXPID="099"
        fi ;;


*)
	# Skip EXP if > 030
        if [[ $EXPID =~ $rePARSEEXPID ]]
        then
                TEST=${BASH_REMATCH[1]}
                if [ "$TEST" -gt 30 ]
                then
                        EXPID="099"
		else
			echo "No mapping required for experiment $EXPID"
                fi
        else
                echo "Error parsing numerical value from EXP ID $EXPID"
                echo "Exiting"
                exit 77
        fi

	;;

esac
   if [ "$EXPID" != "099" ]
   then
     # Set correct class
     CSV_LINE=${ID_S},${TEXT},${CLASS}
     echo "Adding $COUNT. line to $EXPID/output.csv"
     echo ${CSV_LINE} >> $2/$EXPID/output.csv
   else
     echo "Skipping $CLASS exp with ID $EXPID2"
   fi

   COUNT=`expr $COUNT + 1`

fi

done


if [ $COUNT -gt 0 ]
then
 echo "FINISHED: Added $COUNT files to $d/output.csv"
fi


fi

