package team2014.test;

import java.util.ArrayList;
import java.util.Arrays;

import team2014.weka.CrossValidationOutput;
import team2014.weka.Speaker;
import team2014.weka.WekaMagic;
import weka.core.Instances;

public class TestXVal {
	
	public static void main(String[] args) throws Exception {
		Instances data = null;
		
		String arff_dir = args[0];
		String csv_dir = WekaMagic.getParent(arff_dir);
		
		Instances text = WekaMagic.textCSVToInstances(csv_dir + "output.csv","file");
		
		
		String speakerTable = args[1];
		ArrayList<Speaker> slist = WekaMagic.loadSpeakerInfo(speakerTable);
		
		System.out.println(slist);
		
		CrossValidationOutput xval = new CrossValidationOutput(text,"file",slist);
	}
}
