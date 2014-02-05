import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;


public class SoundOnly {

	/**
	 * Runs several tests to determine the impact of all sound features
	 *  
	 * @param args
	 */
	
	//private static final String ARFF_FILE = "C:\\Users\\IBM_ADMIN\\Dropbox\\Detecting Alcohol Intoxication in Speech\\Moritz\\sound_features\\test\\result.arff";
	private static final String CSV_DIR = "C:\\Users\\IBM_ADMIN\\Dropbox\\Detecting Alcohol Intoxication in Speech\\Moritz\\sound_features\\test\\";
	
	public static void main(String[] args) {
		
		SoundOnly testRun = new SoundOnly();
		
		
		try {
			Instances data = null;
			//data = testRun.getSoundInstances(ARFF_FILE); //get all instances from arff file
			
			
			data = testRun.getSoundInstances(CSV_DIR + "sound", "\\output.csv");
			
			System.out.println("Instances read from " + CSV_DIR + ": " + data.numInstances());
			Instances[] datasets = new Instances[3];
			
			//split all instances into 3 sets
			System.out.println("Splitting instances...");
			datasets = testRun.getSoundDatasets(data);
			System.out.println("Train set: " + datasets[0].numInstances() + " instances");
			System.out.println("Cross set: " + datasets[1].numInstances() + " instances");
			System.out.println("Test set: " + datasets[2].numInstances() + " instances");
			
			//run classification tests
			System.out.println("Running test for train set...");
			List<List<Double>> resultTrain = testRun.runTest(datasets[0], datasets[1],datasets[2]);
			//save to CSV
			WekaMagic.printHashMap(resultTrain, CSV_DIR + "result_train.csv");
			
			/*
			System.out.println("Running test for cross set...");
			List<List<Double>> resultCross = testRun.runTest(datasets[1]);
			//save to CSV
			WekaMagic.printHashMap(resultCross, CSV_DIR + "result_cross.csv");
						
			System.out.println("Running test for test set...");
			List<List<Double>> resultTest = testRun.runTest(datasets[2]);
			//save to CSV
			WekaMagic.printHashMap(resultTest, CSV_DIR + "result_test.csv"); */
			
			//Plot everything
			/*
			System.out.println("Plotting results...");
			
			System.out.println("Creating chart " + CSV_DIR + "plot_train.png ...");
			GeneratesPlot.createSound(resultTrain, CSV_DIR, "plot_train.png");
			
			System.out.println("Creating chart " + CSV_DIR + "plot_cross.png ...");
			GeneratesPlot.createSound(resultCross, CSV_DIR, "plot_cross.png");
			
			System.out.println("Creating chart " + CSV_DIR + "plot_test.png ...");
			GeneratesPlot.createSound(resultTest, CSV_DIR, "plot_test.png");*/
			
			System.out.println("Finished operations");
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	private List<List<Double>> runTest(Instances train, Instances cross, Instances test) throws Exception {
		
		/* Runs 10 times logistic to regularize and stores results in list */
		
		List<List<Double>> values = new ArrayList<List<Double>>();
		
		double stdRidge = 0.00000001; //10^-8
		double currentRidge = stdRidge;
		MyClassificationOutput currentResult = null;
		MyClassificationOutput logistic_train_eval, logistic_cross_eval, logistic_test_eval;
		
		for (int i=0;i<15;i++)
		{
			currentRidge = stdRidge * (Math.pow(10, i));
			currentResult = WekaMagic.runLogistic(train, currentRidge, 5);
			
			logistic_train_eval = WekaMagic.applyLogistic(train, currentResult);
			logistic_cross_eval = WekaMagic.applyLogistic(cross, currentResult);
			logistic_test_eval  = WekaMagic.applyLogistic(test,  currentResult);
			
			System.out.println("Fscore for ridge = " + currentRidge);
			System.out.println("f1-score - training: " + logistic_train_eval.getF1Score());
			System.out.println("f1-score - cross:    " + logistic_cross_eval.getF1Score());
			System.out.println("f1-score - test:     " + logistic_test_eval.getF1Score());
			System.out.println("");
			
			// Result processing
			List<Double> ex = new ArrayList<Double>();
			ex.add(0, currentRidge);
			ex.add(1, currentResult.getF1Score());
			ex.add(2, (double) currentResult.getElapsedTime());
			values.add(ex);
		}
		
		return values;
				
	}

	
	public Instances getSoundInstances(String dir, String csv) throws Exception
	{
		Instances sound = WekaMagic.soundArffToInstances(dir);		
		Instances text = WekaMagic.textCSVToInstances( dir + csv,"file");
		
		Instances data = WekaMagic.mergeInstancesBy(sound, text, "file");
		
		
		//delete unnecessary attributes
		data.deleteAttributeAt(data.attribute("text").index());
		data.deleteAttributeAt(data.attribute("file").index());
		//data.deleteAttributeAt(data.attribute("numeric_class").index());
    	
    	return data;
	}

	public Instances[] getSoundDatasets(Instances data)
	{
		return WekaMagic.getStratifiedSplits(data, 100);
	}
}
