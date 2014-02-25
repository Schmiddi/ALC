import java.io.File;
import java.io.FilenameFilter;


import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instances;
import weka.classifiers.Classifier;
import weka.classifiers.misc.SerializedClassifier;
import weka.core.Debug;
import weka.core.converters.ConverterUtils.DataSource;


public class ClassifyTest {
	public static void main(String[] args) {
		
		boolean isWindows = ((System.getProperty("os.name").contains("Windows")))?true:false;
		String fileSep = isWindows?"\\":"/";
		
		try {
			
			/*
			String csv_dir = args[0];			
			data = SoundOnly.getSoundInstances(csv_dir + fileSep + "sound", fileSep + "output.csv");
			
			//build classifier
			MyClassificationOutput classifier = WekaMagic.runLogistic(data, 10.0, 5);
			Classifier c = (Classifier)classifier.getClassifier();
			
			//save classifier to file
			String modelDir = "";
			Debug.saveToFile(modelDir, c);
			
			*/
			
			String modelDir   = args[0];
			String directory  = args[1];
			String csv_file   = args[2];
			
			//load classifier from file
			SerializedClassifier fc = new SerializedClassifier();
			fc.setModelFile(new File(modelDir));
			
			/*
			//build arff from wav file
			String outputArff = "output.arff";
			String command = "/home/alc/tools/opensmile-2.0-rc1/opensmile/SMILExtract -C /home/alc/tools/opensmile-2.0-rc1/opensmile/config/myIS10_paraling.conf -noconsoleoutput -I "+ wav +" -O " + outputArff;
		    Process child = Runtime.getRuntime().exec(command);
		    */
			
			File f = new File(directory);

		    FilenameFilter textFilter = new FilenameFilter() {
		        public boolean accept(File dir, String name) {
		            return name.toLowerCase().endsWith(".arff");
		        }
		    };

		    File[] files = f.listFiles(textFilter); //get all arff files in directory
		    
		    Instances text = WekaMagic.textCSVToInstances(csv_file,"file");
		    
		    WekaMagic.saveToArff(text, "ouput.arff", null);
			
		    int trueC =0;
		    for(File file : files){
			    //get instance from arff file
			    Instances wavsample = ClassifyTest.loadFromSoundArff(file.getAbsolutePath());
			    
			    double pred = fc.classifyInstance(wavsample.get(0));	
			    String preds = wavsample.classAttribute().value((int) pred);
			    
			    String name = file.getName().split("\\.")[0];
			    String reals = "";
			    for(int i=0;i<text.size();i++){
			    	if(text.get(i).stringValue(text.attribute("file")).equals(name)){
			    		double real = text.get(i).value(text.classAttribute());
			    		reals = text.classAttribute().value((int) real);
			    		break;
			    	}
			    }
			    String classify="";
			    if(preds.equals(reals)){
			    	classify = "true ";
			    	trueC++;
			    }
			    else{
			    	classify = "false";
			    }
			    
			    
			    System.out.println(name + " classification: " + classify + " real:" + reals + " pred: " + preds);
			    
		    }
		    System.out.println(trueC + "/" + files.length);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static Instances loadFromSoundArff(String file) throws Exception{
		DataSource source = new DataSource(file); //load ARFF file
		
		Instances data = source.getDataSet();
    	
    	data.deleteStringAttributes(); //delete all string attributes
    	
    	//add class column
    	FastVector values = new FastVector(); /* FastVector is now deprecated. Users can use any java.util.List */
        values.addElement("nonalc");               /* implementation now */
        values.addElement("alc");
    	data.insertAttributeAt(new Attribute("class", values), data.numAttributes());
    	data.setClass(data.attribute("class"));
    	
    	return data;
	}
}
