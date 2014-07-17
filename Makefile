HOME := /home/alc/workspace/ALC/
HOMELIB := $(HOME)lib
JARS := $(HOME)lib/weka.jar
JARS_WEKA := $(HOMELIB)/jcommon-1.0.20.jar:$(HOMELIB)/commons-lang-2.6.jar:$(HOMELIB)/commons-logging-1.1.1.jar:$(HOMELIB)/hamcrest-core-1.3.jar:$(HOMELIB)/hunspell-native-libs-2.4.jar:$(HOMELIB)/javacsv.jar:$(HOMELIB)/jfreechart-1.0.16.jar:$(HOMELIB)/jna-4.0.0.jar:$(HOMELIB)/junit-4.11.jar:$(HOMELIB)/jwordsplitter-3.4.jar:$(HOMELIB)/language-de-2.4.jar:$(HOMELIB)/languagetool-core-2.4.1.jar:$(HOMELIB)/morfologik-fsa-1.8.3.jar:$(HOMELIB)/morfologik-speller-1.8.3.jar:$(HOMELIB)/morfologik-stemming-1.8.3.jar:$(HOMELIB)/segment-1.4.2.jar:$(HOMELIB)/snowball-20051019.jar:$(HOMELIB)/tika-core-1.4.jar:$(HOMELIB)/LibSVM.jar:$(HOMELIB)/libsvm.jar
JARS_TEST := $(HOME)bin
JARS_ALL := $(JARS):$(JARS_TEST):$(JARS_WEKA):$(HOMELIB)/SMOTE.jar
PATH_SOUND_WO_TT := /import/scratch/tjr/tjr40/sound/tests/combined_all_wo_tt/myIS13_ComParE
PATH_CONFIG_13_TT_SOUND_16k := /import/scratch/tjr/tjr40/sound/tests/combined_all/myIS13_ComParE_16k
PATH_CONFIG_13_TT_SOUND := /import/scratch/tjr/tjr40/sound/tests/combined_all/myIS13_ComParE
PATH_CONFIG_11_ORIG_SOUND := /import/scratch/tjr/tjr40/sound/tests/combined_all/alc_is2011
PATH_IS2011_SETS := /home/bas-alc/corpus/DOC/IS2011CHALLENGE
PATH_CONFIG_11_TT_SOUND := /import/scratch/tjr/tjr40/sound/tests/combined_all/myIS11_speaker_state
PATH_SET_WOTT := /import/scratch/tjr/tjr40/sound/tests/combined_all_wo_tt/
PATH_ORIGINAL_CSV := /import/scratch/tjr/tjr40/sound/tests/combined_all/output.csv
PATH_ORIGINAL_IS2011 := /import/scratch/tjr/tjr40/alc_is2011/ALC_Features
PATH_TESTMAPPING := /home/bas-alc/corpus/DOC/IS2011CHALLENGE/TESTMAPPING.txt
PATH_TOCATS := /import/scratch/tjr/tjr40/sound/tests/separate_all/
PATH_SPEECH_REC_TRAIN := /import/scratch/tjr/tjr40/sound/tests/combined_all/output_speech_recognizer_train.csv
PATH_SPEECH_REC_TRAINDEV := /import/scratch/tjr/tjr40/sound/tests/combined_all/output_speech_recognizer_train+dev_is2011.csv
XMX := -Xmx70g
NOW := date +"%Y_%m_%d"
OUTPUT_DIR := /import/scratch/tjr/tjr40/sound/tests/output
WER_OUTPUT := $(OUTPUT_DIR)/mergedOutput.csv



#####################################
##  Build all targets of team2014  ##
#####################################
all: clean andi wekaPlot wekaSVM weka test

help:
	java -classpath $(JARS_ALL) team2014.test.AllIS2011 -help
###################
##   Run tests   ##
###################
sound_config11_orig_smote:
	echo "sound__config11_orig_smote" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 sound -config $(PATH_CONFIG_11_ORIG_SOUND) -s $(PATH_IS2011_SETS) -o $(OUTPUT_DIR) smote -mT 4| ./log.sh

