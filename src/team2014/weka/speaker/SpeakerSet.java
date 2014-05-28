package team2014.weka.speaker;

import java.util.ArrayList;
import java.util.HashSet;

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
	
	
	
	
}
