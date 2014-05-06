package team2014.weka;

public class Utils {
	
	
	/**
	 * return the flag values of the arguments which are passed by the main method
	 * 
	 * @param flag
	 * @param args
	 * @return
	 */
	public static String getFlag(String flag, String [] args){
		for(int i=0;i<args.length;i++){
			if(args[i].equals(flag)){
				if(!args[i].contains("-") || args[i].startsWith("-h")){
					return "flag \""+flag + "\" exists.";
				}
				else{
					return args[i+1];
				}
			}
		}
		return null;
	}
	
	public static String getFlag(String [] flags, String [] args){
		String ret = null;
		for(String flag:flags){
			ret = getFlag(flag, args);
			if(ret != null){
				return ret;
			}
		}
		return null;
	}
	
	public static Boolean isFlag(String [] flags, String args[]){
		String ret = getFlag(flags, args);
		if(ret != null) return true;
		return false;
	}
	
	public static Boolean isFlag(String [] flags, String [] values, String args[]){
		String ret = getFlag(flags, args);
		
		//return error when the wrong type of argument was used
		for(String flag:flags){
			if(!flag.contains("-")){
				return null;
			}
		}
		
		if(ret == null) return false;
		
		for(String val: values){
			if(ret.equals(val)){
				return true;
			}
		}
		return false;
		
	}
}