IS2011_baseline:
	echo "IS2011_baseline" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 sound -config $(PATH_CONFIG_11_TT_SOUND) -s $(PATH_IS2011_SETS) -o $(OUTPUT_DIR) -mT 4 -original $(PATH_ORIGINAL_IS2011) -testmapping $(PATH_TESTMAPPING) smote | ./log.sh

sound_tt_config13_16k_smote:
	echo "sound_tt_config13_16k_smote" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 sound -config $(PATH_CONFIG_13_TT_SOUND_16k) -s $(PATH_IS2011_SETS) -o $(OUTPUT_DIR) smote -mT 4  | ./log.sh

all_config11_orig_smote:
	echo "all__config11_orig_smote" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 sound -config $(PATH_CONFIG_11_ORIG_SOUND) -s $(PATH_IS2011_SETS) -o $(OUTPUT_DIR) smote -mT 4| ./log.sh

WerTest:
	java -classpath $(JARS_ALL) team2014.test.PrepareForWerTest $(PATH_ORIGINAL_CSV) /home/alc/workspace/ALC/speech_recognizer/experiment/output_speech_recognizer_old.csv $(WER_OUTPUT)
	python scripts/wer.py $(WER_OUTPUT)

all_tt_config13:
	echo "all_tt_config13" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 all -config $(PATH_CONFIG_13_TT_SOUND) -s $(PATH_IS2011_SETS) -o $(OUTPUT_DIR) | ./log.sh

all_wott_config13:
	echo "all_wott_config13" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 all -config $(PATH_CONFIG_13_TT_SOUND) -s $(PATH_IS2011_SETS) -o $(OUTPUT_DIR) -wott $(PATH_SET_WOTT) | ./log.sh

all_wott_twtt_config13:
	echo "all_wott_twtt_config13" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 all -config $(PATH_CONFIG_13_TT_SOUND) -s $(PATH_IS2011_SETS) -o $(OUTPUT_DIR) -wott $(PATH_SET_WOTT) twtt | ./log.sh


sound_tt_config13:
	echo "sound_tt_config13" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 sound -config $(PATH_CONFIG_13_TT_SOUND) -s $(PATH_IS2011_SETS) -o $(OUTPUT_DIR) | ./log.sh

sound_wott_config13:
	echo "sound_wott_config13" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 sound -config $(PATH_CONFIG_13_TT_SOUND) -s $(PATH_IS2011_SETS) -o $(OUTPUT_DIR) -wott $(PATH_SET_WOTT) | ./log.sh

sound_wott_twtt_config13:
	echo "sound_wott_twtt_config13" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 sound -config $(PATH_CONFIG_13_TT_SOUND) -s $(PATH_IS2011_SETS) -o $(OUTPUT_DIR) -wott $(PATH_SET_WOTT) twtt | ./log.sh


grammar_tt_config13:
	echo "grammar_tt_config13" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 grammar -config $(PATH_CONFIG_13_TT_SOUND) -s $(PATH_IS2011_SETS) -o $(OUTPUT_DIR) | ./log.sh

grammar_wott_config13:
	echo "grammar_wott_config13" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 grammar -config $(PATH_CONFIG_13_TT_SOUND) -s $(PATH_IS2011_SETS) -o $(OUTPUT_DIR) -wott $(PATH_SET_WOTT) | ./log.sh

grammar_wott_twtt_config13:
	echo "grammar_wott_twtt_config13" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 grammar -config $(PATH_CONFIG_13_TT_SOUND) -s $(PATH_IS2011_SETS) -o $(OUTPUT_DIR) -wott $(PATH_SET_WOTT) twtt | ./log.sh


text_tt_config13:
	echo "text_tt_config13" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 text -config $(PATH_CONFIG_13_TT_SOUND) -s $(PATH_IS2011_SETS) -o $(OUTPUT_DIR) | ./log.sh

text_wott_config13:
	echo "text_wott_config13" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 text -config $(PATH_CONFIG_13_TT_SOUND) -s $(PATH_IS2011_SETS) -o $(OUTPUT_DIR) -wott $(PATH_SET_WOTT) | ./log.sh

