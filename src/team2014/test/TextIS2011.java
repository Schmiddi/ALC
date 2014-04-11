package team2014.test;

import java.util.List;

import team2014.weka.WekaMagic;
import team2014.weka.svm.KernelType;
import weka.core.Instances;

public class TextIS2011 {

	/**
	 * Runs several tests to determine the impact of all sound features
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		boolean isWindows = ((System.getProperty("os.name").contains("Windows"))) ? true : false;
		String fileSep = isWindows ? "\\" : "/";
		String s_key = "file";
		
		if(args.length < 3){
			System.out.println("To less parameters");
		}
		
		try {
			Instances data = null;
			
			String arff_dir = args[0];
			String dirInterspeech = args[1];
			String outputFolder = args[2] + fileSep;
			String csv_dir = WekaMagic.getParent(arff_dir);
			String filenameExtension = "";
			String headerType = "";

			data = WekaMagic.textCSVToInstances(csv_dir + "output.csv", "file");

			int class_index = WekaMagic.setClassIndex(data);
			data.renameAttribute(class_index, "_unique_class_name_");
			WekaMagic.setClassIndex(data);

			System.out.println("Instances read from " + arff_dir + ": " + data.numInstances());

			Boolean withAttributeSelection = false;
			Boolean logistic = false;
			int Kernel = KernelType.RBF.getValue();

			if (args.length >= 4) {
				for(int i=3;i<args.length;i++){
					if(args[i].equals("attr"))
							withAttributeSelection = true;	
					else if(args[i].equals("linear"))
							Kernel = KernelType.LINEAR.getValue();
					else if(args[i].equals("logistic")){
							logistic = true;
					}
				}
			}
			
			if(logistic){
				filenameExtension += "logistic";
				headerType = "logistic";
			}
			else{
				filenameExtension += "svm";	
				
				if(Kernel == KernelType.LINEAR.getValue()){
					headerType = "lin";
					filenameExtension += "_lin";
				}
				else
					filenameExtension += "_rbf";
			}

			Instances[] sets = WekaMagic.getInterspeech2011Sets(dirInterspeech, data, s_key);

			//List<List<Double>> results = WekaMagic.runTestUARIS2011(sets, withAttributeSelection, true);
			
			List<List<Double>> results = null;
			if(logistic){
				results = WekaMagic.runTestUARIS2011LogisticThreads(sets, withAttributeSelection, true);
			}else{
				results = WekaMagic.runTestUARIS2011SVMThreads(sets, withAttributeSelection, true, Kernel);
			}			

			WekaMagic.saveResultIS2011(results, outputFolder, filenameExtension, "text", args, headerType);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
