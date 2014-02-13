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

        int WordsToKeep = 1000000;
        Boolean LowerCase = true;
        int minTermFrequency = 2;
        int maxIterations = 5;
        Double regularization = (Double)null;
        /*
		 * 
		 * All parameter are set
		 */

		
		MyOutput text_data;
		Instances dataRaw;
		
		MyOutput filtered;
		MyClassificationOutput logistic_train;
		

		// Load data to Weka
		text_data = WekaMagic.loadText(currDir);
		dataRaw = text_data.getData();
		
        // Generate the features
		filtered = WekaMagic.generateFeatures(null, WordsToKeep, Ngram,
				ngram_min, ngram_max, LowerCase, NormalizeDocLength, Stemming,
				OutputWordCounts, IDFTransform, TFTransform, Stopword, list1,
				minTermFrequency ); //achtung minterm
		
		
		
		// Run ML algorithm - logistic
		logistic_train = WekaMagic.runLogistic(null, regularization, maxIterations);
		
        ArrayList<MyOutput> filter = new ArrayList<MyOutput>();
        filter.add(filtered);
		CrossValidationOutput cvo = WekaMagic.crossValidation(logistic_train, dataRaw, 10, 1, filter);
	    System.out.println(cvo.getTrainF1Score());
        System.out.println(cvo.getTestF1Score());
    }
}
