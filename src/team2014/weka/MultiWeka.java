package team2014.weka;

import java.util.ArrayList;
import java.util.List;

import team2014.weka.ClassifierE;
import team2014.weka.MyClassificationOutput;
import team2014.weka.MyOutput;
import team2014.weka.WekaMagic;
import weka.core.Instances;


public class MultiWeka implements Runnable {
	   private Thread t;	   
	   private Instances [] sets;
	   private Boolean withAttributeSelection;
	   private Boolean isText;
	   private Double [] parameters;
	   private double threshold;	   
	   private List<Double> listRun;
	   private int classifier;
	   private int smoteKNN;
	   private double smotePercentage;
	   
	   public MultiWeka(Instances[] sets,
			Boolean withAttributeSelection, Boolean isText, Double [] parameters,
			double threshold, int classifier, int smoteKNN, double smotePercentage) {
			super();
			this.t=null;
			this.sets = sets;
			this.withAttributeSelection = withAttributeSelection;
			this.isText = isText;
			this.parameters = parameters;
			this.threshold = threshold;
			this.classifier = classifier;
			this.smoteKNN = smoteKNN;
			this.smotePercentage = smotePercentage;
	   }
	
	   public void run() {
		   System.out.println(t.getName() + " has been started!");
		   listRun = null;
		   MyOutput featuresGen = null;
			
			if(isText){
				// perfect configuration
				Boolean Ngram = true; //default:true - best: true
				int ngram_min = 1;
				int ngram_max = 3;
				Boolean LowerCase = true; //default:true - best: true
				Boolean IDFTransform = false; //default:false - best: true
				Boolean TFTransform = false; //default:false - best: true
				Boolean Stopword = true; //default:true - best: true
				String list1 = "resources\\germanST.txt";
				int wordsToKeep = 1000000;
				Boolean NormalizeDocLength = true; //default:true - best: true
				Boolean OutputWordCounts = true; //default:true - best true
				Boolean Stemming = false; 		//default:true - best: false
				int minTermFrequency = 2; //default:2 - best: 2
				try {
					featuresGen = WekaMagic.generateFeatures(null, wordsToKeep, Ngram,
							ngram_min, ngram_max, LowerCase, NormalizeDocLength, Stemming,
							OutputWordCounts, IDFTransform, TFTransform, Stopword, list1,
							minTermFrequency);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		   
		    //double currentRidge = ridge;//stdRidge * (Math.pow(10, u));
			
			MyOutput filtered = null;
			ArrayList<MyOutput> filters = null;
			
			if(isText || withAttributeSelection || smotePercentage >=0)
				filters = new ArrayList<MyOutput>();
			
			if(isText)
				filters.add(featuresGen);
			
			if (withAttributeSelection) {
				// true binarizeNumericAttributes is important
				Boolean binarizeNumericAttributes = true;
				try {
					filtered = WekaMagic.selectionByInfo(null, binarizeNumericAttributes,
							(Double) threshold);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				filters.add(filtered);
			}
			
			if(smotePercentage >=0 ){
				MyOutput oversample = null;
				try {
					oversample = WekaMagic.smote(null, null, smoteKNN, smotePercentage);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				filters.add(oversample);
				
			}
			
			MyClassificationOutput[] output;
			try {
				output = WekaMagic.validationIS2011(sets, filters, parameters, classifier);			

				// Result processing to lists
				listRun = new ArrayList<Double>();
				
				listRun.add(0, threshold);
				
				for(int i=0;i<parameters.length;i++){
					listRun.add(1+i, parameters[i]);
				}
				
				listRun.add(1 + parameters.length, output[SetType.TRAIN.ordinal()].getUAR());
				listRun.add(2 + parameters.length, output[SetType.DEV.ordinal()].getUAR());
				listRun.add(3 + parameters.length, output[SetType.TEST.ordinal()].getUAR());
				
				listRun.add(4 + parameters.length, output[SetType.TRAINDEV.ordinal()].getUAR());
				listRun.add(5 + parameters.length, output[SetType.TRAINDEVTEST.ordinal()].getUAR());
				
	
				// print all information about the result
				System.out.print(ClassifierE.toString(classifier) + ": ");
				for(int i=0;i<parameters.length;i++){
					System.out.print(ClassifierE.getParameterNameByID(classifier, i) + "=" + parameters[i] + " ");
				}
				
				System.out.print("threshold:" + threshold
						+ " Train UAR: " + output[SetType.TRAIN.ordinal()].getUAR() + " Dev UAR:"
						+ output[SetType.DEV.ordinal()].getUAR() + " Test UAR:"
						+ output[SetType.TEST.ordinal()].getUAR() 
						+ " Train DEV - UAR: " + output[SetType.TRAINDEV.ordinal()].getUAR() 
						+ " TRAINDEVTEST - UAR: " + output[SetType.TRAINDEVTEST.ordinal()].getUAR() +
						"\n");
			
		   } catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	   }
	   
	   public void start ()
	   {
	      if (t == null)
	      {
	         t = new Thread (this, " threshold = " + threshold);
	         t.start ();
	      }
	   }

	   public List<Double> getResult(){
		   return listRun;
	   }
	   
	   public Boolean isAlive(){
		   if(t==null) return false;
		   
		   if(t.isAlive()) return true;
		   return false;
	   }

		public void join() throws InterruptedException {
			t.join();
		}

	}
