import weka.core.Instances;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;

public class MineMulti {
	// final static String FolderWithDropbox = "C:\\Users\\IBM_ADMIN";
	final static String FolderWithDropbox = "E:\\Downloads";

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
		String currDir = "/home/bas-alc/test/rawData/separate_all/DP";

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
						for (Boolean BNA : values) {
							for (int SW : stop) {
								int ngram_max = NG;// 2/3

								settings.put(options.LowerCase, LC);
								settings.put(options.Stemming, ST);

								settings.put(options.NormalizeDocLength, NDL);
								settings.put(options.OutputWordCounts, NDL);
								settings.put(options.IDFTransform, NDL);
								settings.put(options.TFTransform, NDL);
								settings.put(options.BinarizeNumericAttributes,
										BNA);

								title = "NG-"+NG+",LC-"+LC +",ST-"+ST+",NDL-"+NDL+",BNA-"+BNA+",SW-";
								
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

								
								
								MineMulti.runMine(currDir, WordsToKeep,
										ngram_min, ngram_max, list1, threshold,
										settings, title);
								return;
							}
						}
					}
				}
			}
		}
	}

	public static void runMine(String currDir, int WordsToKeep, int ngram_min,
			int ngram_max, String list1, List<Double> threshold,
			Map<options, Boolean> settings, String title) throws Exception {

		/*
		 * 
		 * All parameter are set
		 */

		String[] fullPath = currDir.split("/");
		String fileName = fullPath[fullPath.length - 1];

		MyOutput text_data;
		Instances dataRaw;
		MyOutput filtered;
		Instances dataFiltered;
		MyOutput selected;
		Instances dataSelected;
		MyClassificationOutput logistic;

		// Load data to Weka
		text_data = WekaMagic.loadText(currDir);
		text_data.print();
		dataRaw = text_data.getData();

		// Store raw data from Weka to arff file
		WekaMagic.saveToArff(dataRaw, fileName + "_raw_text", text_data);

		// Generate the features
		filtered = WekaMagic.generateFeatures(dataRaw, WordsToKeep,
				settings.get(options.Ngram), ngram_min, ngram_max,
				settings.get(options.LowerCase),
				settings.get(options.NormalizeDocLength),
				settings.get(options.Stemming),
				settings.get(options.OutputWordCounts),
				settings.get(options.IDFTransform),
				settings.get(options.TFTransform),
				settings.get(options.Stopword), list1);
		filtered.print();
		dataFiltered = filtered.getData();
		// Randomize data, because they are initially sorted
		dataFiltered.randomize(new Random(1));

		// Store featured data from Weka to arff file
		//WekaMagic.saveToArff(dataFiltered, fileName + "_featured", filtered);

		List<List<Double>> results = new ArrayList<List<Double>>();

		for (double currentThreshold : threshold) {
			// Run selection
			selected = WekaMagic.selectionByInfo(dataFiltered,
					settings.get(options.BinarizeNumericAttributes),
					currentThreshold);
			selected.print();
			dataSelected = selected.getData();

			// Backup the selection
			WekaMagic.saveToArff(dataSelected, fileName + "_selected_t"
					+ currentThreshold, selected);

			// Run ML algorithm - logistic
			logistic = WekaMagic.runLogistic(dataSelected);
			logistic.print();
			WekaMagic.saveToArff(null, fileName + "_logistic_t"
					+ currentThreshold, logistic);

			// Result processing
			List<Double> al = new ArrayList<Double>();
			al.add(0,currentThreshold);
			al.add(1,logistic.getUAR());
			al.add(2, (double)logistic.getElapsedTime());
			results.add(al);
		}
		// Store treshold, UAR
		WekaMagic.printHashMap(results, fileName + "_results_"+title+".csv");
		
		GeneratesPlot.create(results,title);
	}
}