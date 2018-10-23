package plugins;

import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import dbs.tables.offLineMsg;
import handler.linkHandler;
import service.config;

public class sessionPool{
	//session刚建立/刚被继承/被继承后持久化结束
	public static final int PREPARING = 0;
	
	//已绑定通信socket
	//同时已绑定consumer
	//此时可向queue发送消息
	//但consumer并不工作
	public static final int SOCK_ONLINE = 1;
	
	//已发送离线消息
	//此时consumer开始工作
	public static final int WORKING = 2;
	
	//socket断开或被清理，等待本地化离线消息
	public static final int WAITING_FOR_SYNC = 3;
	
	//本地化任务已经在处理
	//此时根据getBufferLength计算
	//当检测到状态为PREPARING时，根据数据库存储计算
	public static final int SYNCING = 4;
	
	private boolean working = true;
	private Map<String,session> sessions;
	private long sessionExpire = 0;
	private long clearDelay = 0;
	
	public sessionPool(long sessionExpire,long clearDelay) {
		this.sessionExpire = sessionExpire;
		this.clearDelay = clearDelay;
	}
	
	//sessionThread通过这一方法获取对应session并与之绑定
	public session findSession(String sessionId) {
		return sessions.get(sessionId);
	}
	
	//session持久化后自清理时使用
	public void deleteSession(String sessionId) {
		try {
			sessions.remove(sessionId);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void createSession(String sessionId,int user_id) {
		session newSession = new session(sessionId,user_id,this);
		this.sessions.put(sessionId,newSession);
	}
	
	public void inheritSession(String oldId,String newId) {
		session inheritSession = sessions.get(oldId).inherit(newId);
		sessions.remove(oldId);
		sessions.put(newId, inheritSession);
	}
	
	//清理过久未建立消息连接且数据已稳定的session
	public void clearExpire() {
		while(linkHandler.isWorking()) {
			for (session v : this.sessions.values()) { 
				if(v.getState() == sessionPool.PREPARING && !v.inTime() && v.isConsuming() == false) {
					v.destory();
				}
			}
		}
	}
	
	public void startClear() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(working) {
					clearExpire();
					try {
						Thread.sleep(clearDelay);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}
}

//用户session
//consumer读出信息后，
class session {
	private String sessionId;				//身份识别token
	private Socket sock = null;				//信道
	private sessionThread sThread = null;	
	private long startTime;
	private int user_id;
	private int state;
	private boolean consuming = false;
	private List<String> buffer = new ArrayList<String>();	//离线后消化的消息
	private int appendCount = 0;					//在sync状态下新消息的计数，用于计算直接存入数据库时的编号
	private Semaphore syncSemaphore = new Semaphore(1);	//数据域不稳定时，不允许新thread读取
	private sessionPool masterPool = null;
	
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
	
	public void destory() {
		this.setState(sessionPool.WAITING_FOR_SYNC);
		//TODO 清理任务封装
		//清理sessionThread
		//session封装到task中等待清理
		//同时将id封装到task中
		//避免继承sync状态session时id被修改产生的冲突
	}
	
	//此时数据域也是稳定的
	//并且这一过程一般不会被打断
	public void persistence () {
		try {
			this.syncSemaphore.acquire();
			this.setState(sessionPool.SYNCING);
			List<offLineMsg> OLMs = protocol.loadBatchOLM(this.user_id,this.buffer);
			linkHandler.getDbsConn().OLMHandler.addBatchOLM(OLMs);
			this.clearBuffer();//在状态更改后清空buffer,以免序号计算错误
			this.appendCount = 0;
			this.setState(sessionPool.PREPARING);//这一步是为了在sync状态下被继承的session在新thread中继续运行
			this.syncSemaphore.release();
			this.masterPool.deleteSession(this.sessionId);//这里的删除是针对未被继承的情况
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	//继承，由linkHandler调用，不做过多处理
	synchronized public session inherit(String newSessionId) {
		int stat = this.getState();
		if(this.state != sessionPool.SYNCING) {
			this.sessionId = newSessionId;
			if(stat == sessionPool.WAITING_FOR_SYNC) {
				//TODO 取消本地化任务
			}
			this.sThread.interrupt();
			this.sThread = null;
			this.sock = null;
			this.setState(sessionPool.PREPARING);//到这个状态后会在过期时被清理
		}
		this.startTime = token.timeNow();
		return this;
	}
	
	synchronized public int getState() {
		return this.state;
	}
	
	synchronized public void setState(int state) {
		this.state = state;
	}
	
	public int getUserId() {
		return this.user_id;
	}
	
	synchronized public int getNextNum() {
		return this.buffer.size()+(++this.appendCount);
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
	
	synchronized public void startConsume() {
		this.consuming = true;
	}
	
	synchronized public void endConsume() {
		this.consuming = false;
	}
	
	synchronized public boolean isConsuming() {
		return this.consuming;
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