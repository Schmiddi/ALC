import weka.core.Instances;

public class Science {
	final static String FolderWithDropbox = "C:\\Users\\IBM_ADMIN";
	//final static String FolderWithDropbox = "E:\\Downloads";
	
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

		Boolean Stopword = false; // True/False

		// stop.txt / germanST.txt
		// String list1 =
		// FolderWithDropbox+"\\Dropbox\\Detecting Alcohol Intoxication in Speech\\Felix\\stopwords\\stop.txt";
		String list1 = "resources\\germanST.txt";

		// parameters for Attribute Selection

		Boolean BinarizeNumericAttributes = true; // True/False
		double threshold = 0.007;

		/*
		 * 
		 * All parameter are set
		 */

		
		String[] fullPath = currDir.split("/");
		String fileName = fullPath[fullPath.length - 1];

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
		WekaMagic.saveToArff(dataRaw, fileName + "_raw_text", text_data);
		
		
		//split data to training & test data
		split = WekaMagic.separateTrainTest(dataRaw, 90, 1);
		train_split = split[0];
		test_split  = split[1];
		
		WekaMagic.saveToArff(train_split, fileName + "_raw_train", null);
		WekaMagic.saveToArff(test_split, fileName + "_raw_test", null);
		

		// Generate the features
		filtered = WekaMagic.generateFeatures(train_split,test_split, WordsToKeep, Ngram,
				ngram_min, ngram_max, LowerCase, NormalizeDocLength, Stemming,
				OutputWordCounts, IDFTransform, TFTransform, Stopword, list1);
		filtered.print();
		filtered_train = filtered.getTrainData();
		filtered_test = filtered.getTestData();
		
		// Store featured data from Weka to arff file
		WekaMagic.saveToArff(filtered_train, fileName + "_featured_train", filtered);
		WekaMagic.saveToArff(filtered_test, fileName + "_featured_test", null);
	

		// Run selection
		selected = WekaMagic.selectionByInfo(filtered_train,filtered_test,
				BinarizeNumericAttributes, threshold);
		selected.print();
		selected_train = selected.getTrainData();
		selected_test = selected.getTestData();

		// Backup the selection
		WekaMagic.saveToArff(selected_train, fileName + "_selected_train", selected);
		WekaMagic.saveToArff(selected_test, fileName + "_selected_test", selected);
		
		// Run ML algorithm - logistic
		logistic = WekaMagic.runLogistic(selected_train, selected_test);
		logistic.print();
		WekaMagic.saveToArff(null, fileName + "_logistic_t", logistic);
			
			

	}
}