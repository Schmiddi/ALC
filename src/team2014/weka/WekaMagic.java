package team2014.weka;


import java.io.*;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Locale;
import java.util.StringTokenizer;

import team2014.weka.svm.KernelType;

import weka.core.stemmers.SnowballStemmer;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import weka.core.FastVector;
import weka.core.Instances;
import weka.core.converters.CSVLoader;
import weka.core.converters.TextDirectoryLoader;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.unsupervised.attribute.StringToWordVector;
import weka.core.tokenizers.*;
import weka.core.SelectedTag;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.Logistic;
import weka.classifiers.Evaluation;
import weka.filters.Filter;
import weka.core.Attribute;
import weka.classifiers.Classifier;
import weka.filters.supervised.instance.SMOTE;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.lazy.IBk;
import weka.filters.unsupervised.attribute.Normalize;
import org.languagetool.JLanguageTool;
import org.languagetool.rules.RuleMatch;
import org.languagetool.language.*;

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
	public static int setClassIndex(Instances data){
		int i=-1;
		for(i=0;i<data.numAttributes();i++){
			if(data.attribute(i).isNominal() && 
				(		data.attribute(i).name().equals("class") ||
						data.attribute(i).toString().contains("alc,nonalc") || data.attribute(i).toString().contains("nonalc,alc"))){
				data.setClassIndex(i);
				break;
			}
		}
		return i;
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
	
	public static Instances[] getStratifiedSplits(Instances data, int randseed){
		Instances [] sets = new Instances[3];
		int numFolds = 5;
		
		Instances runInstances = new Instances(data);
	    Random random = new Random(randseed);
	    runInstances.randomize(random);
	    if (runInstances.classAttribute().isNominal() && (numFolds > 1)) {
	      runInstances.stratify(numFolds);
	    }
	    
	    Instances test  = runInstances.testCV(numFolds, 0);
	    Instances cross = runInstances.testCV(numFolds, 1);	
	    Instances train = runInstances.testCV(numFolds, 2);
	    
	    for(int fold=3;fold<numFolds;fold++){
	    	train.addAll(runInstances.testCV(numFolds, fold));
	    }	
	    
	    sets[0] = train;
	    sets[1] = cross;
	    sets[2] = test;
		
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
			Boolean TFTransform, Boolean Stopword, String list, int MinTermFreq)
			throws Exception {
		
			
				return generateFeatures(dataRaw, null, WordsToKeep,
						Ngram, ngram_min, ngram_max, LowerCase,
						NormalizeDocLength, Stemming,
						OutputWordCounts, IDFTransform,
						TFTransform, Stopword, list, MinTermFreq);
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
			Boolean TFTransform, Boolean Stopword, String list, int MinTermFreq)
			throws Exception {
		
		StringToWordVector filter = new StringToWordVector();
		filter.setWordsToKeep(WordsToKeep);

		filter.setIDFTransform(IDFTransform);
		filter.setTFTransform(TFTransform);
		filter.setLowerCaseTokens(LowerCase);
		
		filter.setOutputWordCounts(OutputWordCounts);
		filter.setMinTermFreq(MinTermFreq);
		
		int  [] attributes = new int[1];
        if(train != null){
		    attributes[0] =	train.attribute("text").index(); //eventuell +1 ???
            filter.setAttributeIndicesArray(attributes);
        }

		if (NormalizeDocLength) {
			SelectedTag tag = new SelectedTag(
					StringToWordVector.FILTER_NORMALIZE_ALL,
					StringToWordVector.TAGS_FILTER);
			filter.setNormalizeDocLength(tag);
		}

		if (Stemming) {
			SnowballStemmer st = new SnowballStemmer();
			do{
				//wait until the German stemmer is initialized
			}while(!st.stemmerTipText().contains("german"));
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

		if(train != null){
			filter.setInputFormat(train);
		}

		long startTime = System.currentTimeMillis();
		Instances train_dataFiltered = null;
		if(train != null){
			train_dataFiltered = Filter.useFilter(train, filter); //run filter on training data
		}
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
	
        if(train != null)
		    setClassIndex(train_dataFiltered);
		
		Instances [] dataFiltered = iToArray(train_dataFiltered,test_dataFiltered);

		return new MyOutput(dataFiltered, filter, elapsedTime);
	}
	
	
	/**
	 * this function normalize data in a range from [0,1]
	 * 
	 * @param train
	 * @param test
	 * @return
	 * @throws Exception
	 */
	public static MyOutput normalize(Instances train, Instances test)
			throws Exception {
		
		Normalize filter = new Normalize();
		
		if(train != null){
			filter.setInputFormat(train);
		}

		long startTime = System.currentTimeMillis();
		Instances train_dataFiltered = null;
		if(train != null){
			train_dataFiltered = Filter.useFilter(train, filter); //run filter on training data
		}
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
	
		
		Instances [] dataFiltered = iToArray(train_dataFiltered,test_dataFiltered);

		return new MyOutput(dataFiltered, filter, elapsedTime);
	}
	
	
	public static MyOutput smote(Instances train, Instances test, int kNN, double percent)
	throws Exception {

		SMOTE filter = new SMOTE();
		
		filter.setNearestNeighbors(kNN);
		filter.setPercentage(percent);
		filter.setRandomSeed(1);
		
		if(train != null){
			filter.setInputFormat(train);
		}
		
		long startTime = System.currentTimeMillis();
		Instances train_dataFiltered = null;
		if(train != null){
			train_dataFiltered = Filter.useFilter(train, filter); //run filter on training data
		}
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
	private static Instances [] iToArray(Instances train, Instances test){
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
		
		InfoGainAttributeEval eval = new InfoGainAttributeEval();
		eval.setBinarizeNumericAttributes(BinarizeNumericAttributes);

		as.setEvaluator(eval);

		Ranker r = new Ranker();
		r.setGenerateRanking(true);
		r.setThreshold(threshold);

		as.setSearch(r);
		
		
		Instances train_selected = null;
		long elapsedTime = -1;
		if(train != null){
			as.setInputFormat(train);
	
			long startTime = System.currentTimeMillis();
			train_selected = Filter.useFilter(train, as);
			long stopTime = System.currentTimeMillis();
			elapsedTime = stopTime - startTime;
			setClassIndex(train_selected);
		}
		
		Instances test_selected;
		if(test != null){
			test_selected = Filter.useFilter(test, as);   //run filter on test data
			setClassIndex(test_selected);
		}
		else{
			test_selected = null;
		}
		
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
	public static MyClassificationOutput runLogistic(Instances train, Double ridge, Integer maxIterations)
			throws Exception {
		
		Logistic l = new Logistic();
		long elapsedTime;
		Evaluation eval = null;
		String options;
		
		//set regularization parameter
		if(ridge != null){
			l.setRidge(ridge);
		}
		if(maxIterations != null){
			l.setMaxIts(maxIterations);
		}
		
		options = "single run";	
		
		//build classifier
		long startTime = System.currentTimeMillis();
		if(train != null){
			l.buildClassifier(train);
		}
		long stopTime = System.currentTimeMillis();
		elapsedTime = stopTime - startTime;
		
		//evaluate how good it performes on the training set
		if(train != null){
			eval = new Evaluation(train);
			eval.evaluateModel(l,train);
		}
				
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
	public static MyClassificationOutput applyClassifier(Instances test, MyClassificationOutput classifier)
	throws Exception {

		Evaluation eval;
		String options;
		
		options = "single run";	
		
		//evaluate how good it performes on the test set
		eval = new Evaluation(test);
		eval.evaluateModel((Classifier)classifier.getClassifier(),test);	
				
		return new MyClassificationOutput(classifier.getClassifier(), eval, options, 0);
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
				out.write("\"" + String.format(Locale.US, "%1$.10f", d)
					+ "\"");
				i++;
			}
			out.write("\n");			
		}
		out.close();
	}
	
	/**
	 * print results to csv file
	 * 
	 * @param m    =  List of List of double numbers
	 * @param header = list containing the column titels
	 * @param path =  path to csv file
	 * @throws Exception
	 */
	public static void printHashMap(List<List<Double>> m, List<String> header, String path, String [] intro)
			throws Exception {
		FileWriter fstream;
		BufferedWriter out;
		int i;

		fstream = new FileWriter(path);
		out = new BufferedWriter(fstream);


		// Write intro
		i=0;
		for(String h: intro)
			out.write(h + "\n");	
				
		// Write header
		i=0;
		for(String h: header){
			if(i>0) out.write(",");
			out.write("\"" + h + "\"");
			i++;
		}
		out.write("\n");	
		
		// Write dataset
		for (List<Double> dataset : m) {
			i=0;
			for(Double d : dataset){
				if(i>0) out.write(",");
				out.write("\"" + String.format(Locale.US, "%1$.10f", d)
					+ "\"");
				i++;
			}
			out.write("\n");			
		}
		out.close();
	}
	/**
	 * @param a - Text feature instances 
	 * @param b - Sound feature instances
	 * @param AttributeName - Attribute name for the key column - name of the wav file for each instance
	 * @return
	 * @throws Exception
	 */
	public static Instances mergeInstancesBy(Instances a, Instances b, String AttributeName) throws Exception{
		
		//prevent null pointer exceptions when one or more of the merging sets are null
		if(a == null){
			if(b == null){ //both are null
				return null;
			}
			else{ //only a is null
				return b;
			}
		}
		else{
			if(b==null){ //only b is null
				return a;
			}
		}
		
		Instances merged = new Instances(a);
		Instances a_new,b_new;
		int i,o,u;
		Attribute akey=null,bkey=null;
		
		for(i=0;i<a.numAttributes();i++){
			if(a.attribute(i).name().equals(AttributeName)){
				akey = a.attribute(i);
				break;
			}
		}
	
		for(i=0;i<b.numAttributes();i++){
			if(b.attribute(i).name().equals(AttributeName)){ 
				bkey = b.attribute(i);
				break;
			}
		}
		
		a_new = new Instances(a);
		b_new = new Instances(b);
		
		b_new.renameAttribute(bkey, "bfile");
		
		
		for(i=0;i<b_new.numAttributes();i++){
			a_new.insertAttributeAt(b_new.attribute(i), a_new.numAttributes());
		}
		
		for(i=0;i<a_new.size();i++){
			for(o=0;o<b_new.size();o++){
				if(a_new.get(i).stringValue(akey).equals(b_new.get(o).stringValue(b_new.attribute("bfile")))){
					for(u=0;u<b_new.numAttributes();u++){
						if(b_new.attribute(u).isNominal() || b_new.attribute(u).isString()){
							a_new.get(i).setValue(a_new.attribute(b_new.attribute(u).name()),
								b_new.get(o).stringValue(b_new.attribute(b_new.attribute(u).name())));
						}else{
							a_new.get(i).setValue(a_new.attribute(b_new.attribute(u).name()),
									b_new.get(o).value(b_new.attribute(b_new.attribute(u).name())));
						}
					}
					break;
				}
			}
		}
		
		merged = a_new;
		
		//merged.deleteAttributeAt(merged.attribute(AttributeName).index());
		merged.deleteAttributeAt(merged.attribute("bfile").index());
		
		setClassIndex(merged);
		
		/*
		//if the attribute only has one value -> this column doesn't make any sense at all
		for(i=0;i<merged.numAttributes();i++){
			if(merged.numDistinctValues(i) == 1){
				merged.deleteAttributeAt(i);
			}
		}
		*/
		
		
		return merged;
	}
	
	
public static Instances fastmergeInstancesBy(Instances a, Instances b, String AttributeName) throws Exception{
		
		//prevent null pointer exceptions when one or more of the merging sets are null
		if(a == null){
			if(b == null){ //both are null
				return null;
			}
			else{ //only a is null
				return b;
			}
		}
		else{
			if(b==null){ //only b is null
				return a;
			}
		}
		
		Instances merged = null;
		Instances a_new,b_new;
		int i,u,o;
		Attribute akey=null,bkey=null;
		
		
		if(a.numAttributes()>=b.numAttributes()){
			a_new = new Instances(a);
			b_new = new Instances(b);
		}else{
			a_new = new Instances(b);
			b_new = new Instances(a);
		}
		
		akey = a_new.attribute(AttributeName);
		bkey = b_new.attribute(AttributeName);
		
		b_new.renameAttribute(bkey, "bfile");
		
		
		for(i=0;i<b_new.numAttributes();i++){
			a_new.insertAttributeAt(b_new.attribute(i), a_new.numAttributes());
		}
		
		a_new.sort(akey);
		b_new.sort(bkey);
		//System.out.println("attributes were added - total: " + a_new.numAttributes());
		
		
		for(i=0;i<a_new.size();i++){
			Boolean found = false;
			for(o=0;o<b_new.size();o++){
				if(a_new.get(i).stringValue(akey).equals(b_new.get(o).stringValue(b_new.attribute("bfile")))){
					for(u=0;u<b_new.numAttributes();u++){
						if(b_new.attribute(u).isNominal() || b_new.attribute(u).isString()){
							a_new.get(i).setValue(a_new.attribute(b_new.attribute(u).name()),
								b_new.get(o).stringValue(b_new.attribute(b_new.attribute(u).name())));
						}else{
							a_new.get(i).setValue(a_new.attribute(b_new.attribute(u).name()),
									b_new.get(o).value(b_new.attribute(b_new.attribute(u).name())));
						}
					}
					found = true;
					b_new.remove(o);
					break;
				}
			}
			if(!found){
				System.out.println("One file id is only present in one set: " + a_new.get(i).stringValue(akey));
				throw new Exception();
			}
			//System.out.println("cur: " + (i+1) + ":" + a_new.size());
		}	
		
		merged = a_new;
		
		//merged.deleteAttributeAt(merged.attribute(AttributeName).index());
		merged.deleteAttributeAt(merged.attribute("bfile").index());
		
		setClassIndex(merged);
		
		/*
		//if the attribute only has one value -> this column doesn't make any sense at all
		for(i=0;i<merged.numAttributes();i++){
			if(merged.numDistinctValues(i) == 1){
				merged.deleteAttributeAt(i);
			}
		}
		*/
		
		
		return merged;
	}
	
	
	/**
	 * @param directory: Path to directory which contains all arff which have been generated by OpenSmile
	 * @return all Samples as Instances object
	 * @throws Exception
	 */
	public static Instances soundArffToInstances(String directory) throws Exception{
		//get all arff files in the directory
		//every arff file correspondents to one row
		//the file name will be added as extra column
		
		Instances sound = null;
		int i;
		String key_attr = "file";
		
		File f = new File(directory);

	    FilenameFilter textFilter = new FilenameFilter() {
	        public boolean accept(File dir, String name) {
	            return name.toLowerCase().endsWith(".arff");
	        }
	    };

	    File[] files = f.listFiles(textFilter); //get all arff files in directory
	    
	    Attribute filename = new Attribute(key_attr,(FastVector)null); //create String Attribute
	    
	    for(i=0;i<files.length;i++){
	    	Instances data = loadFromSoundArff(files[i].getPath()); //load ARFF file
	    	
	    	if(i==0){ //initialize sound instances
	    		sound = data;
	    		
	    		sound.insertAttributeAt(filename, 0);	//add Filename feature    		
	    		sound.get(i).setValue(sound.attribute(key_attr), files[i].getName().split("\\.")[0]); //set filename
	    	}
	    	else{ //add row for each file
	    		data.insertAttributeAt(filename, 0);
	    		data.get(0).setValue(sound.attribute(key_attr), files[i].getName().split("\\.")[0]);
	    		
	    		sound.add(data.get(0));
	    	}
	    }
	    
	    return sound;		
	}
	
	
	
	/**
	 * @param data: instances which have been read from csv files (nominal attributes have to be converted to string attributes)
	 * @param attr: name of the attribute whose type has to be casted to string
	 * @throws Exception
	 */
	public static void NominalToString(Instances data, String attr) throws Exception{
		Attribute filename = new Attribute("temp",(FastVector)null); //create String Attribute
	    data.insertAttributeAt(filename, 0); //insert attribute at the first position
	    
	    Attribute tbc = data.attribute(attr); 
	    
	    //copy values from nominal attribute to string attribute
	    for(int i=0;i<data.size();i++){
	    	data.get(i).setValue(data.attribute(filename.name()), data.get(i).stringValue(tbc));
	    }
	    //remove nominal attribute
	    data.deleteAttributeAt(data.attribute(attr).index());
	    
	    //rename string attribute to orginal nominal attribute name
	    data.renameAttribute(data.attribute("temp"), attr);    
	}
	
	
	/**
	 * @param file: path to csv file (which have been generated by scripts/createCsvWithRawText.sh)
	 * @param key: name of the attribute which is the key (in our case the fileid of the wav file)
	 * @return
	 * @throws Exception
	 */
	public static Instances textCSVToInstances(String file,String key) throws Exception{
		CSVLoader loader = new CSVLoader();
	    loader.setSource(new File(file));
	    loader.setFieldSeparator(",");
	    loader.setEnclosureCharacters("@");
	    loader.setNoHeaderRowPresent(false);
	    Instances data = loader.getDataSet();
	    
	    NominalToString(data, key);    //fileid has to be casted to string
	    NominalToString(data, "text"); //text has to be casted to string     
	    
	    data.sort(data.attribute(key)); //nice to have
	    
	    setClassIndex(data);			//set class
	    
	    return data;
		
	}
	
	
	/**
	 * returns the distribution of a certain string in an attribute
	 * 
	 * @param data - instances
	 * @param attr - which attribute shall be investigated
	 * @param s - string which has to be a value in the attribute
	 * @return
	 */
	public static double getDistribution(Instances data, Attribute attr, String s){
		double distr=0;
		
		for(int i=0;i<data.size();i++){
			if(data.get(i).stringValue(attr).equals(s)) distr++;
		}
		
		distr /= (double)data.size();
		
		return distr;
	}
	
	
	/**
	 * Run our own cross validation
	 * 
	 * @param classifier - 
	 * @param data
	 * @param folds
	 * @param randseed - number for the random generator
	 * @param filters - filters which have to be applied first
	 * @return
	 * @throws Exception
	 */
	public static CrossValidationOutput crossValidation(MyClassificationOutput classifier, Instances data, int folds, int randseed, ArrayList<MyOutput> filters) throws Exception{
		CrossValidationOutput cvo = new CrossValidationOutput();
		
		Classifier c = (Classifier)classifier.getClassifier();
		
		Instances runInstances = new Instances(data);
	    Random random = new Random(randseed);
	    runInstances.randomize(random); //shuffle instances randomly
	    if (runInstances.classAttribute().isNominal() && (folds > 1)) {
	      runInstances.stratify(folds); //stratify data to get a equal distribution for all data sets
	    }
		
	    Evaluation eval;
	    //run cross validation
		for (int n = 0; n < folds; n++) {
			   //System.out.println("Running cross fold #" + (n+1) + "/" + folds + " ...");
			  
			   Instances train = runInstances.trainCV(folds, n); //get training data
			   Instances test = runInstances.testCV(folds, n);   //get test data
			   
			   if(filters != null){
				   //apply all filters
				   for(MyOutput m : filters){
					   Filter f = (Filter)m.getOperation();
					   f.setInputFormat(new Instances(train)); //use training data to build the filter
					   
					   if(f instanceof StringToWordVector){
						   int  [] attributes = new int[1];
						   attributes[0] =	train.attribute("text").index(); //eventuell +1 ???
				           ((StringToWordVector)f).setAttributeIndicesArray(attributes);						   
					   }
					   
					   cvo.addFilter(f,n);
					   train = Filter.useFilter(train, f); //use filter on the training data
					   test  = Filter.useFilter(test, f);  //use filter on the test data
				   }
			   }
			   
			   //-------------------------------------------------------------------------
			   // SoundAttributeSelection.AttributeSelectionByClassifier(c, train, test);
			   //-------------------------------------------------------------------------
			   
			   //build classification model
			   long startTime = System.currentTimeMillis();	
			   c.buildClassifier(train);
			   long stopTime = System.currentTimeMillis();
			   long elapsedTime = stopTime - startTime;
			   
			   //evaluate model on training set
			   eval = new Evaluation(train);			   
			   eval.evaluateModel(c,train);	
			   cvo.addTrainingEval(n, new MyClassificationOutput(c,eval,"MyCrossValidation",elapsedTime));
			   
			   //evaluate model on test set
			   eval = new Evaluation(test);
			   eval.evaluateModel(c,test);
			   cvo.addTestEval(n, new MyClassificationOutput(c,eval,"MyCrossValidation",elapsedTime));
		}
		return cvo;
	}
	
	public static int getNumberNonAlc(Instances train){
		int non_alc = 0;
		for(int i=0;i<train.size();i++){		
			String classValue = train.get(i).stringValue(train.classAttribute()); //get class attribute value from instance		
			if(classValue.toUpperCase().contains("NON")){
				non_alc++;
			}
		}
		return non_alc;
	}
	
	public static int getNumberAlc(Instances train){
		return train.size() - getNumberNonAlc(train);
	}
	
	public static double getWeightFactor(Instances train){
		double non_alc = getNumberNonAlc(train);
		return non_alc / (double)(train.size()-non_alc);		
	}
	
	/**
	 * Run our own cross validation
	 * 
	 * @param classifier - 
	 * @param sets
	 * @param filters - filters which have to be applied first
	 * @return
	 * @throws Exception
	 */
	public static MyClassificationOutput[] validationIS2011(Instances[] sets, ArrayList<MyOutput> filters, Double [] parameters, int classifier) throws Exception{
		
		MyClassificationOutput [] output = new MyClassificationOutput[5];
		
		Instances [] sets1 = new Instances[3];
		
		if(filters == null){
			System.out.println("No filters!");
		}
		
		//TODO: perhaps introduce here standarized + smote
		//weka.filters.unsupervised.attribute.Standardize
		//weka.filters.supervised.instance.SMOTE
		
		if(classifier == ClassifierE.SVM.getValue()){ //Classifier is a SVM
			//Feature scaling / normalization 
			//			as proposed by http://www.csie.ntu.edu.tw/~cjlin/papers/guide/guide.pdf
			MyOutput norm = normalize(null, null);
			ArrayList<MyOutput> filtersN = new ArrayList<MyOutput>();
			filtersN.add(0,norm); //add normalization at the beginning of the filter process
			if(filters != null){
				filtersN.addAll(filters);
			}
			filters = filtersN;
		}
		
		sets1 = applyFilters(sets,filters);
		
		System.out.println("Number of attributes: " + sets1[0].numAttributes());
		

		MyClassificationOutput currentResult = null;
		switch(classifier){
			case 1: //Logistic regression
					currentResult = WekaMagic.runLogistic(sets1[SetType.TRAIN.ordinal()], parameters[0], 5);
					break;
			case 2: //SVM
					//System.out.println("alc: " + sets1[0].classAttribute().indexOfValue("alc") + " weight factor: " + getWeightFactor(sets1[0]));
					currentResult = WekaMagic.runSVM(sets1[SetType.TRAIN.ordinal()], parameters[0], parameters[1], new Double[]{1.0,1.0});
					break;
			case 3: //KNN
					currentResult = WekaMagic.runKNN(sets1[SetType.TRAIN.ordinal()], parameters[0].intValue());
					break;
			case 4:	//Naive Bayes
					currentResult = WekaMagic.runNaiveBayes(sets1[SetType.TRAIN.ordinal()], parameters[0]==1.0, parameters[1]==1.0);
					break;
		}
		
		for(int i=0;i<sets1.length;i++){
			output[i] = WekaMagic.applyClassifier(sets1[i], currentResult);
		}
		
		//create model on test + training set
		//here we have to load the training set which was produced by second language model
		Instances trainDev = new Instances(sets[SetType.TRAIN.ordinal()]);
		trainDev.addAll(sets[SetType.DEV.ordinal()]);
		
		Instances [] sets2 = new Instances[2];
		sets2[0] = trainDev;
		sets2[1] = new Instances(sets[SetType.TEST.ordinal()]);
		
		//the instances have to be filtered again since attribute selection makes things different
		sets2 = applyFilters(sets2,filters);
		
		switch(classifier){
			case 1: //Logistic regression
					currentResult = WekaMagic.runLogistic(sets2[0], parameters[0], 5);
					break;
			case 2: //SVM
					currentResult = WekaMagic.runSVM(sets2[0], parameters[0], parameters[1], new Double[]{1.0,1.0});
					break;
			case 3: //KNN
					currentResult = WekaMagic.runKNN(sets2[0], parameters[0].intValue());
					break;
			case 4:	//Naive Bayes
					currentResult = WekaMagic.runNaiveBayes(sets2[0], parameters[0]==1.0, parameters[1]==1.0);
					break;
		}
		
		output[SetType.TRAINDEV.ordinal()] = WekaMagic.applyClassifier(sets2[0], currentResult);
		output[SetType.TRAINDEVTEST.ordinal()] = WekaMagic.applyClassifier(sets2[1],  currentResult);
		
		return output;
	}
	
	
	/**
	 * apply filters to a set of instances
	 * 
	 * @param sets - array of instances
	 * @param filters - ArrayList of filters which will be applied in ascending order
	 * @return
	 * @throws Exception
	 */
	public static Instances [] applyFilters(Instances [] sets, ArrayList<MyOutput> filters) throws Exception{
		Instances [] retsets = WekaMagic.copyInstancesArray(sets);
		if(filters != null){
			//apply all filters
		    for(MyOutput m : filters){
		    	Filter f = (Filter)m.getOperation();
		    	
		    	f.setInputFormat(new Instances(retsets[SetType.TRAIN.ordinal()])); //use training data to build the filter
			   
			    if(f instanceof StringToWordVector){
			    	int  [] attributes = new int[1];
			    	attributes[0] =	retsets[SetType.TRAIN.ordinal()].attribute("text").index(); //eventuell +1 ???
			    	((StringToWordVector)f).setAttributeIndicesArray(attributes);						   
			    }
			    
			    for(int i=0;i<sets.length;i++){
			    	if(i>0 && f instanceof SMOTE){} //don't apply SMOTE to the test set!
			    	else{
			    		if(f instanceof SMOTE && ((SMOTE)f).getPercentage()==0) {
			    			double nal = WekaMagic.getNumberNonAlc(retsets[i]);
			    			double al  = (retsets[i].size() - WekaMagic.getNumberNonAlc(retsets[i]));
			    			
			    			double factor = ((nal / al) * 100)-100;
			    			System.out.println("NAL: " + nal + " ALC: " + al);
			    			
			    			((SMOTE)f).setPercentage(factor);
			    		}
			    		
			    		retsets[i] = Filter.useFilter(retsets[i], f); //use filter on the training data
			    		
			    		if(f instanceof SMOTE) {
			    			System.out.println("NAL: " + WekaMagic.getNumberNonAlc(retsets[i]) + " ALC: " + (retsets[i].size() - WekaMagic.getNumberNonAlc(retsets[i]) ));
			    		}
			    	}
				}
		    }
		}
		
		return retsets;
	}
	
	
	/**
	 * run classifier for sound features with Interspeech 2011 data sets
	 * - only sequential processing!
	 * 
	 * @param sets
	 * @param withAttributeSelection - whether to use attribute selection or not
	 * @return
	 * @throws Exception
	 */
	public static List<List<Double>> runTestUARIS2011(Instances [] sets, Boolean withAttributeSelection) throws Exception {
		return WekaMagic.runTestUARIS2011(sets, withAttributeSelection, false);
	}
	
	/**
	 * Run the test for the IS dataset.
	 * 
	 * @param sets Datasets, train - dev test
	 * @param withAttributeSelection If using attribute selection
	 * @param isText If feature generation is necessary
	 * @return Results of each loop iteration (test)
	 * @throws Exception
	 */
	public static List<List<Double>> runTestUARIS2011(Instances [] sets, Boolean withAttributeSelection, Boolean isText) throws Exception {
		List<List<Double>> values = new ArrayList<List<Double>>();
		
		double stdRidge = 0.00000001; //10^-8
		double currentRidge = stdRidge;
				
		ArrayList<Double> threshold = new ArrayList<Double>();
		threshold.add(0.0);

		if (withAttributeSelection) {
			threshold.add(0.00000001);
			threshold.add(0.0001);
			threshold.add(0.0003);
			threshold.add(0.0006);
			threshold.add(0.001);
			threshold.add(0.002);
			threshold.add(0.0025);
			threshold.add(0.0030);
			threshold.add(0.0035);
			threshold.add(0.004);
			threshold.add(0.005);
			threshold.add(0.006);
			threshold.add(0.007);
			threshold.add(0.008);
			threshold.add(0.01);
		}
		
		MyOutput featuresGen = null;
		
		if(isText){
			// perfect configuration
			Boolean Ngram = true;
			int ngram_min = 1;
			int ngram_max = 3;
			Boolean LowerCase = true;
			Boolean IDFTransform = false;
			Boolean TFTransform = false;
			Boolean Stopword = true;
			String list1 = "resources\\germanST.txt";
			int wordsToKeep = 1000000;
			Boolean NormalizeDocLength = true;
			Boolean OutputWordCounts = true;
			Boolean Stemming = true;
			int minTermFrequency = 2;
			featuresGen = WekaMagic.generateFeatures(null, wordsToKeep, Ngram,
					ngram_min, ngram_max, LowerCase, NormalizeDocLength, Stemming,
					OutputWordCounts, IDFTransform, TFTransform, Stopword, list1,
					minTermFrequency);
		}
		
		System.out.println("Running tests for train, dev and test set...");
		
		//Iterate through different ridge values
		for (int i = 0; i < threshold.size(); i++) {
			for (int u=0;u<15;u++)
			{
				currentRidge = stdRidge * (Math.pow(10, u));
				
				MyOutput filtered = null;
				ArrayList<MyOutput> filters = null;
				
				if(isText || withAttributeSelection)
					filters = new ArrayList<MyOutput>();
				
				if(isText)
					filters.add(featuresGen);
				
				if (withAttributeSelection) {
					// true binarizeNumericAttributes is important
					Boolean binarizeNumericAttributes = true;
					filtered = WekaMagic.selectionByInfo(null, binarizeNumericAttributes,
							(Double) threshold.get(i));
					filters.add(filtered);
				}
				
				MyClassificationOutput [] output = WekaMagic.validationIS2011(sets, filters, new Double[]{currentRidge}, 1);
	
				// Result processing to lists
				List<Double> listRun = new ArrayList<Double>();
				
				listRun.add(0, threshold.get(i));
				listRun.add(1, currentRidge);
				listRun.add(2, output[SetType.TRAIN.ordinal()].getUAR());
				listRun.add(3, output[SetType.DEV.ordinal()].getUAR());
				listRun.add(4, output[SetType.TEST.ordinal()].getUAR());

				values.add(listRun);
				
				// print all information about the result
				System.out.print("ridge:" + currentRidge + " threshold:" + threshold.get(i)
						+ "Train UAR: " + output[SetType.TRAIN.ordinal()].getUAR() + " Dev UAR:"
						+ output[SetType.DEV.ordinal()].getUAR() + " Test UAR:"
						+ output[SetType.TEST.ordinal()].getUAR() + "\n");
			}
		}
		
		return values;
	}
	
	/**
	 * run classifier for sound features with Interspeech 2011 data sets
	 * - parallel processing!
	 * 
	 * @param sets
	 * @param withAttributeSelection - whether to use attribute selection or not
	 * @param smote 
	 * @param maxThreads 
	 * @param text 
	 * @return
	 * @throws Exception
	 */
	public static List<List<Double>> runTestUARIS2011LogisticThreads(Instances [] sets, Boolean withAttributeSelection, int maxThreads, double smote) throws Exception {
		return WekaMagic.runTestUARIS2011LogisticThreads(sets, withAttributeSelection, false,-1,smote);
	}
	
	/**
	 * Run the test for the IS dataset.
	 * 
	 * @param sets Datasets, train - dev test
	 * @param withAttributeSelection If using attribute selection
	 * @param isText If feature generation is necessary
	 * @param maxThreads 
	 * @return Results of each loop iteration (test)
	 * @throws Exception
	 */
	public static List<List<Double>> runTestUARIS2011LogisticThreads(Instances [] sets, Boolean withAttributeSelection, Boolean isText, int maxThreads, double smotep) throws Exception {
		List<List<Double>> values = new ArrayList<List<Double>>();
		
		double stdRidge = 0.00000001; //10^-8
		double currentRidge = stdRidge;
				
		ArrayList<Double> threshold = new ArrayList<Double>();
		threshold.add(0.0);
		
		if (withAttributeSelection) {
			addThreshold(threshold);
		}
				
		System.out.println("Running tests for train, dev and test set...");
		
		int nrThreads=getNumberOfThreads(maxThreads);
		
		MultiWeka [] threads = new MultiWeka[nrThreads];
		
		//Iterate through different ridge values
		int uMax = 15;
		int maxIter = threshold.size() * uMax;
		
		int count = 0;
		
		for (int i = 0; i < threshold.size(); i++) {
			for (int u=0;u<uMax;u++)
			{
				
				currentRidge = stdRidge * (Math.pow(10, u));
				
				// Start all threads
				threads[count%nrThreads] = new MultiWeka(WekaMagic.copyInstancesArray(sets),withAttributeSelection,isText,new Double[]{currentRidge},threshold.get(i),ClassifierE.LOGISTIC.getValue(), 5, smotep);  //TODO: optimize smote
				threads[count%nrThreads].start();
				
				// If all threads are up and running
				if(count % nrThreads == nrThreads-1 || count == maxIter - 1){
					for(MultiWeka r: threads){
						r.join();
						values.add(r.getResult());
					}
				}			
				count++;
			}
		}
		
		return values;
	}
	
	
	public static ArrayList<Double> createCValueSetList(){
		ArrayList<Double> Cval = new ArrayList<Double>();
		
		
		//Cval.add(0.0005);
		//Cval.add(0.001);
		Cval.add(0.005);
		Cval.add(0.01);
		Cval.add(0.02);
		Cval.add(0.03);
		Cval.add(0.04);
		Cval.add(0.06);
		Cval.add(0.08);
		Cval.add(0.1);
		//Cval.add(0.2);
		//Cval.add(0.5);
		//Cval.add(1.0);
		//Cval.add(2.0);
		
		/*
		//optimzied for all
		Cval.add(0.0075);
		Cval.add(0.009);
		Cval.add(0.01);
		Cval.add(0.011);
		Cval.add(0.0125);
		Cval.add(0.015);
		Cval.add(0.0175);
		Cval.add(0.019);
		*/
		
		
		/*
		//optimzied for text
		Cval.add(0.1);
		Cval.add(0.15);
		Cval.add(0.2);
		Cval.add(0.25);
		Cval.add(0.3);
		*/
		
		return Cval;
	}
	
	public static ArrayList<Double> createsmotePercentageValueSetList(double smote){
		ArrayList<Double> SmotePerVal = new ArrayList<Double>();
		
		if(smote > 0){
			SmotePerVal.add(100.0);
		}else if(smote == 0){
			SmotePerVal.add(0.0);
		}else{
			SmotePerVal.add(-1.0); //no application of smote
		}
		
		
		return SmotePerVal;
	}
	
	
	/**
	 * Run the test for the IS dataset.
	 * 
	 * @param sets Datasets, train - dev test
	 * @param withAttributeSelection If using attribute selection
	 * @param isText If feature generation is necessary
	 * @param kernelType - int number of Kernel type defined in team2014.weka.svm
	 * @param maxThreads 
	 * @param smotep 
	 * @return Results of each loop iteration (test)
	 * @throws Exception
	 */
	public static List<List<Double>> runTestUARIS2011SVMThreads(Instances [] sets, Boolean withAttributeSelection, Boolean isText, int kernelType, int maxThreads, double smotep) throws Exception {
		List<List<Double>> values = new ArrayList<List<Double>>();
		
		ArrayList<Double> threshold = new ArrayList<Double>();
		threshold.add(0.0);
		
		if (withAttributeSelection) {
			addThreshold(threshold);
		}
		
		ArrayList<Double> Cval = createCValueSetList();	
		
		
		ArrayList<Double> Gammaval = new ArrayList<Double>();
		double currentC;
		
		if(kernelType == KernelType.RBF.getValue()){
			for(int i=0;i<8;i++){
				Gammaval.add(Math.pow(2,-15+(i*2)));  //from 2^-15, 2^-13, ...
			}
		}
		if(kernelType == KernelType.LINEAR.getValue()){ // if the kernel is linear, we don't need gamma
			Gammaval.add(null);
		}
				
		
		ArrayList<Double> percentages = createsmotePercentageValueSetList(smotep);
		ArrayList<Integer> smoteKNNval = createsmoteKNNValueSetList(smotep);
		
		System.out.println("Running tests for train, dev and test set...");
		
		int nrThreads=getNumberOfThreads(maxThreads);
		
		MultiWeka [] threads = new MultiWeka[nrThreads];
		
		int count = 0;
		
		int wMax = Cval.size();
		int maxIter = threshold.size() * wMax * Gammaval.size() * percentages.size() * smoteKNNval.size();
		
		
		//smote
		for(int smoteKNN:smoteKNNval){ //iterating through all smote percentages
			for(double smotePer:percentages){ //iterating through all smote percentages
				for (int i=0; i<threshold.size(); i++) {		//iterating through Threshold values
					for(int w=0; w<wMax; w++){  				//iterating through C values
						for (int u=0; u<Gammaval.size(); u++)   //iterating through Gamma
						{
							currentC = Cval.get(w);	// range of C
							
							// Start all threads
							threads[count%nrThreads] = new MultiWeka(WekaMagic.copyInstancesArray(sets),withAttributeSelection,isText,
																	new Double[]{currentC, Gammaval.get(u)},threshold.get(i),ClassifierE.SVM.getValue(), smoteKNN, smotePer); 
							threads[count%nrThreads].start();
							
							// If all threads are up and running
							if(count % nrThreads == nrThreads-1 || count == maxIter - 1){
								for(MultiWeka r: threads){
									r.join();
									values.add(r.getResult());
								}
							}			
							count++;
						}
					}
				}
			}
		}
		
		return values;
	}
	
	
	private static ArrayList<Integer> createsmoteKNNValueSetList(double smote) {
		ArrayList<Integer> SmoteKNNVal = new ArrayList<Integer>();
		
		if(smote >=0){
			SmoteKNNVal.add(5);
		}else{
			SmoteKNNVal.add(0); //no application of smote
		}
		
		
		return SmoteKNNVal;
	}

	/**
	 * copy array of instances - similar to clone
	 * 
	 * @param sets
	 * @return
	 */
	public static Instances[] copyInstancesArray(Instances [] sets){
		Instances [] copy = new Instances [sets.length];
		for(int i=0;i<sets.length;i++){
			copy[i] = new Instances(sets[i]);
		}
		return copy;
	}
	
	
	/**
	 * save results of the classifiers to file
	 * 
	 * @param results
	 * @param outputFolder - in which directory to store results
	 * @param fileNameExtension - extension
	 * @param type
	 * @param intro
	 * @param headerType
	 * @throws Exception
	 */
	public static void saveResultIS2011(List<List<Double>> results, String outputFolder, String fileNameExtension, String type, String [] intro, String headerType) throws Exception{
		// Create timestamp
		Date timestamp = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm");
				
		// CSV Header
		List<String> header = new ArrayList<String>();
		header.add("Threshold");
		
		if(headerType.equals("logistic"))
			header.add("Ridge");
		else if(headerType.equals("lin"))
			header.add("C");
		else{
			header.add("C");
			header.add("gamma");
		}
			 
		
	    header.add("Train UAR");
	    header.add("Dev UAR");
	    header.add("Test UAR");
	    header.add("Train+Dev UAR");
	    header.add("Train+Dev vs Test UAR");
		
	    //save to CSV
		WekaMagic.printHashMap(results, header, outputFolder + type + "_IS2011"+fileNameExtension+sdf.format(timestamp) + ".csv", intro);
	}
	
	/**
	 * load instances from arff file
	 * 
	 * @param file - path of arff file to read
	 * @return instances
	 * @throws Exception
	 */
	public static Instances loadFromSoundArff(String file) throws Exception{
		DataSource source = new DataSource(file); //load ARFF file
    	Instances data = source.getDataSet();
    	if(data != null){
    		data.deleteStringAttributes(); //delete all string attributes
    	}
    	else{
    		System.out.println("Please fix the input path!");
    	}
    	
    	return data;
	}

	public static Instances loadArff(String file) throws Exception{
		DataSource source = new DataSource(file); //load ARFF file
    	Instances data = source.getDataSet();
    	if(data == null){
    		System.out.println("Please fix the input path!");
    	}
    	
    	return data;
	}
	
	
	
	/**
	 * method to process the text data to make it suitable to generate the language model
	 * 
	 * @param s - string which has to be processed
	 * @return
	 */
	public static String cleanString(String s){
		String str = s;
		
		String tb_removed = ".*~+_-/\\";
		
		//remove characters
		for(int i=0;i<tb_removed.length();i++){
			String character = "" + tb_removed.charAt(i);
			str = str.replace(character, "");
		}
		
		//definition of tags which have to be removed
		ArrayList<String> tags_RegExp = new ArrayList<String>();
		tags_RegExp.add("<[^>]*>");
		tags_RegExp.add("\\[[^\\]]*\\]");
		
		tags_RegExp.add("#[^#]*#");
		
		//delete whole tags
		for(int i=0;i<tags_RegExp.size();i++){
			str = str.replaceAll(tags_RegExp.get(i), "");
		}
		
		//remove all multiple white spaces
		String nstr =null;
		do{
		  nstr = str;
		  nstr = nstr.replace("  ", " ");
		  if(nstr.equals(str)) break;
		  str = nstr;
		}while(true);
		
		str = str.trim();        //trim white spaces
		
		return str;
	}
	
	
	/**
	 * get attribute text from instances which only contain two string attributes: fileid and text
	 * 
	 * @param n_data - instances
	 * @param s_key - name of the file id attribute - in our case "file"
	 * @return
	 */
	public static Attribute getPhrase(Instances n_data, String s_key){
		Attribute phrase = null;
		for(int i=0;i<n_data.numAttributes();i++){
			if(n_data.attribute(i).isString() && !n_data.attribute(i).name().equals(s_key) && !n_data.attribute(i).isNominal()){
				phrase = n_data.attribute(i);
				break;
			}
		}
		
		return phrase;
	}
	
	
	/**
	 * process instances to be able to build a language model on them
	 * 
	 * @param data - text instances
	 * @param s_key - file id attribute name - in our case "file"
	 * @return
	 */
	public static Instances cleanCorpus(Instances data, String s_key){
		Instances n_data = new Instances(data);
		
		Attribute phrase = getPhrase(n_data,s_key); // get text attribute - "text"
		
				
		for(int i=0;i<n_data.size();i++){
			String s = n_data.get(i).stringValue(phrase);	//get original string value
			
			s = cleanString(s);  //clean string			
		    s = s.toLowerCase(); //to lower case - I am not really sure if this is a good idea
			
			n_data.get(i).setValue(phrase,s);	//set cleaned string	
		}
		
		return n_data;
	}
	
	
	/**
	 * print text file for building the language file
	 * 
	 * @param file - path where to store the file
	 * @param data - instances
	 * @param s_key - file id attribute name - in our case "file"
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */
	public static void createReferenceText(String file, Instances data, String s_key) throws FileNotFoundException, UnsupportedEncodingException{
		PrintWriter writer = new PrintWriter(file, "UTF-8");
		
		Attribute phrase = getPhrase(data,s_key);
		
		for(int i=0;i<data.size();i++){
			writer.println("<s> " + data.get(i).stringValue(phrase) + " </s>");
		}
		writer.close();		
	}
	
	
	/**
	 * returns parent directory of given dir
	 * 
	 * @param dir - path to the directory
	 * @return
	 */
	public static String getParent(String dir){
		
		//get right fileSeparator
		boolean isWindows = ((System.getProperty("os.name").contains("Windows")))?true:false;
		String fileSep = isWindows?"\\":"/";
		
		String parent;
		
		StringTokenizer Tok = new StringTokenizer(dir,fileSep);
		int n=0;
		while (Tok.hasMoreElements()) {
			Tok.nextElement();
			n++;
		}
		
		//generate path of parent directory
		Tok = new StringTokenizer(dir,fileSep);
		int i=0;
		if(isWindows){
			parent = "";
		}else{
			parent = fileSep;
		}
	    while (Tok.hasMoreElements()){
	    	if(i<n-1){
	                parent += Tok.nextElement() + fileSep;
	    	}
	    	else{break;}
	    	i++;
	    }
	    
	    return parent;
	}
	
	
	/**
	 * get sound instances with annotated class (nonalc/alc)
	 * 
	 * @param arffdir - directory of all arff files
	 * @param csv_file - csv file of all text data
	 * @return
	 * @throws Exception
	 */
	public static Instances getSoundInstances(String arffdir, String csv_file) throws Exception
	{
		Instances data = WekaMagic.getSoundInstancesWithFile(arffdir, csv_file);

		//delete unnecessary attributes
		data.deleteAttributeAt(data.attribute("file").index());			  //remove key(file id) attribute
    	
    	return data;
	}
	
	/**
	 * get sound instances with annotated class (nonalc/alc)
	 * 
	 * @param arffdir - directory of all arff files
	 * @param csv_file - csv file of all text data
	 * @return
	 * @throws Exception
	 */
	public static Instances getSoundInstancesWithFile(String arffdir, String csv_file) throws Exception
	{
		//first check whether aggregated arff file exists
		boolean isWindows = ((System.getProperty("os.name").contains("Windows"))) ? true : false;
		String fileSep = isWindows ? "\\" : "/";
		
		String allfolder = arffdir + fileSep + "all";	
		String arfffile = allfolder + fileSep + "sound_all.arff";
		File f = new File(arfffile);
		
		Instances data = null;
		
		if(f.exists()){
			DataSource source = new DataSource(arfffile); //load ARFF file
			data = source.getDataSet();
			System.out.println("fast sound loader");
		}
		else{	
			Instances sound = WekaMagic.soundArffToInstances(arffdir);		  //get sound data
			Instances text = WekaMagic.textCSVToInstances(csv_file,"file");   //get text data (with class)
			
			data = WekaMagic.mergeInstancesBy(sound, text, "file"); //merge text and sound data by using the key fileid
			//delete unnecessary attributes
			data.deleteAttributeAt(data.attribute("text").index()); 		  //remove text attribute
			//data.deleteAttributeAt(data.attribute("file").index());		//remove key(file id) attribute
			//data.deleteAttributeAt(data.attribute("numeric_class").index());
			
			//aggregate data to one arff file
			Boolean success = (new File(allfolder)).mkdirs();
			if (success) {
				WekaMagic.saveToArff(data,arfffile.split("\\.")[0], null);
			}
		}
		
		return data;
	}
	
	
	/**
	 * get grammar instances without file column
	 * 
	 * @param csv_file
	 * @param normalizeSentenceLength
	 * @return
	 * @throws Exception
	 */
	public static Instances getGrammarInstances(String csv_file, Boolean normalizeSentenceLength) throws Exception{
		Instances text = textCSVToInstances(csv_file,"file"); //get text features
		Instances grammar = checkGrammar(text, normalizeSentenceLength); //get grammar features
		
		grammar.deleteAttributeAt(grammar.attribute("text").index()); 		  //remove text attribute
		grammar.deleteAttributeAt(grammar.attribute("file").index());			  //remove key(file id) attribute
		
		
		return grammar;
	}
	
	/**
	 * get grammar instances with file column
	 * 
	 * @param csv_file
	 * @param normalizeSentenceLength
	 * @return
	 * @throws Exception
	 */
	public static Instances getGrammarInstancesWithFile(String csv_file, Boolean normalizeSentenceLength) throws Exception{
		Instances text = textCSVToInstances(csv_file,"file"); //get text features
		Instances grammar = checkGrammar(text, normalizeSentenceLength); //get grammar features
		
		grammar.deleteAttributeAt(grammar.attribute("text").index()); 		  //remove text attribute
		
		if(grammar.attribute("conf_score") != null)
			grammar.deleteAttributeAt(grammar.attribute("conf_score").index());	  //remove confidence score

		return grammar;
	}
	
	
	
	/**
	 * run Support vector machine SVM of libsvm
	 * 
	 * @param train - data set 
	 * @param C - regularization constant
	 * @param gamma - null for linear kernel, Double for RBF
	 * @return
	 * @throws Exception
	 */
	public static MyClassificationOutput runSVM(Instances train, Double C, Double gamma, Double [] classweights)
	throws Exception {

		LibSVM svm = new LibSVM();
		long elapsedTime;
		Evaluation eval = null;
		String options = "SVM: C = " + C + " gamma = " + gamma;
		
		String weightsStr = "";
		for(Double weight : classweights){
			weightsStr += weight + " ";
		}
		svm.setWeights(weightsStr.trim());
		
		//Set cache memory size in MB (default: 40)
		svm.setCacheSize(20000.0); //speed up algorithm - try 20 GB
		
		//Set coef0 in kernel function (default: 0)
		//svm.setCoef0(value); //only necessary for polynomial and sigmoid kernel ??
		//we don't need it _ C-SVC
		
		//Set degree in kernel function (default: 3)
		//svm.setDegree(value); //only necessary for polynomial kernel ??
		//we don't need it _ C-SVC
		
		//Set the epsilon in loss function of epsilon-SVR (default: 0.1)
		//svm.setEps(value); //we don't need it _ C-SVC
		
		//Set the parameter C of C-SVC, epsilon-SVR, and nu-SVR (default: 1)
		svm.setCost(C);	  	
		
		/*
		   Set type of kernel function (default: 2)
			    0 = linear: u'*v
			    1 = polynomial: (gamma*u'*v + coef0)^degree
			    2 = radial basis function: exp(-gamma*|u-v|^2)
			    3 = sigmoid: tanh(gamma*u'*v + coef0)	    
			    
		*/
		if(gamma != null){			
			svm.setKernelType(new SelectedTag(LibSVM.KERNELTYPE_RBF, LibSVM.TAGS_KERNELTYPE));
			
			//Set gamma in kernel function (default:  (1 / number_of_features) )
			svm.setGamma(gamma); //not necessary when applying linear kernel
		}else{
			svm.setKernelType(new SelectedTag(LibSVM.KERNELTYPE_LINEAR, LibSVM.TAGS_KERNELTYPE));
		}
		
		// Turns on normalization of input data (default: off)
		svm.setNormalize(false); //better normalize it beforehand
		
		//Set the epsilon in loss function of epsilon-SVR (default: 0.1)
		//svm.setLoss(value); //we don't need it _ C-SVC
		
		//Set the parameter nu of nu-SVC, one-class SVM, and nu-SVR (default: 0.5)
		//svm.setNu(value); //we don't need it _ C-SVC

		/*
		   Set type of SVM (default: 0)
			    0 = C-SVC				//support vector classification
			    1 = nu-SVC				//support vector classification
			    2 = one-class SVM		//distribution estimation
			    3 = epsilon-SVR			//regression
			    4 = nu-SVR				//regression
		*/
		svm.setSVMType(new SelectedTag(LibSVM.SVMTYPE_C_SVC,LibSVM.TAGS_SVMTYPE));
		
		//Random seed (default = 1)
		svm.setSeed(1);
		
		//Generate probability estimates for classification
		svm.setProbabilityEstimates(true);
		
		//Turns the shrinking heuristics off (default: on)
		svm.setShrinking(true);
		
		//build classifier
		long startTime = System.currentTimeMillis();
		if(train != null){
			svm.buildClassifier(train);
		}
		long stopTime = System.currentTimeMillis();
		elapsedTime = stopTime - startTime;
		
		//evaluate how good it performes on the training set
		if(train != null){
			eval = new Evaluation(train);
			eval.evaluateModel(svm,train);
		}
				
		return new MyClassificationOutput(svm, eval, options, elapsedTime);
	}
	
	public static MyClassificationOutput runKNN(Instances train, int k)
	throws Exception {

		IBk knn = new IBk();
		long elapsedTime;
		Evaluation eval = null;
		String options = "KNN: k = " + k;
		
		knn.setKNN(k);
		
		//build classifier
		long startTime = System.currentTimeMillis();
		if(train != null){
			knn.buildClassifier(train);
		}
		long stopTime = System.currentTimeMillis();
		elapsedTime = stopTime - startTime;
		
		//evaluate how good it performes on the training set
		if(train != null){
			eval = new Evaluation(train);
			eval.evaluateModel(knn,train);
		}
				
		return new MyClassificationOutput(knn, eval, options, elapsedTime);
	}
	
	public static MyClassificationOutput runNaiveBayes(Instances train, Boolean UseKernelEstimator, Boolean UseSupervisedDiscretization)
	throws Exception {

		NaiveBayes naive = new NaiveBayes();
		long elapsedTime;
		Evaluation eval = null;
		String options = "Naive Bayes: UseKernelEstimator = " + UseKernelEstimator + " UseSupervisedDiscretization = " + UseSupervisedDiscretization;
		
		naive.setUseKernelEstimator(UseKernelEstimator);
		naive.setUseSupervisedDiscretization(UseSupervisedDiscretization);
		
		//build classifier
		long startTime = System.currentTimeMillis();
		if(train != null){
			naive.buildClassifier(train);
		}
		long stopTime = System.currentTimeMillis();
		elapsedTime = stopTime - startTime;
		
		//evaluate how good it performes on the training set
		if(train != null){
			eval = new Evaluation(train);
			eval.evaluateModel(naive,train);
		}
				
		return new MyClassificationOutput(naive, eval, options, elapsedTime);
	}
	
	
	/**
	 * translate umlauts into real format
	 * 
	 * @param str
	 * @return
	 */
	private static String setRightUmlauts(String str){
		str = str.replace("\\", "");
		
		str = str.replace("\"u", "ü");
		str = str.replace("\"o", "ö");
		str = str.replace("\"a", "ä");
		
		str = str.replace("\"U", "Ü");
		str = str.replace("\"O", "Ö");
		str = str.replace("\"A", "Ä");
		
		str = str.replace("\"s", "ß");
		
		return str;
	}
	
	
	/**
	 * generate grammar features
	 * 
	 * @param text
	 * @param normalizeSentenceLength
	 * @return
	 * @throws IOException
	 */
	public static Instances checkGrammar(Instances text, Boolean normalizeSentenceLength) throws IOException{
		Instances grammar = new Instances(text);
		
		//JLanguageTool langTool = new JLanguageTool(new German());
		JLanguageTool langTool = new JLanguageTool(new GermanyGerman());
		langTool.activateDefaultPatternRules();
		
		Attribute text_attr = getPhrase(grammar, "file");
		
		for(int i=0;i<grammar.size();i++){
			String s = grammar.get(i).stringValue(text_attr);
			
			s = cleanString(s);			
			s = setRightUmlauts(s);
			s = s.toLowerCase();
		    
			//System.out.println("Sample " + i + "\n" + s);
			
			List<RuleMatch> matches = langTool.check(s);
			
			HashMap<String,Integer> grammar_error_list = new HashMap<String,Integer>();
			 
			for (RuleMatch match : matches) {
			  String ruleName = match.getRule().getId();
			  ruleName.toUpperCase();			  
			  
			  //we don't need komma rules, since kommas are not annotated
			  //since we are case insensitive, skip also case rules
			  if(	!match.getRule().getCategory().getName().contains("Zeichensetzung") && 
					!match.getRule().getCategory().getName().contains("Groß-/Kleinschreibung")
				 ){ 
				  Integer ret = grammar_error_list.put(ruleName, 1);
				  if(ret != null) grammar_error_list.put(ruleName, (ret+1));
				  
				  /*
				  System.out.println("Category: " + match.getRule().getCategory().getName());
				  System.out.println(match.getRule().getId());				  			  
				  
				  System.out.println("Potential error at line " +
				      match.getLine() + ", column " +
				      match.getColumn() + ": " + match.getMessage());
				  System.out.println("Suggested correction: " +
				      match.getSuggestedReplacements());
				  
				  System.out.println();
				  */
				  
			  }
			}
			
			grammar = insertGrammarFeatures(grammar, i, s, grammar_error_list, normalizeSentenceLength);
		}
		
		return grammar;
	}
	
	
	/**
	 * count words of a string
	 * - used for normalization
	 * 
	 * @param in
	 * @return
	 */
	public static int CountWords (String in) {
	   String trim = in.trim();
	   if (trim.isEmpty()) return 0;
	   return trim.split("\\s+").length; //separate string around spaces
	}

	private static Instances insertGrammarFeatures(Instances grammar, int instanceNumber, String currentSentence,
			HashMap<String, Integer> grammar_error_list, Boolean normalizeSentenceLength) {
		
		Instances ngrammar = new Instances(grammar); //make sure no bad changes happen to the input instances
		
		//iterate through all grammar errors which occur in the instance with number i
		Iterator it = grammar_error_list.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        
	        String ruleName = (String) pairs.getKey();
	        double ruleFrequency = (Integer) pairs.getValue();
	        
	        //divide number of occurences by number of words
	        if(normalizeSentenceLength==true){
	        	ruleFrequency = ruleFrequency / ((double)CountWords(currentSentence));
	        }
	        
	        if(ngrammar.attribute(ruleName)==null){ //attribute doesn'tr exist yet
	        	//create new numeric attribute
	        	ngrammar.insertAttributeAt(new Attribute(ruleName), ngrammar.numAttributes());
	        	for(int i=0;i<ngrammar.size();i++){ //set its value to zero for all instances 
	        		ngrammar.get(i).setValue(ngrammar.attribute(ruleName), 0.0);
	        	}
	        }
	        
	        ngrammar.get(instanceNumber).setValue(ngrammar.attribute(ruleName), ruleFrequency);
	        
	        it.remove(); // avoids a ConcurrentModificationException
	    }
		
		return ngrammar;
	}
	
	public static ArrayList<Speaker> loadSpeakerInfo(String SPEAEXT_TBL) throws IOException{
		ArrayList<Speaker> al = new ArrayList<Speaker>();
		
		InputStream    fis;
		BufferedReader br;
		String         line;

		fis = new FileInputStream(SPEAEXT_TBL);
		br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
		int i=0;
		while ((line = br.readLine()) != null) {
			//System.out.println("\""+line+"\"");
			if(i>0 && !line.isEmpty() && line != null && line.length()>5){
				String[] tokens = line.split("\t");
				
				//System.out.println(Arrays.toString(tokens));
				
				int id = -1;
				int age = -1;
				int weight = -1;
				int height = -1;
				
				try
				{
				   id = Integer.parseInt(tokens[0]);
				   age = Integer.parseInt(tokens[2]);
				   weight = Integer.parseInt(tokens[4]);
				   height = Integer.parseInt(tokens[5]);
				}
				catch(NumberFormatException nfe)
				{
				  // don't do anything
				}
				
				
				//					id  sex	    age    accent weight height education profession  smo     drh       com
				al.add(new Speaker(id,tokens[1],age,tokens[3],weight,height,tokens[6],tokens[7],tokens[8],tokens[9],tokens[10]));
			}
			i++;
		}

		// Done with the file
		br.close();
		br = null;
		fis = null;
		
		return al;
		
	}
	
	public static Instances[] getInterspeech2011SetsWithFile(String dir, Instances allInstances, String fileAttribute) throws Exception{
		Instances [] sets = new Instances [3];
		
		//get right fileSeparator
		boolean isWindows = ((System.getProperty("os.name").contains("Windows")))?true:false;
		String fileSep = isWindows?"\\":"/";
		
		String [] filenames = new String[3];
		
		filenames[0] = "TRAIN.TBL"; //train_name
		filenames[1] = "D1.TBL";    //dev_name
		filenames[2] = "TEST.TBL";  //test_name
		
		InputStream    fis;
		BufferedReader br;
		String         line;
		
		for(int i=0;i<filenames.length;i++){
			sets[i] = new Instances(allInstances, 0);
			String filename = dir + fileSep + filenames[i];
			fis = new FileInputStream(filename);
			br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
			while ((line = br.readLine()) != null) {
				//System.out.println("\""+line+"\""); // format example: BLOCK30/SES3066/5653066001_h_01.WAV
				if(!line.isEmpty() && line != null && line.length()>5){
					String[] tokens = line.split("/");
					String id = tokens[tokens.length-1].split("\\.")[0];
					
					Boolean found = false;
					for(int u=0;u<allInstances.size();u++){
								if(allInstances.get(u).stringValue(allInstances.attribute(fileAttribute)).equals(id)){
									sets[i].add(allInstances.get(u));
									found = true;
									break;
								}					
					}
					if(found == false){
						System.out.println("One file id is only present in one file: " + id);
						throw new Exception();
					}
				}
			}
			
			// Done with the file
			br.close();
			br = null;
			fis = null;
		}
		
		return sets;
	}
	
	public static Instances[] getInterspeech2011Sets(String dir, Instances allInstances, String fileAttribute, String category_file) throws Exception{
		Instances [] sets = new Instances [3];
		
		sets = getInterspeech2011SetsWithFile(dir, allInstances, fileAttribute);
		
		//filter by category if necessary
		sets = getInterspeech11ByCategory(sets,fileAttribute,category_file);
		
		for(int i=0;i<sets.length;i++){
			sets[i].deleteAttributeAt(sets[i].attribute(fileAttribute).index());
		}
		
		return sets;
	}

	public static Instances getOutOfSets(Instances[] sets, Instances allInstances, String s_key) {
		Instances outOfSets = new Instances(allInstances, 0);
		Instances merged = new Instances(sets[0]);
		for(int i=1;i<sets.length;i++){
			merged.addAll(sets[i]);
		}
		
		Instances a = null;
		Instances b = null;
		if(merged.size() > allInstances.size()){
			a = new Instances(merged);
			b = new Instances(allInstances);
		}
		else{
			a = new Instances(allInstances);
			b = new Instances(merged);
		}
		
		
		for(int i=0;i<a.size();i++){
			Boolean found = false;		
			for(int u=0;u<b.size();u++){
				if(b.get(u).stringValue(b.attribute(s_key)).equals(a.get(i).stringValue(a.attribute(s_key)))){
						found = true;
						break;
				}
			}
			if(!found){
				outOfSets.add(a.get(i));
				//System.out.println(a.get(i).toString());
			}
		}
		
		return outOfSets;
	}

	public static Instances [] deleteFromSets(Instances[] sets,
			Instances notInSets, String s_key) {
		
		Instances [] setsWO = new Instances [sets.length];
		for(int i=0;i<sets.length;i++){
			setsWO[i] = new Instances(sets[i]);
			
			for(int u=0;u<setsWO[i].size();u++){
				for(int t=0;t<notInSets.size();t++){
					if(setsWO[i].get(u).stringValue(setsWO[i].attribute(s_key).index()).equals(notInSets.get(t).stringValue(notInSets.attribute(s_key).index()))){
						setsWO[i].delete(u);
						u--;
						break;
					}
				}
			}
		}
		
		return setsWO;
	}
	
	public static SpeakerSet matchSpeakerToInstances(String speakerTable, Instances data, String fileAttribute) throws IOException{
		ArrayList<Speaker> speakers = WekaMagic.loadSpeakerInfo(speakerTable);
		
		Attribute key = data.attribute(fileAttribute);
		
		ArrayList<SpeakerSamples> table_speaker_samples = new ArrayList<SpeakerSamples>();
	    for(int i=0;i<data.size();i++){
	    	Sample current = new Sample(data.get(i),key);
	    	
	    	int found = 0;
	 		for(int l=0;l<table_speaker_samples.size();l++){
	    		if( current.getUser_id() == table_speaker_samples.get(l).getSpeaker().getId()){
	    			table_speaker_samples.get(l).addFile(current);
	    			found = 1;
	    			break;
	    		}
    		}
	    	if(found == 0){
		    	for(int u=0;u<speakers.size();u++){
		    		if( current.getUser_id() == speakers.get(u).getId()){
		    			table_speaker_samples.add(new SpeakerSamples(speakers.get(u),current));
		    			found = 1;
		    			break;
		    		}
		    	}
	    	}
	    	if(found==0){
	    		System.out.println("No fitting speaker found for: \"" + data.get(i).stringValue(key) + "\"");
	    	}
	    }
	    return new SpeakerSet(table_speaker_samples);
	}

	public static Instances[] getInterspeech11wott(String dirInterspeech,
			Instances data, String s_key, String dir_wott, Boolean applyOnTest, String category_file) throws Exception {
		
		Instances [] sets = WekaMagic.getInterspeech2011SetsWithFile(dirInterspeech, data, s_key);
		
		//difference between all instances and the set without tongue twisters
		Instances text_wott = WekaMagic.textCSVToInstances(dir_wott + "output.csv",s_key);
		
		Instances notInSets = WekaMagic.getOutOfSets(new Instances [] { text_wott }, data, s_key);				
		
		
		//delete all tongue twisters in the Interspeech 2011 set
		Instances [] is11wott = WekaMagic.deleteFromSets(sets, notInSets, s_key);
		
		//filter by category if necessary
		is11wott = getInterspeech11ByCategory(is11wott,s_key,category_file);
		
		
		//delete corresponding file column
		for(int i=0;i<is11wott.length;i++){
			is11wott[i].deleteAttributeAt(is11wott[i].attribute(s_key).index());
		}
		
		//if the tongue twisters in the test set should stay:
		if(!applyOnTest){
			sets[2].deleteAttributeAt(sets[2].attribute(s_key).index());
			is11wott[2] = sets[2];
		}
		
		return is11wott;
	}
	
	
	public static Instances[] getInterspeech11ByCategory(Instances[] sets, String s_key, String category_file) throws Exception {
		
		if(category_file == null) return sets;
		
		//difference between all instances and the category set 
		Instances text_category = WekaMagic.textCSVToInstances(category_file,s_key);
		
		Instances data = new Instances(sets[0]);
		data.addAll(sets[1]);
		data.addAll(sets[2]);
		
		//get all instances which are not in the category set
		Instances notInSets = WekaMagic.getOutOfSets(new Instances [] { text_category }, data, s_key);				
		
		
		//delete all non category instances in the Interspeech 2011 set
		Instances [] is11category = WekaMagic.deleteFromSets(sets, notInSets, s_key);
		
		
		return is11category;
	}
	
	public static void addThreshold(ArrayList<Double> threshold){
		threshold.add(0.00000001);
		threshold.add(0.0001);
		threshold.add(0.0003);
		threshold.add(0.0006);
		threshold.add(0.001);
		threshold.add(0.002);
		threshold.add(0.0025);
		threshold.add(0.0030);
		threshold.add(0.0035);
		threshold.add(0.004);
		threshold.add(0.005);
		threshold.add(0.006);
		threshold.add(0.007);
		threshold.add(0.008);
		threshold.add(0.01);
	}
	
	public static int getNumberOfThreads(int maxThreads){
		int nrThreads=1;
		if(maxThreads <= 0){
			nrThreads = Runtime.getRuntime().availableProcessors();
			System.out.println("Number of cores: " + nrThreads);
		}else{
			nrThreads = maxThreads;
			System.out.println("Running " + nrThreads + " Threads");
		}
		return nrThreads;
	}

	public static List<List<Double>> runTestUARIS2011KNNThreads(
			Instances[] sets, Boolean withAttributeSelection, Boolean isText,
			int maxThreads, double smotep) throws InterruptedException {
		
		List<List<Double>> values = new ArrayList<List<Double>>();
		
		ArrayList<Double> threshold = new ArrayList<Double>();
		threshold.add(0.0);
		
		if (withAttributeSelection) {
			addThreshold(threshold);
		}
		
		ArrayList<Integer> Kval = new ArrayList<Integer>();
		
		Kval.add(1);
		Kval.add(3);
		Kval.add(5);
		Kval.add(7);
		Kval.add(9);
		Kval.add(11);
		
		System.out.println("Running tests for train, dev and test set...");
		
		double currentK;
		int nrThreads=getNumberOfThreads(maxThreads);
		
		MultiWeka [] threads = new MultiWeka[nrThreads];
		
		int count = 0;
		
		int wMax = Kval.size();
		int maxIter = threshold.size() * wMax;
		
		for (int i=0; i<threshold.size(); i++) {		//iterating through Threshold values
			for(int w=0; w<wMax; w++){  				//iterating through K values
					
				currentK = Kval.get(w);	// range of C
				
				// Start all threads
				threads[count%nrThreads] = new MultiWeka(WekaMagic.copyInstancesArray(sets),withAttributeSelection,isText,
														new Double[]{currentK},threshold.get(i),ClassifierE.KNN.getValue(), 5, smotep); //TODO: optimize smote
				threads[count%nrThreads].start();
				
				// If all threads are up and running
				if(count % nrThreads == nrThreads-1 || count == maxIter - 1){
					for(MultiWeka r: threads){
						r.join();
						values.add(r.getResult());
					}
				}			
				count++;
			}
		}
		
		return values;
	}

	public static List<List<Double>> runTestUARIS2011NBThreads(
			Instances[] sets, Boolean withAttributeSelection, Boolean isText,
			int maxThreads, double smotep) throws InterruptedException {
		
		List<List<Double>> values = new ArrayList<List<Double>>();
		
		ArrayList<Double> threshold = new ArrayList<Double>();
		threshold.add(0.0);
		
		if (withAttributeSelection) {
			addThreshold(threshold);
		}
		
		
		System.out.println("Running tests for train, dev and test set...");
		
		int nrThreads=getNumberOfThreads(maxThreads);
		
		MultiWeka [] threads = new MultiWeka[nrThreads];
		
		int count = 0;
		
		int maxIter = threshold.size() * 4;
		
		for (int i=0; i<threshold.size(); i++) {		//iterating through Threshold values
			for(int kernel=0;kernel<=1;kernel++){
				for(int discret=0;discret<=1;discret++){
				
					// Start all threads
					threads[count%nrThreads] = new MultiWeka(WekaMagic.copyInstancesArray(sets),withAttributeSelection,isText,
															new Double[]{(double)kernel,(double)discret},threshold.get(i),ClassifierE.NB.getValue(), 5, smotep); //TODO: optimize smote
					threads[count%nrThreads].start();
					
					// If all threads are up and running
					if(count % nrThreads == nrThreads-1 || count == maxIter - 1){
						for(MultiWeka r: threads){
							r.join();
							values.add(r.getResult());
						}
					}			
					count++;
				}
			}
		}
		
		return values;
	}
	
	public static HashMap<String,String> LoadTestMapping(String path) throws IOException{
		InputStream    fis;
		BufferedReader br;
		String         line;
		
		HashMap<String,String> testmapping = new HashMap<String,String>();
		
		fis = new FileInputStream(path);
		br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
		while ((line = br.readLine()) != null) {
			//System.out.println("\""+line+"\""); // format example: BLOCK30/SES3066/5653066001_h_01.WAV
			if(!line.isEmpty() && line != null && line.length()>5){
				String[] tokens = line.split("\t");
				
				String id 		= tokens[0].split("/")[1];
				//String classVal = (tokens[0].equals("A")?"AL":"NAL");
				String orgID	= tokens[2].split("\\.")[0];
				testmapping.put(id, orgID);
				
				//System.out.println("id: " + id + " class: " + orgID);
			}
		}
		
		// Done with the file
		br.close();
		br = null;
		fis = null;		
		
		return testmapping;
	}
	

	public static Instances[] convertOriginalToUs(Instances train,
			Instances dev, Instances test, String testmappingFile, Instances data) throws Exception {
		
		Instances sets [] = new Instances [3];
		sets[0] = new Instances(train);
		sets[1] = new Instances(dev);
		sets[2] = new Instances(test);
		
		HashMap<String, String> testmapping = LoadTestMapping(testmappingFile);
		HashMap<String, String> classmapp = FindClassMapping(testmapping,data);
		
		FastVector values = new FastVector(); 
		values.addElement("nonalc");
        values.addElement("alc");    
        sets[2].insertAttributeAt(new Attribute("NewClass", values), sets[2].numAttributes());
        
        sets[2].insertAttributeAt(new Attribute("file", (FastVector) null), sets[2].numAttributes());
        
        for(int i=0;i<sets[2].size();i++){
			String file_id = testmapping.get(sets[2].get(i).stringValue(sets[2].attribute("name")));
			sets[2].get(i).setValue(sets[2].attribute("file"), file_id);
			sets[2].get(i).setValue(sets[2].attribute("NewClass"), classmapp.get(sets[2].get(i).stringValue(sets[2].attribute("name"))));
		}
		
		sets[2].deleteAttributeAt(sets[2].attribute("name").index());
		sets[2].deleteAttributeAt(sets[2].attribute("class").index());
		sets[2].renameAttribute(sets[2].attribute("NewClass"), "class");
		
		for(int u=0;u<3;u++){
			setClassIndex(sets[u]);
			
			//System.out.println("attribute size: " + train.size());			
			//System.out.println("attribute num: " + train.numAttributes());			
			//System.out.println("class attribute: " + train.classAttribute().name());
			
			if(u<2){
				sets[u].renameAttribute(sets[u].attribute("name"), "file");
				sets[u].renameAttributeValue(sets[u].classAttribute(), "NAL", "nonalc");
				sets[u].renameAttributeValue(sets[u].classAttribute(), "AL",  "alc");
			}
		}
		
		
		return sets;
	}

	private static HashMap<String, String> FindClassMapping(
			HashMap<String, String> testmappingFile, Instances data) throws Exception {
		
		HashMap<String, String> classmap = new HashMap<String, String>();
		
		
		Iterator it = testmappingFile.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        //System.out.println(pairs.getKey() + " = " + pairs.getValue());
	        
	        Boolean found = false;
	        for(int i=0;i<data.size();i++){
	        	if(data.get(i).stringValue(data.attribute("file")).equals(pairs.getValue())){
	        		classmap.put((String)pairs.getKey(), data.get(i).stringValue(data.classAttribute()));
	        		found = true;
	        		break;
	        	}
	        }
	        if(found == false){
	        	System.out.println("One file id is only present in one file: " + (String)pairs.getKey());
				throw new Exception();
	        }
	        
	        //it.remove(); // avoids a ConcurrentModificationException
	    }
		return classmap;
	}
	
}
