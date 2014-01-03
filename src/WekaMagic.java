import java.io.*;
import java.util.List;
import java.util.Random;
import java.util.Locale;

import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import weka.core.Instances;
import weka.core.converters.TextDirectoryLoader;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.unsupervised.attribute.StringToWordVector;
import weka.core.stemmers.*;
import weka.core.tokenizers.*;
import weka.core.SelectedTag;
import weka.classifiers.functions.Logistic;
import weka.classifiers.Evaluation;
import weka.filters.Filter;

/**
 * @author Felix Neutatz
 *
 */

/**
 * @author Felix Neutatz
 *
 */
public class WekaMagic {
	
	/**
	 * load text files from data folder to get Weka instances
	 * 			the subfolders correspond to classes (in this case alc/nonalc)
	 * 
	 * @param currDir = path of the data folder
	 * @return
	 * @throws Exception
	 */
	public static MyOutput loadText(String currDir) throws Exception {
		TextDirectoryLoader loader = new TextDirectoryLoader();
		loader.setDirectory(new File(currDir));

		long startTime = System.currentTimeMillis();
		Instances dataRaw = loader.getDataSet();
		long stopTime = System.currentTimeMillis();
		long elapsedTime = stopTime - startTime;
		
		setClassIndex(dataRaw);
		
		return new MyOutput(dataRaw, loader, elapsedTime);
	}
	
	/**
	 * This function makes sure that the class column is always set correctly
	 * -> is not really necessary, but nice to have
	 * 
	 * @param data
	 */
	public static void setClassIndex(Instances data){
		int i;
		for(i=0;i<data.numAttributes();i++){
			if(data.attribute(i).toString().contains("alc,nonalc")){
				data.setClassIndex(i);
				break;
			}
		}
	}
	
	/**
	 * split training set from test set to get a scientific result
	 * 
	 * @param data      = all given instances
	 * @param percent   = size of the training set in percent
	 * @param randseed  = random integer number to define randomness
	 * @return
	 */
	public static Instances[] separateTrainTest(Instances data, double percent, int randseed){
		Instances [] sets = new Instances[2];
		
		//Randomize all given instances
		data.randomize(new Random(randseed));
		
		// Percent split
		int fnum = data.numInstances();
		int trainSize = (int) Math.round(fnum * percent / 100);
		int testSize = fnum - trainSize;
		
		sets[0] = new Instances(data, 0, trainSize);          //train
		sets[1] = new Instances(data, trainSize, testSize);   //test
		
		setClassIndex(sets[0]);
		setClassIndex(sets[1]);
		
		return sets;			
	}
	
	
	
	/**
	 * Returns a part (percentage) of a set of instances
	 * 
	 * @param data - original set
	 * @param percent - percentage
	 * @param randseed - random factor
	 * @return
	 */
	public static Instances getPartOf(Instances data, double percent, int randseed){
		Instances set;
		
		//Randomize all given instances
		data.randomize(new Random(randseed));
		
		// Percent split
		int fnum = data.numInstances();
		int trainSize = (int) Math.round(fnum * percent / 100);
		
		set = new Instances(data, 0, trainSize);      
		setClassIndex(set);
		
		return set;			
	}
	
	
	/**
	 * split training set from test set to get a scientific result
	 * 
	 * 		default: 
	 * 					60% - training size
	 * 					20% - cross validation size
	 * 					20% - test size
	 * 
	 * @param data      = all given instances
	 * @param percent   = size of the training set in percent
	 * @param randseed  = random integer number to define randomness
	 * @return
	 */
	public static Instances[] separateInstances(Instances data, double [] percent, int randseed){
		Instances [] sets = new Instances[percent.length];
		
		//Randomize all given instances
		data.randomize(new Random(randseed));
				
		// Percent split
		int fnum = data.numInstances();
		
		
		int start_point=0;
		int number_to_copy;
		
		for(int i=0;i<percent.length;i++){		
			number_to_copy = (int) Math.round(fnum * percent[i] / 100);
			sets[i] = new Instances(data, start_point, number_to_copy);
			setClassIndex(sets[i]);
			start_point += number_to_copy;
		}
		
		return sets;			
	}
	
	

	
	/**
	 * generate features from pure text data
	 * 
	 * @param dataRaw      		  = instances which have been loaded from the data folder by loadText()
	 * @param WordsToKeep  		  = numbers of words to keep (kept feature number)
	 * @param Ngram        		  = n-gram will be created (true/false) 
	 * @param ngram_min	        		= minimum degree of n-gram (1,2,3) default=1
	 * @param ngram_max					= maximum degree of n-gram (1,2,3) default=3
	 * @param LowerCase    		  = all words will be transformed to lower case format
	 * @param NormalizeDocLength  = normalize word count dependent on the document length
	 * @param Stemming			  = use german snowball stemming (true/false)
	 * @param OutputWordCounts    = count the occurance of words (true/false)
	 * @param IDFTransform		  = ??? (true/false)
	 * @param TFTransform 		  = ??? (true/false)
	 * @param Stopword   		  = use a stop word list (true/false)
	 * @param list					   = path to the stop word list text file
	 * @return
	 * @throws Exception
	 */
	public static MyOutput generateFeatures(Instances dataRaw, int WordsToKeep,
			Boolean Ngram, int ngram_min, int ngram_max, Boolean LowerCase,
			Boolean NormalizeDocLength, Boolean Stemming,
			Boolean OutputWordCounts, Boolean IDFTransform,
			Boolean TFTransform, Boolean Stopword, String list)
			throws Exception {
		
			
				return generateFeatures(dataRaw, null, WordsToKeep,
						Ngram, ngram_min, ngram_max, LowerCase,
						NormalizeDocLength, Stemming,
						OutputWordCounts, IDFTransform,
						TFTransform, Stopword, list);
	}
	
