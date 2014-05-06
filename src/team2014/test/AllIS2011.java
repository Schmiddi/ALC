package team2014.test;

import java.util.List;

import weka.core.Instances;

import team2014.weka.svm.KernelType;
import team2014.weka.WekaMagic;
import team2014.weka.Utils;


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
		
		Boolean text    = false;
		Boolean grammar = false;
		Boolean sound   = false;
		
		Instances dataText	  = null;
		Instances dataGrammar = null;
		Instances dataSound   = null;
		
		if(Utils.isFlag(new String[]{"h","help","-h","-help"},args)){
			System.out.println("Run classifier test for Interspeech 2011 datasets");
			System.out.println("This will leverage all cores of your machine.");
			System.out.println("Please allocate suffient memory!\n");
			
			System.out.println("Parameters:\n");
			
			System.out.println("h,help,-h,-help: 		this will print a detailed description how to use this program\n\n");
			
			System.out.println("Choose your features: ");			
			System.out.println("	text   ,text_features: 		use text features");
			System.out.println("	grammar,grammar_features: 	use grammar features");
			System.out.println("	sound  ,sound_features: 	use acoustic features");
			System.out.println("	all    ,all_features: 		use all availabe features (default)\n");
			
			System.out.println("Specify config / name the parent diretory of all arff files which have been generated by OpenSmile");
			System.out.println("-c <path>, -config <path>");
			System.out.println("Example: -c /import/scratch/tjr/tjr40/sound/tests/combined_all/myIS13_ComParE\n");
			
			System.out.println("Specify distribution of training, dev, test sets / name directory which includes TRAIN.TBL, TEST.TBL, D1.TBL");
			System.out.println("-s <path>, -set_definition <path>");
			System.out.println("Example: -s /home/bas-alc/corpus/DOC/IS2011CHALLENGE\n");
			
			System.out.println("Specify output folder");
			System.out.println("-o <path>, -output <path>");
			System.out.println("Example: -o /home/alc/workspace/ALC/output\n");
			
			System.out.println("Specify number of threads running parallel");
			System.out.println("-mT <number>, -maxThreads <number>");
			System.out.println("Default: -1 (use as many threads as cores)");
			
			System.out.println("Classify only instances without tongue twisters / name the folder which contains only arff files without tongue twisters");
			System.out.println("-wott <path>, -without_tongue_twisters <path>");
			System.out.println("Example: -wott /import/scratch/tjr/tjr40/sound/tests/combined_all_wo_tt/");
			System.out.println("Focus on ending with a file separator!\n");
				
			System.out.println("\tDo not exclude the tongue twisters from the test set");
			System.out.println("\ttwtt, test_with_tongue_twisters");
			System.out.println("\tDefault: false\n");
			
			System.out.println("Use attribute/feature selection");
			System.out.println("attr, attribute_selection (default: not used)\n");
			
			System.out.println("Choose method to create your classifier");
			System.out.println("-m <method>, -method <method>\n");
			
			System.out.println("\tRun logistic regression");
			System.out.println("\t-m log, -m logistic_regression\n");	
			
			System.out.println("\tRun support vector machine (default algorithm)");
			System.out.println("\t-m svm, -m support_vector_machine\n");
			
			System.out.println("\t\tChoose kernel for support vector machine");
			System.out.println("\t\t-k <kernel>, -kernel <kernel>\n");
			
			System.out.println("\t\t\tUse linear kernel (default kernel)");
			System.out.println("\t\t\t-k lin, -k linear\n");
			
			System.out.println("\t\t\tUse rbf kernel");
			System.out.println("\t\t\t-k rbf, -k radial_basis_function\n");
			
			System.exit(1);
			
		}
		
		
		if(Utils.isFlag(new String[]{"text","text_features"},args))       text = true;
		if(Utils.isFlag(new String[]{"grammar","grammar_features"},args)) grammar = true;
		if(Utils.isFlag(new String[]{"sound","sound_features"},args)) 	  sound = true;
		
		if(Utils.isFlag(new String[]{"all","all_features"},args) ||
			(!text && !grammar && !sound)){
			
				if(!Utils.isFlag(new String[]{"all","all_features"},args))
					System.out.println("You should have chosen features! Default features: all");
			
				text    = true;
				grammar = true;
				sound   = true;
		}
		
		
		
		
		
		if(args.length < 3){
			System.out.println("Too few parameters");
		}
		
		try {			
			String arff_dir       = Utils.getFlag(new String[]{"-c","-config"}, args);
			String dirInterspeech = Utils.getFlag(new String[]{"-s","-set_definition"}, args);
			String outputFolder   = Utils.getFlag(new String[]{"-o","-output"}, args) + fileSep;
			String csv_dir        = WekaMagic.getParent(arff_dir);

			if(text){
				dataText    = WekaMagic.textCSVToInstances(csv_dir + "output.csv", "file");
				int class_index = WekaMagic.setClassIndex(dataText);
				dataText.renameAttribute(class_index, "classText");
				dataText.setClassIndex(-1);
				System.out.println("Text features loaded.");
			}
			if(grammar){		
				dataGrammar = WekaMagic.getGrammarInstancesWithFile(csv_dir + "output.csv", true);
				int class_index = WekaMagic.setClassIndex(dataGrammar);
				dataGrammar.renameAttribute(class_index, "classGrammar");
				dataGrammar.setClassIndex(-1);
				System.out.println("Grammar features loaded.");
			}
			if(sound){
				dataSound   = WekaMagic.getSoundInstancesWithFile(arff_dir, csv_dir + "output.csv"); 
				int class_index = WekaMagic.setClassIndex(dataSound);
				dataSound.renameAttribute(class_index, "classSound");
				dataSound.setClassIndex(-1);
				System.out.println("Sound features loaded.");
			}		
			
			
			// Merge datasets
			System.out.println("Merge started!");
			Instances dataTextGrammar = WekaMagic.fastmergeInstancesBy(dataText, dataGrammar, "file");
			Instances data            = WekaMagic.fastmergeInstancesBy(dataTextGrammar, dataSound, "file"); 
			System.out.println("Merge done!");
			
			data.setClassIndex(-1);
			
			//delete unnecessary class columns
			if(text && grammar)
				data.deleteAttributeAt(data.attribute("classGrammar").index());
			if((text || grammar) && sound)
				data.deleteAttributeAt(data.attribute("classSound").index());
			
			WekaMagic.setClassIndex(data);
			
			//get data set distribution
			Instances[] sets = null;
			
			if(Utils.isFlag(new String[]{"-wott","-without_tongue_twisters"},args))
			{
				String dir_wott = Utils.getFlag(new String[]{"-wott","-without_tongue_twisters"},args);
				Boolean applyOnTest = !Utils.isFlag(new String[]{"twtt","test_with_tongue_twisters"},args);
				
				sets = WekaMagic.getInterspeech11wott(dirInterspeech, data, s_key, dir_wott, applyOnTest);
			}else{
				sets = WekaMagic.getInterspeech2011Sets(dirInterspeech, data, s_key);
			}
			
			//get classification algorithm configurations
            Boolean withAttributeSelection = false;			
			Boolean logistic = false;
			int Kernel = KernelType.LINEAR.getValue();
            String filenameExtension = "";
            String headerType = "";

			if (args.length >= 4) {
					if(Utils.isFlag(new String[]{"attr","attribute_selection"},args))
							withAttributeSelection = true;	
					
					if(Utils.isFlag(new String[]{"-m","-method"},new String[]{"log","logistic_regression"},args))	
							logistic = true;
					if(Utils.isFlag(new String[]{"-m","-method"},new String[]{"svm","support_vector_machine"},args) || logistic == false){	
							logistic = false;
							
							if(Utils.isFlag(new String[]{"-k","-kernel"},new String[]{"lin","linear"},args))	
									Kernel = KernelType.LINEAR.getValue();
							if(Utils.isFlag(new String[]{"-k","-kernel"},new String[]{"rbf","radial_basis_function"},args))
									Kernel = KernelType.RBF.getValue();
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
			
			int maxThreads = -1;
			if(Utils.isFlag(new String[]{"-mT","-maxThreads"},args)){
				maxThreads = Integer.parseInt(Utils.getFlag(new String[]{"-mT","-maxThreads"},args));
			}
						
			
			System.out.println("Total number of Instances: " + (sets[0].size() + sets[1].size() + sets[2].size()));
			
			List<List<Double>> results = null;
			if(logistic){
				results = WekaMagic.runTestUARIS2011LogisticThreads(sets, withAttributeSelection, text, maxThreads);
			}else{
				results = WekaMagic.runTestUARIS2011SVMThreads(sets, withAttributeSelection, text, Kernel, maxThreads);
			}
			
			String name = "class_" + ((text)?"text-":"") + ((grammar)?"grammar-":"") + ((sound)?"sound-":""); 
			
			WekaMagic.saveResultIS2011(results, outputFolder, filenameExtension, name, args, headerType);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
