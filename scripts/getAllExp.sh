#!/bin/bash

if [ -z "$1" -o -z "$2" -o -z "$3" ]
then
    echo "Usage: $0 <OUTPUT-CSV> <TARGET-DIR> <SEARCH-DIR-ORIG-WAVS>"
else

COUNT=-1
SUCC_COUNT=0

COUNTALC=0
COUNTNALC=0

reID="([^,]+)"
reEXP="[0-9]{7}([0-9]{3})"
reCLASS="[^,]+\,[^,]+\,(\w+)"

TD=${2%/}

mkdir $TD/001
mkdir $TD/002
mkdir $TD/003
mkdir $TD/004
mkdir $TD/005
mkdir $TD/006
mkdir $TD/007
mkdir $TD/008
mkdir $TD/009
mkdir $TD/010
mkdir $TD/011
mkdir $TD/012
mkdir $TD/013
mkdir $TD/014
mkdir $TD/015
mkdir $TD/016
mkdir $TD/017
mkdir $TD/018
mkdir $TD/019
mkdir $TD/020
mkdir $TD/021
mkdir $TD/022
mkdir $TD/023
mkdir $TD/024
mkdir $TD/025
mkdir $TD/026
mkdir $TD/027
mkdir $TD/028
mkdir $TD/029
mkdir $TD/030


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

001 )
	if [ "$CLASS" == "alc" ]
	then
		echo "EXP $EXPID"
		COUNTALC=`expr $COUNTALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/$EXPID/$ID.wav
	fi

	if [ "$CLASS" == "nonalc" ]
	then
		echo "EXP $EXPID"
		COUNTNALC=`expr $COUNTNALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/001/$ID.wav
	fi ;;

002 )
	if [ "$CLASS" == "alc" ]
	then
		echo "EXP $EXPID"
		COUNTALC=`expr $COUNTALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/$EXPID/$ID.wav
	fi ;;

003 )
	if [ "$CLASS" == "alc" ]
	then
		echo "EXP $EXPID"
		COUNTALC=`expr $COUNTALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/$EXPID/$ID.wav
	fi ;;

004 )
	if [ "$CLASS" == "alc" ]
	then
		echo "EXP $EXPID"
		COUNTALC=`expr $COUNTALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/$EXPID/$ID.wav
	fi ;;

005 )
	if [ "$CLASS" == "alc" ]
	then
		echo "EXP $EXPID"
		COUNTALC=`expr $COUNTALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/$EXPID/$ID.wav
	fi ;;

006 )
	if [ "$CLASS" == "alc" ]
	then
		echo "EXP $EXPID"
		COUNTALC=`expr $COUNTALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/$EXPID/$ID.wav
	fi ;;

007 )
	if [ "$CLASS" == "alc" ]
	then
		echo "EXP $EXPID"
		COUNTALC=`expr $COUNTALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/$EXPID/$ID.wav
	fi ;;

008 )
	if [ "$CLASS" == "alc" ]
	then
		echo "EXP $EXPID"
		COUNTALC=`expr $COUNTALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/$EXPID/$ID.wav
	fi

	if [ "$CLASS" == "nonalc" ]
	then
		echo "EXP $EXPID"
		COUNTNALC=`expr $COUNTNALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/008/$ID.wav
	fi ;;

009 )
	if [ "$CLASS" == "alc" ]
	then
		echo "EXP $EXPID"
		COUNTALC=`expr $COUNTALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/$EXPID/$ID.wav
	fi ;;

010 )
	if [ "$CLASS" == "alc" ]
	then
		echo "EXP $EXPID"
		COUNTALC=`expr $COUNTALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/$EXPID/$ID.wav
	fi

	if [ "$CLASS" == "nonalc" ]
	then
		echo "EXP $EXPID"
		COUNTNALC=`expr $COUNTNALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/010/$ID.wav
	fi ;;

011 )
	if [ "$CLASS" == "alc" ]
	then
		echo "EXP $EXPID"
		COUNTALC=`expr $COUNTALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/$EXPID/$ID.wav
	fi ;;

