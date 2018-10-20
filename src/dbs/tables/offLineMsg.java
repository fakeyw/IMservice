package dbs.tables;

public class offLineMsg {
	private int to;
	private int serial_num;
	private int from;
	private String timestamp;
	private String content;
	
	public offLineMsg(int to,int serial_num,int from,String timestamp,String content) {
		this.to = to;
		this.serial_num = serial_num;
		this.from = from;
		this.timestamp = timestamp;
		this.content = content;
	}
	
	public void setTo(int to) {
		this.to = to;
	}
	
	public int getTo() {
		return this.to;
	}
	
	public void setSerial_num(int serial_num) {
		this.serial_num = serial_num;
	}
	
	public int getSerial_num() {
		return this.serial_num;
	}
	
	public void setFrom(int from) {
		this.from = from;
	}
	
	public int getFrom() {
		return this.from;
	}
	
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	
	public String getTimestamp() {
		return this.timestamp;
	}
	
	public void setContent(String content) {
		this.content = content;
	}
	
	public String getContent() {
		return this.content;
	}
	
}
