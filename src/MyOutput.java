import weka.core.Instances;
import java.util.Arrays;
import weka.core.OptionHandler;

public class MyOutput {
	private Instances data;
	private Object operation;
	private long elapsedTime;

	public MyOutput(Instances data, Object operation, long elapsedTime) {
		super();
		this.data = data;
		this.operation = operation;
		this.elapsedTime = elapsedTime;
	}

	public long getElapsedTime() {
		return elapsedTime;
	}

	public void setElapsedTime(long elapsedTime) {
		this.elapsedTime = elapsedTime;
	}

	public Instances getData() {
		return data;
	}

	public void setData(Instances data) {
		this.data = data;
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

	public String toString() {
		String s = getName() + ": " + Arrays.toString(getParams())
				+ " exec-time: " + elapsedTime;
		return s;
	}

	public void print() {
		System.out.println(this.toString());
	}

}