012 )
	if [ "$CLASS" == "alc" ]
	then
		echo "EXP $EXPID"
		COUNTALC=`expr $COUNTALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/$EXPID/$ID.wav
	fi

	if [ "$CLASS" == "nonalc" ]
	then
		echo "EXP $EXPID"
		COUNTNALC=`expr $COUNTNALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/012/$ID.wav
	fi ;;

013 )
	if [ "$CLASS" == "alc" ]
	then
		echo "EXP $EXPID"
		COUNTALC=`expr $COUNTALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/$EXPID/$ID.wav
	fi

	if [ "$CLASS" == "nonalc" ]
	then
		echo "EXP $EXPID"
		COUNTNALC=`expr $COUNTNALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/013/$ID.wav
	fi ;;

014 )
	if [ "$CLASS" == "alc" ]
	then
		echo "EXP $EXPID"
		COUNTALC=`expr $COUNTALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/$EXPID/$ID.wav
	fi

	if [ "$CLASS" == "nonalc" ]
	then
		echo "EXP $EXPID"
		COUNTNALC=`expr $COUNTNALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/014/$ID.wav
	fi ;;

015 )
	if [ "$CLASS" == "alc" ]
	then
		echo "EXP $EXPID"
		COUNTALC=`expr $COUNTALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/$EXPID/$ID.wav
	fi

	if [ "$CLASS" == "nonalc" ]
	then
		echo "EXP $EXPID"
		COUNTNALC=`expr $COUNTNALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/015/$ID.wav
	fi ;;
	
016 )
	if [ "$CLASS" == "alc" ]
	then
		echo "EXP $EXPID"
		COUNTALC=`expr $COUNTALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/$EXPID/$ID.wav
	fi ;;

017 )
	if [ "$CLASS" == "alc" ]
	then
		echo "EXP $EXPID"
		COUNTALC=`expr $COUNTALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/$EXPID/$ID.wav
	fi ;;

018 )
	if [ "$CLASS" == "alc" ]
	then
		echo "EXP $EXPID"
		COUNTALC=`expr $COUNTALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/$EXPID/$ID.wav
	fi ;;

019 )
	if [ "$CLASS" == "alc" ]
	then
		echo "EXP $EXPID"
		COUNTALC=`expr $COUNTALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/$EXPID/$ID.wav
	fi

	if [ "$CLASS" == "nonalc" ]
	then
		echo "EXP $EXPID"
		COUNTNALC=`expr $COUNTNALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/019/$ID.wav
	fi ;;

020 )
	if [ "$CLASS" == "alc" ]
	then
		echo "EXP $EXPID"
		COUNTALC=`expr $COUNTALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/$EXPID/$ID.wav
	fi

	if [ "$CLASS" == "nonalc" ]
	then
		echo "EXP $EXPID"
		COUNTNALC=`expr $COUNTNALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/020/$ID.wav
	fi ;;

021 )
	if [ "$CLASS" == "alc" ]
	then
		echo "EXP $EXPID"
		COUNTALC=`expr $COUNTALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/$EXPID/$ID.wav
	fi ;;

022 )
	if [ "$CLASS" == "alc" ]
	then
		echo "EXP $EXPID"
		COUNTALC=`expr $COUNTALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/$EXPID/$ID.wav
	fi ;;

023 )
	if [ "$CLASS" == "alc" ]
	then
		echo "EXP $EXPID"
		COUNTALC=`expr $COUNTALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/$EXPID/$ID.wav
	fi

	if [ "$CLASS" == "nonalc" ]
	then
		echo "EXP $EXPID"
		COUNTNALC=`expr $COUNTNALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/007/$ID.wav
	fi ;;

024 )
	if [ "$CLASS" == "alc" ]
	then
		echo "EXP $EXPID"
		COUNTALC=`expr $COUNTALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/$EXPID/$ID.wav
	fi

	if [ "$CLASS" == "nonalc" ]
	then
		echo "EXP $EXPID"
		COUNTNALC=`expr $COUNTNALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/017/$ID.wav
	fi ;;

025 )
	if [ "$CLASS" == "alc" ]
	then
		echo "EXP $EXPID"
		COUNTALC=`expr $COUNTALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/$EXPID/$ID.wav
	fi ;;

