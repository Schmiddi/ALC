package team2014.weka;

public class Sample {
	private String user_id;
	private String block_nr;
	private String session_nr;
	private String recording_nr;
	private String type; 			//h  : headset channel, m : mouse micro
	private String version;
	private String alc;
		
	public Sample(String filename, String alc){
		this.user_id    	= filename.substring(0,3);
		this.block_nr   	= filename.substring(3,5);
		this.session_nr 	= filename.substring(5,7);
		this.recording_nr   = filename.substring(7,10);
		this.type    		= filename.substring(11,12);
		this.version   		= filename.substring(13,15);
		this.alc 			= alc;
	}

	public String getUser_idStr() {
		return user_id;
	}
	
	public int getUser_id() {
		return Integer.parseInt(this.user_id);
	}

	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}

	public String getBlock_nrStr() {
		return block_nr;
	}
	public int getBlock_nr() {
		return Integer.parseInt(this.block_nr);
	}

	public void setBlock_nr(String block_nr) {
		this.block_nr = block_nr;
	}

	public String getSession_nrStr() {
		return session_nr;
	}
	
	public int getSession_nr() {
		return Integer.parseInt(this.session_nr);
	}

	public void setSession_nr(String session_nr) {
		this.session_nr = session_nr;
	}

	public String getRecording_nrStr() {
		return recording_nr;
	}
	
	public int getRecording_nr() {
		return Integer.parseInt(this.recording_nr);
	}

	public void setRecording_nr(String recording_nr) {
		this.recording_nr = recording_nr;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getVersionStr() {
		return version;
	}
	
	public int getVersion() {
		return Integer.parseInt(this.version);
	}

	public void setVersion(String version) {
		this.version = version;
	}
	
	public String getFilename(){
		return user_id + block_nr + session_nr + recording_nr + "_" + type + "_" + version;
	}
	
	public Boolean isIntoxicated(){
		if(alc.toUpperCase().contains("NON")){
			return false;
		}
		return true;
	}
	
	public Boolean isSober(){
		return !isIntoxicated();
	}
	
}
