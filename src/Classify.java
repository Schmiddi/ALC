import java.io.File;


import weka.core.Instances;
import weka.classifiers.Classifier;
import weka.classifiers.misc.SerializedClassifier;
import weka.core.Debug;


public class Classify {
	public static void main(String[] args) {
		
		boolean isWindows = ((System.getProperty("os.name").contains("Windows")))?true:false;
		String fileSep = isWindows?"\\":"/";
		
		try {
			Instances data = null;
			
			String csv_dir = args[0];			
			data = SoundOnly.getSoundInstances(csv_dir + fileSep + "sound", fileSep + "output.csv");
			
			//build classifier
			MyClassificationOutput classifier = WekaMagic.runLogistic(data, 10.0, 5);
			Classifier c = (Classifier)classifier.getClassifier();
			
			//save classifier to file
			String modelDir = "";
			Debug.saveToFile(modelDir, c);
			
			//load classifier from file
			SerializedClassifier fc = new SerializedClassifier();
			fc.setModelFile(new File(modelDir));
			
			//classify wav
			String wav = args[1];
			
			//build arff from wav file
			String outputArff = "output.arff";
			String command = "/home/alc/tools/opensmile-2.0-rc1/opensmile/SMILExtract -C /home/alc/tools/opensmile-2.0-rc1/opensmile/config/myIS10_paraling.conf -noconsoleoutput -I "+ wav +" -O " + outputArff;
		    Process child = Runtime.getRuntime().exec(command);
		    
		    //get instance from arff file
		    Instances wavsample = WekaMagic.loadFromSoundArff(outputArff);
		    
		    for(int i=0;i<wavsample.size();i++){
		    	double pred = fc.classifyInstance(wavsample.get(i));	
		    	System.out.println("predicted: " + data.classAttribute().value((int) pred));
		    }			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
