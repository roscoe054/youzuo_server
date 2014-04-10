package cn.server.model;

public class Restaurant {
	private String name;
	private int evaluation;
	private int waitingNum;
	private String location;
	private String tel;
	private String introduct;

	public Restaurant() {
		super();
	}

	public Restaurant(String name, int eva, int wai, String loc, String tel,
			String intro) {
		super();
		this.name = name;
		this.evaluation = eva;
		this.waitingNum = wai;
		this.location = loc;
		this.tel = tel;
		this.introduct = intro;
	}

	// name
	public String getName() {
		return name;
	}

	public void setId(String name) {
		this.name = name;
	}

	// evaluation
	public int getevaluation() {
		return evaluation;
	}

	public void setevaluation(int evaluation) {
		this.evaluation = evaluation;
	}

	// waitingNum
	public int getwaitingNum() {
		return waitingNum;
	}

	public void setwaitingNum(int waitingNum) {
		this.waitingNum = waitingNum;
	}

	// location
	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	// tel
	public String gettel() {
		return tel;
	}

	public void settel(String tel) {
		this.tel = tel;
	}

	// introduct
	public String getintroduct() {
		return introduct;
	}

	public void setintroduct(String introduct) {
		this.introduct = introduct;
	}
}