text_wott_twtt_config13:
	echo "text_wott_twtt_config13" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 text -config $(PATH_CONFIG_13_TT_SOUND) -s $(PATH_IS2011_SETS) -o $(OUTPUT_DIR) -wott $(PATH_SET_WOTT) twtt | ./log.sh

sound_text_wott_config13:
	echo "sound_text_wott_config13" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 sound text -config $(PATH_CONFIG_13_TT_SOUND) -s $(PATH_IS2011_SETS) -o $(OUTPUT_DIR) -wott $(PATH_SET_WOTT) | ./log.sh

## 2011 Configs

sound_text_wott_config11:
	echo "sound_text_wott_config11" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 sound text -config $(PATH_CONFIG_11_TT_SOUND) -s $(PATH_IS2011_SETS) -o $(OUTPUT_DIR) -wott $(PATH_SET_WOTT) | ./log.sh

all_tt_config11:
	echo "all_tt_config11" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 all -config $(PATH_CONFIG_11_TT_SOUND) -s $(PATH_IS2011_SETS) -o $(OUTPUT_DIR) | ./log.sh

all_wott_config11:
	echo "all_wott_config11" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 all -config $(PATH_CONFIG_11_TT_SOUND) -s $(PATH_IS2011_SETS) -o $(OUTPUT_DIR) -wott $(PATH_SET_WOTT) | ./log.sh

all_wott_twtt_config11:
	echo "all_wott_twtt_config11" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 all -config $(PATH_CONFIG_11_TT_SOUND) -s $(PATH_IS2011_SETS) -o $(OUTPUT_DIR) -wott $(PATH_SET_WOTT) twtt | ./log.sh


sound_tt_config11:
	echo "sound_tt_config11" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 sound -config $(PATH_CONFIG_11_TT_SOUND) -s $(PATH_IS2011_SETS) -o $(OUTPUT_DIR) | ./log.sh

sound_wott_config11:
	echo "sound_wott_config11" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 sound -config $(PATH_CONFIG_11_TT_SOUND) -s $(PATH_IS2011_SETS) -o $(OUTPUT_DIR) -wott $(PATH_SET_WOTT) | ./log.sh

sound_wott_twtt_config11:
	echo "sound_wott_twtt_config11" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 sound -config $(PATH_CONFIG_11_TT_SOUND) -s $(PATH_IS2011_SETS) -o $(OUTPUT_DIR) -wott $(PATH_SET_WOTT) twtt | ./log.sh

text_tt_config11_smote:
	echo "text_tt_config11_smote" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 text -config $(PATH_CONFIG_11_TT_SOUND) -s $(PATH_IS2011_SETS) -o $(OUTPUT_DIR) smote -mT 4 | ./log.sh

text_wott_config11:
	echo "text_wott_config11" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 text -config $(PATH_CONFIG_11_TT_SOUND) -s $(PATH_IS2011_SETS) -o $(OUTPUT_DIR) -wott $(PATH_SET_WOTT) | ./log.sh

grammar_wott_config11:
	echo "grammar_wott_config11" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 grammar -config $(PATH_CONFIG_11_TT_SOUND) -s $(PATH_IS2011_SETS) -o $(OUTPUT_DIR) -wott $(PATH_SET_WOTT) | ./log.sh


all_wott_config13_16k:
	echo "all_wott_config13_16k" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 all -config $(PATH_CONFIG_13_TT_SOUND_16k) -s $(PATH_IS2011_SETS) -o $(OUTPUT_DIR) -wott $(PATH_SET_WOTT) | ./log.sh

sound_tt_config11_smote:
	echo "sound_tt_config_11_smote" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 sound -config $(PATH_CONFIG_11_TT_SOUND) -s $(PATH_IS2011_SETS) -o $(OUTPUT_DIR) smote -mT 4| ./log.sh

all_tt_config11_smote:
	echo "all_tt_config_11_smote" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 all -config $(PATH_CONFIG_11_TT_SOUND) -s $(PATH_IS2011_SETS) -o $(OUTPUT_DIR) smote -mT 4| ./log.sh


