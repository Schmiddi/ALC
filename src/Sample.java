import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import weka.core.Instances;

public class Sample {
	final static String FolderWithDropbox = "C:\\Users\\IBM_ADMIN";
	//final static String FolderWithDropbox = "E:\\Downloads";
	
	public static void main(String[] args) throws Exception {
		/**
		 * 
		 * Set parameter of input function
		 */

		// parameters for TextDirectoryLoader
		String currDir = null;

		if(args.length > 0)
		    currDir = args[0];
		else
		    currDir = FolderWithDropbox+"\\Dropbox\\Detecting Alcohol Intoxication in Speech\\Felix\\Backup\\DP_real\\rawData\\DP";

		// parameters for StringToVector
        List<Boolean> values = new ArrayList<Boolean>();
		values.add(true);
		values.add(false);
        List<Integer> ngrams = new ArrayList<Integer>();
		ngrams.add(2);
		ngrams.add(3);
		List<Integer> tristate = new ArrayList<Integer>();
		tristate.add(1);
		tristate.add(2);
		tristate.add(3);

		//int ngram_max = 3; // 2/3

		//Boolean NormalizeDocLength = true; // True/False
		//Boolean Stemming = true; // True/False

		Boolean IDFTransform = true; // True/False
		Boolean TFTransform = true; // True/False
		Boolean Stopword = false; // True/False

		// stop.txt / germanST.txt
		// String list1 =
		// FolderWithDropbox+"\\Dropbox\\Detecting Alcohol Intoxication in Speech\\Felix\\stopwords\\stop.txt";
		String list1 = "resources\\germanST.txt";
        String title = "";

        for(Boolean NDL: values){
            for(Boolean ST:values){
                for(Integer ngram:ngrams){
                    for(Integer SW:tristate){
                        for(Integer IDFTF: tristate){

                            title = "NDL-"+NDL+",ST-"+ST +","+ngram+"gram, SW-";
                            
                            if (SW == 1) {
                                Stopword =  true;
                                list1 = "resources\\germanST.txt";
                                title += "germanST";
                            } else if (SW == 2) {
                                Stopword = true;
                                list1 = "resources\\stop.txt";
                                title += "stop";
                            } else {
                                Stopword = false;
                                list1 = null;
                                title += "false";
                            }
                            
                            if (IDFTF == 1) {
                                IDFTransform = true;
                                TFTransform = true;
                                title += ",IDF-true, TFT-true";
                            } else if (IDFTF == 2) {
                                IDFTransform = false;
                                TFTransform = true;
                                title += ",IDF-false, TFT-true";

                            } else {
                                IDFTransform = false;
                                TFTransform = false;
                                title += ",IDF-false, TFT-false";
                            }
                            System.out.println("#####" + title+ "#####");
                            runSample(currDir,NDL,ST,ngram,Stopword,list1, IDFTransform, TFTransform);
                        }
                    }
                }
            }
        }
    }

