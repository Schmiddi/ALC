package team2014.test;
import java.util.ArrayList;
import java.util.List;

import team2014.weka.GeneratesPlot;
import team2014.weka.MyClassificationOutput;
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
		
		try {
			Instances data = null;
			
			String arff_dir = args[0];
			String csv_dir = WekaMagic.getParent(arff_dir);
			
			// Get all Instances
			data = WekaMagic.getSoundInstancesWithFile(arff_dir, csv_dir + "output.csv");
			// Split Instances
			Instances [] sets = WekaMagic.getInterspeech2011Sets(args[1], data, s_key);

			System.out.println("Instances read from " + arff_dir + ": " + data.numInstances());
			List<List<List<Double>>> results = testRun.runTestUAR(sets);
			
			
			arff_dir += fileSep;
			
			//save to CSV
			WekaMagic.printHashMap(results.get(0), arff_dir + "result_train_soundOnlyIS2011.csv");//Train set
			WekaMagic.printHashMap(results.get(1), arff_dir + "result_dev_soundOnlyIS2011.csv");//Dev set
			WekaMagic.printHashMap(results.get(2), arff_dir + "result_test_soundOnlyIS2011.csv");//Test set
			
					
			//Plot everything
			String xLabel = "Ridge";
			String yLabel = "UAR";
			
			System.out.println("Plotting results...");
			
			System.out.println("Creating chart " + arff_dir + "plot_train_cross_validation.png ...");
			GeneratesPlot.create(results.get(0), arff_dir, "plot_train_cross_validation.png", xLabel, yLabel);
			
			System.out.println("Creating chart " + arff_dir + "plot_dev_cross_validation.png ...");
			GeneratesPlot.create(results.get(1), arff_dir, "plot_dev_cross_validation.png", xLabel, yLabel);
			
			System.out.println("Creating chart " + arff_dir + "plot_test_cross_validation.png ...");
			GeneratesPlot.create(results.get(2), arff_dir, "plot_test_cross_validation.png", xLabel, yLabel);
			
			System.out.println("Finished operations");
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	private List<List<List<Double>>> runTestUAR(Instances [] sets) throws Exception {
		
		/* Runs 10 times logistic to regularize and stores results in list */
		
		List<List<List<Double>>> values = new ArrayList<List<List<Double>>>();
		
		double stdRidge = 0.00000001; //10^-8
		double currentRidge = stdRidge;
		
		//create 3 lists storing data for 3 sets
		List<List<Double>> listTrain = new ArrayList<List<Double>>();
		List<List<Double>> listDev = new ArrayList<List<Double>>();
		List<List<Double>> listTest = new ArrayList<List<Double>>();
		
		System.out.println("Running tests for train, cross and test set...");
		//Iterate through different ridge values
		for (int i=0;i<15;i++)
		{
			currentRidge = stdRidge * (Math.pow(10, i));
			
			System.out.println("Cross validation for ridge = " + currentRidge);
			
			
			MyClassificationOutput [] output = WekaMagic.validationIS2011(sets, null, currentRidge);

			// Result processing to lists
			List<Double> exTrain = new ArrayList<Double>();
			List<Double> exDev = new ArrayList<Double>();
			List<Double> exTest = new ArrayList<Double>();
			
			exTrain.add(0, currentRidge);
			exTrain.add(1, output[SetType.TRAIN.ordinal()].getUAR());
			exTrain.add(2, output[SetType.TRAIN.ordinal()].getF1Score());
			listTrain.add(exTrain);
			
			exDev.add(0, currentRidge);
			exDev.add(1, output[SetType.DEV.ordinal()].getUAR());
			exDev.add(2, output[SetType.DEV.ordinal()].getF1Score());
			listDev.add(exDev);
			
			exTest.add(0, currentRidge);
			exTest.add(1, output[SetType.TEST.ordinal()].getUAR());
			exTest.add(2, output[SetType.TEST.ordinal()].getF1Score());
			listDev.add(exTest);
			
		}
		
		values.add(listTrain);
		values.add(listDev);
		values.add(listTest);
		
		return values;
				
	}
	

}