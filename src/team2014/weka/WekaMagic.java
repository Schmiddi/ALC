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


import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import weka.core.FastVector;
import weka.core.Instances;
import weka.core.converters.CSVLoader;
import weka.core.converters.TextDirectoryLoader;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.unsupervised.attribute.StringToWordVector;
import weka.core.stemmers.*;
import weka.core.tokenizers.*;
import weka.core.SelectedTag;
import weka.classifiers.functions.Logistic;
import weka.classifiers.functions.SMO;
import weka.classifiers.Evaluation;
import weka.filters.Filter;
import weka.core.Attribute;
import weka.classifiers.Classifier;

import org.languagetool.JLanguageTool;
import org.languagetool.rules.RuleMatch;
import org.languagetool.language.*;

import com.sun.org.apache.xpath.internal.operations.Mult;

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
	public static int setClassIndex(Instances data){
		int i=-1;
		for(i=0;i<data.numAttributes();i++){
			if(data.attribute(i).isNominal() && (data.attribute(i).toString().contains("alc,nonalc") || data.attribute(i).toString().contains("nonalc,alc"))){
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
	public static void printHashMap(List<List<Double>> m, String[] header, String path)
			throws Exception {
		FileWriter fstream;
		BufferedWriter out;
		int i;

		fstream = new FileWriter(path);
		out = new BufferedWriter(fstream);


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
		
		if(merged.attribute("class") != null){
			merged.setClass(merged.attribute("class"));
		}else{
			setClassIndex(merged);
		}
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
	
	/**
	 * Run our own cross validation
	 * 
	 * @param classifier - 
	 * @param sets
	 * @param filters - filters which have to be applied first
	 * @return
	 * @throws Exception
	 */
	public static MyClassificationOutput[] validationIS2011(Instances[] sets, ArrayList<MyOutput> filters, double currentRidge) throws Exception{
		
		MyClassificationOutput [] output = new MyClassificationOutput[5];
		
		Instances [] sets1 = new Instances[3];
		
		if(filters == null){
			System.out.println("No filters!");
		}
		sets1 = applyFilters(sets,filters);	//something went wrong here
		

		MyClassificationOutput currentResult = WekaMagic.runLogistic(sets1[SetType.TRAIN.ordinal()], currentRidge, 5);
		
		for(int i=0;i<sets1.length;i++){
			output[i] = WekaMagic.applyLogistic(sets1[i], currentResult);
		}
		
		//create model on test + training set
		Instances trainDev = new Instances(sets[SetType.TRAIN.ordinal()]);
		trainDev.addAll(sets[SetType.DEV.ordinal()]);
		
		Instances [] sets2 = new Instances[2];
		sets2[0] = trainDev;
		sets2[1] = new Instances(sets[SetType.TEST.ordinal()]);
		
		//the instances have to be filtered again since attribute selection makes things different
		sets2 = applyFilters(sets2,filters);
		
		currentResult = WekaMagic.runLogistic(sets2[0], currentRidge, 5);
		
		output[SetType.TRAINDEV.ordinal()] = WekaMagic.applyLogistic(sets2[0], currentResult);
		output[SetType.TRAINDEVTEST.ordinal()] = WekaMagic.applyLogistic(sets2[1],  currentResult);
		
		return output;
	}
	
	/*
	public static MyClassificationOutput[] validationSVM(Instances[] sets, ArrayList<MyOutput> filters, double currentRidge) throws Exception{
		
		MyClassificationOutput [] output = new MyClassificationOutput[3];
		
		sets = applyFilters(sets,filters);

		//MyClassificationOutput currentResult = WekaMagic.runSVM(train, C, epsilon)
		//	WekaMagic.runLogistic(sets[SetType.TRAIN.ordinal()], currentRidge, 5);
		
		output[SetType.TRAIN.ordinal()] = WekaMagic.applyLogistic(sets[SetType.TRAIN.ordinal()], currentResult);
		output[SetType.DEV.ordinal()] = WekaMagic.applyLogistic(sets[SetType.DEV.ordinal()], currentResult);
		output[SetType.TEST.ordinal()] = WekaMagic.applyLogistic(sets[SetType.TEST.ordinal()],  currentResult);
		
		return output;
	}*/
	
	public static Instances [] applyFilters(Instances [] sets, ArrayList<MyOutput> filters) throws Exception{
		Instances [] retsets = WekaMagic.copyInstancesArray(sets);
		if(filters != null){
			//apply all filters
		    for(MyOutput m : filters){
		    	Filter f = (Filter)m.getOperation();
			    f.setInputFormat(new Instances(sets[SetType.TRAIN.ordinal()])); //use training data to build the filter
			   
			    if(f instanceof StringToWordVector){
			    	int  [] attributes = new int[1];
			    	attributes[0] =	sets[SetType.TRAIN.ordinal()].attribute("text").index(); //eventuell +1 ???
			    	((StringToWordVector)f).setAttributeIndicesArray(attributes);						   
			    }
			    
			    for(int i=0;i<sets.length;i++){
			    	retsets[i] = Filter.useFilter(retsets[i], f); //use filter on the training data
				}
		    }
		}
		
		return retsets;
	}
	
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
				
				MyClassificationOutput [] output = WekaMagic.validationIS2011(sets, filters, currentRidge);
	
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
	
	public static List<List<Double>> runTestUARIS2011Threads(Instances [] sets, Boolean withAttributeSelection) throws Exception {
		return WekaMagic.runTestUARIS2011Threads(sets, withAttributeSelection, false);
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
	public static List<List<Double>> runTestUARIS2011Threads(Instances [] sets, Boolean withAttributeSelection, Boolean isText) throws Exception {
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
				
		System.out.println("Running tests for train, dev and test set...");
		
		int cores = Runtime.getRuntime().availableProcessors();
		System.out.println("Number of cores: " + cores);
		
		MultiWeka [] threads = new MultiWeka[cores];
		
		//Iterate through different ridge values
		int uMax = 15;
		int maxIter = threshold.size() * uMax;
		
		int count = 0;
		
		for (int i = 0; i < threshold.size(); i++) {
			for (int u=0;u<uMax;u++)
			{
				
				currentRidge = stdRidge * (Math.pow(10, u));
				
				// Start all threads
				threads[count%cores] = new MultiWeka(WekaMagic.copyInstancesArray(sets),withAttributeSelection,isText,currentRidge,threshold.get(i)); 
				threads[count%cores].start();
				
				// If all threads are up and running
				if(count % cores == cores-1 || count == maxIter - 1){
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
	
	public static Instances[] copyInstancesArray(Instances [] sets){
		Instances [] copy = new Instances [sets.length];
		for(int i=0;i<sets.length;i++){
			copy[i] = new Instances(sets[i]);
		}
		return copy;
	}
	
	public static void saveResultIS2011(List<List<Double>> results, String outputFolder, Boolean withAttributeSelection, String type) throws Exception{
		// Create timestamp
		Date timestamp = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm");
		
		// Create attr string if with attribute selection
		String attr = "";
		if(withAttributeSelection)
			attr = "attr";
		
		
		// CSV Header
		String[] header = {"Threshold","Ridge","Train UAR", "Dev UAR", "Test UAR", "Train+Dev UAR", "Train+Dev vs Test UAR"};
		//save to CSV
		WekaMagic.printHashMap(results, header, outputFolder + type + "_IS2011"+attr+sdf.format(timestamp) + ".csv");
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
	
	
	/**
	 * method to process the text data to make it suitable to generate the language model
	 * 
	 * @param s - string which has to be processed
	 * @return
	 */
	public static String cleanString(String s){
		String str = s;
		
		String tb_removed = ".*~+_-/\\";
		
		//remove chararacters
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
		Instances sound = WekaMagic.soundArffToInstances(arffdir);		  //get sound data
		Instances text = WekaMagic.textCSVToInstances(csv_file,"file");   //get text data (with class)
		
		Instances data = WekaMagic.mergeInstancesBy(sound, text, "file"); //merge text and sound data by using the key fileid
				
		//delete unnecessary attributes
		data.deleteAttributeAt(data.attribute("text").index()); 		  //remove text attribute
		//data.deleteAttributeAt(data.attribute("file").index());		//remove key(file id) attribute
		//data.deleteAttributeAt(data.attribute("numeric_class").index());
    	
    	return data;
	}
	
	
	public static Instances getGrammarInstances(String csv_file, Boolean normalizeSentenceLength) throws Exception{
		Instances text = textCSVToInstances(csv_file,"file"); //get text features
		Instances grammar = checkGrammar(text, normalizeSentenceLength); //get grammar features
		
		grammar.deleteAttributeAt(grammar.attribute("text").index()); 		  //remove text attribute
		grammar.deleteAttributeAt(grammar.attribute("file").index());			  //remove key(file id) attribute
		
		
		return grammar;
	}
	
	public static Instances getGrammarInstancesWithFile(String csv_file, Boolean normalizeSentenceLength) throws Exception{
		Instances text = textCSVToInstances(csv_file,"file"); //get text features
		Instances grammar = checkGrammar(text, normalizeSentenceLength); //get grammar features
		
		grammar.deleteAttributeAt(grammar.attribute("text").index()); 		  //remove text attribute

		return grammar;
	}
	
	
	
	public static MyClassificationOutput runSVM(Instances train, Double C, Double epsilon)
	throws Exception {

		SMO svm = new SMO();
		long elapsedTime;
		Evaluation eval = null;
		String options;
		
		//set regularization parameter
		if(C != null){
			svm.setC(C);
		}
		if(epsilon != null){
			svm.setEpsilon(epsilon);
		}
		
		svm.setNumFolds(10);
		svm.setRandomSeed(1);
		
		options = "single run";	
		
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
	
	public static Instances[] getInterspeech2011Sets(String dir, Instances allInstances, String fileAttribute) throws Exception{
		Instances [] sets = new Instances [3];
		
		sets = getInterspeech2011SetsWithFile(dir, allInstances, fileAttribute);
		
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
		
		System.out.println("size merged: " + merged.size());
		
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
			}
		}
		
		return outOfSets;
	}

	
}
