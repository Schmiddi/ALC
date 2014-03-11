package team2014.weka;

public class Speaker {
	private int 	scd; 		 // speaker id
	private String  sex;
	private int 	age;
	private String 	acc;		 // kind of accent
	private int 	wei;		 // weight
	private int		hei;		 // height
	private String  edu;		 // education
	private String  pro;		 // profession
	private String  smo;		 // is Smoker
	private String  drh;         // drinking habits
	private String  com;
	
	
	public Speaker(int id, String sex, int age, String accent, int weight, int height,
			String education, String profession, String smo, String drh, String com) {
		super();
		this.scd = id;
		this.sex = sex;
		this.age = age;
		this.acc = accent;
		this.wei = weight;
		this.hei = height;
		this.edu = education;
		this.pro = profession;
		this.smo = smo;
		this.drh = drh;
		this.com = com;
	}


	public int getId() {
		return scd;
	}


	public void setId(int id) {
		this.scd = id;
	}


	public String getSex() {
		return sex;
	}


	public void setSex(String sex) {
		this.sex = sex;
	}


	public int getAge() {
		return age;
	}


	public void setAge(int age) {
		this.age = age;
	}


	public String getAccent() {
		return acc;
	}


	public void setAccent(String accent) {
		this.acc = accent;
	}


	public int getWeight() {
		return wei;
	}


	public void setWeight(int weight) {
		this.wei = weight;
	}


	public int getHeight() {
		return hei;
	}


	public void setHeight(int height) {
		this.hei = height;
	}


	public String getEducation() {
		return edu;
	}


	public void setEducation(String education) {
		this.edu = education;
	}


	public String getProfession() {
		return pro;
	}


	public void setProfession(String profession) {
		this.pro = profession;
	}


	public String getSmo() {
		return smo;
	}


	public void setSmo(String smo) {
		this.smo = smo;
	}


	public String getDrh() {
		return drh;
	}


	public void setDrh(String drh) {
		this.drh = drh;
	}


	public String getCom() {
		return com;
	}


	public void setCom(String com) {
		this.com = com;
	}
	
	public String toString(){
		String s ="";
		
		s += this.scd + " ";
		s += this.sex + " ";
		s += this.age + " ";
		s += this.acc + " ";
		s += this.wei + " ";
		s += this.hei + " ";
		s += this.edu + " ";
		s += this.pro + " ";
		s += this.smo + " ";
		s += this.drh + " ";
		s += this.com + " ";
		s += "\n";
		
		return s;
	}
	
	public Boolean isMale(){
		if(this.sex.toUpperCase().equals("M")){
			return true;
		}
		return false;		
	}
	
	public Boolean isFemale(){
		return !isMale();
	}
	
	
	
}
