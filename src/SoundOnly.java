import java.util.ArrayList;
import java.util.List;

import weka.core.Instances;


public class SoundOnly {

	/**
	 * Runs several tests to determine the impact of all sound features
	 *  
	 * @param args
	 */
	
	//private static final String CSV_DIR = "C:\\Users\\IBM_ADMIN\\Dropbox\\Detecting Alcohol Intoxication in Speech\\Moritz\\sound_features\\test\\";
	
	public static void main(String[] args) {
		
		SoundOnly testRun = new SoundOnly();
		boolean isWindows = ((System.getProperty("os.name").contains("Windows")))?true:false;
		String fileSep = isWindows?"\\":"/";
		
		try {
			Instances data = null;
			//data = testRun.getSoundInstances(ARFF_FILE); //get all instances from arff file
			
			String csv_dir = args[0];
			data = testRun.getSoundInstances(csv_dir + fileSep + "sound", fileSep + "output.csv");
			
			System.out.println("Instances read from " + csv_dir + ": " + data.numInstances());

			Instances[] datasets = new Instances[3];
			
			//split all instances into 3 sets
			System.out.println("Splitting instances...");
			datasets = getSoundDatasets(data);
			System.out.println("Train set: " + datasets[0].numInstances() + " instances");
			System.out.println("Cross set: " + datasets[1].numInstances() + " instances");
			System.out.println("Test set: " + datasets[2].numInstances() + " instances");
			
			//run classification tests
			List<List<List<Double>>> results = testRun.runTest(datasets[0], datasets[1],datasets[2]);
			
			//Adjust file path for linux
			if (isWindows==false)
				csv_dir += "/";
			
			//save to CSV
			WekaMagic.printHashMap(results.get(0), csv_dir + "result_train.csv");//Train set
			WekaMagic.printHashMap(results.get(1), csv_dir + "result_cross.csv");//Cross set
			WekaMagic.printHashMap(results.get(2), csv_dir + "result_test.csv");//Test set
			
			
		
			//Plot everything
			System.out.println("Plotting results...");
									
			System.out.println("Creating chart " + csv_dir + "plot_train.png ...");
			GeneratesPlot.createSound(results.get(0), csv_dir, "plot_train.png");
			
			System.out.println("Creating chart " + csv_dir + "plot_cross.png ...");
			GeneratesPlot.createSound(results.get(1), csv_dir, "plot_cross.png");
			
			System.out.println("Creating chart " + csv_dir + "plot_test.png ...");
			GeneratesPlot.createSound(results.get(2), csv_dir, "plot_test.png");
			
			System.out.println("Finished operations");
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	private List<List<List<Double>>> runTest(Instances train, Instances cross, Instances test) throws Exception {
		
		/* Runs 10 times logistic to regularize and stores results in list */
		
		List<List<List<Double>>> values = new ArrayList<List<List<Double>>>();
		
		double stdRidge = 0.00000001; //10^-8
		double currentRidge = stdRidge;
		MyClassificationOutput currentResult = null;
		MyClassificationOutput logistic_train_eval, logistic_cross_eval, logistic_test_eval;
		
		//create 3 lists storing data for 3 sets
		List<List<Double>> listTrain = new ArrayList<List<Double>>();
		List<List<Double>> listCross = new ArrayList<List<Double>>();
		List<List<Double>> listTest = new ArrayList<List<Double>>();
		
		System.out.println("Running tests for train, cross and test set...");
		//Iterate through different ridge values
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
			
			// Result processing to lists
			List<Double> exTrain = new ArrayList<Double>();
			List<Double> exCross = new ArrayList<Double>();
			List<Double> exTest = new ArrayList<Double>();
			
			exTrain.add(0, currentRidge);
			exTrain.add(1, logistic_train_eval.getF1Score());
			listTrain.add(exTrain);
			
			exCross.add(0, currentRidge);
			exCross.add(1, logistic_cross_eval.getF1Score());
			listCross.add(exCross);
			
			exTest.add(0, currentRidge);
			exTest.add(1, logistic_test_eval.getF1Score());
			listTest.add(exTest);
			
		}
		
		values.add(listTrain);
		values.add(listCross);
		values.add(listTest);
		
		return values;
				
	}

	
	public static Instances getSoundInstances(String dir, String csv) throws Exception
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

	public static Instances[] getSoundDatasets(Instances data)
	{
		return WekaMagic.getStratifiedSplits(data, 100);
	}
}
