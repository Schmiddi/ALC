import java.io.*;
import java.util.List;
import java.util.Random;
import java.util.Iterator;
import java.util.Map;
import java.util.ArrayList;
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

public class WekaMagic {

	public static MyOutput loadText(String currDir) throws Exception {
		TextDirectoryLoader loader = new TextDirectoryLoader();
		loader.setDirectory(new File(currDir));

		long startTime = System.currentTimeMillis();
		Instances dataRaw = loader.getDataSet();
		long stopTime = System.currentTimeMillis();
		long elapsedTime = stopTime - startTime;

		return new MyOutput(dataRaw, loader, elapsedTime);
	}

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
		Instances dataFiltered = filter.useFilter(dataRaw, filter);
		long stopTime = System.currentTimeMillis();
		long elapsedTime = stopTime - startTime;

		return new MyOutput(dataFiltered, filter, elapsedTime);
	}

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
		Instances selected = as.useFilter(data, as);
		long stopTime = System.currentTimeMillis();
		long elapsedTime = stopTime - startTime;

		return new MyOutput(selected, as, elapsedTime);
	}

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
		// String currDir = args[0];

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