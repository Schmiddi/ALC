#!/usr/bin/env python

# calculates the word error rate (based on the Levenshtein [or edit] distance between two word sequences).
#
# usage:
# wer.py 'i know' "i don't know" (recognized word sequence and reference)
# wer.py example.utd (using the fields 'choice1' as recognized word sequence and 'ortho' as reference)
# wer.py example.utd --raw (print numbers of errors line by line)
#
#=============================================================================
#
#    Copyright (C) 2006-2007 all rights reserved SpeechCycle, Inc
#
#    http://www.speechcycle.com
#    Confidential
#
#
#=============================================================================
#
# Description : (Enter description)
#
# Created by :  (david@speechcycle.com)
# Created On :  (2007-06-15)
# 
#-----------------------------------------------------------------------------
# SVN location  - $URL$
# Last Author    - $Author$
# Last check in    - $Date$
# VSS version  - $Revision$
#
#==============================================================================

# changed 2010-11-03 by david@speechcycle.com (error fix for the case that hyp and ref where specified on the command line rather than in a .utd file)

def log_message(message_level, message):
  log_level = 4
  if message_level <= log_level:
    print message

# a is hypothesized sequence, b is reference
def levenshtein(hyp, ref):
    "Calculates the Levenshtein distance between hyp and ref."
    n, m = len(hyp), len(ref)
        
    current = range(n+1)
    for i in range(1, m+1):
        previous, current = current, [i]+[0]*n
        for j in range(1, n+1):
            add, delete = previous[j]+1, current[j-1]+1
            change = previous[j-1]
            #print hyp[j-1], ref[i-1]
            #print "i = %d, j = %d" % (i, j)
            #print previous, current
            if hyp[j-1] != ref[i-1]:
                change = change + 1
            #if add < delete and add < change:
            #elif delete < add and delete < change:
            #elif change < delete and change < add:
            current[j] = min(add, delete, change)
            #print previous, current
            #print add, delete, change
            
    return current[n]

if __name__=="__main__":
	from sys import argv
	from utdreader import utdreader
	
	isRaw = 0
	if len(argv) == 3:
		if argv[2] == '--raw':
			isRaw = 1

# if one argument was supplied, then assume it's a utd file
	if len(argv) == 2 or isRaw == 1:
		utd_lines = utdreader(argv[1])

# running tally of the reference words and hypothesis words
		ref_len = 0
		hyp_len = 0
		total_errors = 0	 # running tally of the total errors
		for line in utd_lines:
# if multiple parses are returned by the recognizer, only consider the first one
			ortho_line = line.ortho.split("#")[0]
			# print ortho_line
			choice1_line = line.choice1.split("#")[0]

# add the current utterance to the running tallies
			ref_len += len(ortho_line.split())
			hyp_len += len(choice1_line.split())

# compute the minimum number of errors between the two strings
			errors = levenshtein(choice1_line.split(), ortho_line.split())
			
			if isRaw == 1:
				print ",",ortho_line,",",choice1_line,",",errors
			
			total_errors += errors
			#print ortho_line
			#log_message(3, "Ref: "+ortho_line+"\nHyp: "+choice1_line+"\nAli: \nSub=, Ins=, Del=, Errs="+str(errors)+"\n\n");
# otherwise, assume the input is two strings
	else:
		hyp = argv[1].split()
		ref = argv[2].split()
		total_errors = levenshtein(hyp, ref)
		ref_len = len(ref)
		hyp_len = len(hyp)

	if isRaw == 0:
		wer = (float(total_errors) / ref_len) * 100
		
		print "Percent Total Error\t=  %5.1f%%\t(%5d)\n" % (wer, total_errors)
		print "Ref. Words\t\t=\t\t(%5d)" % ref_len
		print "Hyp. Words\t\t=\t\t(%5d)" % hyp_len