######################################
##          Categories text         ##
######################################

# output_DP.csv  output_DQ.csv  output_EC.csv  output_LN.csv  output_LS.csv  output_LT.csv  output_MP.csv  output_MQ.csv  output_RA.csv  output_RR.csv  output_RT.csv

text_tt_config11_smote_cat_all: text_tt_config11_smote_cat_DP text_tt_config11_smote_cat_DQ text_tt_config11_smote_cat_EC text_tt_config11_smote_cat_LN text_tt_config11_smote_cat_LS text_tt_config11_smote_cat_LT text_tt_config11_smote_cat_MP text_tt_config11_smote_cat_MQ text_tt_config11_smote_cat_RA text_tt_config11_smote_cat_RR text_tt_config11_smote_cat_RT

text_tt_config11_smote_cat_DP:
	echo "text_tt_config_11_smote_cat_DP" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 text -config $(PATH_CONFIG_11_TT_SOUND) -s $(PATH_IS2011_SETS) -o $(OUTPUT_DIR) -smote 0 -mT 4 -sc $(PATH_TOCATS)output_DP.csv | ./log.sh

text_tt_config11_smote_cat_DQ:
	echo "text_tt_config_11_smote_cat_DQ" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 text -config $(PATH_CONFIG_11_TT_SOUND) -s $(PATH_IS2011_SETS) -o $(OUTPUT_DIR) -smote 0 -mT 4 -sc $(PATH_TOCATS)output_DQ.csv | ./log.sh

text_tt_config11_smote_cat_EC:
	echo "text_tt_config_11_smote_cat_EC" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 text -config $(PATH_CONFIG_11_TT_SOUND) -s $(PATH_IS2011_SETS) -o $(OUTPUT_DIR) -smote 0 -mT 4 -sc $(PATH_TOCATS)output_EC.csv | ./log.sh

text_tt_config11_smote_cat_LN:
	echo "text_tt_config_11_smote_cat_LN" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 text -config $(PATH_CONFIG_11_TT_SOUND) -s $(PATH_IS2011_SETS) -o $(OUTPUT_DIR) -smote 0 -mT 4 -sc $(PATH_TOCATS)output_LN.csv | ./log.sh

text_tt_config11_smote_cat_LS:
	echo "text_tt_config_11_smote_cat_LS" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 text -config $(PATH_CONFIG_11_TT_SOUND) -s $(PATH_IS2011_SETS) -o $(OUTPUT_DIR) -smote 0 -mT 4 -sc $(PATH_TOCATS)output_LS.csv | ./log.sh

text_tt_config11_smote_cat_LT:
	echo "text_tt_config_11_smote_cat_LT" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 text -config $(PATH_CONFIG_11_TT_SOUND) -s $(PATH_IS2011_SETS) -o $(OUTPUT_DIR) -smote 0 -mT 4 -sc $(PATH_TOCATS)output_LT.csv | ./log.sh

text_tt_config11_smote_cat_MP:
	echo "text_tt_config_11_smote_cat_MP" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 text -config $(PATH_CONFIG_11_TT_SOUND) -s $(PATH_IS2011_SETS) -o $(OUTPUT_DIR) -smote 0 -mT 4 -sc $(PATH_TOCATS)output_MP.csv | ./log.sh

text_tt_config11_smote_cat_MQ:
	echo "text_tt_config_11_smote_cat_MQ" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 text -config $(PATH_CONFIG_11_TT_SOUND) -s $(PATH_IS2011_SETS) -o $(OUTPUT_DIR) -smote 0 -mT 4 -sc $(PATH_TOCATS)output_MQ.csv | ./log.sh

text_tt_config11_smote_cat_RA:
	echo "text_tt_config_11_smote_cat_RA" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 text -config $(PATH_CONFIG_11_TT_SOUND) -s $(PATH_IS2011_SETS) -o $(OUTPUT_DIR) -smote 0 -mT 4 -sc $(PATH_TOCATS)output_RA.csv | ./log.sh

