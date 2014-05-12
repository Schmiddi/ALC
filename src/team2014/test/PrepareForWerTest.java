package team2014.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import team2014.weka.WekaMagic;

public class PrepareForWerTest {
	public static void main(String[] args) {
		if (args.length < 3) {
			System.out.println("To less arguments, you need 3 arguments!");
			return;
		}

		String pathRecognizedSentence = args[0];
		String pathOriginalTranscription = args[1];
		String pathOutput = args[2];

		Map<String, String> mapRecognized = null;
		Map<String, String> mapOriginal = null;

		try {
			mapRecognized = readOutputFile(pathRecognizedSentence);
			mapOriginal = readOutputFile(pathOriginalTranscription);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<String> result = mergeToResultLine(mapRecognized, mapOriginal);

		System.out.println(result.get(0));

		FileWriter fstream;
		BufferedWriter out;

		try {
			fstream = new FileWriter(pathOutput);
			out = new BufferedWriter(fstream);
			out.write("choice1:ortho\n");
			for (String line : result) {
				out.write(line);
				out.write("\n");
			}
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static Map<String, String> readOutputFile(String path) throws IOException {
		Map<String, String> map = new HashMap<String, String>();

		File file = new File(path);

		FileInputStream fileStream = new FileInputStream(file);

		DataInputStream in = new DataInputStream(fileStream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));

		String strLine;

		// Read File Line By Line
		while ((strLine = br.readLine()) != null) {
			String[] splitResult = strLine.split(",");

			// Clean Text
			splitResult[1] = WekaMagic.cleanString(splitResult[1]);
			// to lower case - I am not really sure if this is a good idea
			splitResult[1] = splitResult[1].toLowerCase();

			map.put(splitResult[0], splitResult[1]);
		}

		in.close();

		return map;
	}

	public static List<String> mergeToResultLine(Map<String, String> mapRecognized,
			Map<String, String> mapOriginal) {
		List<String> result = new ArrayList<String>();
		Iterator it = mapRecognized.entrySet().iterator();

		while (it.hasNext()) {
			@SuppressWarnings("unchecked")
			Map.Entry<String, String> pairs = (Map.Entry<String, String>) it.next();
			
			String original = mapOriginal.get(pairs.getKey());
			
			// Skip if it's not available in the original file
			if(original != null)
				result.add(pairs.getValue() + ":" + original);
		}
		return result;
	}
}
