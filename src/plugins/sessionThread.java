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
import plugins.receiver;
import service.config;

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
			
			receiver r = null;
			try {
				r = new receiver(config.RMQHost, "directMsgExchanger", this.masterSession);
				this.masterSession.setReceiver(r);
			} catch (Exception e) {
				this.masterSession.setState(sessionPool.PREPARING);
				e.printStackTrace();
			}
			
			this.masterSession.setState(sessionPool.SOCK_ONLINE);//先setReceiver创建queue，再sock_online接收消息，发送完离线消息再receiver.start()
			this.masterSession.finishSync();
			
			//发送离线消息
			List<offLineMsg> messages = linkHandler.getDbsConn().OLMHandler.getNewOLM(masterSession.getUserId());
			this.masterSession.appendBuffer(protocol.packBatchOLM(messages));	//先写入buffer再清数据库（大不了多发一遍）
			linkHandler.getDbsConn().OLMHandler.clearOLM(masterSession.getUserId());
			if(this.masterSession.bufferSize() != 0) {
				for(String s:masterSession.getBuffer()) {
					try {
						this.writeSock(s);
					} catch (IOException e) {
						e.printStackTrace();
						this.masterSession.interrupt();
					}
				}
				this.masterSession.clearBuffer();
			}
			
			this.masterSession.setState(sessionPool.WORKING);
			if(!r.start()) {//TODO 如果这启动失败了queue里的消息就没了
				this.masterSession.interrupt();
			}else {
				this.masterSession.startConsume();//在旧socket自我消亡后才会endConsume
			}
			//后面如果遇到io exception 则将session状态改为preparing 等待清理/重连
			while(linkHandler.isWorking()) {
				try {
					this.currentInfo = this.readSock();
					//TODO 要解出的信息：发送目标|内容|时间(直接生成)
					List res = protocol.parseMsg(currentInfo);
					int userId = (int)res.get(0);
					String timestamp = (String)res.get(1);
					String content = (String)res.get(2);
					//content 里也包括时间信息、从谁发的信息，但不需要包括发送给谁
					int userState = linkHandler.getUserState(userId);
					//TODO 补完发送逻辑
					switch(userState) {
						case linkHandler.USER_OFFLINE:
						case sessionPool.PREPARING:
						case sessionPool.SYNCING://都是直接向数据库存，gtmd序列号，我决定用timestamp排序,管tmd什么序号直接往数据库存
							
							break;
						case sessionPool.SOCK_ONLINE:
						case sessionPool.WORKING:	//向exchanger发送
							
							break;
						case sessionPool.WAITING_FOR_SYNC://向buffer附加，consumer写完才会进入这一阶段，所以不用担心冲突
							
							break;
					}
					
				} catch (IOException e) {
					this.masterSession.interrupt();
					e.printStackTrace();
				}
			}
			this.masterSession.interrupt();
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