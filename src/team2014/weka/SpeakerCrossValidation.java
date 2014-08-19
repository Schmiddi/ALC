package team2014.weka;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import weka.classifiers.Classifier;
import weka.classifiers.misc.SerializedClassifier;
import weka.core.Attribute;
import weka.core.Debug;
import weka.core.FastVector;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Normalize;

public class SpeakerCrossValidation {
	
	public static void run(SpeakerSet speakerdata, String speakerTable, Instances data, String key) throws Exception{
		int numberSpeakerTest = 1;
		
		ArrayList<Double> clist = WekaMagic.createCValueSetList();
		
		WekaMagic.setClassIndex(data);
		int i;

		Instances totaltest = speakerdata.buildTest(data,key,10);
		
		SpeakerSet speakerData = WekaMagic.matchSpeakerToInstances(speakerTable, totaltest, key);
		System.out.println("test: ");
		speakerData.printInfo();
		System.out.println();
		
		System.out.println("dev: ");
		speakerdata.printInfo();
		System.out.println();
		
		totaltest.deleteAttributeAt(totaltest.attribute(key).index());
		System.out.println("Total test size: " + totaltest.size());
		
		for(int c=0;c<clist.size();c++){
			double totalUAR = 0;
			double realUAR = 0;
			for(i=0;1==1;i++){
				Instances train = speakerdata.getTrain(i,data,key,numberSpeakerTest);			
				Instances test  = speakerdata.getTest(i,data,key,numberSpeakerTest);			
				
				if(test.size()==0) break;
				
				//delete attribute filename
				train.deleteAttributeAt(train.attribute(key).index());
				test.deleteAttributeAt(test.attribute(key).index());
				
				System.out.println("train/test: " + train.size() + "/" + test.size());
				
				
				//Normalize:
				MyOutput normalized = WekaMagic.normalize(train, test);				
				train = normalized.getTrainData();
				test = normalized.getTestData();
				
				//set class
				WekaMagic.setClassIndex(train);
				WekaMagic.setClassIndex(test);
				
				//classify
				MyClassificationOutput classifier = WekaMagic.runSVM(train, clist.get(c), null,  new Double[]{1.0,1.0});
				MyClassificationOutput test_classifier = WekaMagic.applyClassifier(test,classifier);
				System.out.println("UAR: " + test_classifier.getUAR());
				
				totalUAR  +=  test_classifier.getUAR();	
				
				System.out.println(i + ". Current total UAR: " + (totalUAR / (i+1)));
			}
			System.out.println("C: " +  clist.get(c) + " Final total UAR: " + (totalUAR / i));
			
			//get whole dev set
			Instances train = speakerdata.getTrain(0,data,key,numberSpeakerTest);			
			Instances test  = speakerdata.getTest(0,data,key,numberSpeakerTest);
			
			train.addAll(test);
			train.deleteAttributeAt(train.attribute(key).index());
			
			//normalize based on dev set
			MyOutput normalized = WekaMagic.normalize(train, totaltest);			
			train = normalized.getTrainData();
			test = normalized.getTestData();
			
			//set class
			WekaMagic.setClassIndex(train);
			WekaMagic.setClassIndex(test);		
			
			//classify
			MyClassificationOutput classifier = WekaMagic.runSVM(train, clist.get(c), null,  new Double[]{1.0,1.0});			
			MyClassificationOutput test_classifier = WekaMagic.applyClassifier(test,classifier);
			System.out.println("C: " +  clist.get(c) + " Totaltest UAR: " + test_classifier.getUAR());
		}	
	}
	
	public static Filter createModel(SpeakerSet speakerdata, Instances data, double C, String key) throws Exception{
		
		Instances train = new Instances(data);
		
		train.deleteAttributeAt(data.attribute(key).index());
		
		MyOutput normalized = WekaMagic.normalize(train, null);
		train = normalized.getTrainData();
		
		WekaMagic.setClassIndex(train);
		MyClassificationOutput classifier = WekaMagic.runSVM(train, C, null,  new Double[]{1.0,1.0});
		
		Classifier c = (Classifier)classifier.getClassifier();
		
		//save classifier to file
		String modelDir = "classifier_myIS11_SVM.dat";
		Debug.saveToFile(modelDir, c);		
		
		Instances wavsample = null;
		
		train = new Instances(data);
		
		train.deleteAttributeAt(data.attribute(key).index());
		WekaMagic.setClassIndex(train);

		Normalize filter = new Normalize();
		filter.setInputFormat(train);
		wavsample = Filter.useFilter(train, filter);
		
		ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream("normalize.filter"));
		oos.writeObject((Filter)filter);
		oos.flush();
		oos.close();
		

