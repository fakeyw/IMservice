package handler;

import java.util.Map;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import plugins.crypto;
import plugins.sessionPool;
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
	private static sessionPool sessions;
	private static ServerSocket ss;
	private static Conn2mysql conn;
	private static boolean working = false;
	
	static {
		userSessionMapper = new HashMap<Integer,String>();
		sessions= new sessionPool(config.sessionExpire,config.clearDelay);
		conn = Conn2mysql.getConn();
	}
	
	public static void start(){
		try {
			ss = new ServerSocket(12580);
			working = true;
			sessions.startClear();
		}catch(Exception e) {
			e.printStackTrace();
		}
		while(working) {
			Socket socket;
			try {
				socket = ss.accept();
				new sessionThread(socket,sessions);
			} catch (IOException e) {
				e.printStackTrace();
			}
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
			sessions.createSession(sessionId,user_id);
		}else {	//重复登录/断线重连时继承之前的session
			sessions.inheritSession(oldSessionId, sessionId);
		}
		userSessionMapper.put(user_id, sessionId);
		return sessionId;
	}
	
	public static void stop() {
		working = false;
	}
	
	public static boolean isWorking() {
		return working;
	}
	
	public static Conn2mysql getDbsConn() {
		return conn;
	}
}


