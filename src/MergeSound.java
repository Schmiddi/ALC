import weka.core.Instances;



 
public class MergeSound {
	
	public static void main(String[] args) throws Exception {
		String dir = "C:\\Users\\IBM_ADMIN\\Dropbox\\Detecting Alcohol Intoxication in Speech\\Moritz\\sound_features\\test\\sound";
		
		
		
		Instances sound = WekaMagic.soundArffToInstances(dir);
		
		WekaMagic.saveToArff(sound, dir + "\\sound", null);
		
		Instances text = WekaMagic.textCSVToInstances( dir + "\\output.csv");
		
		WekaMagic.saveToArff(text, dir + "\\text", null);
		
		System.out.println("sound: " + sound.size() + " text: " + text.size());
		
		Instances merged = WekaMagic.mergeInstancesBy(sound, text, "file");
		
		//System.out.println(merged);	
		
		WekaMagic.saveToArff(merged, dir + "\\result", null);
	
	    
	    
	}
}
