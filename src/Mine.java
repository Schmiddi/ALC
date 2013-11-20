import weka.core.Instances;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;

public class Mine {
	//final static String FolderWithDropbox = "C:\\Users\\IBM_ADMIN";
	final static String FolderWithDropbox = "E:\\Downloads";
	
	public static void main(String[] args) throws Exception {
		/**
		 * 
		 * Set parameter of input function
		 */

		// parameters for TextDirectoryLoader
		String currDir = FolderWithDropbox+"\\Dropbox\\Detecting Alcohol Intoxication in Speech\\Felix\\Backup\\DP_real\\rawData\\DP";

		// parameters for StringToVector

		int WordsToKeep = 1000000;

		Boolean Ngram = true; // True/False
		int ngram_min = 1;
		int ngram_max = 3; // 2/3

		Boolean LowerCase = true; // True/False
		Boolean NormalizeDocLength = true; // True/False
		Boolean Stemming = true; // True/False

		Boolean OutputWordCounts = true; // True/False (necessary for
											// IDFTransform, TFTransform,
											// NormalizeDocLength)
		Boolean IDFTransform = true; // True/False
		Boolean TFTransform = true; // True/False

		Boolean Stopword = true; // True/False

		// stop.txt / germanST.txt
		// String list1 =
		// FolderWithDropbox+"\\Dropbox\\Detecting Alcohol Intoxication in Speech\\Felix\\stopwords\\stop.txt";
		String list1 = "resources\\germanST.txt";

		// parameters for Attribute Selection

		Boolean BinarizeNumericAttributes = true; // True/False
		double threshold = 0.004;

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
		int t;

		// Load data to Weka
		text_data = WekaMagic.loadText(currDir);
		text_data.print();
		dataRaw = text_data.getData();

		// Store raw data from Weka to arff file
		WekaMagic.saveToArff(dataRaw, fileName + "_raw_text", text_data);

		// Generate the features
		filtered = WekaMagic.generateFeatures(dataRaw, WordsToKeep, Ngram,
				ngram_min, ngram_max, LowerCase, NormalizeDocLength, Stemming,
				OutputWordCounts, IDFTransform, TFTransform, Stopword, list1);
		filtered.print();
		dataFiltered = filtered.getData();
		// Randomize data, because they are initially sorted
		dataFiltered.randomize(new Random(1));

		// Store featured data from Weka to arff file
		WekaMagic.saveToArff(dataFiltered, fileName + "_featured", filtered);

		
		Map<Double, ArrayList<Object>> results = new HashMap<Double, ArrayList<Object>>();

		for (t = 40; t <= 100; t++) {

			threshold = t * 0.0001;

			// Run selection
			selected = WekaMagic.selectionByInfo(dataFiltered,
					BinarizeNumericAttributes, threshold);
			selected.print();
			dataSelected = selected.getData();

			// Backup the selection
			WekaMagic.saveToArff(dataSelected, fileName + "_selected_t" + t,
					selected);
			
			// Run ML algorithm - logistic
			logistic = WekaMagic.runLogistic(dataSelected);
			logistic.print();
			WekaMagic.saveToArff(null, fileName + "_logistic_t" + t, logistic);
			
			// Result processing
			ArrayList<Object> al = new ArrayList<Object>();
			al.add(0, logistic.getUAR());
			al.add(1, logistic.getElapsedTime());
			results.put(threshold, al);
		}
		// Store treshold, UAR
		ArrayList<Integer> tl = new ArrayList<Integer>();
		tl.add(0, 1);
		tl.add(1, 2);
		WekaMagic.printHashMap(results, tl, fileName + "_results_2.csv");

	}
}