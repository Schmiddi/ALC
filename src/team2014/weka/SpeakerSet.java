package team2014.weka;

import java.util.ArrayList;
import java.util.HashSet;

import weka.core.Instances;

public class SpeakerSet {
	private ArrayList<SpeakerSamples> speakerSamples;

	public SpeakerSet(ArrayList<SpeakerSamples> speakerSamples) {
		super();
		this.speakerSamples = speakerSamples;
	}

	public ArrayList<SpeakerSamples> getSpeakerSamples() {
		return speakerSamples;
	}

	public void setSpeakerSamples(ArrayList<SpeakerSamples> speakerSamples) {
		this.speakerSamples = speakerSamples;
	}
	
	public int getNumberALC(){
		int count = 0;
		for(SpeakerSamples s: speakerSamples){
			count += s.getNumberOfIntoxicated();
		}
		return count;
	}
	public int getNumberNONALC(){
		int count = 0;
		for(SpeakerSamples s: speakerSamples){
			count += s.getNumberOfSober();
		}
		return count;
	}
	
	public int getNumberFemaleSamples(){
		int count = 0;
		for(SpeakerSamples s: speakerSamples){
			if(s.getSpeaker().getSex().equals("F")){
				count += s.getSize();
			}
		}
		return count;
	}
	
	public int getNumberFemale(){
		int count = 0;
		for(SpeakerSamples s: speakerSamples){
			if(s.getSpeaker().getSex().equals("F")){
				count++;
			}
		}
		return count;
	}
	
	public int getNumberMaleSamples(){
		int count = 0;
		for(SpeakerSamples s: speakerSamples){
			if(s.getSpeaker().getSex().equals("M")){
				count += s.getSize();
			}
		}
		return count;
	}
	
	public int getNumberMale(){
		int count = 0;
		for(SpeakerSamples s: speakerSamples){
			if(s.getSpeaker().getSex().equals("M")){
				count++;
			}
		}
		return count;
	}
	
	public int numberSpeakers(){
		return speakerSamples.size();
	}
	
	public int numberSamples(){
		int count = 0;
		for(SpeakerSamples s: speakerSamples){
			count += s.getSize();
		}
		return count;
	}
	
	public void printInfo(){
		System.out.println("Number of speakers: " + numberSpeakers() + " (m: " + getNumberMale() + " | f: " + getNumberFemale()+")");
		System.out.println("Number of samples: " + numberSamples());
		System.out.println("Sample gender balance: (m: " + getNumberMaleSamples() + " | f: " + getNumberFemaleSamples()+")");
		System.out.println("Class balance: (nonalc: " + getNumberNONALC() + " | alc: " + getNumberALC()+")");
	}
	
	public HashSet<Integer> getDistincRecTypeSet(){
		HashSet<Integer> list = new HashSet<Integer>();
		
		for(SpeakerSamples s: speakerSamples){
			list.addAll(s.getDistincRecTypeSet());
		}
		
		return list;
	}
	
	public void reduceMale(int male,int size){
		int count=0;
		for(int i=0;i<speakerSamples.size();i++){
			SpeakerSamples sp = speakerSamples.get(i);
			if(sp.getSpeaker().isMale() && sp.getSize() == size){
				//System.out.println("m: " + sp.getSize());
				speakerSamples.remove(i);
				i--;
				count++;
				if(count>= male) break;
				
			}
		}
	}
	
	public void reduceFemale(int female,int size){
		int count=0;
		for(int i=0;i<speakerSamples.size();i++){
			SpeakerSamples sp = speakerSamples.get(i);
			if(sp.getSpeaker().isFemale() && sp.getSize() == size){
				//System.out.println("m: " + sp.getSize());
				speakerSamples.remove(i);
				i--;
				count++;
				if(count>= female) break;
				
			}
		}
	}
	
	public Instances buildTest(Instances data,String key, int numberSpeaker){
		Instances test = new Instances(data,-1);
		ArrayList<String> subset = new ArrayList<String>();
		
		int female=-1; Boolean fbool = false;
		int male =-1; Boolean mbool = false;
		
		for(int i=0;i<speakerSamples.size();i++){
			if(speakerSamples.get(i).getSpeaker().isFemale()){
				female++;
			}
			else{
				male++;
			}
			
			if((male >= 0) && (male < numberSpeaker) && speakerSamples.get(i).getSpeaker().isMale()){	
				for(Sample s:speakerSamples.get(i).getFiles()){
					subset.add(s.getFilename());
				}
				speakerSamples.remove(i);
				i--;
				//System.out.println("i-test: " +i);
			}	
			if((female >= 0) && (female < numberSpeaker) && speakerSamples.get(i).getSpeaker().isFemale()){
				for(Sample s:speakerSamples.get(i).getFiles()){
					subset.add(s.getFilename());
				}	
				speakerSamples.remove(i);
				i--;
				//System.out.println("i-test: " +i);
			}
			
		}
		
		for(int i=0;i<data.size();i++){
			for(String in_key:subset){
				if(data.get(i).stringValue(data.attribute(key)).equals(in_key)){
					test.add(data.get(i));
					break;
				}
			}
		}
		return test;
	}
	
	
	
