package team2014.test;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import team2014.weka.GeneratesPlot;
import team2014.weka.MyClassificationOutput;
import team2014.weka.MyOutput;
import team2014.weka.SetType;
import team2014.weka.WekaMagic;
import weka.core.Instances;


public class SoundOnlyIS2011 {

	/**
	 * Runs several tests to determine the impact of all sound features
	 *  
	 * @param args
	 */
	
	//private static final String ARFF_FILE = "C:\\Users\\IBM_ADMIN\\Dropbox\\Detecting Alcohol Intoxication in Speech\\Moritz\\sound_features\\test\\result.arff";
	//private static final String CSV_DIR = "C:\\Users\\IBM_ADMIN\\Dropbox\\Detecting Alcohol Intoxication in Speech\\Moritz\\sound_features\\test\\";
	
	public static void main(String[] args) {
		
		SoundOnlyIS2011 testRun = new SoundOnlyIS2011();
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
			
			List<List<Double>> results = testRun.runTestUAR(sets, withAttributeSelection);
			
			
			// Create timestamp
			Date timestamp = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm");
			
			// Create attr string if with attribute selection
			String attr = "";
			if(withAttributeSelection)
				attr = "attr";
			
			
			// CSV Header
			String[] header = {"Threshold","Ridge","Train UAR", "Dev UAR", "Test UAR"};
			//save to CSV
			WekaMagic.printHashMap(results, header, outputFolder + "sound_IS2011"+attr+sdf.format(timestamp) + ".csv");
						
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	private List<List<Double>> runTestUAR(Instances [] sets, Boolean withAttributeSelection) throws Exception {
		
		/* Runs 10 times logistic to regularize and stores results in list */
		
		List<List<Double>> values = new ArrayList<List<Double>>();
		
		double stdRidge = 0.00000001; //10^-8
		double currentRidge = stdRidge;
				
		ArrayList<Double> threshold = new ArrayList<Double>();
		threshold.add(0.0);

		if (withAttributeSelection) {
			threshold.add(0.00000001);
			threshold.add(0.0001);
			threshold.add(0.0003);
			threshold.add(0.0006);
			threshold.add(0.001);
			threshold.add(0.002);
			threshold.add(0.0025);
			threshold.add(0.0030);
			threshold.add(0.0035);
			threshold.add(0.004);
			threshold.add(0.005);
			threshold.add(0.006);
			threshold.add(0.007);
			threshold.add(0.008);
			threshold.add(0.01);
		}
		
		System.out.println("Running tests for train, cross and test set...");
		//Iterate through different ridge values
		for (int i = 0; i < threshold.size(); i++) {
			for (int u=0;u<15;u++)
			{
				currentRidge = stdRidge * (Math.pow(10, u));
				
				MyOutput filtered = null;
				ArrayList<MyOutput> filters = null;
				
				if (withAttributeSelection) {
					filters = new ArrayList<MyOutput>();
					// true binarizeNumericAttributes is important
					Boolean binarizeNumericAttributes = true;
					filtered = WekaMagic.selectionByInfo(null, binarizeNumericAttributes,
							(Double) threshold.get(i));
					filters.add(filtered);
				}
				
				MyClassificationOutput [] output = WekaMagic.validationIS2011(sets, filters, currentRidge);
	
				// Result processing to lists
				List<Double> listRun = new ArrayList<Double>();
				
				listRun.add(0, threshold.get(i));
				listRun.add(1, currentRidge);
				listRun.add(2, output[SetType.TRAIN.ordinal()].getUAR());
				listRun.add(3, output[SetType.DEV.ordinal()].getUAR());
				listRun.add(4, output[SetType.TEST.ordinal()].getUAR());

				values.add(listRun);
				
				// print all information about the result
				System.out.print("ridge:" + currentRidge + " threshold:" + threshold.get(i)
						+ "Train UAR: " + output[SetType.TRAIN.ordinal()].getUAR() + " Dev UAR:"
						+ output[SetType.DEV.ordinal()].getUAR() + " Test UAR:"
						+ output[SetType.TEST.ordinal()].getUAR() + "\n");
			}
		}
		
		
		return values;
				
	}
}