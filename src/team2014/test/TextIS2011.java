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

public class TextIS2011 {

	/**
	 * Runs several tests to determine the impact of all sound features
	 * 
	 * @param args
	 */

	// private static final String ARFF_FILE =
	// "C:\\Users\\IBM_ADMIN\\Dropbox\\Detecting Alcohol Intoxication in Speech\\Moritz\\sound_features\\test\\result.arff";
	// private static final String CSV_DIR =
	// "C:\\Users\\IBM_ADMIN\\Dropbox\\Detecting Alcohol Intoxication in Speech\\Moritz\\sound_features\\test\\";

	public static void main(String[] args) {

		TextIS2011 testRun = new TextIS2011();
		boolean isWindows = ((System.getProperty("os.name").contains("Windows"))) ? true : false;
		String fileSep = isWindows ? "\\" : "/";
		String s_key = "file";

		try {
			Instances data = null;

			String arff_dir = args[0];
			String csv_dir = WekaMagic.getParent(arff_dir);

			data = WekaMagic.textCSVToInstances(csv_dir + "output.csv", "file");

			int class_index = WekaMagic.setClassIndex(data);
			data.renameAttribute(class_index, "_unique_class_name_");
			WekaMagic.setClassIndex(data);

			System.out.println("Instances read from " + arff_dir + ": " + data.numInstances());

			Boolean withAttributeSelection = false;

			if (args.length >= 3) {
				if (args[2].equals("attr"))
					withAttributeSelection = true;
			}

			Instances[] sets = WekaMagic.getInterspeech2011Sets(args[1], data, s_key);

			List<List<List<Double>>> results = testRun.runTest(sets, withAttributeSelection);

			arff_dir += fileSep;

			// Create timestamp
			Date timestamp = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm");

			// Create attr string if with attribute selection
			String attr = "";
			if(withAttributeSelection)
				attr = "attr";
						
			// save to CSV
			WekaMagic.printHashMap(results.get(0), arff_dir + sdf.format(timestamp)
					+ "result_train_textIS2011"+attr+".csv");
			WekaMagic.printHashMap(results.get(1), arff_dir + sdf.format(timestamp)
					+ "result_dev_textIS2011"+attr+".csv");
			WekaMagic.printHashMap(results.get(2), arff_dir + sdf.format(timestamp)
					+ "result_test_textIS2011"+attr+".csv");

			// Plot everything			
			String xLabel = "Ridge";
			String yLabel = "UAR";

			System.out.println("Plotting results...");

			System.out.println("Creating chart " + arff_dir + sdf.format(timestamp)
					+ "plot_train_textIS2011"+attr+".png ...");
			GeneratesPlot.create(results.get(0), arff_dir, sdf.format(timestamp)
					+ "plot_train_textIS2011"+attr+".png", xLabel, yLabel);

			System.out.println("Creating chart " + arff_dir + sdf.format(timestamp)
					+ "plot_dev_textIS2011"+attr+".png ...");
			GeneratesPlot.create(results.get(1), arff_dir, sdf.format(timestamp)
					+ "plot_dev_textIS2011"+attr+".png", xLabel, yLabel);

			System.out.println("Creating chart " + arff_dir + sdf.format(timestamp)
					+ "plot_test_textIS2011"+attr+".png ...");
			GeneratesPlot.create(results.get(2), arff_dir, sdf.format(timestamp)
					+ "plot_test_textIS2011"+attr+".png", xLabel, yLabel);
			System.out.println("Finished operations");

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Run the test
	 * 
	 * @param data
	 * @param withAttributeSelection
	 *            If true, attribute selection will be applied, otherwise
	 *            skipped
	 * @return
	 * @throws Exception
	 */
	private List<List<List<Double>>> runTest(Instances[] sets, Boolean withAttributeSelection)
			throws Exception {

		/* Runs 10 times logistic to regularize and stores results in list */

		List<List<List<Double>>> values = new ArrayList<List<List<Double>>>();

		double stdRidge = 0.00000001; // 10^-8
		double currentRidge = stdRidge;

		// create 3 lists storing data for 3 sets
		List<List<Double>> listTrain = new ArrayList<List<Double>>();
		List<List<Double>> listDev = new ArrayList<List<Double>>();
		List<List<Double>> listTest = new ArrayList<List<Double>>();

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

		System.out.println("Running tests for train, dev and test set...");

		// perfect configuration
		Boolean Ngram = true;
		int ngram_min = 1;
		int ngram_max = 3;
		Boolean LowerCase = true;
		Boolean IDFTransform = false;
		Boolean TFTransform = false;
		Boolean Stopword = true;
		String list1 = "resources\\germanST.txt";
		int wordsToKeep = 1000000;
		Boolean NormalizeDocLength = true;
		Boolean OutputWordCounts = true;
		Boolean Stemming = true;
		int minTermFrequency = 2;

		for (int i = 0; i < threshold.size(); i++) {
			for (int u = 0; u < 15; u++) // 0 - 15
			{
				currentRidge = stdRidge * (Math.pow(10, u));

				// currentResult = WekaMagic.runLogistic((Instances)null,
				// (Double)currentRidge, 5);
				// currentResult = WekaMagic.runSVM((Instances)null,
				// (Double)currentRidge, null);

				MyOutput featuresGen = WekaMagic.generateFeatures(null, wordsToKeep, Ngram,
						ngram_min, ngram_max, LowerCase, NormalizeDocLength, Stemming,
						OutputWordCounts, IDFTransform, TFTransform, Stopword, list1,
						minTermFrequency);

				MyOutput filtered = null;
				if (withAttributeSelection) {
					// true binarizeNumericAttributes is important
					Boolean binarizeNumericAttributes = true;
					filtered = WekaMagic.selectionByInfo(null, binarizeNumericAttributes,
							(Double) threshold.get(i));
				}

				ArrayList<MyOutput> filters = new ArrayList<MyOutput>();
				filters.add(featuresGen);

				if (withAttributeSelection)
					filters.add(filtered);

				MyClassificationOutput[] output = WekaMagic.validationIS2011(sets, filters,
						currentRidge);

				// Result processing to lists
				List<Double> exTrain = new ArrayList<Double>();
				List<Double> exDev = new ArrayList<Double>();
				List<Double> exTest = new ArrayList<Double>();

				exTrain.add(0, currentRidge);
				exTrain.add(1, output[SetType.TRAIN.ordinal()].getUAR());
				listTrain.add(exTrain);

				exDev.add(0, currentRidge);
				exDev.add(1, output[SetType.DEV.ordinal()].getUAR());
				listDev.add(exDev);

				exTest.add(0, currentRidge);
				exTest.add(1, output[SetType.TEST.ordinal()].getUAR());
				listDev.add(exTest);

				// print all information about the result
				System.out.print("ridge:" + currentRidge + " threshold:" + threshold.get(i)
						+ "Train UAR: " + output[SetType.TRAIN.ordinal()].getUAR() + " Dev UAR:"
						+ output[SetType.DEV.ordinal()].getUAR() + " Test UAR:"
						+ output[SetType.TEST.ordinal()].getUAR());

				System.out.print("\n");
			}
		}

		values.add(listTrain);
		values.add(listDev);
		values.add(listTest);

		return values;

	}
}