	public Instances getTest(int fold, Instances data,String key, int numberSpeaker){
		Instances test = new Instances(data,-1);
		ArrayList<String> subset = new ArrayList<String>();
		
		int female=-1; Boolean fbool = false;
		int male =-1; Boolean mbool = false;
		
		for(int i=0;i<speakerSamples.size();i++){
			if(speakerSamples.get(i).getSpeaker().isFemale()){
				female++;
			}
			else{
				male++;
			}
			
			if((male >= fold*numberSpeaker) && (male < (fold+1)*numberSpeaker) && speakerSamples.get(i).getSpeaker().isMale()){	
				for(Sample s:speakerSamples.get(i).getFiles()){
					subset.add(s.getFilename());
				}
				//System.out.println("i-test: " +i);
			}	
			if((female >= fold*numberSpeaker) && (female < (fold+1)*numberSpeaker) && speakerSamples.get(i).getSpeaker().isFemale()){
				for(Sample s:speakerSamples.get(i).getFiles()){
					subset.add(s.getFilename());
				}			
				//System.out.println("i-test: " +i);
			}
		}
		
		for(int i=0;i<data.size();i++){
			for(String in_key:subset){
				if(data.get(i).stringValue(data.attribute(key)).equals(in_key)){
					test.add(data.get(i));
					break;
				}
			}
		}
		return test;
	}
	public Instances getTrain(int fold, Instances data,String key, int numberSpeaker){
		Instances train = new Instances(data,-1);
		ArrayList<String> subset = new ArrayList<String>();
		
		int female=-1; 
		int male =-1; 
		
		for(int i=0;i<speakerSamples.size();i++){
			if(speakerSamples.get(i).getSpeaker().isFemale()){
				female++;
			}
			else{
				male++;
			}
			if((male >= fold*numberSpeaker) && (male < (fold+1)*numberSpeaker) && speakerSamples.get(i).getSpeaker().isMale()){	
				//System.out.println("i-train: " +i);
			}	
			else if((female >= fold*numberSpeaker) && (female < (fold+1)*numberSpeaker) && speakerSamples.get(i).getSpeaker().isFemale()){
				//System.out.println("i-train: " +i);
			}
			else{
				for(Sample s:speakerSamples.get(i).getFiles()){
					subset.add(s.getFilename());
				}
			}
		}
		
		for(int i=0;i<data.size();i++){
			for(String in_key:subset){
				if(data.get(i).stringValue(data.attribute(key)).equals(in_key)){
					train.add(data.get(i));
					break;
				}
			}
		}
		return train;
	}
	
	public void reduceBySize(int size){
		for(int i=0;i<speakerSamples.size();i++){
			SpeakerSamples sp = speakerSamples.get(i);
			if(sp.getSize() == size){
				//System.out.println("m: " + sp.getSize());
				speakerSamples.remove(i);
				i--;
				
			}
		}
	}
	
	public void printALCDistributionByRecType(){
		HashSet<Integer> list = getDistincRecTypeSet();
		
		System.out.println("\n Alc distribution by type:\n");
		
		int total =0;
		for(int type: list){
			int alc =0;
			int nonalc =0;
			for(SpeakerSamples s: speakerSamples){
				alc += s.getALCByRecType(type);
				nonalc += s.getNONALCByRecType(type);
			}
			System.out.println("Type: " + type + " (nonalc: " + nonalc + " | alc: " + alc+") = "+ (double)alc / (double)(nonalc+alc) );
			
			total += alc + nonalc;
		}
		
		System.out.println("\ntotal: " + total + "\n\n");
	}

	public void filterByClassBalance(double d) {
		for(int i=0;i<speakerSamples.size();i++){
			SpeakerSamples sp = speakerSamples.get(i);
			if((double)sp.getNumberOfIntoxicated()/sp.getSize() != d){
				//System.out.println("m: " + sp.getSize());
				speakerSamples.remove(i);
				i--;
				
			}
		}
	}
	
	
	
	
}
