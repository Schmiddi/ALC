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
	
	private static final String ARFF_FILE = "C:\\Users\\IBM_ADMIN\\Dropbox\\Detecting Alcohol Intoxication in Speech\\Moritz\\sound_features\\test\\result.arff";
	private static final String CSV_DIR = "C:\\Users\\IBM_ADMIN\\Dropbox\\Detecting Alcohol Intoxication in Speech\\Moritz\\sound_features\\test\\";
	
	public static void main(String[] args) {
		
		SoundOnly testRun = new SoundOnly();
		
		
		try {
			Instances data = null;
			data = testRun.getSoundInstances(ARFF_FILE); //get all instances from arff file
			System.out.println("Instances read from " + ARFF_FILE + ": " + data.numInstances());
			Instances[] datasets = new Instances[3];
			
			//split all instances into 3 sets
			System.out.println("Splitting instances...");
			datasets = testRun.getSoundDatasets(data);
			System.out.println("Train set: " + datasets[0].numInstances() + " instances");
			System.out.println("Cross set: " + datasets[1].numInstances() + " instances");
			System.out.println("Test set: " + datasets[2].numInstances() + " instances");
			
			//run classification tests
			System.out.println("Running test for train set...");
			List<List<Double>> resultTrain = testRun.runTest(datasets[0]);
			//save to CSV
			WekaMagic.printHashMap(resultTrain, CSV_DIR + "result_train.csv");
			
			System.out.println("Running test for cross set...");
			List<List<Double>> resultCross = testRun.runTest(datasets[1]);
			//save to CSV
			WekaMagic.printHashMap(resultCross, CSV_DIR + "result_cross.csv");
						
			System.out.println("Running test for test set...");
			List<List<Double>> resultTest = testRun.runTest(datasets[2]);
			//save to CSV
			WekaMagic.printHashMap(resultTest, CSV_DIR + "result_test.csv");
			
			//Plot everything
			System.out.println("Plotting results...");
			
			System.out.println("Creating chart " + CSV_DIR + "plot_train.png ...");
			GeneratesPlot.createSound(resultTrain, CSV_DIR, "plot_train.png");
			
			System.out.println("Creating chart " + CSV_DIR + "plot_cross.png ...");
			GeneratesPlot.createSound(resultCross, CSV_DIR, "plot_cross.png");
			
			System.out.println("Creating chart " + CSV_DIR + "plot_test.png ...");
			GeneratesPlot.createSound(resultTest, CSV_DIR, "plot_test.png");
			
			System.out.println("Finished operations");
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	private List<List<Double>> runTest(Instances dataset) throws Exception {
		
		/* Runs 10 times logistic to regularize and stores results in list */
		
		List<List<Double>> values = new ArrayList<List<Double>>();
		
		double stdRidge = 0.00000001; //10^-8
		double currentRidge = stdRidge;
		MyClassificationOutput currentResult = null;
		
		for (int i=0;i<13;i++)
		{
			currentRidge = stdRidge * (Math.pow(10, i));
			currentResult = WekaMagic.runLogistic(dataset, currentRidge, 5);
			
			// Result processing
			List<Double> ex = new ArrayList<Double>();
			ex.add(0, currentRidge);
			ex.add(1, currentResult.getUAR());
			ex.add(2, (double) currentResult.getElapsedTime());
			values.add(ex);
		}
		
		return values;
				
	}

	
	public Instances getSoundInstances(String arff_file) throws Exception
	{
		BufferedReader reader = new BufferedReader(new FileReader(arff_file));
		ArffReader arff = new ArffReader(reader);
		Instances data = arff.getData();
				
		//delete unnecessary attributes
		data.deleteAttributeAt(1584);
    	data.deleteAttributeAt(1583);
    	data.deleteAttributeAt(0);
    	
    	//set class attribute
    	data.setClassIndex(data.numAttributes()-1);
    	
    	return data;
	}

	public Instances[] getSoundDatasets(Instances data)
	{
		return WekaMagic.getStratifiedSplits(data, 100);
	}
}
