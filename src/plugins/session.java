package plugins;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import dbs.tables.offLineMsg;
import handler.linkHandler;
import plugins.sessionPool.persistTask;
import service.config;

//用户session
//consumer读出信息后，
public class session {
	private String sessionId;				//身份识别token
	private Socket sock = null;				//信道
	private sessionThread sThread = null;	
	private long startTime;
	private int user_id;
	private int state;
	private boolean consuming = false;
	private List<String> buffer = new ArrayList<String>();	//离线后消化的消息
	private Semaphore syncSemaphore = new Semaphore(1);	//数据域不稳定时，不允许新thread读取
	private sessionPool masterPool = null;
	private receiver r = null;
	protected persistTask pt = null;
	
	public session(String sessionId,int user_id,sessionPool sp){
		this.sessionId = sessionId;
		this.user_id = user_id;
		this.startTime = token.timeNow();
		this.setState(sessionPool.PREPARING);
		this.masterPool = sp;
	}
	
	public boolean inTime() {
		return token.timeNow()-this.startTime < config.sessionExpire;
	}
	
	//sessionThread调用，将自己与session绑定
	synchronized public void bindThread(sessionThread st) {
		this.sThread = st;
		this.setState(sessionPool.SOCK_ONLINE);
	}
	
	//socket突然断开/连接建立失败时由sessionThread调用
	//初始化session环境（保留consumer的工作状态）
	//如果receiver在的话，要保证其在工作状态，并向其发送结束符
	public void interrupt() {
		synchronized(this) {
			sessionThread temp = this.sThread;
			this.sock = null;
			this.sThread = null;
			if(this.getState() == sessionPool.WAITING_FOR_SYNC) {
				this.pt.cancel();
				this.setPersistTask(null);
			}
			this.setState(sessionPool.PREPARING);
			if(this.r!= null && !this.r.isWorking()){
				this.startConsume();
				if(this.r.start()) {	//先start再发送结束符
					this.r.addEndPoint();
				}
			}
			this.r = null;
			temp.interrupt();//最后断掉线程本身
		}
	}
	
	//此时数据域也是稳定的
	//并且这一过程一般不会被打断
	public void persistence () {
		try {
			this.syncSemaphore.acquire();
			this.setState(sessionPool.SYNCING);
			List<offLineMsg> OLMs = protocol.loadBatchOLM(this.user_id,this.buffer);
			linkHandler.getDbsConn().OLMHandler.addBatchOLM(OLMs);
			this.setState(sessionPool.PREPARING);//这一步是为了在sync状态下被继承的session在新thread中继续运行
			this.clearBuffer();//清空buffer
			this.syncSemaphore.release();
			this.masterPool.deleteSession(this.sessionId);//这里的删除是针对未被继承的情况，id也没被更改，id被改的情况下旧映射都被删了
			linkHandler.userOffLine(this.user_id,sessionId);//清除用户-sessionId映射
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	//继承旧session，除了正在持久化的不能打断外，其他状态的session都会被初始化并且强制进入清理queue的状态
	public session inherit(String newSessionId) {
		synchronized(this) {
			this.sessionId = newSessionId;
			if(this.getState() != sessionPool.SYNCING) {
				this.interrupt();
			}
			this.startTime = token.timeNow();
			return this;
		}
	}
	
	//保证状态获取一致
	public int getState() {
		synchronized(this) {
			return this.state;
		}
	}
	
	public void setState(int state) {
		synchronized(this) {
			this.state = state;
		}
	}
	
	public int getUserId() {
		return this.user_id;
	}
	
	synchronized public int bufferSize() {
		return this.buffer.size();
	}
	
	synchronized public void appendBuffer(List<String> strs) {
		for(String s:strs) {
			this.buffer.add(s);
		}
	}
	
	public List<String> getBuffer(){
		return this.buffer;
	}
	
	synchronized public void clearBuffer() {
		this.buffer = new ArrayList<String>();
	}
	
	public void startConsume() {
		synchronized(this) {
			this.consuming = true;
		}
	}
	
	public void endConsume() {
		synchronized(this) {
			this.consuming = false;
		}
	}
	
	public boolean isConsuming() {
		synchronized(this) {
			return this.consuming;
		}
	}
	
	public void setReceiver(receiver r) {
		this.r = r;
	}
	
	public void setPersistTask(persistTask pt) {
		synchronized(this) {
			this.pt = pt;
		}
	}
	
	public void finishSync() {
		try {
			//阻塞到数据域稳定
			this.syncSemaphore.acquire();
			this.syncSemaphore.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