026 )
	if [ "$CLASS" == "alc" ]
	then
		echo "EXP $EXPID"
		COUNTALC=`expr $COUNTALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/$EXPID/$ID.wav
	fi

	if [ "$CLASS" == "nonalc" ]
	then
		echo "EXP $EXPID"
		COUNTNALC=`expr $COUNTNALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/006/$ID.wav
	fi ;;

027 )
	if [ "$CLASS" == "alc" ]
	then
		echo "EXP $EXPID"
		COUNTALC=`expr $COUNTALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/$EXPID/$ID.wav
	fi ;;

028 )
	if [ "$CLASS" == "alc" ]
	then
		echo "EXP $EXPID"
		COUNTALC=`expr $COUNTALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/$EXPID/$ID.wav
	fi ;;

029 )
	if [ "$CLASS" == "alc" ]
	then
		echo "EXP $EXPID"
		COUNTALC=`expr $COUNTALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/$EXPID/$ID.wav
	fi

	if [ "$CLASS" == "nonalc" ]
	then
		echo "EXP $EXPID"
		COUNTNALC=`expr $COUNTNALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/009/$ID.wav
	fi ;;

030 )
	if [ "$CLASS" == "alc" ]
	then
		echo "EXP $EXPID"
		COUNTALC=`expr $COUNTALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/$EXPID/$ID.wav
	fi

	if [ "$CLASS" == "nonalc" ]
	then
		echo "EXP $EXPID"
		COUNTNALC=`expr $COUNTNALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/005/$ID.wav
	fi ;;

031 )
	if [ "$CLASS" == "alc" ]
	then
		echo "EXP $EXPID"
		COUNTALC=`expr $COUNTALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/$EXPID/$ID.wav
	fi

	if [ "$CLASS" == "nonalc" ]
	then
		echo "EXP $EXPID"
		COUNTNALC=`expr $COUNTNALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/011/$ID.wav
	fi ;;

032 )
	if [ "$CLASS" == "alc" ]
	then
		echo "EXP $EXPID"
		COUNTALC=`expr $COUNTALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/$EXPID/$ID.wav
	fi

	if [ "$CLASS" == "nonalc" ]
	then
		echo "EXP $EXPID"
		COUNTNALC=`expr $COUNTNALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/003/$ID.wav
	fi ;;

034 )
	if [ "$CLASS" == "alc" ]
	then
		echo "EXP $EXPID"
		COUNTALC=`expr $COUNTALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/$EXPID/$ID.wav
	fi

	if [ "$CLASS" == "nonalc" ]
	then
		echo "EXP $EXPID"
		COUNTNALC=`expr $COUNTNALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/002/$ID.wav
	fi ;;

036 )
	if [ "$CLASS" == "alc" ]
	then
		echo "EXP $EXPID"
		COUNTALC=`expr $COUNTALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/$EXPID/$ID.wav
	fi

	if [ "$CLASS" == "nonalc" ]
	then
		echo "EXP $EXPID"
		COUNTNALC=`expr $COUNTNALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/016/$ID.wav
	fi ;;

038 )
	if [ "$CLASS" == "alc" ]
	then
		echo "EXP $EXPID"
		COUNTALC=`expr $COUNTALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/$EXPID/$ID.wav
	fi

	if [ "$CLASS" == "nonalc" ]
	then
		echo "EXP $EXPID"
		COUNTNALC=`expr $COUNTNALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/018/$ID.wav
	fi ;;

