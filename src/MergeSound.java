import weka.core.Instances;



 
public class MergeSound {
	
	public static void main(String[] args) throws Exception {
		String dir = "C:\\Users\\IBM_ADMIN\\Dropbox\\Detecting Alcohol Intoxication in Speech\\Moritz\\sound_features\\test\\sound";
		
		
		
		Instances sound = WekaMagic.soundArffToInstances(dir);
		
		Instances text = WekaMagic.textCSVToInstances( dir + "\\output.csv");
		
		Instances merged = WekaMagic.mergeInstancesBy(sound, text, "file");
		
		//System.out.println(merged);	
		
		WekaMagic.saveToArff(merged, dir + "\\result", null);
	
	    
	    
	}
}
