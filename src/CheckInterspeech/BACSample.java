package CheckInterspeech;

public class BACSample {
	private String file;
	private double bac;
	
	public BACSample(String file, double bac) {
		super();
		this.file = file;
		this.bac = bac;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public double getBac() {
		return bac;
	}

	public void setBac(double bac) {
		this.bac = bac;
	}
	
	
}
