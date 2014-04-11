package team2014.test;
import java.util.ArrayList;
import java.util.List;

import team2014.weka.CrossValidationOutput;
import team2014.weka.MyClassificationOutput;
import team2014.weka.MyOutput;
import team2014.weka.WekaMagic;
import team2014.weka.plot.GeneratesPlot;
import weka.classifiers.Classifier;
import weka.core.Instances;

import weka.attributeSelection.AttributeSelection;
import weka.attributeSelection.BestFirst;
import weka.attributeSelection.WrapperSubsetEval;
import weka.core.SelectedTag;


public class SoundAttributeSelection {

	/**
	 * Runs several tests to determine the impact of all sound features
	 *  
	 * @param args
	 */
	
	//private static final String ARFF_FILE = "C:\\Users\\IBM_ADMIN\\Dropbox\\Detecting Alcohol Intoxication in Speech\\Moritz\\sound_features\\test\\result.arff";
	//private static final String CSV_DIR = "C:\\Users\\IBM_ADMIN\\Dropbox\\Detecting Alcohol Intoxication in Speech\\Moritz\\sound_features\\test\\";
	
	public static void main(String[] args) {
		
		SoundAttributeSelection testRun = new SoundAttributeSelection();
		boolean isWindows = ((System.getProperty("os.name").contains("Windows")))?true:false;
		String fileSep = isWindows?"\\":"/";
		
		try {
			Instances data = null;
			
			String arff_dir = args[0];
			String csv_dir = WekaMagic.getParent(arff_dir);
			
			data = WekaMagic.getSoundInstances(arff_dir, csv_dir + "output.csv");
			
			System.out.println("Instances read from " + arff_dir + ": " + data.numInstances());
			List<List<List<Double>>> results = testRun.runTest(data);
			
			
			arff_dir += fileSep;
			
			//save to CSV
			WekaMagic.printHashMap(results.get(0), arff_dir + "attr_result_train_cross_validation.csv");//Train set
			WekaMagic.printHashMap(results.get(1), arff_dir + "attr_result_cross_cross_validation.csv");//Cross set
			
					
			//Plot everything
			System.out.println("Plotting results...");
			
			System.out.println("Creating chart " + arff_dir + "attr_plot_train_cross_validation.png ...");
			GeneratesPlot.createSound(results.get(0), arff_dir, "attr_plot_train_cross_validation.png");
			
			System.out.println("Creating chart " + arff_dir + "attr_plot_test_cross_validation.png ...");
			GeneratesPlot.createSound(results.get(1), arff_dir, "attr_plot_test_cross_validation.png");
			
			System.out.println("Finished operations");
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	private List<List<List<Double>>> runTest(Instances data) throws Exception {
		
		/* Runs 10 times logistic to regularize and stores results in list */
		
		List<List<List<Double>>> values = new ArrayList<List<List<Double>>>();
		
		double stdRidge = 0.00000001; //10^-8
		double currentRidge = stdRidge;
		MyClassificationOutput currentResult = null;
		
		//create 3 lists storing data for 3 sets
		List<List<Double>> listTrain = new ArrayList<List<Double>>();
		List<List<Double>> listCross = new ArrayList<List<Double>>();
		
		
		ArrayList<Double> threshold = new ArrayList<Double>();
		threshold.add(0.0);		
		threshold.add(0.00000001);
		threshold.add(0.0001);
		threshold.add(0.0003);
		threshold.add(0.0006);
		threshold.add(0.001);
		threshold.add(0.002);
		threshold.add(0.0025);
		threshold.add(0.0030);
		threshold.add(0.0035);
		threshold.add(0.004);
		threshold.add(0.005);
		threshold.add(0.006);
		threshold.add(0.007);
		threshold.add(0.008);
		threshold.add(0.01);
		
		System.out.println("Running tests for train, cross and test set...");
		//Iterate through different ridge values
		
		System.out.println("general Number Attributes: " + data.numAttributes());
		
		for (int i=0;i<threshold.size();i++)
		{
			for (int u=0;u<15;u++)  // 0 - 15
			{
				currentRidge = stdRidge * (Math.pow(10, u));
				
				currentResult = WekaMagic.runLogistic((Instances)null, (Double)currentRidge, 5);
				//currentResult = WekaMagic.runSVM((Instances)null, (Double)currentRidge, null);
				
				//true binarizeNumericAttributes is important
				MyOutput filtered = WekaMagic.selectionByInfo(null, true, (Double)threshold.get(i));			
				ArrayList<MyOutput> filter = new ArrayList<MyOutput>();
		        filter.add(filtered);			
				
				CrossValidationOutput cvo = WekaMagic.crossValidation(currentResult, data, 10, 1, filter);
				
				
				// Result processing to lists
				List<Double> exTrain = new ArrayList<Double>();
				List<Double> exCross = new ArrayList<Double>();
				
				exTrain.add(0, threshold.get(i));
				exTrain.add(1, cvo.getTrainUAR());
				listTrain.add(exTrain);
				
				
				Instances ndata = cvo.processDataByFilters(data,0);
				
	            exCross.add(0, threshold.get(i));
				exCross.add(1, cvo.getTestUAR());
				exCross.add(2, currentRidge);				
				exCross.add(3, (double)ndata.numAttributes());
				listCross.add(exCross);
				
				//print all information about the result
				System.out.print("ridge: " + currentRidge +" threshold = " + threshold.get(i) + " UAR: "+ cvo.getTestUAR()+ " attr: "+ ndata.numAttributes() + " attr names: ");
				
				for(int t=0;t<ndata.numAttributes();t++){
					System.out.print(ndata.attribute(t).name() + ",");
				}
				System.out.print("\n");
			}
		}
		
		values.add(listTrain);
		values.add(listCross);
		
		return values;
				
	}
	
	public static void AttributeSelectionByClassifier(Classifier c, Instances train, Instances test) throws Exception{

		WrapperSubsetEval bestSubsetAttr = new WrapperSubsetEval();
		bestSubsetAttr.setClassifier(c);
		/*bestSubsetAttr.setFolds(10);
		bestSubsetAttr.setSeed(1);*/
		bestSubsetAttr.setEvaluationMeasure(new SelectedTag(WrapperSubsetEval.EVAL_FMEASURE, WrapperSubsetEval.TAGS_EVALUATION));
		
		
		BestFirst search = new BestFirst();
		//search.setLookupCacheSize(100);
							
		AttributeSelection attsel = new AttributeSelection();				
		attsel.setEvaluator(bestSubsetAttr);
        attsel.setSearch(search);
        attsel.setRanking(true);
        /*attsel.setFolds(3);
        attsel.setSeed(1);*/
        attsel.setXval(false);
        
        //attsel.SelectAttributes(ndata);
        //System.out.println(attsel.CVResultsString());
        
        attsel.SelectAttributes(train);              
        train = attsel.reduceDimensionality(train);
        test = attsel.reduceDimensionality(test);
        
        for(int t=0;t<train.numAttributes();t++){
			System.out.print(train.attribute(t).name() + ",");
		}
		System.out.print("\n");
	}
	

}