    public static void runSample(String currDir ,Boolean NormalizeDocLength, Boolean Stemming,int  ngram_max,Boolean Stopword,String list1,Boolean IDFTransform, Boolean TFTransform) throws Exception{
        Boolean Ngram = true; // True/False
		int ngram_min = 1;

        Boolean OutputWordCounts = true; // True/False (necessary for
											// IDFTransform, TFTransform,
											// NormalizeDocLength)

        Boolean BinarizeNumericAttributes = true; // True/False
        double threshold = 0.04;
		int WordsToKeep = 1000000;
        Boolean LowerCase = true;
        int minTermFrequency = 2;
        int maxIterations = 5;
        Double regularization = (Double)null;
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
		Instances cross_validation_split;
		Instances test_split;
		
		MyOutput filtered;
		Instances filtered_train;
		Instances filtered_cross_validation;
		Instances filtered_test;
		
		MyOutput selected;
		Instances selected_train;
		Instances selected_cross_validation;
		Instances selected_test;
		
		MyClassificationOutput logistic_train;
		
		MyClassificationOutput logistic_train_eval;
		MyClassificationOutput logistic_cross_eval;
		MyClassificationOutput logistic_test_eval;

		// Load data to Weka
		text_data = WekaMagic.loadText(currDir);
		//text_data.print();
		dataRaw = text_data.getData();
		
		// Store raw data from Weka to arff file
//		WekaMagic.saveToArff(dataRaw, fileName + "_raw_text", text_data);
		
		
		//split data to training & test data
		//split = WekaMagic.separateInstances(dataRaw, new double[]{60,20,20}, 1);
		split = WekaMagic.getStratifiedSplits(dataRaw, 1);
		train_split 		   = split[0];
		cross_validation_split = split[1];
		test_split  		   = split[2];
		
		for(int i=0;i<dataRaw.numAttributes();i++){
			System.out.println(dataRaw.attribute(i).name());
		}
		
		System.out.println("raw: " + dataRaw.size() + " distr: " + WekaMagic.getDistribution(dataRaw, dataRaw.attribute("@@class@@"), "alc"));
		
		System.out.println("train: " + train_split.size() + " distr: " + WekaMagic.getDistribution(train_split, train_split.attribute("@@class@@"), "alc"));
		System.out.println("cross: " + cross_validation_split.size() + " distr: " + WekaMagic.getDistribution(cross_validation_split, cross_validation_split.attribute("@@class@@"), "alc"));
		System.out.println("test: " + test_split.size() + " distr: " + WekaMagic.getDistribution(test_split, test_split.attribute("@@class@@"), "alc"));
		
		
		
		//WekaMagic.saveToArff(train_split, fileName + "_raw_train", null);
		//WekaMagic.saveToArff(test_split, fileName + "_raw_test", null);
		
		// Generate the features
		filtered = WekaMagic.generateFeatures(train_split, WordsToKeep, Ngram,
				ngram_min, ngram_max, LowerCase, NormalizeDocLength, Stemming,
				OutputWordCounts, IDFTransform, TFTransform, Stopword, list1,
				minTermFrequency ); //achtung minterm
		
		
		filtered.print();
		
		filtered_train 				= filtered.getTrainData();
		filtered_cross_validation 	= WekaMagic.applyFilter(cross_validation_split, filtered);
		filtered_test 				= WekaMagic.applyFilter(test_split, filtered);
		
		
		// Store featured data from Weka to arff file
//		WekaMagic.saveToArff(filtered_train, fileName + "_featured_train", filtered);
//		WekaMagic.saveToArff(filtered_test, fileName + "_featured_test", null);
	

		// Run selection
		selected = WekaMagic.selectionByInfo(filtered_train,
				BinarizeNumericAttributes, threshold);
		selected.print();

		selected_train 			  = selected.getTrainData();
		selected_cross_validation = WekaMagic.applyFilter(filtered_cross_validation, selected);
		selected_test             = WekaMagic.applyFilter(filtered_test, selected);

		// Backup the selection
//		WekaMagic.saveToArff(selected_train, fileName + "_selected_train", selected);
//		WekaMagic.saveToArff(selected_test, fileName + "_selected_test", selected);
		
		// Run ML algorithm - logistic
		logistic_train = WekaMagic.runLogistic(filtered_train, regularization, maxIterations);
		logistic_train.print();
//		WekaMagic.saveToArff(null, fileName + "_logistic_t", logistic);
		
		logistic_train_eval = WekaMagic.applyLogistic(filtered_train, logistic_train);
		logistic_cross_eval = WekaMagic.applyLogistic(filtered_cross_validation, logistic_train);
		logistic_test_eval  = WekaMagic.applyLogistic(filtered_test, logistic_train);
		
		System.out.println("f1-score - training: " + logistic_train_eval.getF1Score());
		System.out.println("f1-score - cross:    " + logistic_cross_eval.getF1Score());
		System.out.println("f1-score - test:     " + logistic_test_eval.getF1Score());
	}
}
