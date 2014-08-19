#!/bin/env Python

# Returns a column specified by a column name from a given .utd file.
#
# usage:
# utdreader.py <.utd file> <column name>
#
# utdreader can also be called as a module for returning the complete .utd file's content as a table.
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
# Created by :  (krishna@speechcycle.com)
# Created On :  (?)
# 
#-----------------------------------------------------------------------------
# SVN location  - $URL: http://2003build:8080/engrepos/Trunk/SpeechScience/scripts/utdreader.py $
# Last Author    - $Author: davids $
# Last check in    - $Date: 2010-05-28 11:04:28 -0400 (Fri, 28 May 2010) $
# VSS version  - $Revision: 21031 $
#
#==============================================================================

# changed 2010-05-24 by david@speechcycle.com (allowing for empty lines in .utd files)
# changed 2007-11-23 by david@speechcycle.com ('print filename' outcommented)
# changed 2007-11-23 by david@speechcycle.com (the validity check whether ortho and parsed_ortho are in the file header is now a warning)
# changed 2007-10-18 by david@speechcycle.com (utdKeys and databaseKeys updated, database2utd included)
# changed 2007-10-05 by david@speechcycle.com (machine_parse included)
# changed 2007-09-18 by david@speechcycle.com (utdKeys and databaseKeys included)

import sys
import string

# a utd line object (values)
class utd:
  def __repr__(self):
    return string.join(self.__dict__.values(),':')

utdKeys = [
  "file",
  "ortho",
  "parsed_ortho",
  "choice1",
  "parsed_choice1",
  "dm_name",
  "call_no",
  "utt_no",
  "guris"
]

databaseKeys = [
  "localAudioPath",
  "parsableText",
  "annotatedValue",
  "rawText",
  "recValue",
  "dialogModule",
  "callId",
  "uttId",
  "grammar"
]

utd2database = {}
iLine = 0
for utdLine in utdKeys:
	utd2database[utdLine] = databaseKeys[iLine]
	iLine+=1

database2utd = {}
iLine = 0
for utdLine in databaseKeys:
	database2utd[utdLine] = utdKeys[iLine]
	iLine+=1

# make records from utd file lines
def utdreader(filename,header=0):
  fd = file(filename)

  #print filename

  # the input lines from the utd file
  lines = fd.readlines()

  ulist = []

  # this array stores the names of the fields in the utd file
  fields = []

  # the first line of the utd file must be the header
  header_line = lines[0]

  # some older utd files place a # at the beginning of the header line
  fields = string.replace(header_line,'#','').strip().split(':')
  #print fields

  # this check might not be the best idea...can we think of any other way to verify that we have a valid header for the utd file? (checking all fields prob. isn't the best idea, since some field names occur rarely as data points in the utterances, e.g. 'file')
  # if 'ortho' not in fields and 'parsed_ortho' not in fields:
  #   sys.stderr.write("%s does not appear to be a valid utd file (the header doesn't contain ortho or parsed_ortho\n" % filename)
  
  for i in lines[1:]:
    s = i.strip().split(':')
    #print s
    #to allow for empty lines (such as at the end of a .utd file)
    if s == ['']:
      continue
    u = utd()
    ulist.append(u)

    # if fields is empty then we have an error because there is no header file
    for j in range(len(fields)):
      if fields[j]:
        u.__dict__[fields[j]] = s[j]
  if(header):
      return ulist, fields
  else:
    return ulist
  # close the file
  #fd.close()

def utdwriter(filename, list, fields):
  fdo = file(filename,'w')
  header = string.join(fields,':')
  #print "#"+header
  fdo.write(header+"\n");
  for line in list:
    outLine = ''
    for j in range(len(fields)):
      if(j != len(fields) -1):
        outLine = outLine + line.__dict__[fields[j]] + ":"
      else :
        outLine = outLine + line.__dict__[fields[j]]
    #print outLine
    fdo.write(outLine+'\n')
  fdo.close();

