HOME := /home/alc/workspace/ALC/
JARS := $(HOME)lib/weka.jar

# Build all targets
all: andi test weka

# Build classes from package andi
andi:
	javac -classpath $(JARS) -d bin src/andi/*.java


test:
	javac -d bin src/team2014/test/*.java


weka:
	javac -d src/team2014/weka/*.java

# Clean all
clean:
	rm -f bin/andi/*.class
	rm -f bin/team2014/test/*.class
	rm -f bin/team2014/weka/*.class
	rm -f bin/*.class
