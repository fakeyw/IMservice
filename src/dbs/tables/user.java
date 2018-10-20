package dbs.tables;

public class user {
	private int user_id;
	private String credential;
	private int credential_type;
	private String hashed_password;
	private int group;
	private String nickname;
	private String sign;
	private int gender;
	
	public user(int user_id,String credential,int credential_type,String hashed_password,
			int group,String nickname,String sign,int gender) {
		this.user_id = user_id;
		this.credential = credential;
		this.credential_type = credential_type;
		this.hashed_password = hashed_password;
		this.group = group;
		this.nickname = nickname;
		this.sign = sign;
		this.gender = gender;
	}
	
	public user(int user_id,String hashed_password) {
		this.user_id = user_id;
		this.hashed_password = hashed_password;
	}
	
	public void setUser_id(int user_id) {
		this.user_id = user_id;
	}
	
	public int getUser_id() {
		return this.user_id;
	}
	
	public void setCedential(String credential) {
		this.credential = credential;
	}
	
	public String getCredential() {
		return this.credential;
	}
	
	public void setCredential_type(int credential_type) {
		this.credential_type = credential_type;
	}
	
	public int getCredential_type() {
		return this.credential_type;
	}
	
	public void setHashed_password(String hashed_password) {
		this.hashed_password = hashed_password;
	}
	
	public String getHashed_password() {
		return this.hashed_password;
	}
	
	public void userGroup(int group) {
		this.group = group;
	}
	
	public int getGroup() {
		return this.group;
	}
	
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	
	public String getNickname() {
		return this.nickname;
	}
	
	public void setSign(String sign) {
		this.sign = sign;
	}
	
	public String getSign() {
		return this.sign;
	}
	
	public void setgender(int gender) {
		this.gender = gender;
	}
	
	public int getGender() {
		return this.gender;
	}

}
