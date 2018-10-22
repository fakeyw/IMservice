package plugins;

import java.io.InputStream;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.Semaphore;

import handler.linkHandler;
import service.config;

//用户session
//consumer读出信息后，
public class session {
	//session刚建立
	public static final int PREPERING = 0;
	
	//已绑定通信socket
	//此时可向queue发送消息
	//但consumer并不工作
	public static final int SOCK_ONLINE = 1;
	
	//已发送离线消息
	//此时consumer开始工作
	public static final int WORKING = 2;
	
	//socket断开或被清理，等待本地化离线消息
	public static final int WAITING_FOR_SYNC = 3;
	
	//本地化任务已经在处理
	public static final int SYNCING = 4;
	
	private String sessionId;				//身份识别token
	private Socket sock = null;				//信道
	private sessionThread sThread = null;	
	private long startTime;
	private int user_id;
	private int state;
	private List<String> buffer;			//消化的离线消息
	private Semaphore syncSemaphore = new Semaphore(1);	//继承sync状态的session，sessionThread阻塞到信号量被持久化函数释放
	
	public session(String sessionId,int user_id){
		this.sessionId = sessionId;
		this.user_id = user_id;
		this.startTime = token.timeNow();
		this.state = this.PREPERING;
	}
	
	public boolean inTime() {
		return token.timeNow()-this.startTime > config.sessionExpire;
	}
	
	//sessionThread调用，将自己与session绑定
	public void bindThread(sessionThread st) {
		this.sThread = st;
		this.state = this.SOCK_ONLINE;
	}
	
	public void destory() {
		this.state = this.WAITING_FOR_SYNC;
		//让consumer将queue中余下的消息读入buffer
		//清理sessionThread
		//session封装到task中等待清理
	}
	
	//封装的task将调用这个方法进行持久化
	//看看能不能让登录挂起到持久化结束
	public void persistence () {
		try {
			this.syncSemaphore.acquire();
			this.state = this.SYNCING;
			//持久化过程
			this.state = this.PREPERING;//这一步是为了在sync状态下被继承的session在新thread中继续运行，下面的删除不影响已被转移的session
			this.syncSemaphore.release();
			linkHandler.deleteSession(this.sessionId);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	//继承，由linkHandler调用，不做过多处理
	public session inherit(String newSessionId) {
		if(this.state != this.SYNCING) {
			if(this.state == this.PREPERING) {
				this.sessionId = newSessionId;
			}else if(this.state == SOCK_ONLINE) {
				//正在发送离线消息的处理
			}else if(this.state == this.WORKING) {
				//清理sessionThread和旧socket
			}else if(this.state == this.WAITING_FOR_SYNC) {
				//取消本地化任务
			}
			this.startTime = token.timeNow();
			this.state = this.PREPERING;
		}
		//如果正在同步，则不更改状态，继承后的sessionThread会wait到本地化结束再继续进程
		//TODO 也可考虑用saction来打断数据存储，直接从buffer读，但感觉很麻烦的样子
		return this;
	}
	
	public int getState() {
		return this.state;
	}
	
	public void finishSync() {
		
	}
}