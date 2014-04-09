package team2014.test;
import java.util.List;

import team2014.weka.WekaMagic;
import weka.core.Instances;


public class SoundOnlyIS2011 {

	/**
	 * Runs several tests to determine the impact of all sound features
	 *  
	 * @param args
	 */
	
	public static void main(String[] args) {
		
		boolean isWindows = ((System.getProperty("os.name").contains("Windows")))?true:false;
		String fileSep = isWindows?"\\":"/";
		String s_key="file";
		
		if(args.length < 3){
			System.out.println("To less parameters");
		}
		
		try {
			Instances data = null;
			
			String arff_dir = args[0];
			String dirInterspeech = args[1];
			String outputFolder = args[2] + fileSep;
			
			String csv_dir = WekaMagic.getParent(arff_dir);
			
			// Get all Instances
			data = WekaMagic.getSoundInstancesWithFile(arff_dir, csv_dir + "output.csv");
			// Split Instances
			Instances [] sets = WekaMagic.getInterspeech2011Sets(dirInterspeech, data, s_key);

			System.out.println("Instances read from " + arff_dir + ": " + data.numInstances());
			
			Boolean withAttributeSelection = false;

			if (args.length >= 4) {
				if (args[3].equals("attr"))
					withAttributeSelection = true;
			}
			
			//List<List<Double>> results = WekaMagic.runTestUARIS2011(sets, withAttributeSelection);
			
			List<List<Double>> results = WekaMagic.runTestUARIS2011LogisticThreads(sets, withAttributeSelection);
			
			WekaMagic.saveResultIS2011(results, outputFolder, withAttributeSelection, "sound");
						
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}