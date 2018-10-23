package plugins;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

import dbs.tables.offLineMsg;
import handler.linkHandler;

//由linkHandler在接收新socket时创建
//绑定流程：
//在获取到sessionId时
//不存在就不多bb，扔个错误回去
//如果存在则绑定，发送一段确认信息
//1.检查session状态
//2.如果在sync则等待状态变为prepareing后继续
//	此时状态改为sock_online
//3.向mq注册consumer(consumer每次从queue中获取到信息后，都要检查session状态已确定消息写入方向，preparing -> buffer, other -> socket)
//4.检查session的buffer里有没有消息
//5.如果没有，检查数据库里有没有离线消息
//6.状态改为working,consumer开始工作
public class sessionThread extends Thread{
	private Socket sock = null;
	private String currentInfo = "";
	private session masterSession = null;
	private sessionPool sp = null;
	
	public sessionThread(Socket sock,sessionPool sp){
		this.sock = sock;
		this.sp = sp;
	}
	
	@Override
	public void run() {
		try {
			this.currentInfo = readSock();
			if(this.currentInfo != null) {
				session target = this.sp.findSession(currentInfo);
				if(target != null) {
					target.bindThread(this);
					this.masterSession = target;
					this.writeSock("bingo!");
				}else {
					this.writeSock("session not found.");
				}
			}
		} catch (IOException e) {
			masterSession = null;
			e.printStackTrace();
		}
		
		if(this.masterSession != null) {
			//等待旧consumer将queue中剩余消息转移
			//或者已进入的持久化过程完成
			this.masterSession.finishSync();
			this.masterSession.setState(sessionPool.SOCK_ONLINE);
			//TODO 绑定新consumer
			this.masterSession.startConsume();
			
			//发送离线消息
			List<offLineMsg> messages = linkHandler.getDbsConn().OLMHandler.getNewOLM(masterSession.getUserId());
			this.masterSession.appendBuffer(protocol.packBatchOLM(messages));	//先写入buffer再清数据库（大不了多发一遍）
			linkHandler.getDbsConn().OLMHandler.clearOLM(masterSession.getUserId());
			if(this.masterSession.bufferSize() != 0) {
				for(String s:masterSession.getBuffer()) {
					try {
						this.writeSock(s);
					} catch (IOException e) {
						this.masterSession.setState(sessionPool.PREPARING);
						e.printStackTrace();
					}
				}
			}
			
		}
		
		//后面如果遇到io exception 则将session状态改为prepareing 等待清理/重连
		//TODO 开始接收用户消息
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