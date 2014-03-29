package team2014.test;
import team2014.weka.CrossValidationOutput;
import team2014.weka.WekaMagic;
import weka.core.Instances;


public class TrainingIS2011 {
	public static void main(String[] args) {
		
		
			boolean isWindows = ((System.getProperty("os.name").contains("Windows")))?true:false;
			String fileSep = isWindows?"\\":"/";
			
			String s_key="file";
			
			try {
				Instances data = null;
				
				String arff_dir = args[0];
				String csv_dir = WekaMagic.getParent(arff_dir);
				
				//Instances sound = WekaMagic.soundArffToInstances(arff_dir);		
				Instances text = WekaMagic.textCSVToInstances(csv_dir + "output.csv",s_key);
				
				//data = WekaMagic.mergeInstancesBy(sound, text, s_key);
				
				System.out.println("whole data size: " + text.size());
				Instances [] sets = WekaMagic.getInterspeech2011Sets(args[1], text, s_key);
				
				Instances train = sets[0];
				Instances dev   = sets[1];
				Instances test  = sets[2];
				
				System.out.println("train size: " + train.size());
				System.out.println("dev size: " + dev.size());
				System.out.println("test size: " + test.size());
				
				
				//difference between all instances and the instances selected by interspeech 2011
				sets = WekaMagic.getInterspeech2011SetsWithFile(args[1], text, s_key);
				
				Instances notInSets = WekaMagic.getOutOfSets(sets, text, s_key);				
				System.out.println("out of interspeech sets - size: " + notInSets.size());
				System.out.println(notInSets);
				
				//difference between all instances and the set without tongue twisters
				Instances text_wott = WekaMagic.textCSVToInstances(csv_dir + "output_wott.csv",s_key);
				
				notInSets = WekaMagic.getOutOfSets(new Instances [] { text_wott }, text, s_key);				
				System.out.println("deleted tongue twister - size: " + notInSets.size());
				System.out.println(notInSets); //print all affected tongue twisters
				
				//difference between interspeech 2011 instances and set without tongue twisters
				
				
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
}