041 )
	if [ "$CLASS" == "alc" ]
	then
		echo "EXP $EXPID"
		COUNTALC=`expr $COUNTALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/$EXPID/$ID.wav
	fi

	if [ "$CLASS" == "nonalc" ]
	then
		echo "EXP $EXPID"
		COUNTNALC=`expr $COUNTNALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/021/$ID.wav
	fi ;;

042 )
	if [ "$CLASS" == "alc" ]
	then
		echo "EXP $EXPID"
		COUNTALC=`expr $COUNTALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/$EXPID/$ID.wav
	fi

	if [ "$CLASS" == "nonalc" ]
	then
		echo "EXP $EXPID"
		COUNTNALC=`expr $COUNTNALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/022/$ID.wav
	fi ;;

046 )
	if [ "$CLASS" == "alc" ]
	then
		echo "EXP $EXPID"
		COUNTALC=`expr $COUNTALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/$EXPID/$ID.wav
	fi

	if [ "$CLASS" == "nonalc" ]
	then
		echo "EXP $EXPID"
		COUNTNALC=`expr $COUNTNALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/025/$ID.wav
	fi ;;

048 )
	if [ "$CLASS" == "alc" ]
	then
		echo "EXP $EXPID"
		COUNTALC=`expr $COUNTALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/$EXPID/$ID.wav
	fi

	if [ "$CLASS" == "nonalc" ]
	then
		echo "EXP $EXPID"
		COUNTNALC=`expr $COUNTNALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/027/$ID.wav
	fi ;;

049 )
	if [ "$CLASS" == "alc" ]
	then
		echo "EXP $EXPID"
		COUNTALC=`expr $COUNTALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/$EXPID/$ID.wav
	fi

	if [ "$CLASS" == "nonalc" ]
	then
		echo "EXP $EXPID"
		COUNTNALC=`expr $COUNTNALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/028/$ID.wav
	fi ;;

050 )
	if [ "$CLASS" == "alc" ]
	then
		echo "EXP $EXPID"
		COUNTALC=`expr $COUNTALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/$EXPID/$ID.wav
	fi

	if [ "$CLASS" == "nonalc" ]
	then
		echo "EXP $EXPID"
		COUNTNALC=`expr $COUNTNALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/029/$ID.wav
	fi ;;

051 )
	if [ "$CLASS" == "alc" ]
	then
		echo "EXP $EXPID"
		COUNTALC=`expr $COUNTALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/$EXPID/$ID.wav
	fi

	if [ "$CLASS" == "nonalc" ]
	then
		echo "EXP $EXPID"
		COUNTNALC=`expr $COUNTNALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/024/$ID.wav
	fi ;;

055 )
	if [ "$CLASS" == "alc" ]
	then
		echo "EXP $EXPID"
		COUNTALC=`expr $COUNTALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/$EXPID/$ID.wav
	fi

	if [ "$CLASS" == "nonalc" ]
	then
		echo "EXP $EXPID"
		COUNTNALC=`expr $COUNTNALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/026/$ID.wav
	fi ;;

059 )
	if [ "$CLASS" == "alc" ]
	then
		echo "EXP $EXPID"
		COUNTALC=`expr $COUNTALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/$EXPID/$ID.wav
	fi

	if [ "$CLASS" == "nonalc" ]
	then
		echo "EXP $EXPID"
		COUNTNALC=`expr $COUNTNALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/023/$ID.wav
	fi ;;

060 )
	if [ "$CLASS" == "alc" ]
	then
		echo "EXP $EXPID"
		COUNTALC=`expr $COUNTALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/$EXPID/$ID.wav
	fi

	if [ "$CLASS" == "nonalc" ]
	then
		echo "EXP $EXPID"
		COUNTNALC=`expr $COUNTNALC + 1`
		SRC=`find $3 -name $ID.wav`
		cp $SRC $TD/030/$ID.wav
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
 echo "Found ALC instances:    $COUNTALC"
 echo "Found NONALC instances: $COUNTNALC"
fi

fi
