package CheckInterspeech;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.*;



public class MapBAC {
	
	
	
	public static ArrayList<ArrayList<String>> getInterspeechFiles(String dir) throws IOException{
		//get right fileSeparator
		boolean isWindows = ((System.getProperty("os.name").contains("Windows")))?true:false;
		String fileSep = isWindows?"\\":"/";
		
		ArrayList<ArrayList<String>> sets = new ArrayList<ArrayList<String>>();
		
		String [] filenames = new String[3];
		
		filenames[0] = "TRAIN.TBL"; //train_name
		filenames[1] = "D1.TBL";    //dev_name
		filenames[2] = "TEST.TBL";  //test_name
		
		InputStream    fis;
		BufferedReader br;
		String         line;
		
		for(int i=0;i<filenames.length;i++){
			ArrayList<String> set = new ArrayList<String>();
			String filename = dir + fileSep + filenames[i];
			fis = new FileInputStream(filename);
			br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
			while ((line = br.readLine()) != null) {
				//System.out.println("\""+line+"\""); // format example: BLOCK30/SES3066/5653066001_h_01.WAV
				if(!line.isEmpty() && line != null && line.length()>5){
					String[] tokens = line.split("/");
					String id = tokens[tokens.length-1].split("\\.")[0];
					set.add(id);
					
				}
			}
			sets.add(i,set);
			
			// Done with the file
			br.close();
			br = null;
			fis = null;
		}
		
		return sets;
	}
	
	public static HashMap<String,Double> getBACMap(String mapFile) throws NumberFormatException, IOException{
		InputStream    fis;
		BufferedReader br;
		String         line;
		
		HashMap<String,Double> map = new HashMap<String,Double>();

		fis = new FileInputStream(mapFile);
		br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
		while ((line = br.readLine()) != null) {
			//System.out.println("\""+line+"\""); // format example: BLOCK30/SES3066/5653066001_h_01.WAV
			if(!line.isEmpty() && line != null && !line.contains("bac")){
				String[] tokens = line.split(",");
				if(tokens[0].length()>3){
					String id = tokens[0];
					double BAC = Double.parseDouble(tokens[1]);
					map.put(id, BAC);
					
					//System.out.println("id: " + id + " BAC: " + BAC );
				}
			}
		}
		
		// Done with the file
		br.close();
		br = null;
		fis = null;
		
		return map;
	}
	
	
	public static void main(String[] args) throws IOException {
		String InterspeechDoc = args[0];
		String mapFile = args[1];
		
		ArrayList<ArrayList<String>> sets = getInterspeechFiles(InterspeechDoc);
		HashMap<String,Double> map 		  = getBACMap(mapFile);
		
		ArrayList<ArrayList<BACSample>> mapped = getMapping(sets,map);
		
		outputMaps(mapped);
		
		print(mapped,0.0005);
		
	}

	private static void outputMaps(ArrayList<ArrayList<BACSample>> mapped) throws FileNotFoundException, UnsupportedEncodingException {
		
		String [] filenames = new String[3];
		
		filenames[0] = "TRAIN_BAC.csv"; //train_name
		filenames[1] = "D1_BAC.csv";    //dev_name
		filenames[2] = "TEST_BAC.csv";  //test_name
		
		for(int i=0;i<filenames.length;i++){
		
			PrintWriter writer = new PrintWriter(filenames[i], "UTF-8");			
			
			for(BACSample s : mapped.get(i)){
				writer.println(s.getFile() + "," + s.getBac());
			}
			writer.close();	
		}
		
	}
	
	public static void print(ArrayList<ArrayList<BACSample>> mapped, double limit){
		String [] setname = new String[3];
		
		setname[0] = "TRAIN"; //train_name
		setname[1] = "D1";    //dev_name
		setname[2] = "TEST";  //test_name
		
		for(int i=0;i<setname.length;i++){
		
					
			int sum = 0;
			for(BACSample s : mapped.get(i)){
				if(s.getBac() > limit){
					sum++;
				}				
			}
			
			System.out.println(setname[i] + ": ");
			System.out.println("NONALC: " + (mapped.get(i).size() - sum) + " ALC: " + sum);
			System.out.println();
		}
	}

	private static ArrayList<ArrayList<BACSample>> getMapping(
			ArrayList<ArrayList<String>> sets, HashMap<String, Double> map) {
		
		ArrayList<ArrayList<BACSample>> mapped = new ArrayList<ArrayList<BACSample>>();
		
		for(ArrayList<String> set:sets){
			ArrayList<BACSample> mapset = new ArrayList<BACSample>();
			for(String file:set){
				
				mapset.add(new BACSample(file,map.get(file)));
			}
			mapped.add(mapset);
		}
		
		return mapped;
	}
}
