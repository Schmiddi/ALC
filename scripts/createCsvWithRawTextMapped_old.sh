#!/bin/bash

if [ -z "$1" -o -z "$2" -o -z "$3" ]
then
    echo "Usage: $0 <SOURCE-OUTPUT-CSV> <MAPPING-CSV> <NEW-OUTPUT-CSV>"
else

COUNT=0

# Delete old csv file
rm -f $3


# regex
reID="([^\s|\,]+)"
reTEXT="[^,]\,+([^,]+)"
reCLASS="[^\s]+\,(\w+)"


# Add column names as first row
echo "file,text,class" > $3

exec<$2
while read line
do 

 # Get ID from current line
 if [[ $line =~ $reID ]]
 then
     ID_S=${BASH_REMATCH[1]}
     COUNT=`expr $COUNT + 1`
     echo "----------------------------------"
     echo "#$COUNT"
     echo "--------------"
     echo "ID: $ID_S"
 else
     echo "ID not found for line $line"
     echo "Exiting with error"
     exit 1
 fi

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
     echo "+++++ SPLITTED ++++++"
     for i in "${ADDR[@]}"; do
             # process "$i"
             echo "$i"
         done
     echo "+++++ END SPLITTED ++++++"
     
     
     
     
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
     if [[ $line =~ $reCLASS ]]
     then
         CLASS=${BASH_REMATCH[1]}
         echo "Class: $CLASS"
     else
         echo "Corrext class not found in line $line"
         echo "Exiting with error"
         exit 4
     fi

     # Set correct class
     CSV_LINE=${ID_S},${TEXT},${CLASS}
     echo ${CSV_LINE} >> $3
 fi

done


if [ $COUNT -gt 0 ]
then
 echo "FINISHED: Added $COUNT files to $3"
fi

fi
