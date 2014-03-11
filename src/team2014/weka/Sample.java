package team2014.weka;

import weka.core.Attribute;
import weka.core.Instance;

public class Sample {	
	private Instance ins;
	private Attribute key; //attribute which contains filename
		
	public Sample(Instance ins, Attribute key){		
		this.ins			= ins;
		this.key 			= key;
	}
	
	public String getAlc() {
		return ins.stringValue(ins.classAttribute());
	}

	public Instance getIns() {
		return ins;
	}
	public void setIns(Instance ins) {
		this.ins = ins;
	}



	public String getUser_idStr() {
		return ins.stringValue(key).substring(0,3);
	}
	
	public int getUser_id() {
		return Integer.parseInt(getUser_idStr());
	}

	
	public String getBlock_nrStr() {
		return ins.stringValue(key).substring(3,5);
	}
	public int getBlock_nr() {
		return Integer.parseInt(getBlock_nrStr());
	}

	public String getSession_nrStr() {
		return ins.stringValue(key).substring(5,7);
	}
	
	public int getSession_nr() {
		return Integer.parseInt(getSession_nrStr());
	}

	public String getRecording_nrStr() {
		return ins.stringValue(key).substring(7,10);
	}
	
	public int getRecording_nr() {
		return Integer.parseInt(getRecording_nrStr());
	}

	public String getType() {
		return ins.stringValue(key).substring(11,12);
	}


	public String getVersionStr() {
		return ins.stringValue(key).substring(13,15);
	}
	
	public int getVersion() {
		return Integer.parseInt(getVersionStr());
	}
	
	public String getFilename(){
		//return getUser_idStr() + getBlock_nrStr() + getSession_nrStr() + getRecording_nrStr() + "_" + getType() + "_" + getVersionStr();
		return ins.stringValue(key);
	}
	
	public Boolean isIntoxicated(){
		String alc = ins.stringValue(ins.classAttribute()); //get class attribute value from instance		
		if(alc.toUpperCase().contains("NON")){
			return false;
		}
		return true;
	}
	
	public Boolean isSober(){
		return !isIntoxicated();
	}
	
}
