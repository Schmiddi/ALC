#!/bin/bash

if [ -z "$1" -o -z "$2" -o -z "$3" ]
then
    echo "Usage: $0 <OUTPUT-CSV> <TARGET-DIR> <SEARCH-DIR-ORIG-WAVS>"
else

COUNT=-1
SUCC_COUNT=0

COUNT1=0
COUNT2=0
COUNT3=0
COUNT4=0
COUNT5=0

reID="([^,]+)"
reEXP="[0-9]{7}([0-9]{3})"
reCLASS="[^,]+\,[^,]+\,(\w+)"

TD=${2%/}

mkdir $TD/1
mkdir $TD/2
mkdir $TD/3
mkdir $TD/4
mkdir $TD/5

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
                COUNT1=`expr $COUNT1 + 1`
                SRC=`find $3 -name $ID.wav`
                cp $SRC $TD/1/$ID.wav
                ;;

            041 ) 
                if [ "$CLASS" == "nonalc" ]
                then
                    echo "EXP2"
                    COUNT2=`expr $COUNT2 + 1`
                    SRC=`find $3 -name $ID.wav`
                    cp $SRC $TD/2/$ID.wav
                fi ;;

            021 )
                if [ "$CLASS" == "alc" ]
                then
                    echo "EXP2"
                    COUNT2=`expr $COUNT2 + 1`
                    SRC=`find $3 -name $ID.wav`
                    cp $SRC $TD/2/$ID.wav
                fi ;;
            
	    059 )
                if [ "$CLASS" == "nonalc" ]
                then
                    echo "EXP3"
                    COUNT3=`expr $COUNT3 + 1`
                    SRC=`find $3 -name $ID.wav`
                    cp $SRC $TD/3/$ID.wav
                fi ;;

            023 )
                if [ "$CLASS" == "alc" ]
                then
                    echo "EXP3"
                    COUNT3=`expr $COUNT3 + 1`
                    SRC=`find $3 -name $ID.wav`
                    cp $SRC $TD/3/$ID.wav
                fi ;;
	
	    051 )
                if [ "$CLASS" == "nonalc" ]
                then
                    echo "EXP4"
                    COUNT4=`expr $COUNT4 + 1`
                    SRC=`find $3 -name $ID.wav`
                    cp $SRC $TD/4/$ID.wav
                fi ;;

            024 )
                if [ "$CLASS" == "alc" ]
                then
                    echo "EXP4"
                    COUNT4=`expr $COUNT4 + 1`
                    SRC=`find $3 -name $ID.wav`
                    cp $SRC $TD/4/$ID.wav
                fi ;;

	    050 )
                if [ "$CLASS" == "nonalc" ]
                then
                    echo "EXP5"
                    COUNT5=`expr $COUNT5 + 1`
                    SRC=`find $3 -name $ID.wav`
                    cp $SRC $TD/5/$ID.wav
                fi ;;

            029 )
                if [ "$CLASS" == "alc" ]
                then
                    echo "EXP5"
                    COUNT5=`expr $COUNT5 + 1`
                    SRC=`find $3 -name $ID.wav`
                    cp $SRC $TD/5/$ID.wav
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
 echo "Found EXP1 instances: $COUNT1"
 echo "Found EXP2 instances: $COUNT2"
 echo "Found EXP3 instances: $COUNT3"
 echo "Found EXP4 instances: $COUNT4"
 echo "Found EXP5 instances: $COUNT5"
fi

fi