text_tt_config11_smote_cat_RR:
	echo "text_tt_config_11_smote_cat_RR" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 text -config $(PATH_CONFIG_11_TT_SOUND) -s $(PATH_IS2011_SETS) -o $(OUTPUT_DIR) -smote 0 -mT 4 -sc $(PATH_TOCATS)output_RR.csv | ./log.sh

text_tt_config11_smote_cat_RT:
	echo "text_tt_config_11_smote_cat_RT" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 text -config $(PATH_CONFIG_11_TT_SOUND) -s $(PATH_IS2011_SETS) -o $(OUTPUT_DIR) -smote 0 -mT 4 -sc $(PATH_TOCATS)output_RT.csv | ./log.sh

######################################
##          Categories sound        ##
######################################

# output_DP.csv  output_DQ.csv  output_EC.csv  output_LN.csv  output_LS.csv  output_LT.csv  output_MP.csv  output_MQ.csv  output_RA.csv  output_RR.csv  output_RT.csv

sound_tt_config11_smote_cat_all: sound_tt_config11_smote_cat_DP sound_tt_config11_smote_cat_DQ sound_tt_config11_smote_cat_EC sound_tt_config11_smote_cat_LN sound_tt_config11_smote_cat_LS sound_tt_config11_smote_cat_LT sound_tt_config11_smote_cat_MP sound_tt_config11_smote_cat_MQ sound_tt_config11_smote_cat_RA sound_tt_config11_smote_cat_RR sound_tt_config11_smote_cat_RT

sound_tt_config11_smote_cat_DP:
	echo "sound_tt_config_11_smote_cat_DP" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 sound -config $(PATH_CONFIG_11_TT_SOUND) -s $(PATH_IS2011_SETS) -o $(OUTPUT_DIR) -smote 0 -mT 4 -sc $(PATH_TOCATS)output_DP.csv | ./log.sh

sound_tt_config11_smote_cat_DQ:
	echo "sound_tt_config_11_smote_cat_DQ" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 sound -config $(PATH_CONFIG_11_TT_SOUND) -s $(PATH_IS2011_SETS) -o $(OUTPUT_DIR) -smote 0 -mT 4 -sc $(PATH_TOCATS)output_DQ.csv | ./log.sh

sound_tt_config11_smote_cat_EC:
	echo "sound_tt_config_11_smote_cat_EC" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 sound -config $(PATH_CONFIG_11_TT_SOUND) -s $(PATH_IS2011_SETS) -o $(OUTPUT_DIR) -smote 0 -mT 4 -sc $(PATH_TOCATS)output_EC.csv | ./log.sh

sound_tt_config11_smote_cat_LN:
	echo "sound_tt_config_11_smote_cat_LN" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 sound -config $(PATH_CONFIG_11_TT_SOUND) -s $(PATH_IS2011_SETS) -o $(OUTPUT_DIR) -smote 0 -mT 4 -sc $(PATH_TOCATS)output_LN.csv | ./log.sh

sound_tt_config11_smote_cat_LS:
	echo "sound_tt_config_11_smote_cat_LS" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 sound -config $(PATH_CONFIG_11_TT_SOUND) -s $(PATH_IS2011_SETS) -o $(OUTPUT_DIR) -smote 0 -mT 4 -sc $(PATH_TOCATS)output_LS.csv | ./log.sh

sound_tt_config11_smote_cat_LT:
	echo "sound_tt_config_11_smote_cat_LT" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 sound -config $(PATH_CONFIG_11_TT_SOUND) -s $(PATH_IS2011_SETS) -o $(OUTPUT_DIR) -smote 0 -mT 4 -sc $(PATH_TOCATS)output_LT.csv | ./log.sh

sound_tt_config11_smote_cat_MP:
	echo "sound_tt_config_11_smote_cat_MP" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 sound -config $(PATH_CONFIG_11_TT_SOUND) -s $(PATH_IS2011_SETS) -o $(OUTPUT_DIR) -smote 0 -mT 4 -sc $(PATH_TOCATS)output_MP.csv | ./log.sh

