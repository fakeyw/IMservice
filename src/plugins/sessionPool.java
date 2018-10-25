package plugins;

import java.io.IOException;

import java.util.Map;
import plugins.workerPool.basicTask;

import handler.linkHandler;

public class sessionPool{
	//session刚建立/刚被继承/被继承后持久化结束
	public static final int PREPARING = 0;
	
	//绑定通信socket时立即更改
	//此时可向queue发送消息
	//不等待consumer向buffer写入完成的原因是：写入的是旧consumer，新consumer接收新消息
	//新consumer不工作
	public static final int SOCK_ONLINE = 1;
	
	//已发送离线消息
	//此时consumer开始工作
	public static final int WORKING = 2;
	
	//socket断开或被清理，等待本地化离线消息
	public static final int WAITING_FOR_SYNC = 3;
	
	//本地化任务已经在处理
	public static final int SYNCING = 4;
	
	private boolean working = true;
	private Map<String,session> sessions;
	private long sessionExpire = 0;
	private long clearDelay = 0;
	private workerPool cleaner = null;
	
	public sessionPool(long sessionExpire,long clearDelay) {
		this.sessionExpire = sessionExpire;
		this.clearDelay = clearDelay;
		this.cleaner = new workerPool(1000);
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
	public void clearExpire() throws IOException {
		while(linkHandler.isWorking()) {
			for (session v : this.sessions.values()) { 
				if(v.getState() == sessionPool.PREPARING && !v.inTime() && v.isConsuming() == false) {
					v.setState(sessionPool.WAITING_FOR_SYNC);
					persistTask pt = new persistTask(v);
					v.setPersistTask(pt);
					this.cleaner.addTask(pt);
				}
			}
		}
	}
	
	public void startClear() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(working) {
					try {
						clearExpire();
						Thread.sleep(clearDelay);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}
	
	public class persistTask implements basicTask{
		private session targetSession;
		private boolean canceled;
		
		public persistTask(session target) {
			this.targetSession = target;
			this.canceled = false;
		}

		@Override
		public void run() {
			if(!this.canceled) {
				this.targetSession.setState(sessionPool.SYNCING);
				this.targetSession.persistence();
			}
		}

		@Override
		public void cancel() {
			synchronized(this) {
				this.canceled = true;
			}
		}
	}
}

