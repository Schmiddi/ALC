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
		
		return new MyOutput(dataRaw, loader, elapsedTime);
	}
	
	/**
	 * split training set from test set to get a scientific result
	 * 
	 * @param data      = all given instances
	 * @param percent   = size of the training set in percent
	 * @param randseed  = random integer number to define randomness
	 * @return
	 */
	public Instances[] separateTrainTest(Instances data, float percent, int randseed){
		Instances [] sets = new Instances[2];
		
		//Randomize all given instances
		data.randomize(new Random(randseed));
		
		// Percent split
		int fnum = data.numInstances();
		int trainSize = (int) Math.round(fnum * percent / 100);
		int testSize = fnum - trainSize;
		
		sets[0] = new Instances(data, 0, trainSize);          //train
		sets[1] = new Instances(data, trainSize, testSize);   //test
		
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

		filter.setInputFormat(dataRaw);

		long startTime = System.currentTimeMillis();
		Instances dataFiltered = Filter.useFilter(dataRaw, filter);
		long stopTime = System.currentTimeMillis();
		long elapsedTime = stopTime - startTime;

		return new MyOutput(dataFiltered, filter, elapsedTime);
	}
	
	
	/**
	 * @param data      		    		= instances which have been processed by generateFeatures()
	 * @param BinarizeNumericAttributes     = ??? (true/false) 
	 * @param threshold						= how much information gain does a feature need to be kept 
	 * 										  (0 - 0.01)
	 * @return
	 * @throws Exception
	 */
	public static MyOutput selectionByInfo(Instances data,
			Boolean BinarizeNumericAttributes, double threshold)
			throws Exception {
		AttributeSelection as = new AttributeSelection();
		as.setInputFormat(data);

		InfoGainAttributeEval eval = new InfoGainAttributeEval();
		eval.setBinarizeNumericAttributes(BinarizeNumericAttributes);

		as.setEvaluator(eval);

		Ranker r = new Ranker();
		r.setGenerateRanking(true);
		r.setThreshold(threshold);

		as.setSearch(r);

		long startTime = System.currentTimeMillis();
		Instances selected = Filter.useFilter(data, as);
		long stopTime = System.currentTimeMillis();
		long elapsedTime = stopTime - startTime;

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
		Logistic l = new Logistic();

		int folds = 10;
		int seed = 1;

		Evaluation eval = new Evaluation(data);

		long startTime = System.currentTimeMillis();
		eval.crossValidateModel(l, data, folds, new Random(seed));
		long stopTime = System.currentTimeMillis();
		long elapsedTime = stopTime - startTime;

		String options = "-cv -x " + folds + " -s " + seed;

		return new MyClassificationOutput(l, eval, options, elapsedTime);
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