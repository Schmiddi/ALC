HOME := /home/alc/workspace/ALC/
HOMELIB := $(HOME)lib
JARS := $(HOME)lib/weka.jar
JARS_WEKA := $(HOMELIB)/jcommon-1.0.20.jar:$(HOMELIB)/commons-lang-2.6.jar:$(HOMELIB)/commons-logging-1.1.1.jar:$(HOMELIB)/hamcrest-core-1.3.jar:$(HOMELIB)/hunspell-native-libs-2.4.jar:$(HOMELIB)/javacsv.jar:$(HOMELIB)/jfreechart-1.0.16.jar:$(HOMELIB)/jna-4.0.0.jar:$(HOMELIB)/junit-4.11.jar:$(HOMELIB)/jwordsplitter-3.4.jar:$(HOMELIB)/language-de-2.4.jar:$(HOMELIB)/languagetool-core-2.4.1.jar:$(HOMELIB)/morfologik-fsa-1.8.3.jar:$(HOMELIB)/morfologik-speller-1.8.3.jar:$(HOMELIB)/morfologik-stemming-1.8.3.jar:$(HOMELIB)/segment-1.4.2.jar:$(HOMELIB)/snowball-20051019.jar:$(HOMELIB)/tika-core-1.4.jar:$(HOMELIB)/LibSVM.jar:$(HOMELIB)/libsvm.jar
JARS_TEST := $(HOME)bin
JARS_ALL := $(JARS):$(JARS_TEST):$(JARS_WEKA)
PATH_SOUND_WO_TT := /import/scratch/tjr/tjr40/sound/tests/combined_all_wo_tt/myIS13_ComParE
PATH_SOUND := /import/scratch/tjr/tjr40/sound/tests/combined_all/myIS13_ComParE
PATH_IS2011_SETS := /home/bas-alc/corpus/DOC/IS2011CHALLENGE
PATH_SOUND_IS11 := /import/scratch/tjr/tjr40/sound/tests/combined_all/myIS11_speaker_state
PATH_SET_WO_TT := /import/scratch/tjr/tjr40/sound/tests/combined_all_wo_tt/
XMX := -Xmx38g
NOW := date +"%Y_%m_%d"
OUTPUT_DIR := /home/alc/workspace/ALC/output

# Build all targets of team2014
all: clean andi wekaSpeaker wekaPlot wekaSVM weka test


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
##   Run tests   ##
###################
runSOIS_attr_is11:
	echo "runSOIS_sound11" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.SoundOnlyIS2011 $(PATH_SOUND_IS11) $(PATH_IS2011_SETS) $(OUTPUT_DIR) "attr" "linear"| ./log.sh

runSOIS_sound11:
	echo "runSOIS_sound11" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.SoundOnlyIS2011 $(PATH_SOUND_IS11) $(PATH_IS2011_SETS) $(OUTPUT_DIR) | ./log.sh

runAISwAttr:
	echo "runAISwAttr" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 $(PATH_SOUND) $(PATH_IS2011_SETS) $(OUTPUT_DIR) "attr" | ./log.sh

runAIS:
	echo "runAIS" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.AllIS2011 $(PATH_SOUND) $(PATH_IS2011_SETS) $(OUTPUT_DIR) | ./log.sh

runGISwAttr:
	echo "runGISwAttr" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.GrammarIS2011 $(PATH_SOUND) $(PATH_IS2011_SETS) $(OUTPUT_DIR) "attr" | ./log.sh

runGIS:
	echo "runGIS" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.GrammarIS2011 $(PATH_SOUND) $(PATH_IS2011_SETS) $(OUTPUT_DIR) | ./log.sh

runTISwAttr:
	echo "runTISwAttr" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.TextIS2011 $(PATH_SOUND) $(PATH_IS2011_SETS) $(OUTPUT_DIR) | ./log.sh

runTIS:
	echo "runTIS" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.TextIS2011 $(PATH_SOUND) $(PATH_IS2011_SETS) $(OUTPUT_DIR) "attr" | ./log.sh
	
runSOISwAttr:
	echo "runSOISwAttr" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.SoundOnlyIS2011 $(PATH_SOUND) $(PATH_IS2011_SETS) $(OUTPUT_DIR) "attr" | ./log.sh

runSOIS:
	echo "runSOIS" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.SoundOnlyIS2011 $(PATH_SOUND) $(PATH_IS2011_SETS) $(OUTPUT_DIR) $(arg)| ./log.sh

runSASwoTT:
	echo "runSASwoTT" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.SoundAttributeSelection $(PATH_SOUND_WO_TT) | ./log.sh

runSAS:
	echo "runSAS" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.SoundAttributeSelection $(PATH_SOUND) | ./log.sh

runGASwoTT:
	echo "runGASwoTT" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.GrammarAttributeSelection $(PATH_SOUND_WO_TT) | ./log.sh

runGAS:
	echo "runGAS" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.GrammarAttributeSelection $(PATH_SOUND) | ./log.sh

runSCV:
	echo "runSCV" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.SoundOnlyCrossValidation $(PATH_SOUND) | ./log.sh

runSCVwoTT:
	echo "runSCVwoTT" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.SoundOnlyCrossValidation $(PATH_SOUND_WO_TT) | ./log.sh

runTAS:
	echo "runTAS" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.TextAttributeSelection $(PATH_SOUND) | ./log.sh

runTASwoTT:
	echo "runTASwoTT" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.TextAttributeSelection $(PATH_SOUND_WO_TT) | ./log.sh

runTASfalse:
	echo "runTASfalse" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.TextAttributeSelection $(PATH_SOUND) "false" | ./log.sh

runTASfalsewoTT:
	echo "runTASfalsewoTT" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.TextAttributeSelection $(PATH_SOUND_WO_TT) "false" | ./log.sh

runTCV:
	echo "runTCV" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.TextCrossValidation $(PATH_SOUND) | ./log.sh

runTCVwoTT:
	echo "runTCVwoTT" | ./log.sh
	java $(XMX) -classpath $(JARS_ALL) team2014.test.TextCrossValidation $(PATH_SOUND_WO_TT) | ./log.sh

my:
	echo $(NOW)


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