sound_tt_config11_smote_cat_MQ:
	echo "sound_tt_config_11_smote_cat_MQ" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 sound -config $(PATH_CONFIG_11_TT_SOUND) -s $(PATH_IS2011_SETS) -o $(OUTPUT_DIR) -smote 0 -mT 4 -sc $(PATH_TOCATS)output_MQ.csv | ./log.sh

sound_tt_config11_smote_cat_RA:
	echo "sound_tt_config_11_smote_cat_RA" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 sound -config $(PATH_CONFIG_11_TT_SOUND) -s $(PATH_IS2011_SETS) -o $(OUTPUT_DIR) -smote 0 -mT 4 -sc $(PATH_TOCATS)output_RA.csv | ./log.sh

sound_tt_config11_smote_cat_RR:
	echo "sound_tt_config_11_smote_cat_RR" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 sound -config $(PATH_CONFIG_11_TT_SOUND) -s $(PATH_IS2011_SETS) -o $(OUTPUT_DIR) -smote 0 -mT 4 -sc $(PATH_TOCATS)output_RR.csv | ./log.sh

sound_tt_config11_smote_cat_RT:
	echo "sound_tt_config_11_smote_cat_RT" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 sound -config $(PATH_CONFIG_11_TT_SOUND) -s $(PATH_IS2011_SETS) -o $(OUTPUT_DIR) -smote 0 -mT 4 -sc $(PATH_TOCATS)output_RT.csv | ./log.sh

######################################
##          Categories all          ##
######################################

# output_DP.csv  output_DQ.csv  output_EC.csv  output_LN.csv  output_LS.csv  output_LT.csv  output_MP.csv  output_MQ.csv  output_RA.csv  output_RR.csv  output_RT.csv

all_tt_config11_smote_cat_all: all_tt_config11_smote_cat_DP all_tt_config11_smote_cat_DQ all_tt_config11_smote_cat_EC all_tt_config11_smote_cat_LN all_tt_config11_smote_cat_LS all_tt_config11_smote_cat_LT all_tt_config11_smote_cat_MP all_tt_config11_smote_cat_MQ all_tt_config11_smote_cat_RA all_tt_config11_smote_cat_RR all_tt_config11_smote_cat_RT

all_tt_config11_smote_cat_DP:
	echo "all_tt_config_11_smote_cat_DP" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 all -config $(PATH_CONFIG_11_TT_SOUND) -s $(PATH_IS2011_SETS) -o $(OUTPUT_DIR) -smote 0 -mT 4 -sc $(PATH_TOCATS)output_DP.csv | ./log.sh

all_tt_config11_smote_cat_DQ:
	echo "all_tt_config_11_smote_cat_DQ" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 all -config $(PATH_CONFIG_11_TT_SOUND) -s $(PATH_IS2011_SETS) -o $(OUTPUT_DIR) -smote 0 -mT 4 -sc $(PATH_TOCATS)output_DQ.csv | ./log.sh

all_tt_config11_smote_cat_EC:
	echo "all_tt_config_11_smote_cat_EC" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 all -config $(PATH_CONFIG_11_TT_SOUND) -s $(PATH_IS2011_SETS) -o $(OUTPUT_DIR) -smote 0 -mT 4 -sc $(PATH_TOCATS)output_EC.csv | ./log.sh

all_tt_config11_smote_cat_LN:
	echo "all_tt_config_11_smote_cat_LN" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 all -config $(PATH_CONFIG_11_TT_SOUND) -s $(PATH_IS2011_SETS) -o $(OUTPUT_DIR) -smote 0 -mT 4 -sc $(PATH_TOCATS)output_LN.csv | ./log.sh

all_tt_config11_smote_cat_LS:
	echo "all_tt_config_11_smote_cat_LS" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 all -config $(PATH_CONFIG_11_TT_SOUND) -s $(PATH_IS2011_SETS) -o $(OUTPUT_DIR) -smote 0 -mT 4 -sc $(PATH_TOCATS)output_LS.csv | ./log.sh

