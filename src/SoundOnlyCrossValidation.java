import java.util.ArrayList;
import java.util.List;

import weka.classifiers.functions.Logistic;
import weka.core.Debug;
import weka.core.Instances;


public class SoundOnlyCrossValidation {

	/**
	 * Runs several tests to determine the impact of all sound features
	 *  
	 * @param args
	 */
	
	//private static final String ARFF_FILE = "C:\\Users\\IBM_ADMIN\\Dropbox\\Detecting Alcohol Intoxication in Speech\\Moritz\\sound_features\\test\\result.arff";
	//private static final String CSV_DIR = "C:\\Users\\IBM_ADMIN\\Dropbox\\Detecting Alcohol Intoxication in Speech\\Moritz\\sound_features\\test\\";
	
	public static void main(String[] args) {
		
		SoundOnlyCrossValidation testRun = new SoundOnlyCrossValidation();
		boolean isWindows = ((System.getProperty("os.name").contains("Windows")))?true:false;
		String fileSep = isWindows?"\\":"/";
		
		
		try {
			Instances data = null;
			//data = testRun.getSoundInstances(ARFF_FILE); //get all instances from arff file
			
			String csv_dir = args[0];
			
			data = SoundOnly.getSoundInstances(csv_dir + fileSep + "sound", fileSep + "output.csv");
			
			System.out.println("Instances read from " + csv_dir + ": " + data.numInstances());
			List<List<List<Double>>> results = testRun.runTest(data);
			
			
			//Adjust file path
			csv_dir += fileSep;
			
			//save to CSV
			WekaMagic.printHashMap(results.get(0), csv_dir + "result_train_cross_validation.csv");//Train set
			WekaMagic.printHashMap(results.get(1), csv_dir + "result_cross_cross_validation.csv");//Cross set
			
					
			//Plot everything
			System.out.println("Plotting results...");
			
			System.out.println("Creating chart " + csv_dir + "plot_train_cross_validation.png ...");
			GeneratesPlot.createSound(results.get(0), csv_dir, "plot_train_cross_validation.png");
			
			System.out.println("Creating chart " + csv_dir + "plot_test_cross_validation.png ...");
			GeneratesPlot.createSound(results.get(1), csv_dir, "plot_test_cross_validation.png");
			
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
			
			System.out.println("Cross validation for ridge = " + currentRidge);
			currentResult = WekaMagic.runLogistic(data, currentRidge, 5);
			CrossValidationOutput cvo = WekaMagic.crossValidation(currentResult, data, 10, 1, null);
			
			
			String modelDir = "neu";
			Debug.saveToFile(modelDir, cvo.getBestClassifier());
			
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
