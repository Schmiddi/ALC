package team2014.weka;

import java.util.ArrayList;
import java.util.Random;

import team2014.weka.speaker.Sample;
import team2014.weka.speaker.Speaker;
import team2014.weka.speaker.SpeakerSamples;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.Instances;
import weka.filters.Filter;

public class CrossValidationOutput {
	private ArrayList<MyClassificationOutput> trainingEval;
	private ArrayList<MyClassificationOutput> testEval;
	private ArrayList<ArrayList<Filter>> filters;
	
	private ArrayList<ArrayList<String>> train_key_set;
	private ArrayList<ArrayList<String>> test_key_set;
	private int folds;
	private int randseed;
	
	private Instances table;
	private Attribute key;
	
	//new cross validation
	private ArrayList<ArrayList<SpeakerSamples>> train_sp_set;
	private ArrayList<ArrayList<SpeakerSamples>> test_sp_set;
	
	
	public CrossValidationOutput(){
		trainingEval = new ArrayList<MyClassificationOutput>();
		testEval = new ArrayList<MyClassificationOutput>();
		filters = new ArrayList<ArrayList<Filter>>();
	}
	
	public CrossValidationOutput(Instances table, String s_key, ArrayList<Speaker> speakers){
		//initialize anything to null
		trainingEval = new ArrayList<MyClassificationOutput>();
		testEval = new ArrayList<MyClassificationOutput>();
		filters = new ArrayList<ArrayList<Filter>>();
		
		//initialize input configuration
		this.table = table;
		folds = 10;
		randseed = 1;
		
		//get key attribute
		key = null;
		for(int i=0;i<table.numAttributes();i++){
			if(table.attribute(i).isString() && !table.attribute(i).isNominal() && table.attribute(i).name().equals(s_key)){
				key = table.attribute(i);
				break;
			}
		}
		
		//randomize instances
		Instances runInstances = new Instances(table);
	    Random random = new Random(randseed);
	    runInstances.randomize(random);
	    
	    
	    //get all instances in form of speaker samples
	    ArrayList<SpeakerSamples> table_speaker_samples = new ArrayList<SpeakerSamples>();
	    for(int i=0;i<table.size();i++){
	    	Sample current = new Sample(table.get(i),key);
	    	
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
	    		System.out.println("No fitting speaker found for: \"" + table.get(i).stringValue(key) + "\"");
	    	}
	    }
	    
	    //---------------------------------------------------------------------------------------
	    //test 
	    //---------------------------------------------------------------------------------------
	    
	    System.out.println("total number of instances: " + table.size());
	    
	    int total     =0;
	    int male      =0;
	    int alc       =0;
	    
	    int male120   =0;
	    int male90    =0;
	    int female120 =0;
	    int female90  =0;
	    int totalSpeaker =0;
	    
	    for(int i=0;i<table_speaker_samples.size();i++){
	    	totalSpeaker++;
	    	System.out.println("Speaker (" + table_speaker_samples.get(i).getSpeaker().getSex() + "): "+ (double)table_speaker_samples.get(i).getNumberOfIntoxicated() / table_speaker_samples.get(i).getSize());
	    	
	    	System.out.println(table_speaker_samples.get(i).getSize());
	    	total += table_speaker_samples.get(i).getSize();
	    	alc += table_speaker_samples.get(i).getNumberOfIntoxicated();
	    	if(table_speaker_samples.get(i).getSpeaker().isMale()) {
	    		male += table_speaker_samples.get(i).getSize();
	    		if(table_speaker_samples.get(i).getSize()==90){
	    			male90++;
	    		}else{
	    			male120++;
	    		}
	    	}
	    	else{
	    		if(table_speaker_samples.get(i).getSize()==90){
	    			female90++;
	    		}else{
	    			female120++;
	    		}
	    	}
	    }
	    
	    System.out.println("total: " + total + " male: "+ male + " female: " + (total-male));
	    System.out.println("alc: "+ alc + " sober: " + (total-alc));
	    
	    System.out.println("female 120: " + female120 + " female 90: " + female90);
	    System.out.println("male 120: " + male120 + " male 90: " + male90);
	    
	    System.out.println("number of speaker: " + totalSpeaker);
	    
	    
	    //---------------------------------------------------------------------------------------
	    //test end
	    //---------------------------------------------------------------------------------------
	    
	    
	    if (runInstances.classAttribute().isNominal() && (folds > 1)) {
	    	//stratify by class, gender - make it speaker independent
	    	
	    }
		
	    
	    train_key_set = new ArrayList<ArrayList<String>>();
		test_key_set  = new ArrayList<ArrayList<String>>();
	}
	
	public CrossValidationOutput(Instances table, String s_key){
		trainingEval = new ArrayList<MyClassificationOutput>();
		testEval = new ArrayList<MyClassificationOutput>();
		filters = new ArrayList<ArrayList<Filter>>();
		
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
	
	public void addFilter(Filter filter, int foldNr){
		if(foldNr == 0){
			ArrayList<Filter> newfilter = new ArrayList<Filter>();
			filters.add(newfilter);
		}
		filters.get(filters.size()-1).add(foldNr,filter);
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
	
	
	/**
	 * use all filters which have been used in the cross validation for one certain fold (0-x) on given instances
	 * 
	 * @param data - instances which will be processed
	 * @param foldNr - fold number of cross validation 0 - n
	 * @return
	 * @throws Exception
	 */
	public Instances processDataByFilters(Instances data, int foldNr) throws Exception{
		Instances pData = new Instances(data);
		if(filters != null){
			//apply all filters
			for(ArrayList<Filter> fl : filters){
			      Filter f = fl.get(foldNr);          //get filter for fold number x
				  pData = Filter.useFilter(pData, f); //use filter 		   
			}
		}
		return pData;
	}
}