all_tt_config11_smote_cat_LT:
	echo "all_tt_config_11_smote_cat_LT" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 all -config $(PATH_CONFIG_11_TT_SOUND) -s $(PATH_IS2011_SETS) -o $(OUTPUT_DIR) -smote 0 -mT 4 -sc $(PATH_TOCATS)output_LT.csv | ./log.sh

all_tt_config11_smote_cat_MP:
	echo "all_tt_config_11_smote_cat_MP" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 all -config $(PATH_CONFIG_11_TT_SOUND) -s $(PATH_IS2011_SETS) -o $(OUTPUT_DIR) -smote 0 -mT 4 -sc $(PATH_TOCATS)output_MP.csv | ./log.sh

all_tt_config11_smote_cat_MQ:
	echo "all_tt_config_11_smote_cat_MQ" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 all -config $(PATH_CONFIG_11_TT_SOUND) -s $(PATH_IS2011_SETS) -o $(OUTPUT_DIR) -smote 0 -mT 4 -sc $(PATH_TOCATS)output_MQ.csv | ./log.sh

all_tt_config11_smote_cat_RA:
	echo "all_tt_config_11_smote_cat_RA" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 all -config $(PATH_CONFIG_11_TT_SOUND) -s $(PATH_IS2011_SETS) -o $(OUTPUT_DIR) -smote 0 -mT 4 -sc $(PATH_TOCATS)output_RA.csv | ./log.sh

all_tt_config11_smote_cat_RR:
	echo "all_tt_config_11_smote_cat_RR" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 all -config $(PATH_CONFIG_11_TT_SOUND) -s $(PATH_IS2011_SETS) -o $(OUTPUT_DIR) -smote 0 -mT 4 -sc $(PATH_TOCATS)output_RR.csv | ./log.sh

all_tt_config11_smote_cat_RT:
	echo "all_tt_config_11_smote_cat_RT" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 all -config $(PATH_CONFIG_11_TT_SOUND) -s $(PATH_IS2011_SETS) -o $(OUTPUT_DIR) -smote 0 -mT 4 -sc $(PATH_TOCATS)output_RT.csv | ./log.sh

grammar_tt_config11_smote_cat_all:
	for number in DP DQ EC LN LS LT MP MQ RA RR RT; do\
		echo "GRAMMAR: Run category "$$number | ./log.sh; \
		name=$(PATH_TOCATS)"output_"$$number".csv"; \
		java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 grammar -config $(PATH_CONFIG_11_TT_SOUND) -s $(PATH_IS2011_SETS) -o $(OUTPUT_DIR) -smote 0 -mT 4 -sc $$name | ./log.sh ;\
	done

#####################################
##   Run experiment wise		   ##
#####################################

text_tt_config11_smote_exp_all:
	for number in 001  002  003  005  006  007  008  009  010  011  012  013  014  015  016  017  018  019  020  021  022  023  024  025  026  027  028  029  030; do\
		echo "TEXT: Run experiment "$$number | ./log.sh; \
		name="/import/scratch/tjr/tjr40/sound/tests/single_experiments/"$$number"/output.csv"; \
		java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 text -config $(PATH_CONFIG_11_TT_SOUND) -s $(PATH_IS2011_SETS) -o $(OUTPUT_DIR) -smote 0 -mT 4 -sc $$name | ./log.sh ;\
	done

sound_tt_config11_smote_exp_all:
	for number in 001  002  003  005  006  007  008  009  010  011  012  013  014  015  016  017  018  019  020  021  022  023  024  025  026  027  028  029  030; do\
		echo "SOUND: Run experiment "$$number | ./log.sh;\
		name="/import/scratch/tjr/tjr40/sound/tests/single_experiments/"$$number"/output.csv"; \
		java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 sound -config $(PATH_CONFIG_11_TT_SOUND) -s $(PATH_IS2011_SETS) -o $(OUTPUT_DIR) -smote 0 -mT 4 -sc $$name | ./log.sh ;\
	done

