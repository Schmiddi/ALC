import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import weka.core.Instances;

public class ScienceMulti {
	// final static String FolderWithDropbox = "C:\\Users\\IBM_ADMIN";

	// final static String FolderWithDropbox = "E:\\Downloads";

	enum options {
		Ngram, LowerCase, NormalizeDocLength, Stemming, OutputWordCounts, IDFTransform, TFTransform, Stopword, BinarizeNumericAttributes
	}

	public static void main(String[] args) throws Exception {
		/**
		 * 
		 * Set parameter of input function
		 */
		Map<options, Boolean> settings = new HashMap<options, Boolean>();
		List<Double> threshold = new ArrayList<Double>();

		// parameters for TextDirectoryLoader
		
		// String inputFolder = "/home/bas-alc/test/rawData/separate_all/DP";
		String inputFolder = "E:/Dropbox/Detecting Alcohol Intoxication in Speech/Felix/Backup/DP_real/rawData/DP";
        
		String[] fullPath = inputFolder.split("/");
		String fileName = fullPath[fullPath.length - 1];
		
		String outputMainFolder = "E:/Dropbox/Detecting Alcohol Intoxication in Speech/Dennis";
		
		// Create run folder
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd,HH-mm");

        Date resultdate = new Date(System.currentTimeMillis());
        
        String outputFolder = outputMainFolder+"/Result-"+fileName+","+sdf.format(resultdate);
		Boolean success = (new File(outputFolder)).mkdirs();
		if (!success) {
		    System.out.println("Directory creation failed");
		    return;
		}
		outputFolder += "/";
		// Set Thresholds

		threshold.add(0.0001);
		threshold.add(0.0003);
		threshold.add(0.0006);
		threshold.add(0.001);
		threshold.add(0.003);
		threshold.add(0.004);
		threshold.add(0.005);
		threshold.add(0.006);
		threshold.add(0.007);
		threshold.add(0.008);
		threshold.add(0.01);

		List<Boolean> values = new ArrayList<Boolean>();
		values.add(true);
		values.add(false);
		List<Integer> ngram = new ArrayList<Integer>();
		ngram.add(2);
		ngram.add(3);
		List<Integer> stop = new ArrayList<Integer>();
		stop.add(1);
		stop.add(2);
		stop.add(3);
		// parameters for StringToVector

		int WordsToKeep = 100000;
		settings.put(options.Ngram, true);
		// Boolean Ngram = true; // True/False
		int ngram_min = 1;
		String list1 = null;

		String title;

		for (int NG : ngram) {
			for (Boolean LC : values) {
				for (Boolean ST : values) {
					for (Boolean NDL : values) {
						for (int SW : stop) {
							int ngram_max = NG;// 2/3

							settings.put(options.LowerCase, LC);
							settings.put(options.Stemming, ST);

							settings.put(options.NormalizeDocLength, NDL);
							settings.put(options.OutputWordCounts, NDL);
							settings.put(options.IDFTransform, NDL);
							settings.put(options.TFTransform, NDL);
							settings.put(options.BinarizeNumericAttributes, true);

							title = "NG-" + NG + ",LC-" + LC + ",ST-" + ST + ",NDL-" + NDL + ",SW-";

							if (SW == 1) {
								settings.put(options.Stopword, true);
								list1 = "resources\\germanST.txt";
								title += "germanST";
							} else if (SW == 2) {
								settings.put(options.Stopword, true);
								list1 = "resources\\stop.txt";
								title += "stop";
							} else {
								settings.put(options.Stopword, false);
								list1 = null;
								title += "false";
							}
							// parameters for Attribute Selection

							// stop.txt / germanST.txt

							ScienceMulti.runScience(inputFolder, WordsToKeep, ngram_min, ngram_max,
									list1, threshold, settings, title, outputFolder);
						}
					}
				}
			}
		}
	}

	public static void runScience(String currDir, int WordsToKeep, int ngram_min, int ngram_max,
			String list1, List<Double> threshold, Map<options, Boolean> settings, String title,String outputFolder)
			throws Exception {

		/**
		 * 
		 * Set parameter of input function
		 */

		

		MyOutput text_data;
		Instances dataRaw;

		Instances[] split;
		Instances train_split;
		Instances test_split;

		MyOutput filtered;
		Instances filtered_train;
		Instances filtered_test;

		MyOutput selected;
		Instances selected_train;
		Instances selected_test;
		MyClassificationOutput logistic;

		// Load data to Weka
		text_data = WekaMagic.loadText(currDir);
		text_data.print();
		dataRaw = text_data.getData();

		// Store raw data from Weka to arff file
		WekaMagic.saveToArff(dataRaw, outputFolder + title + "raw_text", text_data);

		// split data to training & test data
		split = WekaMagic.separateTrainTest(dataRaw, 90, 1);
		train_split = split[0];
		test_split = split[1];

		WekaMagic.saveToArff(train_split, outputFolder + title + "raw_train", null);
		WekaMagic.saveToArff(test_split, outputFolder + title + "raw_test", null);

		// Generate the features
		filtered = WekaMagic.generateFeatures(train_split, test_split, WordsToKeep, settings
				.get(options.Ngram), ngram_min, ngram_max, settings.get(options.LowerCase),
				settings.get(options.NormalizeDocLength), settings.get(options.Stemming), settings
						.get(options.OutputWordCounts), settings.get(options.IDFTransform),
				settings.get(options.TFTransform), settings.get(options.Stopword), list1);
		filtered.print();
		filtered_train = filtered.getTrainData();
		filtered_test = filtered.getTestData();

		// Store featured data from Weka to arff file
		WekaMagic.saveToArff(filtered_train, outputFolder + title +"_featured_train", filtered);
		WekaMagic.saveToArff(filtered_test, outputFolder + title + "_featured_test", null);

		List<List<Double>> results = new ArrayList<List<Double>>();

		for (double currentThreshold : threshold) {
			// Run selection
			selected = WekaMagic.selectionByInfo(filtered_train, filtered_test, settings
					.get(options.BinarizeNumericAttributes), currentThreshold);
			selected.print();
			selected_train = selected.getTrainData();
			selected_test = selected.getTestData();

			// Backup the selection
			WekaMagic.saveToArff(selected_train, outputFolder + title + "_" + currentThreshold + "_selected_train", selected);
			WekaMagic.saveToArff(selected_test, outputFolder + title + "_" + currentThreshold + "_selected_test", selected);

			// Run ML algorithm - logistic
			logistic = WekaMagic.runLogistic(selected_train, selected_test);
			logistic.print();
			WekaMagic.saveToArff(null, outputFolder + title + "_" + currentThreshold + "_logistic_t", logistic);
			// Result processing
			List<Double> al = new ArrayList<Double>();
			al.add(0, currentThreshold);
			al.add(1, logistic.getUAR());
			al.add(2, (double) logistic.getElapsedTime());
			al.add(3, (double) selected.getFeatureNumber());
			results.add(al);
		}
		// Store treshold, UAR
		WekaMagic.printHashMap(results, outputFolder + title + "_results.csv");

		GeneratesPlot.create(results, outputFolder, title);
	}
}
