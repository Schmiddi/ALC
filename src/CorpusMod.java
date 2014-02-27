import weka.core.Instances;


public class CorpusMod {
	public static void main(String[] args) {
		
		
			String str = " <\"ah> j\"ungeren <\"ah> Datums  eine Frau die offenbar  grade <\"ah> eine T\"ur ja \\\"offnet ";
			      str += "\n#pronounceErrors# #stutters# #repairs# #repairs#\n";
			      str += "\n#pronounceErrors# #stutters# #repairs# #repairs#\n";
			      str += " sdd ... ... hallo punbkte ... as\n";
			      str += " sdd + .~+~ hallo punbkte ~ as\n";
			      
			System.out.println(str);
			System.out.println(WekaMagic.cleanString(str));
			
			boolean isWindows = ((System.getProperty("os.name").contains("Windows")))?true:false;
			String fileSep = isWindows?"\\":"/";
			
			String s_key="file";
			
			try {
				Instances data = null;
				
				String arff_dir = args[0];
				String csv_dir = WekaMagic.getParent(arff_dir);
				
				Instances sound = WekaMagic.soundArffToInstances(arff_dir);		
				Instances text = WekaMagic.textCSVToInstances(csv_dir + "output.csv","file");
				
				data = WekaMagic.mergeInstancesBy(sound, text, "file");
				
				CrossValidationOutput cvo = new CrossValidationOutput(data,s_key);
				
				Instances train = cvo.getTrainSet(0);
				
				Instances filtered_train = WekaMagic.cleanCorpus(train, s_key);
				
				WekaMagic.createReferenceText("language.txt", filtered_train, s_key);
				
				
				
				
				
				
				
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
}
