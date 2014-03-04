HOME := /home/alc/workspace/ALC/
HOMELIB := $(HOME)lib
JARS := $(HOME)lib/weka.jar
JARS_WEKA := $(HOMELIB)/jcommon-1.0.20.jar:$(HOMELIB)/commons-lang-2.6.jar:$(HOMELIB)/commons-logging-1.1.1.jar:$(HOMELIB)/hamcrest-core-1.3.jar:$(HOMELIB)/hunspell-native-libs-2.4.jar:$(HOMELIB)/javacsv.jar:$(HOMELIB)/jfreechart-1.0.16.jar:$(HOMELIB)/jna-4.0.0.jar:$(HOMELIB)/junit-4.11.jar:$(HOMELIB)/jwordsplitter-3.4.jar:$(HOMELIB)/language-de-2.4.jar:$(HOMELIB)/languagetool-core-2.4.1.jar:$(HOMELIB)/morfologik-fsa-1.8.3.jar:$(HOMELIB)/morfologik-speller-1.8.3.jar:$(HOMELIB)/morfologik-stemming-1.8.3.jar:$(HOMELIB)/segment-1.4.2.jar:$(HOMELIB)/snowball-20051019.jar:$(HOMELIB)/tika-core-1.4.jar
JARS_TEST := $(HOME)bin

# Build all targets of team2014
all: andi weka test

# Build classes from package andi
andi: src/andi/*.java
	javac -classpath $(JARS) -d bin src/andi/*.java


test: src/team2014/test/*.java
	javac -classpath $(JARS):$(JARS_TEST):$(JARS_WEKA) -d bin src/team2014/test/*.java


weka: src/team2014/weka/*.java
	javac -classpath $(JARS):$(JARS_WEKA) -d bin src/team2014/weka/*.java

runSAS:
	java -classpath $(JARS):$(JARS_TEST):$(JARS_WEKA) team2014.test.SoundAttributeSelection /import/scratch/tjr/tjr40/sound/tests/combined_all_wo_tt/myIS13_ComParE

# Clean all
clean:
	rm -f bin/andi/*.class
	rm -f bin/team2014/test/*.class
	rm -f bin/team2014/weka/*.class
	rm -f bin/*.class