all_tt_config11_smote_exp_all:
	for number in 001  002  003  005  006  007  008  009  010  011  012  013  014  015  016  017  018  019  020  021  022  023  024  025  026  027  028  029  030; do\
		echo "ALL: Run experiment "$$number | ./log.sh; \
		name="/import/scratch/tjr/tjr40/sound/tests/single_experiments/"$$number"/output.csv"; \
		java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 all -config $(PATH_CONFIG_11_TT_SOUND) -s $(PATH_IS2011_SETS) -o $(OUTPUT_DIR) -smote 0 -mT 4 -sc $$name | ./log.sh ;\
	done

text_speech_rec_tt_config11_smote_exp_all:
	for number in 001  002  003  005  006  007  008  009  010  011  012  013  014  015  016  017  018  019  020  021  022  023  024  025  026  027  028  029  030; do\
		echo "TEXT - TRAIN: Run experiment "$$number | ./log.sh;\
		name="/import/scratch/tjr/tjr40/sound/tests/single_experiments/"$$number"/output.csv"; \
		java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 text -config $(PATH_CONFIG_11_TT_SOUND) -s $(PATH_IS2011_SETS) -o $(OUTPUT_DIR) -smote 0 -mT 4 -sc $$name -tp $(PATH_SPEECH_REC_TRAIN) | ./log.sh ;\
		echo "TEXT - TRAIN-DEV: Run experiment "$$number | ./log.sh ;\
		java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 text -config $(PATH_CONFIG_11_TT_SOUND) -s $(PATH_IS2011_SETS) -o $(OUTPUT_DIR) -smote 0 -mT 4 -sc $$name -tp $(PATH_SPEECH_REC_TRAINDEV) | ./log.sh ;\
	done


all_speech_rec_tt_config11_smote_exp_all:
	for number in 001  002  003  005  006  007  008  009  010  011  012  013  014  015  016  017  018  019  020  021  022  023  024  025  026  027  028  029  030; do\
		echo "ALL - TRAIN: Run experiment "$$number | ./log.sh ;\
		name="/import/scratch/tjr/tjr40/sound/tests/single_experiments/"$$number"/output.csv"; \
		java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 all -config $(PATH_CONFIG_11_TT_SOUND) -s $(PATH_IS2011_SETS) -o $(OUTPUT_DIR) -smote 0 -mT 4 -sc $$name -tp $(PATH_SPEECH_REC_TRAIN) | ./log.sh ;\
		echo "ALL - TRAIN-DEV: Run experiment "$$number | ./log.sh;\
		java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 all -config $(PATH_CONFIG_11_TT_SOUND) -s $(PATH_IS2011_SETS) -o $(OUTPUT_DIR) -smote 0 -mT 4 -sc $$name -tp $(PATH_SPEECH_REC_TRAINDEV) | ./log.sh ;\
	done
#####################################
##   Build classes from packages   ##
#####################################

andi: src/andi/*.java
	javac -classpath $(JARS) -d bin src/andi/*.java

test: src/team2014/test/*.java
	javac -classpath $(JARS_ALL) -d bin src/team2014/test/*.java

weka: src/team2014/weka/*.java
	javac -classpath $(JARS_ALL) -d bin src/team2014/weka/*.java

wekaParallel: src/team2014/weka/parallel/*.java
	javac -classpath $(JARS_ALL) -d bin src/team2014/weka/parallel/*.java

wekaSpeaker: src/team2014/weka/speaker/*.java
	javac -classpath $(JARS_ALL) -d bin src/team2014/weka/speaker/*.java

wekaPlot: src/team2014/weka/plot/*.java
	javac -classpath $(JARS_ALL) -d bin src/team2014/weka/plot/*.java

wekaSVM: src/team2014/weka/svm/*.java
	javac -classpath $(JARS_ALL) -d bin src/team2014/weka/svm/*.java


###################
##   Clean all   ##
###################

clean:
	rm -f bin/andi/*.class
	rm -f bin/team2014/test/*.class
	rm -f bin/team2014/weka/*.class
	rm -f bin/team2014/weka/parallel/*.class
	rm -f bin/team2014/weka/plot/*.class
	rm -f bin/team2014/weka/speaker/*.class
	rm -f bin/team2014/weka/svm/*.class
	rm -f bin/*.class