		double pred = -1;
		double percent = -1;

		//double prednonalc = -1;
		//double predalc = -1;
		
		for(int i=0;i<wavsample.size();i++){

			pred       = c.classifyInstance(wavsample.get(i));
			percent    = c.distributionForInstance(wavsample.get(i))[(int)pred];			
			
			System.out.println("name: " + data.get(i).stringValue(data.attribute(key)) + " pred: " + pred + "  real: " + wavsample.get(i).stringValue(wavsample.classAttribute()) + " percent: " + percent);
		}
		
		
		
		System.out.println("Start classification");
		//load classifier from file
		SerializedClassifier fc = new SerializedClassifier();
		fc.setModelFile(new File(modelDir));
		
		for(int i=0;i<wavsample.size();i++){

			pred       = fc.classifyInstance(wavsample.get(i));
			percent    = fc.distributionForInstance(wavsample.get(i))[(int)pred];			
			
			System.out.println("name: " + data.get(i).stringValue(data.attribute(key)) + " pred: " + pred + "  real: " + wavsample.get(i).stringValue(wavsample.classAttribute()) + " percent: " + percent);
		}
		
		
		return filter;
		
		
	}
	
	public static void check() throws Exception{
		
		System.out.println("Start classification");
		//load classifier from file
		String modelDir = "classifier_myIS11_SVM.dat";
		SerializedClassifier fc = new SerializedClassifier();
		fc.setModelFile(new File(modelDir));

		String file ="C:\\Users\\IBM_ADMIN\\Dropbox\\Detecting Alcohol Intoxication in Speech\\Moritz\\Halef\\alc_100.arff";

		
		//load
		DataSource source = new DataSource(file); //load ARFF file
		if(source == null){
			System.out.println("file: " + file);
			System.out.println("Arff file not found!");
		}

    	Instances data = source.getDataSet();

    	data.deleteStringAttributes(); //delete all string attributes

    	//add class column
    	FastVector values = new FastVector(); /* FastVector is now deprecated. Users can use any java.util.List */
        values.addElement("nonalc");               /* implementation now */
        values.addElement("alc");
    	data.insertAttributeAt(new Attribute("class", values), data.numAttributes());
    	data.setClass(data.attribute("class"));
		
		
		
    	ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream("normalize.filter"));
		Filter f = (Filter) ois.readObject();
		ois.close();
		
		data = Filter.useFilter(data, f);

		double pred = -1;
		double percent = -1;

		//double prednonalc = -1;
		//double predalc = -1;

		pred       = fc.classifyInstance(data.get(0));
		percent    = fc.distributionForInstance(data.get(0))[(int)pred];

		System.out.println(" pred: " + pred + " percent: " + percent);
	}
	
	
public static void check(Instances train) throws Exception{
		
		System.out.println("Start classification");
		//load classifier from file
		String modelDir = "classifier_myIS11_SVM.dat";
		SerializedClassifier fc = new SerializedClassifier();
		fc.setModelFile(new File(modelDir));

		String file ="C:\\Users\\IBM_ADMIN\\Dropbox\\Detecting Alcohol Intoxication in Speech\\Moritz\\Halef\\audio1403613881530.arff";

		
		//load
		DataSource source = new DataSource(file); //load ARFF file
		if(source == null){
			System.out.println("file: " + file);
			System.out.println("Arff file not found!");
		}

    	Instances data = source.getDataSet();

    	data.deleteStringAttributes(); //delete all string attributes

    	//add class column
    	FastVector values = new FastVector(); /* FastVector is now deprecated. Users can use any java.util.List */
        values.addElement("nonalc");               /* implementation now */
        values.addElement("alc");
    	data.insertAttributeAt(new Attribute("class", values), data.numAttributes());
    	data.setClass(data.attribute("class"));
		
		
		
		
		Normalize filter = new Normalize();
		filter.setInputFormat(data);
		data = Filter.useFilter(data, filter);

		double pred = -1;
		double percent = -1;

		//double prednonalc = -1;
		//double predalc = -1;

		pred       = fc.classifyInstance(data.get(0));
		percent    = fc.distributionForInstance(data.get(0))[(int)pred];

		System.out.println(" pred: " + pred + " percent: " + percent);
	}
}
