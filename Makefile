HOME := /home/alc/workspace/ALC/
HOMELIB := $(HOME)lib
JARS := $(HOME)lib/weka.jar
JARS_WEKA := $(HOMELIB)/jcommon-1.0.20.jar:$(HOMELIB)/commons-lang-2.6.jar:$(HOMELIB)/commons-logging-1.1.1.jar:$(HOMELIB)/hamcrest-core-1.3.jar:$(HOMELIB)/hunspell-native-libs-2.4.jar:$(HOMELIB)/javacsv.jar:$(HOMELIB)/jfreechart-1.0.16.jar:$(HOMELIB)/jna-4.0.0.jar:$(HOMELIB)/junit-4.11.jar:$(HOMELIB)/jwordsplitter-3.4.jar:$(HOMELIB)/language-de-2.4.jar:$(HOMELIB)/languagetool-core-2.4.1.jar:$(HOMELIB)/morfologik-fsa-1.8.3.jar:$(HOMELIB)/morfologik-speller-1.8.3.jar:$(HOMELIB)/morfologik-stemming-1.8.3.jar:$(HOMELIB)/segment-1.4.2.jar:$(HOMELIB)/snowball-20051019.jar:$(HOMELIB)/tika-core-1.4.jar
JARS_TEST := $(HOME)bin
JARS_ALL := $(JARS):$(JARS_TEST):$(JARS_WEKA)
PATH_SOUND_WO_TT := /import/scratch/tjr/tjr40/sound/tests/combined_all_wo_tt/myIS13_ComParE
PATH_SOUND := /import/scratch/tjr/tjr40/sound/tests/combined_all/myIS13_ComParE
PATH_IS2011_SETS := /home/bas-alc/corpus/DOC/IS2011CHALLENGE
XMX := -Xmx38g
NOW := date +"%Y_%m_%d"

# Build all targets of team2014
all: andi weka test

# Build classes from package andi
andi: src/andi/*.java
	javac -classpath $(JARS) -d bin src/andi/*.java


test: src/team2014/test/*.java
	javac -classpath $(JARS_ALL) -d bin src/team2014/test/*.java


weka: src/team2014/weka/*.java
	javac -classpath $(JARS):$(JARS_WEKA) -d bin src/team2014/weka/*.java


runGISwAttr:
	java $(XMX) -classpath $(JARS_ALL) team2014.test.GrammarIS2011 $(PATH_SOUND) $(PATH_IS2011_SETS) "attr"

runTISwAttr:
	java $(XMX) -classpath $(JARS_ALL) team2014.test.TextIS2011 $(PATH_SOUND) $(PATH_IS2011_SETS) "attr"

runTIS:
	java $(XMX) -classpath $(JARS_ALL) team2014.test.TextIS2011 $(PATH_SOUND) $(PATH_IS2011_SETS)
	
runSOIS2011wAttr:
	java $(XMX) -classpath $(JARS_ALL) team2014.test.SoundOnlyIS2011 $(PATH_SOUND) $(PATH_IS2011_SETS) "attr"
	
runSOIS2011:
	java $(XMX) -classpath $(JARS_ALL) team2014.test.SoundOnlyIS2011 $(PATH_SOUND) $(PATH_IS2011_SETS)

runSASwoTT:
	java $(XMX) -classpath $(JARS_ALL) team2014.test.SoundAttributeSelection $(PATH_SOUND_WO_TT)

runSAS:
	java $(XMX) -classpath $(JARS_ALL) team2014.test.SoundAttributeSelection $(PATH_SOUND)

runGASwoTT:
	java $(XMX) -classpath $(JARS_ALL) team2014.test.GrammarAttributeSelection $(PATH_SOUND_WO_TT)

runGAS:
	java $(XMX) -classpath $(JARS_ALL) team2014.test.GrammarAttributeSelection $(PATH_SOUND)

runSCV:
	java $(XMX) -classpath $(JARS_ALL) team2014.test.SoundOnlyCrossValidation $(PATH_SOUND)

runSCVwoTT:
	java $(XMX) -classpath $(JARS_ALL) team2014.test.SoundOnlyCrossValidation $(PATH_SOUND_WO_TT)

runTAS:
	java $(XMX) -classpath $(JARS_ALL) team2014.test.TextAttributeSelection $(PATH_SOUND)

runTASwoTT:
	java $(XMX) -classpath $(JARS_ALL) team2014.test.TextAttributeSelection $(PATH_SOUND_WO_TT)

runTASfalse:
	java $(XMX) -classpath $(JARS_ALL) team2014.test.TextAttributeSelection $(PATH_SOUND) "false"

runTASfalsewoTT:
	java $(XMX) -classpath $(JARS_ALL) team2014.test.TextAttributeSelection $(PATH_SOUND_WO_TT) "false"

runTCV:
	java $(XMX) -classpath $(JARS_ALL) team2014.test.TextCrossValidation $(PATH_SOUND)

runTCVwoTT:
	java $(XMX) -classpath $(JARS_ALL) team2014.test.TextCrossValidation $(PATH_SOUND_WO_TT)

my:
	echo $(NOW)

# Clean all
clean:
	rm -f bin/andi/*.class
	rm -f bin/team2014/test/*.class
	rm -f bin/team2014/weka/*.class
	rm -f bin/*.class
