package team2014.weka.svm;

public enum KernelType {
	LINEAR(1), RBF(2);
    private int value;

    private KernelType(int value) {
            this.value = value;
    }
    
    
    
    public int getValue() {
		return value;
	}

    public void setValue(int value) {
		this.value = value;
	}

	public static String toString(int val){
		switch(val){
			case 1: return "Linear Kernel";
			case 2: return "RBF Kernel";   		
		}
		return null;
	}
}