	/**
	 * generate features from pure text data
	 * 
	 * @param train      		  = instances of the training set
	 * @param test      		  = instances of the test set
	 * @param WordsToKeep  		  = numbers of words to keep (kept feature number)
	 * @param Ngram        		  = n-gram will be created (true/false) 
	 * @param ngram_min	        		= minimum degree of n-gram (1,2,3) default=1
	 * @param ngram_max					= maximum degree of n-gram (1,2,3) default=3
	 * @param LowerCase    		  = all words will be transformed to lower case format
	 * @param NormalizeDocLength  = normalize word count dependent on the document length
	 * @param Stemming			  = use german snowball stemming (true/false)
	 * @param OutputWordCounts    = count the occurance of words (true/false)
	 * @param IDFTransform		  = ??? (true/false)
	 * @param TFTransform 		  = ??? (true/false)
	 * @param Stopword   		  = use a stop word list (true/false)
	 * @param list					   = path to the stop word list text file
	 * @return
	 * @throws Exception
	 */
	public static MyOutput generateFeatures(Instances train, Instances test, int WordsToKeep,
			Boolean Ngram, int ngram_min, int ngram_max, Boolean LowerCase,
			Boolean NormalizeDocLength, Boolean Stemming,
			Boolean OutputWordCounts, Boolean IDFTransform,
			Boolean TFTransform, Boolean Stopword, String list)
			throws Exception {
		if (IDFTransform || TFTransform || NormalizeDocLength)
			OutputWordCounts = true;

		StringToWordVector filter = new StringToWordVector();
		filter.setWordsToKeep(WordsToKeep);

		filter.setIDFTransform(IDFTransform);
		filter.setTFTransform(TFTransform);
		filter.setLowerCaseTokens(LowerCase);
		filter.setOutputWordCounts(OutputWordCounts);

		if (NormalizeDocLength) {
			SelectedTag tag = new SelectedTag(
					StringToWordVector.FILTER_NORMALIZE_ALL,
					StringToWordVector.TAGS_FILTER);
			filter.setNormalizeDocLength(tag);
		}

		if (Stemming) {
			SnowballStemmer st = new SnowballStemmer();
			st.setStemmer("german");
			filter.setStemmer(st);
		}

		Tokenizer t = new WordTokenizer();

		if (Ngram) {
			NGramTokenizer nt = new NGramTokenizer();
			nt.setNGramMinSize(ngram_min);
			nt.setNGramMaxSize(ngram_max);

			t = nt;
		}

		filter.setTokenizer(t);

		filter.setUseStoplist(Stopword);
		if (Stopword) {
			File f = new File(list);
			filter.setStopwords(f);
		}

		filter.setInputFormat(train);

		long startTime = System.currentTimeMillis();
		Instances train_dataFiltered = Filter.useFilter(train, filter); //run filter on training data
		long stopTime = System.currentTimeMillis();
		long elapsedTime = stopTime - startTime;
		
		Instances test_dataFiltered;
		if(test != null){
			test_dataFiltered = Filter.useFilter(test, filter);   //run filter on test data
			setClassIndex(test_dataFiltered);
		}
		else{
			test_dataFiltered = null;
		}
		
		setClassIndex(train_dataFiltered);
		
		Instances [] dataFiltered = iToArray(train_dataFiltered,test_dataFiltered);

		return new MyOutput(dataFiltered, filter, elapsedTime);
	}
	
	
	
