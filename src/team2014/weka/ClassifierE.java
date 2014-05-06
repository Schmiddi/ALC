package team2014.weka;

public enum ClassifierE {
	LOGISTIC(1), SVM(2),KNN(3);
    private int value;

    private ClassifierE(int value) {
            this.value = value;
    }
    
    
    
    public int getValue() {
		return value;
	}

    public static String getParameterNameByID(int classifier, int id){
    	if(classifier == LOGISTIC.value){
    		return "ridge";
    	}
    	if(classifier == KNN.value){
    		return "K";
    	}
    	if(classifier == SVM.value){
    		switch(id){
    			case 0: return "C";
    			case 1: return "gamma";
    		}
    	}
    	return "";
    }

	public void setValue(int value) {
		this.value = value;
	}

	public static String toString(int val){
		switch(val){
			case 1: return "Logistic Regression";
			case 2: return "SVM"; 
			case 3: return "KNN";  
		}
		return null;
	}
}
