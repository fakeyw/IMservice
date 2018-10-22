package handler;

import java.util.Map;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import plugins.crypto;
import plugins.session;
import plugins.sessionThread;
import service.config;
import dbs.Conn2mysql;
import dbs.tables.user;

/**
 * springMVC接收到登录信息后传给这个组件
 * 完成session建立（或转移）后返回token
 * 维护user_id-token-session映射
 * 维护等待新连接的socket
 * @author F4k3yw
 */
public class linkHandler {
	//TODO 把hashMap换成ConcurrentHashMap？
	private static Map<Integer,String> userSessionMapper;
	private static Map<String,session> sessions;
	private static ServerSocket ss;
	private static Conn2mysql conn;
	private static boolean working = false;
	
	static {
		userSessionMapper = new HashMap<Integer,String>();
		sessions = new HashMap<String,session>();
		conn = Conn2mysql.getConn();
	}
	
	public static void start(){
		try {
			ss = new ServerSocket(12580);
			working = true;
			new Thread(new Runnable() {
				@Override
				public void run() {
					linkHandler.clearExpire();
					try {
						Thread.sleep(config.clearDelay);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}).start();
		}catch(Exception e) {
			e.printStackTrace();
		}
		while(working) {
			Socket socket;
			try {
				socket = ss.accept();
				new sessionThread(socket);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	//sessionThread通过这一方法获取对应session并与之绑定
	public static session findSession(String sessionId) {
		return sessions.get(sessionId);
	}
	
	//session持久化后自清理时使用
	public static void deleteSession(String sessionId) {
		try {
			sessions.remove(sessionId);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String userSigned(String credential) {
		user u = conn.userHandler.getUserByCred(credential, 0);
		int user_id = u.getUser_id();
		String sessionId = crypto.getUUID();
		String oldSessionId = null;
		oldSessionId = userSessionMapper.get(user_id);
		//用户未登录
		if(oldSessionId == null) {
			session newSession = new session(sessionId,user_id);
			sessions.put(sessionId, newSession);
		}else {	//重复登录时继承之前的session
			session inheritSession = sessions.get("oldSessionId").inherit(sessionId);
			sessions.remove(oldSessionId);
			sessions.put(sessionId, inheritSession);
		}
		userSessionMapper.put(user_id, sessionId);
		return sessionId;
	}
	
	//清理过久未建立消息连接的session
	public static void clearExpire() {
		while(linkHandler.isWorking()) {
			for (session v : sessions.values()) { 
				if(v.getState() == session.PREPERING && !v.inTime()) {
					v.destory();
				}
			}
		}
	}
	
	public static void stop() {
		working = false;
	}
	
	public static boolean isWorking() {
		return working;
	}
}


