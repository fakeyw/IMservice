package dbs.tables;

public class notice {
	private int serial_num;
	private String timestamp;
	private int type;
	private String content;
	
	public notice(int serial_num,String timestamp,int type,String content) {
		this.serial_num = serial_num;
		this.timestamp = timestamp;
		this.type = type;
		this.content = content;
	}
	
	public void setSerial_num(int serial_num) {
		this.serial_num = serial_num;
	}
	
	public int getSerial_num() {
		return this.serial_num;
	}
	
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	
	public String getTimestamp() {
		return this.timestamp;
	}
	
	public void setType(int type) {
		this.type = type;
	}
	
	public int getType() {
		return this.type;
	}
	
	public void setContent(String content) {
		this.content = content;
	}
	
	public String getContent() {
		return this.content;
	}
}
