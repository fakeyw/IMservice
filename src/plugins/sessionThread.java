package plugins;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

import handler.linkHandler;

//由linkHandler在接收新socket时创建
//绑定流程：
//在获取到sessionId时
//不存在就不多bb，扔个错误回去
//如果存在则绑定，发送一段确认信息
//1.检查session状态
//2.如果在sync则等待状态变为prepareing后继续
//	此时状态改为sock_online
//3.检查session的buffer里有没有消息
//4.如果没有，检查数据库里有没有离线消息
//5.状态改为working,consumer开始工作
public class sessionThread extends Thread{
	private Socket sock = null;
	private String currentInfo = "";
	private session masterSession = null;
	
	public sessionThread(Socket sock){
		this.sock = sock;
	}
	
	@Override
	public void run() {
		try {
			this.currentInfo = readSock();
			if(this.currentInfo != null) {
				session target = linkHandler.findSession(currentInfo);
				if(target != null) {
					target.bindThread(this);
					this.masterSession = target;
					this.writeSock("bingo!");
				}else {
					this.writeSock("session not found.");
				}
			}
		} catch (IOException e) {
			//这里断开连接，session还是prepareing状态，不用作处理
			e.printStackTrace();
		}
		
		if(this.masterSession != null) {
			if(masterSession.getState() == session.SYNCING) {
				masterSession.finishSync();
			}
			//开始发送离线消息
		}
	}
	
	private String readSock() throws IOException {
		String next = null;
		String info = null;
		InputStream is;
		is = this.sock.getInputStream();
		InputStreamReader isr =new InputStreamReader(is);
		BufferedReader br =new BufferedReader(isr);
		while((next=br.readLine())!=null){
			info += next;
		}
		return info;
	}
	
	private void writeSock(String str) throws IOException {
		OutputStream os = this.sock.getOutputStream();
		PrintWriter pw = new PrintWriter(os);
		pw.write(str);
		pw.flush();
	}
}