	/**
	 * Apply filter on test instances
	 * @param data
	 * @param filter
	 * @return
	 * @throws Exception
	 */
	public static Instances applyFilter(Instances data, MyOutput filter) throws Exception{
		Instances test_dataFiltered = Filter.useFilter(data, (Filter) filter.getOperation());   //run filter on test data
		setClassIndex(test_dataFiltered);
		return test_dataFiltered;
	}
	
	
	/**
	 * convert single instances to array
	 * 
	 * @param train = instances of the training set
	 * @param test  = instances of the test set
	 * @return
	 */
	public static Instances [] iToArray(Instances train, Instances test){
		Instances [] data = new Instances[2];
		data[0] = train;
		data[1] = test;
		return data;
	}
	
	
	/**
	 * @param data      		    		= instances which have been processed by generateFeatures()
	 * @param BinarizeNumericAttributes     = ??? (true/false) // set always to true
	 * @param threshold						= how much information gain does a feature need to be kept 
	 * 										  (0 - 0.01)
	 * @return
	 * @throws Exception
	 */
	public static MyOutput selectionByInfo(Instances data,
			Boolean BinarizeNumericAttributes, double threshold)
			throws Exception {
		return selectionByInfo(data, null,
				BinarizeNumericAttributes, threshold);
	}
	
	/**
	 * @param data      		    		= instances which have been processed by generateFeatures()
	 * @param BinarizeNumericAttributes     = ??? (true/false)  // set always to true
	 * @param threshold						= how much information gain does a feature need to be kept 
	 * 										  (0 - 0.01)
	 * @return
	 * @throws Exception
	 */
	public static MyOutput selectionByInfo(Instances train, Instances test,
			Boolean BinarizeNumericAttributes, double threshold)
			throws Exception {
		AttributeSelection as = new AttributeSelection();
		as.setInputFormat(train);

		InfoGainAttributeEval eval = new InfoGainAttributeEval();
		eval.setBinarizeNumericAttributes(BinarizeNumericAttributes);

		as.setEvaluator(eval);

		Ranker r = new Ranker();
		r.setGenerateRanking(true);
		r.setThreshold(threshold);

		as.setSearch(r);

		long startTime = System.currentTimeMillis();
		Instances train_selected = Filter.useFilter(train, as);
		long stopTime = System.currentTimeMillis();
		long elapsedTime = stopTime - startTime;
		
		Instances test_selected;
		if(test != null){
			test_selected = Filter.useFilter(test, as);   //run filter on test data
			setClassIndex(test_selected);
		}
		else{
			test_selected = null;
		}
		
		setClassIndex(train_selected);
		
		Instances [] selected = iToArray(train_selected,test_selected);

		return new MyOutput(selected, as, elapsedTime);
	}
	
	/**
	 * run Logistic Regression
	 * 
	 * @param data = instances which have been processed by selectionByX()
	 * @return
	 * @throws Exception
	 */
	public static MyClassificationOutput runLogistic(Instances data)
			throws Exception {
		return runLogistic(data, (Instances)null);
	}
	
	
	/**
	 * Run logistic regression and get immediately the evaluation of the classifier for a certain test set
	 * 
	 * 		ridge / regularization parameter is set to default!
	 * 
	 * @param train - training set
	 * @param test  - test set
	 * @return
	 * @throws Exception
	 */
	public static MyClassificationOutput runLogistic(Instances train, Instances test)
			throws Exception {
		
		Logistic l = new Logistic();
		long elapsedTime;
		Evaluation eval;
		String options;
		
		if(test == null){

			int folds = 10;
			int seed = 1;
			
			options = "-cv -x " + folds + " -s " + seed;
	
			eval = new Evaluation(train);
	
			long startTime = System.currentTimeMillis();
			eval.crossValidateModel(l, train, folds, new Random(seed));
			long stopTime = System.currentTimeMillis();
			elapsedTime = stopTime - startTime;
		}
		else{	
			options = "single run";
			
			long startTime = System.currentTimeMillis();
			l.buildClassifier(train);
			long stopTime = System.currentTimeMillis();
			elapsedTime = stopTime - startTime;
			
			eval = new Evaluation(test);
			eval.evaluateModel(l,test);
		}
				
		return new MyClassificationOutput(l, eval, options, elapsedTime);
	}
	
	
	/**
	 * Run logistic regression and get only the evaluation of training set
	 * 		--> please run the classifier afterwards on the cross-validation set and on the test set!!
	 * 
	 * @param train - training set
	 * @param ridge - regularization parameter
	 * @return
	 * @throws Exception
	 */
	public static MyClassificationOutput runLogistic(Instances train, Double ridge)
			throws Exception {
		
		Logistic l = new Logistic();
		long elapsedTime;
		Evaluation eval;
		String options;
		
		//set regularization parameter
		if(ridge != null){
			l.setRidge(ridge);
		}
		
		options = "single run";	
		
		//build classifier
		long startTime = System.currentTimeMillis();
		l.buildClassifier(train);
		long stopTime = System.currentTimeMillis();
		elapsedTime = stopTime - startTime;
		
		//evaluate how good it performes on the training set
		eval = new Evaluation(train);
		eval.evaluateModel(l,train);
		
				
		return new MyClassificationOutput(l, eval, options, elapsedTime);
	}

	
	/**
	 * Apply Logoistic regression classifier on a certain test set
	 * 
	 * @param test - test set
	 * @param classifier - output object of runLogistic
	 * @return
	 * @throws Exception
	 */
	public static MyClassificationOutput applyLogistic(Instances test, MyClassificationOutput classifier)
	throws Exception {

		Logistic l = (Logistic) classifier.getClassifier();
		Evaluation eval;
		String options;
		
		options = "single run";	
		
		//evaluate how good it performes on the test set
		eval = new Evaluation(test);
		eval.evaluateModel(l,test);	
				
		return new MyClassificationOutput(l, eval, options, 0);
	}
	
	
	/**
	 * save current instances and their description (how to build these instances)
	 * @param data      		= current instances
	 * @param path				= name of the output file (without extension)
	 * 									-> it will create two files:
	 * 										filename.arff (weka output format) &
	 * 										filename_desc.txt (description about the instances)
	 * @param m					= MyOutput / MyClassificationOutput
	 * @throws Exception
	 */
	public static void saveToArff(Instances data, String path, Object m)
			throws Exception {
		FileWriter fstream;
		BufferedWriter out;

		if (data != null) {
			String arff = path + ".arff";
			fstream = new FileWriter(arff);
			out = new BufferedWriter(fstream);
			out.write(data.toString());
			out.close();
			System.out.println(arff + " created");
		}

		if (m != null) {
			String desc = path + "_desc.txt";
			fstream = new FileWriter(desc);
			out = new BufferedWriter(fstream);
			out.write(m.toString());
			out.close();
		}
	}

