package team2014.test;
import team2014.weka.MyClassificationOutput;
import team2014.weka.MyOutput;
import team2014.weka.WekaMagic;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Nachstellen {

	public static void main(String[] args) throws Exception {
		/**
		 * 
		 * Set parameter of input function
		 */

		// parameters for TextDirectoryLoader
		String currDir = "C:\\Users\\IBM_ADMIN\\Dropbox\\Detecting Alcohol Intoxication in Speech\\Felix\\Backup\\DP_real\\rawData\\DP";

		// parameters for StringToVector

		int WordsToKeep = 20000;

		Boolean Ngram = false;
		int ngram_min = 1;
		int ngram_max = 3;

		Boolean LowerCase = false;
		Boolean NormalizeDocLength = false;
		Boolean Stemming = false;

		Boolean OutputWordCounts = false;
		Boolean IDFTransform = false;
		Boolean TFTransform = false;

		Boolean Stopword = false;
		// String list1 =
		// "C:\\Users\\IBM_ADMIN\\Dropbox\\Detecting Alcohol Intoxication in Speech\\Felix\\stopwords\\stop.txt";
		String list1 = "C:\\Users\\IBM_ADMIN\\Dropbox\\Detecting Alcohol Intoxication in Speech\\Felix\\stopwords\\germanST.txt";

		// parameters for Attribute Selection

		Boolean BinarizeNumericAttributes = true;
		double threshold = 0.005;

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

		text_data = WekaMagic.loadText(currDir);
		text_data.print();
		dataRaw = text_data.getData();

		WekaMagic.saveToArff(dataRaw, fileName + "_raw_text", text_data);

		filtered = WekaMagic.generateFeatures(dataRaw, WordsToKeep, Ngram,
				ngram_min, ngram_max, LowerCase, NormalizeDocLength, Stemming,
				OutputWordCounts, IDFTransform, TFTransform, Stopword, list1,
				1); //achtung minterm
		filtered.print();
		dataFiltered = filtered.getData();
		dataFiltered.randomize(new Random(1));

		WekaMagic.saveToArff(dataFiltered, fileName + "_featured", filtered);

		List<List<Double>> results = new ArrayList<List<Double>>();

		for (t = 0; t <= 100; t++) {

			threshold = (double)t * (double)0.0001;

			selected = WekaMagic.selectionByInfo(dataFiltered,
					BinarizeNumericAttributes, threshold);
			selected.print();
			dataSelected = selected.getData();

			WekaMagic.saveToArff(dataSelected, fileName + "_selected_t" + t,
					selected);

			logistic = WekaMagic.runLogistic(dataSelected);
			logistic.print();
			WekaMagic.saveToArff(null, fileName + "_logistic_t" + t, logistic);

			// Result processing
			List<Double> al = new ArrayList<Double>();
			al.add(0,threshold);
			al.add(1,logistic.getUAR());
			al.add(2, (double)logistic.getElapsedTime());
			results.add(al);
		}
		WekaMagic.printHashMap(results, fileName + "_results.csv");

	}
}