#!/bin/bash

if [ -z "$1" -o -z "$2" ]
then
    echo "Usage: $0 <CLASSES-DIRECTORY> <OUTPUT-CSV>"
else

COUNT=0

# Get all sub directories to iterate through
DIRS=`find $1 -type d`

# Delete old csv file
rm -f $2

# Add column names as first row
echo "file,text,class" > $2

for d in $DIRS
do 

 # Get all hlb files in current sub directory
 FILES=${d%/}/*.hlb
 c=`find ${d%/}/*.hlb 2> /dev/null | wc -l`
 COUNT=`expr $COUNT + $c`
 if [ $c -gt 0 ]
 then
  echo "Processing ${#FILES[@]} files in sub directory $d"

  for f in $FILES
  do
   BASENAME=`basename $f`
   ID=${BASENAME:0:-4}
   TEXT=`cat $f`
   CLASS=`basename $d`
   echo "Adding data from $BASENAME ($CLASS) to csv ..."
   CSV_LINE=${ID},${TEXT},${CLASS}
   echo ${CSV_LINE} >> $2
  done

 fi

done

if [ $COUNT -gt 0 ]
then
 echo "FINISHED: Added $COUNT files to $2"
fi

fi