	/**
	 * print results to csv file
	 * 
	 * @param m    =  List of List of double numbers
	 * @param path =  path to csv file
	 * @throws Exception
	 */
	public static void printHashMap(List<List<Double>> m, String path)
			throws Exception {
		FileWriter fstream;
		BufferedWriter out;
		int i;

		fstream = new FileWriter(path);
		out = new BufferedWriter(fstream);

		for (List<Double> dataset : m) {
			i=0;
			for(Double d : dataset){
				if(i>0) out.write(",");
				out.write("\"" + String.format(Locale.US, "%1$.4f", d)
					+ "\"");
				i++;
			}
			out.write("\n");			
		}
		out.close();
	}

	public static void main(String[] args) throws Exception {
		/**
		 * 
		 * Set parameter of input function
		 */

		// parameters for TextDirectoryLoader
		String currDir = "C:\\Users\\IBM_ADMIN\\Dropbox\\Detecting Alcohol Intoxication in Speech\\Felix\\Backup\\DP_real\\rawData\\DP";

		// parameters for StringToVector

		int WordsToKeep = 1000000;

		Boolean Ngram = true;
		int ngram_min = 1;
		int ngram_max = 3;

		Boolean LowerCase = true;
		Boolean NormalizeDocLength = true;
		Boolean Stemming = true;

		Boolean OutputWordCounts = true;
		Boolean IDFTransform = true;
		Boolean TFTransform = true;

		Boolean Stopword = true;
		// String list1 =
		// "C:\\Users\\IBM_ADMIN\\Dropbox\\Detecting Alcohol Intoxication in
		// Speech\\Felix\\stopwords\\stop.txt";
		String list1 = "C:\\Users\\IBM_ADMIN\\Dropbox\\Detecting Alcohol Intoxication in Speech\\Felix\\stopwords\\germanST.txt";

		// parameters for Attribute Selection

		Boolean BinarizeNumericAttributes = true;
		double threshold = 0.004;

		/*
		 * 
		 * All parameter are set
		 */

		String[] fullPath = currDir.split("/");
		String fileName = fullPath[fullPath.length - 1];

		MyOutput text_data = loadText(currDir);
		text_data.print();
		Instances dataRaw = text_data.getData();

		saveToArff(dataRaw, fileName + "_raw_text", text_data);

		MyOutput filtered = generateFeatures(dataRaw, WordsToKeep, Ngram,
				ngram_min, ngram_max, LowerCase, NormalizeDocLength, Stemming,
				OutputWordCounts, IDFTransform, TFTransform, Stopword, list1);
		filtered.print();
		Instances dataFiltered = filtered.getData();

		saveToArff(dataFiltered, fileName + "_featured", filtered);

		MyOutput selected = selectionByInfo(dataFiltered,
				BinarizeNumericAttributes, threshold);
		selected.print();
		Instances dataSelected = selected.getData();

		saveToArff(dataSelected, fileName + "_selected", selected);

		MyClassificationOutput logistic = runLogistic(dataSelected);
		logistic.print();
		saveToArff(null, fileName + "_logistic", logistic);
	}
}