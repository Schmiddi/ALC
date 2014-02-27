package team2014.test;
import java.io.*;

import weka.core.Instances;
import weka.core.converters.TextDirectoryLoader;
import weka.filters.unsupervised.attribute.StringToWordVector;
import weka.core.stemmers.*;
import weka.core.tokenizers.*;
import weka.core.SelectedTag;
import weka.filters.Filter;

public class LoadDir {
	public static void main(String[] args) throws Exception {
		// String currDir = args[0];

		/**
		 * 
		 * Set parameter of input function
		 */
		String currDir = "C:\\Users\\IBM_ADMIN\\Dropbox\\Detecting Alcohol Intoxication in Speech\\Felix\\Backup\\DP";
		int WordsToKeep = 1000000;

		Boolean Ngram = true;
		int ngram_min = 1;
		int ngram_max = 3;

		Boolean LowerCase = true;
		Boolean NormalizeDocLength = false;
		Boolean Stemming = true;

		Boolean OutputWordCounts = true;
		Boolean IDFTransform = false;
		Boolean TFTransform = false;

		/*
		 * 
		 * All parameter are set
		 */

		if (IDFTransform || TFTransform || NormalizeDocLength)
			OutputWordCounts = true;

		String[] fullPath = currDir.split("/");
		String fileName = fullPath[fullPath.length - 1];

		TextDirectoryLoader loader = new TextDirectoryLoader();
		loader.setDirectory(new File(currDir));
		Instances dataRaw = loader.getDataSet();

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
			nt.setNGramMinSize(ngram_max);

			t = nt;
		}

		filter.setTokenizer(t);

		filter.setInputFormat(dataRaw);
		Instances dataFiltered = Filter.useFilter(dataRaw, filter);

		FileWriter fstream = new FileWriter(fileName + ".arff");
		BufferedWriter out = new BufferedWriter(fstream);
		out.write(dataFiltered.toString());
		out.close();
		System.out.println(fileName + ".arff created");

	}
}