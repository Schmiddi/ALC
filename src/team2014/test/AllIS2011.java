package team2014.test;

import java.util.List;


import team2014.weka.svm.KernelType;
import team2014.weka.WekaMagic;
import weka.core.Instances;

public class AllIS2011 {

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
			String arff_dir = args[0];
			String dirInterspeech = args[1];
			String outputFolder = args[2] + fileSep;
			String csv_dir = WekaMagic.getParent(arff_dir);

			Instances dataText    = WekaMagic.textCSVToInstances(csv_dir + "output.csv", "file");
			Instances dataGrammar = WekaMagic.getGrammarInstancesWithFile(csv_dir + "output.csv", true);
			Instances dataSound   = WekaMagic.soundArffToInstances(arff_dir); //without class column
			
			//delete class column to prevent issues during merging
			dataGrammar.renameAttribute(dataGrammar.classIndex(), "classGrammar");
			
			// Process Text
			int class_index = WekaMagic.setClassIndex(dataText);
			dataText.renameAttribute(class_index, "_unique_class_name_");
			WekaMagic.setClassIndex(dataText);

			System.out.println("Merge started!");
			// Merge datasets
			Instances dataTextGrammar = WekaMagic.mergeInstancesBy(dataText, dataGrammar, "file");
			Instances data = WekaMagic.mergeInstancesBy(dataTextGrammar, dataSound, "file"); 
			System.out.println("Merge done!");
			data.deleteAttributeAt(data.attribute("classGrammar").index());
			
			Instances[] sets = WekaMagic.getInterspeech2011Sets(dirInterspeech, data, s_key);
			
            Boolean withAttributeSelection = false;			
			Boolean logistic = false;
			int Kernel = KernelType.RBF.getValue();
            String filenameExtension = "";
            String headerType = "";

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
			//List<List<Double>> results = WekaMagic.runTestUARIS2011(sets, withAttributeSelection);
			List<List<Double>> results = null;
			if(logistic){
				results = WekaMagic.runTestUARIS2011LogisticThreads(sets, withAttributeSelection, true);
			}else{
				results = WekaMagic.runTestUARIS2011SVMThreads(sets, withAttributeSelection, true, Kernel);
			}
			WekaMagic.saveResultIS2011(results, outputFolder, filenameExtension, "all", args, headerType);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
