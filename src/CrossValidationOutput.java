import java.util.ArrayList;

import weka.classifiers.Classifier;
import weka.filters.Filter;

public class CrossValidationOutput {
	private ArrayList<MyClassificationOutput> trainingEval;
	private ArrayList<MyClassificationOutput> testEval;
	private ArrayList<Filter> filters;
	
	public CrossValidationOutput(){
		trainingEval = new ArrayList<MyClassificationOutput>();
		testEval = new ArrayList<MyClassificationOutput>();
		filters = new ArrayList<Filter>();
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
