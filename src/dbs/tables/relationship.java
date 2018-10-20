package dbs.tables;

public class relationship {
	private int from;
	private int to;
	private int rela;
	private int judge;
	
	public relationship(int from,int to,int rela,int judge) {
		this.from = from;
		this.to = to;
		this.rela = rela;
		this.judge = judge;
	}
	
	public void setFrom(int from) {
		this.from = from;
	}
	
	public int getFrom() {
		return this.from;
	}
	
	public void setTo(int to) {
		this.to = to;
	}
	
	public int getTo() {
		return this.to;
	}
	
	public void setRela(int rela) {
		this.rela = rela;
	}
	
	public int getRela() {
		return this.rela;
	}
	
	public void setJudge(int judge) {
		this.judge = judge;
	}
	
	public int getJudge() {
		return this.judge;
	}
}

