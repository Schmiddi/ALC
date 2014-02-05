import java.util.ArrayList;
import java.util.List;

import weka.classifiers.Evaluation;
import weka.classifiers.functions.Logistic;
import weka.core.Instances;


public class SoundOnlyCrossValidation {

	/**
	 * Runs several tests to determine the impact of all sound features
	 *  
	 * @param args
	 */
	
	//private static final String ARFF_FILE = "C:\\Users\\IBM_ADMIN\\Dropbox\\Detecting Alcohol Intoxication in Speech\\Moritz\\sound_features\\test\\result.arff";
	private static final String CSV_DIR = "C:\\Users\\IBM_ADMIN\\Dropbox\\Detecting Alcohol Intoxication in Speech\\Moritz\\sound_features\\test\\";
	
	public static void main(String[] args) {
		
		SoundOnlyCrossValidation testRun = new SoundOnlyCrossValidation();
		
		
		try {
			Instances data = null;
			//data = testRun.getSoundInstances(ARFF_FILE); //get all instances from arff file
			
			
			data = SoundOnly.getSoundInstances(CSV_DIR + "sound", "\\output.csv");
			
			System.out.println("Instances read from " + CSV_DIR + ": " + data.numInstances());
			List<List<List<Double>>> results = testRun.runTest(data);
			//save to CSV
			WekaMagic.printHashMap(results.get(0), CSV_DIR + "result_train_cross_validation.csv");//Train set
			WekaMagic.printHashMap(results.get(1), CSV_DIR + "result_cross_cross_validation.csv");//Cross set
			
			
		
			//Plot everything
			System.out.println("Plotting results...");
			
			System.out.println("Creating chart " + CSV_DIR + "plot_train_cross_validation.png ...");
			GeneratesPlot.createSound(results.get(0), CSV_DIR, "plot_train_cross_validation.png");
			
			System.out.println("Creating chart " + CSV_DIR + "plot_test_cross_validation.png ...");
			GeneratesPlot.createSound(results.get(1), CSV_DIR, "plot_test_cross_validation.png");
			
			System.out.println("Finished operations");
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	private List<List<List<Double>>> runTest(Instances data) throws Exception {
		
		/* Runs 10 times logistic to regularize and stores results in list */
		
		List<List<List<Double>>> values = new ArrayList<List<List<Double>>>();
		
		double stdRidge = 0.00000001; //10^-8
		double currentRidge = stdRidge;
		MyClassificationOutput currentResult = null;
		
		//create 3 lists storing data for 3 sets
		List<List<Double>> listTrain = new ArrayList<List<Double>>();
		List<List<Double>> listCross = new ArrayList<List<Double>>();
		
		System.out.println("Running tests for train, cross and test set...");
		//Iterate through different ridge values
		for (int i=0;i<15;i++)
		{
			currentRidge = stdRidge * (Math.pow(10, i));
			
			Logistic l = new Logistic();
			l.setRidge(currentRidge);
			l.setMaxIts(5);
			
			currentResult = WekaMagic.runLogistic(data, currentRidge, 5);
			CrossValidationOutput cvo = WekaMagic.crossValidation(currentResult, data, 10, 1);
			
			
			// Result processing to lists
			List<Double> exTrain = new ArrayList<Double>();
			List<Double> exCross = new ArrayList<Double>();
			
			exTrain.add(0, currentRidge);
			exTrain.add(1, cvo.getTrainF1Score());
			listTrain.add(exTrain);
			
			exCross.add(0, currentRidge);
			exCross.add(1, cvo.getTestF1Score());
			listCross.add(exCross);
			
		}
		
		values.add(listTrain);
		values.add(listCross);
		
		return values;
				
	}
}
