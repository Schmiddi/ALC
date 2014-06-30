package team2014.test;
import java.util.ArrayList;

import team2014.weka.CrossValidationOutput;
import team2014.weka.Speaker;
import team2014.weka.SpeakerCrossValidation;
import team2014.weka.SpeakerSamples;
import team2014.weka.SpeakerSet;
import team2014.weka.WekaMagic;
import weka.core.Instances;
import weka.filters.Filter;


public class Experiments {
	public static void main(String[] args) {
		
		
			boolean isWindows = ((System.getProperty("os.name").contains("Windows")))?true:false;
			String fileSep = isWindows?"\\":"/";
			
			String s_key="file";
			
			try {
				Instances data = null;
				
				String arff_dir = args[0];
				String csv_dir = WekaMagic.getParent(arff_dir);
				
				String speakerTable = args[2];
				
				
				
				
				Instances sound = WekaMagic.getSoundInstancesWithFile(arff_dir, csv_dir + "output.csv"); 	
				Instances text = WekaMagic.textCSVToInstances(csv_dir + "output.csv",s_key);
				
				//data = WekaMagic.mergeInstancesBy(sound, text, s_key);
				
				System.out.println("whole data size: " + text.size());
				
				SpeakerSet speakerData = WekaMagic.matchSpeakerToInstances(speakerTable, text, s_key);
				
				speakerData.printInfo();
				System.out.println();
				
				//speakerData.reduceFemale(6, 1);
				//speakerData.reduceMale(14,2);
				//speakerData.reduceBySize(1);
				speakerData.filterByClassBalance(0.5);
				
				if(speakerData.getNumberMale()>speakerData.getNumberFemale()){
					speakerData.reduceMale(speakerData.getNumberMale()-speakerData.getNumberFemale(),2);
				}
				else{
					speakerData.reduceFemale(speakerData.getNumberFemale() - speakerData.getNumberMale(), 2);
				}
				
				speakerData.printInfo();
				System.out.println();
				
				//SpeakerCrossValidation.check();
				
				//Filter f = SpeakerCrossValidation.createModel(speakerData, sound, 0.04, s_key);
				
				//SpeakerCrossValidation.check();
				
				SpeakerCrossValidation.run(speakerData, speakerTable, sound,  s_key);
				
				
				/*
				Instances [] sets = WekaMagic.getInterspeech2011Sets(args[1], text, s_key, null);
				
				Instances train = sets[0];
				Instances dev   = sets[1];
				Instances test  = sets[2];
				
				
				
				//difference between all instances and the instances selected by interspeech 2011
				sets = WekaMagic.getInterspeech2011SetsWithFile(args[1], text, s_key);

				for(int i=0;i<3;i++){
					SpeakerSet speakerData = WekaMagic.matchSpeakerToInstances(speakerTable, sets[i], s_key);
				
					System.out.println("Details on " + name[i] + " set in the original version");
					speakerData.printInfo();
					System.out.println();
				}
				System.out.println();
				
				
				
				Instances notInSets;// = WekaMagic.getOutOfSets(sets, text, s_key);
				//SpeakerSet speakerDataWott = WekaMagic.matchSpeakerToInstances(speakerTable, notInSets, s_key);
				//speakerDataWott.printALCDistributionByRecType();
				
				
				
				//System.out.println("out of interspeech sets - size: " + notInSets.size());
				//System.out.println(notInSets);
				
				
				
				//difference between all instances and the set without tongue twisters
				Instances text_wott = WekaMagic.textCSVToInstances(csv_dir + "output_wott.csv",s_key);
				
				notInSets = WekaMagic.getOutOfSets(new Instances [] { text_wott }, text, s_key);				
				System.out.println("deleted tongue twister - size: " + notInSets.size());
				//System.out.println(notInSets); //print all affected tongue twisters
				SpeakerSet speakerDataWott = WekaMagic.matchSpeakerToInstances(speakerTable, notInSets, s_key);
				speakerDataWott.printALCDistributionByRecType();
				
				//delete all tongue twisters in the Interspeech 2011 set
				
				Instances [] is11wott = WekaMagic.deleteFromSets(sets, notInSets, s_key);
				
				System.out.println();
				for(int i=0;i<3;i++){
					SpeakerSet speakerData = WekaMagic.matchSpeakerToInstances(speakerTable, is11wott[i], s_key);
				
					System.out.println("Details on " + name[i] + "set without tongue twisters:");
					speakerData.printInfo();
					System.out.println();
				}
				System.out.println();

			
				
				//category				
				String category_file = args[3];
				
				Instances [] cat = WekaMagic.getInterspeech11ByCategory(is11wott,s_key,category_file);
				
				Instances [] catwott = WekaMagic.deleteFromSets(cat, notInSets, s_key);
				
				System.out.println();
				for(int i=0;i<3;i++){
					SpeakerSet speakerData = WekaMagic.matchSpeakerToInstances(speakerTable, catwott[i], s_key);
				
					System.out.println("Details on " + name[i] + "set without tongue twisters:");
					speakerData.printInfo();
					System.out.println();
				}
				System.out.println();
				
				
				
				*/
				
				
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
}
