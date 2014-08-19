#!/bin/bash

if [ -z "$1" -o -z "$2" -o -z "$3" ]
then
    echo "Usage: $0 <SOURCE-OUTPUT-CSV> <MAPPING-CSV> <EXP-OUTPUT-CSV-DIR>"
else


# regex
reID="([^\s|\,]+)"
reTEXT="[^,]\,+([^,]+)"
reCLASS="[^\s]+\,(\w+)"
reFirstLine="(file)"

# Get all sub directories to iterate through
DIRS=`find $3 -type d`

# Iterate through all EXP dirs
for d in $DIRS
do

COUNT=0

# Delete old csv file
echo "Deleting $d/output.csv"
rm -f $d/output.csv


# Add column names as first row
echo "Creating new CSV in $d"
echo "file,text,class" > $d/output.csv


# Get every file and look up corresponding values (text, class)
FILES=${d%/}/*.wav

echo "Files: $FILES"
for f in $FILES
do
 BASENAME=`basename $f`
 IDEXP=${BASENAME:0:-4}
 echo "IDEXP: $IDEXP"


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
    # echo "----------------------------------"
    # echo "#$COUNT"
    # echo "--------------"
    # echo "ID: $ID_S"
 else
     echo "ID not found for line $line"
     echo "Exiting with error"
     exit 1
 fi

if [ "$ID_S" == "$IDEXP" ]
then
 # Get class from mapping csv for ID_S
 LINE_M=`grep $ID_S $1`
 if [[ -z $LINE_M ]]
 then
     echo "ID not found in file $1"
     echo "Exiting with error"
     exit 2
 else
     # Get text from source output csv
     echo "Line: $LINE_M"
     
     IFS=',' read -ra ADDR <<< "$LINE_M"
     #echo "+++++ SPLITTED ++++++"
    #for i in "${ADDR[@]}"; do
             # process "$i"
             #echo "$i"
        #done
     #echo "+++++ END SPLITTED ++++++"
     
     
     
     
     if [[ $LINE_M =~ $reTEXT ]]
     then
        TEXT=${BASH_REMATCH[1]}
        echo "Text: $TEXT"


if [[ $TEXT == "alc" ]]
then
    echo "Error"
    exit 99
fi






     else
        echo "TEXT not found in line $LINE_M"
        echo "Exiting with error"
        exit 3
     fi
     
     # Get correct class from mapping csv
      LINE_S=`grep $ID_S $2`
      if [[ -z $LINE_S ]]
      then
          echo "ID not found in mapping file $2"
          echo "Exiting with error"
          exit 5
      fi

      
      # Get class from LINE_S
      
     if [[ $LINE_S =~ $reCLASS ]]
     then
         CLASS=${BASH_REMATCH[1]}
         echo "Class: $CLASS"
     else
         echo "Corrext class not found in line $LINE_S"
         echo "Exiting with error"
         exit 4
     fi

     # Set correct class
     CSV_LINE=${ID_S},${TEXT},${CLASS}
     echo "Adding $COUNT. line to $d/output.csv"
     echo ${CSV_LINE} >> $d/output.csv
     COUNT=`expr $COUNT + 1`

 fi

fi

fi


done


if [ $COUNT -gt 0 ]
then
 echo "FINISHED: Added $COUNT files to $d/output.csv"
fi

done

done




fi
