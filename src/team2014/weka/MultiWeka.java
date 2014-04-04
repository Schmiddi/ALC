package team2014.weka;

import java.util.ArrayList;
import java.util.List;

import weka.core.Instances;


public class MultiWeka implements Runnable {
	   private Thread t;	   
	   private Instances [] sets;
	   private Boolean withAttributeSelection;
	   private Boolean isText;
	   private double currentRidge;
	   private double threshold;	   
	   private List<Double> listRun;
	   
	   
	   public MultiWeka(Instances[] sets,
			Boolean withAttributeSelection, Boolean isText, double ridge,
			double threshold) {
			super();
			this.t=null;
			this.sets = sets;
			this.withAttributeSelection = withAttributeSelection;
			this.isText = isText;
			this.currentRidge = ridge;
			this.threshold = threshold;
	   }
	
	   public void run() {
		   System.out.println(t.getName() + " has been started!");
		   listRun = null;
		   MyOutput featuresGen = null;
			
			if(isText){
				// perfect configuration
				Boolean Ngram = true;
				int ngram_min = 1;
				int ngram_max = 3;
				Boolean LowerCase = true;
				Boolean IDFTransform = false;
				Boolean TFTransform = false;
				Boolean Stopword = true;
				String list1 = "resources\\germanST.txt";
				int wordsToKeep = 1000000;
				Boolean NormalizeDocLength = true;
				Boolean OutputWordCounts = true;
				Boolean Stemming = true;
				int minTermFrequency = 2;
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
			
			if(isText || withAttributeSelection)
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
			
			MyClassificationOutput[] output;
			try {
				output = WekaMagic.validationIS2011(sets, filters, currentRidge);			

				// Result processing to lists
				listRun = new ArrayList<Double>();
				
				listRun.add(0, threshold);
				listRun.add(1, currentRidge);
				listRun.add(2, output[SetType.TRAIN.ordinal()].getUAR());
				listRun.add(3, output[SetType.DEV.ordinal()].getUAR());
				listRun.add(4, output[SetType.TEST.ordinal()].getUAR());
				
				listRun.add(5, output[SetType.TRAINDEV.ordinal()].getUAR());
				listRun.add(6, output[SetType.TRAINDEVTEST.ordinal()].getUAR());
				
	
				// print all information about the result
				System.out.print("ridge:" + currentRidge + " threshold:" + threshold
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
	         t = new Thread (this, "ridge = " + currentRidge + " threshold = " + threshold);
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