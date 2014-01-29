#!/bin/bash

if [ -z "$1" ]
then
    echo "Usage: $0 <WAV-DIRECTORY>"
else


CONF=/home/alc/tools/opensmile-2.0-rc1/opensmile/config/prosodyAcf.conf
BIN=/home/alc/tools/opensmile-2.0-rc1/opensmile/SMILExtract

FILES=${1%/}/*.wav

for f in $FILES
do
  echo "Processing $f ..."
  $BIN -C $CONF 0 -noconsoleoutput -I $f -O ${f:0:-4}.csv 
done

fi
