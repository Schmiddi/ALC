#!/bin/bash

if [ -z "$1" ]
then
    echo "Usage: $0 <WAV-DIRECTORY>"
else


CONF=/home/alc/tools/opensmile-2.0-rc1/opensmile/config/IS10_paraling.conf
BIN=/home/alc/tools/opensmile-2.0-rc1/opensmile/SMILExtract

FILES=${1%/}/*.wav
ARFF=${1%/}/feature_output.arff

for f in $FILES
do
  echo "Processing $f ..."
  $BIN -C $CONF 0 -noconsoleoutput -I $f -O $ARFF
done

fi

