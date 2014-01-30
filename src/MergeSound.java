import weka.core.Instances;



 
public class MergeSound {
	
	public static void main(String[] args) throws Exception {
		String par = "C:\\Users\\IBM_ADMIN\\Dropbox\\Detecting Alcohol Intoxication in Speech\\Moritz\\sound_features\\test\\";
		String dir = par + "sound";
		
		
		
		
		Instances sound = WekaMagic.soundArffToInstances(dir);
		
		WekaMagic.saveToArff(sound, par+ "sound", null);
		
		Instances text = WekaMagic.textCSVToInstances( dir + "\\output.csv","file");
		
		WekaMagic.saveToArff(text, par + "text", null);
		
		Instances merged = WekaMagic.mergeInstancesBy(sound, text, "file");
		
		//System.out.println(merged);	
		
		WekaMagic.saveToArff(merged, par + "result", null);
	
	    
	    
	}
}
