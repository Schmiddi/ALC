package team2014.weka;

import java.util.ArrayList;
import java.util.HashSet;



public class SpeakerSamples {
	private ArrayList<Sample> files;
	private Speaker speaker;
	
	
	
	public SpeakerSamples(ArrayList<Sample> files, Speaker speaker) {
		super();
		this.files = files;
		this.speaker = speaker;
	}
	
	public SpeakerSamples(Speaker speaker) {
		super();
		this.files = new ArrayList<Sample>();
		this.speaker = speaker;
	}
	
	public SpeakerSamples(Speaker speaker, Sample file) {
		super();
		this.files = new ArrayList<Sample>();
		this.speaker = speaker;
		files.add(file);
	}
	
	
	public Boolean isElementofSample(String filename){
		for(int i=0;i<files.size();i++){
			if(files.get(i).equals(filename)){
				return true;
			}
		}
		return false;
	}
	
	public void addFile(Sample filename){
		files.add(filename);
	}
	
	public ArrayList<Sample> getFiles() {
		return files;
	}
	public void setFiles(ArrayList<Sample> files) {
		this.files = files;
	}
	public Speaker getSpeaker() {
		return speaker;
	}
	public void setSpeaker(Speaker speaker) {
		this.speaker = speaker;
	}
	public int getSize(){
		return files.size();
	}
	public int getNumberOfIntoxicated(){
		int sum=0;
		for(Sample s:files){
			if(s.isIntoxicated()) sum++;
		}
		return sum;
	}
	public int getNumberOfSober(){
		return getSize()-getNumberOfIntoxicated();
	}
	
	public int getNumberOfExperimentByNr(int rec){
		int sum=0;
		for(Sample s:files){
			if(s.getRecording_nr() == rec){
				sum++;
			}
		}
		return sum;
	}
	
	public HashSet<Integer> getDistincRecTypeSet(){
		HashSet<Integer> list = new HashSet<Integer>();
		
		for(Sample s: files){
			list.add(s.getRecording_nr());
		}
		
		return list;
	}

	public int getALCByRecType(int type) {
		int sum=0;
		for(Sample s: files){
			if(s.getRecording_nr()==type && s.isIntoxicated()){
				sum++;
			}
		}
		return sum;
	}
	
	public int getNONALCByRecType(int type) {
		int sum=0;
		for(Sample s: files){
			if(s.getRecording_nr()==type && s.isSober()){
				sum++;
			}
		}
		return sum;
	}
	
}
