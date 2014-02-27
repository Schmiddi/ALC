package team2014.test;
import team2014.weka.MyClassificationOutput;
import team2014.weka.WekaMagic;
import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.Debug;


public class SoundClassifier {
public static void main(String[] args) {
		
		boolean isWindows = ((System.getProperty("os.name").contains("Windows")))?true:false;
		String fileSep = isWindows?"\\":"/";
		
		try {
			Instances data = null;
			
			String arff_dir = args[0];
			String csv_dir = WekaMagic.getParent(arff_dir);
			
			//load sound data
			
			System.out.println("ARFF: " + arff_dir);
			System.out.println("CSV: " + csv_dir + "output.csv");
			
			data = WekaMagic.getSoundInstances(arff_dir, csv_dir + "output.csv");
			
			//due to experiments we know that 10.000 is the best value for the ridge
			//we use the whole data set to create the best classifier for the web service
			MyClassificationOutput currentResult = WekaMagic.runLogistic(data, 10000.0, 5);
			
			//save classifier to file
			String modelFile = args[1];
		    Debug.saveToFile(modelFile, (Classifier)currentResult.getClassifier());
			
			System.out.println("Classifier saved");
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
