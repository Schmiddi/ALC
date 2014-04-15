#!/bin/bash

while read line
do

    echo $(date +"%Y-%m-%d %H:%M:%S") "("$(hostname)"):" $line >> /import/scratch/tjr/tjr40/log.txt

done
