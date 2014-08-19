#!/bin/bash

if [ -z "$1" -o -z "$2" ]
then
    echo "Usage: $0 <OUTPUT-CSV> <TARGET-CSVS-DIR>"
else

COUNT=-1
SUCC_COUNT=0

COUNT1A=0
COUNT2A=0
COUNT3A=0
COUNT4A=0
COUNT5A=0
COUNT1N=0
COUNT2N=0
COUNT3N=0
COUNT4N=0
COUNT5N=0



reID="([^,]+)"
reEXP="[0-9]{7}([0-9]{3})"
reTEXT="[^,]+\,([^,]+)"
reCLASS="[^,]+\,[^,]+\,(\w+)"

TD=${2%/}

#Remove old CSVs
rm -f $TD/1/output.csv
rm -f $TD/2/output.csv
rm -f $TD/3/output.csv
rm -f $TD/4/output.csv
rm -f $TD/5/output.csv

#Add header lines for CSVs
echo "file,text,class" > $TD/1/output.csv
echo "file,text,class" > $TD/2/output.csv
echo "file,text,class" > $TD/3/output.csv
echo "file,text,class" > $TD/4/output.csv
echo "file,text,class" > $TD/5/output.csv


exec<$1
while read line
do
   COUNT=`expr $COUNT + 1`

   if [ "$COUNT" == "0" ]
   then
       continue
   fi;

   # Get ID
   if [[ $line =~ $reID ]]
   then
      ID=${BASH_REMATCH[1]}

   else
      echo "No ID match in line $COUNT"
      echo "Aborting"
      exit 9
   fi


   # Get EXPID
   if [[ $line =~ $reEXP ]]
   then
      EXPID=${BASH_REMATCH[1]}

      if [[ $line =~ $reTEXT ]]
      then
	TEXT=${BASH_REMATCH[1]}
      else
	echo "No text match in line $COUNT"
	echo "Aborting"
	exit 1
      fi

      if [[ $line =~ $reCLASS ]]
      then
          CLASS=${BASH_REMATCH[1]}

      else
        echo "No class match in line $COUNT"
        echo "Aborting"
        exit 1
      fi



      #Handle experiment ID
      case $EXPID in
            008 ) 
                echo "EXP1"
                if [ "$CLASS" == "nonalc" ]
                then
		  COUNT1N=`expr $COUNT1N + 1`
		else
		  COUNT1A=`expr $COUNT1A + 1`
		fi
                # Add to CSV
		CSV_LINE=${ID},${TEXT},${CLASS}
		echo ${CSV_LINE} >> $TD/1/output.csv
                ;;

            041 ) 
                if [ "$CLASS" == "nonalc" ]
                then
                    echo "EXP2"
                    COUNT2N=`expr $COUNT2N + 1`
		    # Add to CSV
                    CSV_LINE=${ID},${TEXT},${CLASS}
                    echo ${CSV_LINE} >> $TD/2/output.csv
                fi ;;

            021 )
                if [ "$CLASS" == "alc" ]
                then
                    echo "EXP2"
                    COUNT2A=`expr $COUNT2A + 1`
                    # Add to CSV
                    CSV_LINE=${ID},${TEXT},${CLASS}
                    echo ${CSV_LINE} >> $TD/2/output.csv
                fi ;;

	    059 )
                if [ "$CLASS" == "nonalc" ]
                then
                    echo "EXP3"
                    COUNT3N=`expr $COUNT3N + 1`
                    # Add to CSV
                    CSV_LINE=${ID},${TEXT},${CLASS}
                    echo ${CSV_LINE} >> $TD/3/output.csv
                fi ;;

            023 )
                if [ "$CLASS" == "alc" ]
                then
                    echo "EXP3"
                    COUNT3A=`expr $COUNT3A + 1`
                    # Add to CSV
                    CSV_LINE=${ID},${TEXT},${CLASS}
                    echo ${CSV_LINE} >> $TD/3/output.csv
                fi ;;

	    051 )
                if [ "$CLASS" == "nonalc" ]
                then
                    echo "EXP4"
                    COUNT4N=`expr $COUNT4N + 1`
                    # Add to CSV
                    CSV_LINE=${ID},${TEXT},${CLASS}
                    echo ${CSV_LINE} >> $TD/4/output.csv
                fi ;;

            024 )
                if [ "$CLASS" == "alc" ]
                then
                    echo "EXP4"
                    COUNT4A=`expr $COUNT4A + 1`
                    # Add to CSV
                    CSV_LINE=${ID},${TEXT},${CLASS}
                    echo ${CSV_LINE} >> $TD/4/output.csv
                fi ;;

	    050 )
                if [ "$CLASS" == "nonalc" ]
                then
                    echo "EXP5"
                    COUNT5N=`expr $COUNT5N + 1`
                    # Add to CSV
                    CSV_LINE=${ID},${TEXT},${CLASS}
                    echo ${CSV_LINE} >> $TD/5/output.csv
                fi ;;

            029 )
                if [ "$CLASS" == "alc" ]
                then
                    echo "EXP5"
                    COUNT5A=`expr $COUNT5A + 1`
                    # Add to CSV
                    CSV_LINE=${ID},${TEXT},${CLASS}
                    echo ${CSV_LINE} >> $TD/5/output.csv
                fi ;;

               *)
                 ;;
      esac

      SUCC_COUNT=`expr $SUCC_COUNT + 1`

   else
      echo "No ID match in line $COUNT"
      echo "Aborting"
      exit 2
   fi

done

if [ $SUCC_COUNT -gt 0 ]
then
 echo "Found EXP1 ALC  instances: $COUNT1A"
 echo "Found EXP1 NALC instances: $COUNT1N"
 echo " "
 echo "Found EXP2 ALC  instances: $COUNT2A"
 echo "Found EXP2 NALC instances: $COUNT2N"
 echo " "
 echo "Found EXP3 ALC  instances: $COUNT3A"
 echo "Found EXP3 NALC instances: $COUNT3N"
 echo " "
 echo "Found EXP4 ALC  instances: $COUNT4A"
 echo "Found EXP4 NALC instances: $COUNT4N"
 echo " "
 echo "Found EXP5 ALC  instances: $COUNT5A"
 echo "Found EXP5 NALC instances: $COUNT5N"
 echo " "

fi

fi
