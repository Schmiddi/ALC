import weka.core.Instances;
import java.util.Arrays;
import weka.core.OptionHandler;

public class MyOutput {
	private Instances [] data = new Instances[2];
	private Object operation;
	private long elapsedTime;

	public MyOutput(Instances data, Object operation, long elapsedTime) {
		super();
		this.data[0] = data;
		this.operation = operation;
		this.elapsedTime = elapsedTime;
	}
	
	public MyOutput(Instances [] data, Object operation, long elapsedTime) {
		super();
		this.data = data;
		this.operation = operation;
		this.elapsedTime = elapsedTime;
	}
	
	public void setData(Instances[] data) {
		this.data = data;
	}

	public long getElapsedTime() {
		return elapsedTime;
	}

	public void setElapsedTime(long elapsedTime) {
		this.elapsedTime = elapsedTime;
	}

	public Instances getData() {
		return data[0];
	}
	
	public Instances getTrainData() {
		return data[0];
	}
	
	public Instances getTestData() {
		return data[1];
	}

	public void setData(Instances data) {
		this.data[0] = data;
	}
	
	public void setTrainData(Instances data) {
		this.data[0] = data;
	}
	
	public void setTestData(Instances data) {
		this.data[1] = data;
	}

	public Object getOperation() {
		return operation;
	}

	public void setOperation(Object operation) {
		this.operation = operation;
	}

	public String getName() {
		String s = operation.getClass().getName();
		return s;
	}

	public String[] getParams() {
		OptionHandler oh = (OptionHandler) operation;
		return oh.getOptions();
	}
	
	public int getFeatureNumber(){
		return data[0].numAttributes();
	}

	public String toString() {
		String s = getName() + ": " + Arrays.toString(getParams())
				+ "\nexec-time: " + elapsedTime 
				+ "\nNumber of features: " + getFeatureNumber()
				+ "\n";
			
		return s;
	}

	public void print() {
		System.out.println(this.toString());
	}

}
