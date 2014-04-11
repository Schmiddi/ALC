package team2014.test;
import team2014.weka.MyClassificationOutput;
import team2014.weka.MyOutput;
import team2014.weka.WekaMagic;
import team2014.weka.plot.GeneratesPlot;
import weka.core.Instances;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
		String inputFolder = "/home/bas-alc/test/rawData/separate_all/DP";
		//String inputFolder = "E:/Dropbox/Detecting Alcohol Intoxication in Speech/Felix/Backup/DP_real/rawData/DP";
        
		String[] fullPath = inputFolder.split("/");
		String fileName = fullPath[fullPath.length - 1];
		
		//String outputMainFolder = "E:/Dropbox/Detecting Alcohol Intoxication in Speech/Dennis";
		String outputMainFolder = "/home/bas-alc/TestResult";
		
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

								
								
								MineMulti.runMine(inputFolder, WordsToKeep,
										ngram_min, ngram_max, list1, threshold,
										settings, title, outputFolder);
							}
						}
					}
				}
			}
		}
	}

	public static void runMine(String currDir, int WordsToKeep, int ngram_min,
			int ngram_max, String list1, List<Double> threshold,
			Map<options, Boolean> settings, String title, String outputFolder) throws Exception {

		/*
		 * 
		 * All parameter are set
		 */

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
		WekaMagic.saveToArff(dataRaw, outputFolder + title + "_raw_text", text_data);

		// Generate the features
		filtered = WekaMagic.generateFeatures(dataRaw, WordsToKeep,
				settings.get(options.Ngram), ngram_min, ngram_max,
				settings.get(options.LowerCase),
				settings.get(options.NormalizeDocLength),
				settings.get(options.Stemming),
				settings.get(options.OutputWordCounts),
				settings.get(options.IDFTransform),
				settings.get(options.TFTransform),
				settings.get(options.Stopword), list1,
				1); //achtung minterm
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
			WekaMagic.saveToArff(dataSelected, outputFolder + title + "_" + currentThreshold + "_selected_t", selected);

			// Run ML algorithm - logistic
			logistic = WekaMagic.runLogistic(dataSelected);
			logistic.print();
			WekaMagic.saveToArff(null, outputFolder + title + "_" + currentThreshold + "_logistic_t", logistic);

			// Result processing
			List<Double> al = new ArrayList<Double>();
			al.add(0,currentThreshold);
			al.add(1,logistic.getUAR());
			al.add(2, (double)logistic.getElapsedTime());
			results.add(al);
		}
		// Store treshold, UAR
		WekaMagic.printHashMap(results, outputFolder + title + "_results_.csv");
		
		GeneratesPlot.create(results,outputFolder,title,"Threshold","UAR");
	}
}