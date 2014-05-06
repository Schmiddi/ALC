package team2014.weka.speaker;

import java.util.ArrayList;

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
		System.out.println("Class balance: (alc: " + getNumberALC() + " | nonalc: " + getNumberNONALC()+")");
	}
	
	
	
}
