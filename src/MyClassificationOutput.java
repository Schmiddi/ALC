import java.util.Arrays;
import weka.core.OptionHandler;
import weka.classifiers.Evaluation;

public class MyClassificationOutput {
	private Object classifier;
	private Evaluation eval;
	private String eval_options;
	private long elapsedTime;
	private Evaluation test_eval;

	public MyClassificationOutput(Object classifier, Evaluation eval, Evaluation real_eval,
			String eval_options, long elapsedTime) {
		super();
		this.classifier = classifier;
		this.eval = eval;
		this.test_eval = real_eval;
		this.eval_options = eval_options;
		this.elapsedTime = elapsedTime;
	}
	
	
	
	
	public Evaluation getTest_eval() {
		return test_eval;
	}
	public void setTest_eval(Evaluation test_eval) {
		this.test_eval = test_eval;
	}
	public long getElapsedTime() {
		return elapsedTime;
	}

	public void setElapsedTime(long elapsedTime) {
		this.elapsedTime = elapsedTime;
	}

	public Object getClassifier() {
		return classifier;
	}

	public void setClassifier(Object classifier) {
		this.classifier = classifier;
	}

	public Evaluation getEval() {
		return eval;
	}

	public void setEval(Evaluation eval) {
		this.eval = eval;
	}

	public String getEval_options() {
		return eval_options;
	}

	public void setEval_options(String eval_options) {
		this.eval_options = eval_options;
	}

	public String[] getClassifierParams() {
		OptionHandler oh = (OptionHandler) classifier;
		return oh.getOptions();
	}

	public double getUARTrain() {
		double unweightedAverageRecall = (eval.recall(0) + eval.recall(1)) / 2;
		return unweightedAverageRecall;
	}
	public double getUARTest() {
		double unweightedAverageRecall = (test_eval.recall(0) + test_eval.recall(1)) / 2;
		return unweightedAverageRecall;
	}

	public String toString() {
		String s = "Training:\n"
		         + "weka.classifiers.Evaluation: " + getEval_options()
				 + "\nexec-time: " + elapsedTime + "\n\n";
		s += classifier.getClass().getName() + ": "
				+ Arrays.toString(getClassifierParams()) + "\n\n";

		s += "\nunweightedAverageRecall = " + getUARTrain();
		s += "\n" + eval.toSummaryString();
	    s += "\n\n" + "----------------------------------------------------------------------------" + "\n\n";
	    s += "Test:\n";
		s += "\nunweightedAverageRecall = " + getUARTest();
		s += "\n" + test_eval.toSummaryString();
		s += "\n\n";
		return s;
	}

	public void print() {
		System.out.println(this.toString());
	}

}
