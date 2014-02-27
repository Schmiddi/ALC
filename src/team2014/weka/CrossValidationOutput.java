package team2014.weka;

import java.util.ArrayList;
import java.util.Random;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.Instances;
import weka.filters.Filter;

public class CrossValidationOutput {
	private ArrayList<MyClassificationOutput> trainingEval;
	private ArrayList<MyClassificationOutput> testEval;
	private ArrayList<Filter> filters;
	
	private ArrayList<ArrayList<String>> train_key_set;
	private ArrayList<ArrayList<String>> test_key_set;
	private int folds;
	private int randseed;
	
	private Instances table;
	private Attribute key;
	
	
	
	public CrossValidationOutput(){
		trainingEval = new ArrayList<MyClassificationOutput>();
		testEval = new ArrayList<MyClassificationOutput>();
		filters = new ArrayList<Filter>();
	}
	
	public CrossValidationOutput(Instances table, String s_key){
		trainingEval = new ArrayList<MyClassificationOutput>();
		testEval = new ArrayList<MyClassificationOutput>();
		filters = new ArrayList<Filter>();
		
		this.table = table;
		folds = 10;
		randseed = 1;
		
		key = null;
		for(int i=0;i<table.numAttributes();i++){
			if(table.attribute(i).isString() && !table.attribute(i).isNominal() && table.attribute(i).name().equals(s_key)){
				key = table.attribute(i);
				break;
			}
		}
		
		Instances runInstances = new Instances(table);
	    Random random = new Random(randseed);
	    runInstances.randomize(random);
	    if (runInstances.classAttribute().isNominal() && (folds > 1)) {
	      runInstances.stratify(folds);
	    }
		
	    
	    train_key_set = new ArrayList<ArrayList<String>>();
		test_key_set  = new ArrayList<ArrayList<String>>();
	    
		
		//save which file is in the training set / test set
	    ArrayList<String> keylist = null;
    	for (int n = 0; n < folds; n++) {
		   Instances train = runInstances.trainCV(folds, n);
		   
		   keylist = new ArrayList<String>();
		   for(int i=0;i<train.size();i++){
			   keylist.add(train.get(i).stringValue(key));
		   }
		   train_key_set.add(n,keylist);
		   
		   Instances test = runInstances.testCV(folds, n);
		   
		   keylist = new ArrayList<String>();
		   for(int i=0;i<test.size();i++){
			   keylist.add(test.get(i).stringValue(key));
		   }
		   test_key_set.add(n,keylist);		   
		   
    	}	
	}
	
	public Instances getTrainSet(int n){
		ArrayList<String> keys = train_key_set.get(n);
		
		Instances train = new Instances(table);
		train.delete();
		
		for(int i=0;i<keys.size();i++){
			for(int u=0;u<table.size();u++){
				if(table.get(u).stringValue(key).equals(keys.get(i))){
					train.add(table.get(u));
					break;
				}
			}
		}
		return train;
	}
	
	public Instances getTrainSetWOString(int n){
		Instances data = getTrainSet(n);
		data.deleteStringAttributes();
		return data;
	}
	
	public Instances getTestSet(int n){
		ArrayList<String> keys = test_key_set.get(n);
		
		Instances test = new Instances(table);
		test.delete();
		
		for(int i=0;i<keys.size();i++){
			for(int u=0;u<table.size();u++){
				if(table.get(u).stringValue(key).equals(keys.get(i))){
					test.add(table.get(u));
					break;
				}
			}
		}
		return test;
	}
	
	public Instances getTestSetWOString(int n){
		Instances data = getTestSet(n);
		data.deleteStringAttributes();
		return data;
	}
	
	public void addFilter(Filter filter){
		filters.add(filter);
	}
	
	public void addTrainingEval(int foldNr, MyClassificationOutput eval){
		trainingEval.add(foldNr, eval);
	}
	public void addTestEval(int foldNr, MyClassificationOutput eval){
		testEval.add(foldNr, eval);
	}
	
	public int numFolds(){
		return trainingEval.size();
	}
	
	public double getTestF1Score(){
		double fscore = 0;
		for(MyClassificationOutput eval: testEval){
			fscore += eval.getF1Score();
		}
		fscore /= numFolds();
		return fscore;
	}
	
	public double getTestUAR(){
		double fscore = 0;
		for(MyClassificationOutput eval: testEval){
			fscore += eval.getUAR();
		}
		fscore /= numFolds();
		return fscore;
	}
	
	public double getTestF1ScoreMax(){
		double fscore = -1;
		for(MyClassificationOutput eval: testEval){
			if(eval.getF1Score()>fscore){
				fscore = eval.getF1Score();
			}
		}
		return fscore;
	}
	public double getTestF1ScoreMin(){
		double fscore = 2;
		for(MyClassificationOutput eval: testEval){
			if(eval.getF1Score()<fscore){
				fscore = eval.getF1Score();
			}
		}
		return fscore;
	}
	
	public Classifier getBestClassifier(){
		return (Classifier)getBest().getClassifier();
	}
	public MyClassificationOutput getBest(){
		double fscore = -1;
		MyClassificationOutput best = null;
		for(MyClassificationOutput eval: testEval){
			if(eval.getF1Score()>fscore){
				fscore = eval.getF1Score();
				best = eval;
			}
		}
		return best;
	}
	
	
	public double getTrainF1Score(){
		double fscore = 0;
		for(MyClassificationOutput eval: trainingEval){
			fscore += eval.getF1Score();
		}
		fscore /= numFolds();
		return fscore;
	}
	
	public double getTrainUAR(){
		double fscore = 0;
		for(MyClassificationOutput eval: trainingEval){
			fscore += eval.getUAR();
		}
		fscore /= numFolds();
		return fscore;
	}
	
	public double getTrainF1ScoreMax(){
		double fscore = -1;
		for(MyClassificationOutput eval: trainingEval){
			if(eval.getF1Score()>fscore){
				fscore = eval.getF1Score();
			}
		}
		return fscore;
	}
	public double getTrainF1ScoreMin(){
		double fscore = 2;
		for(MyClassificationOutput eval: trainingEval){
			if(eval.getF1Score()<fscore){
				fscore = eval.getF1Score();
			}
		}
		return fscore;
	}
}
