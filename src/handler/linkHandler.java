package handler;

import java.util.Map;
import java.util.concurrent.TimeoutException;
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

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * springMVC接收到登录信息后传给这个组件
 * 完成session建立（或转移）后返回token
 * 维护user_id-token-session映射
 * 维护等待新连接的socket
 * @author F4k3yw
 */
public class linkHandler {
	//TODO 把hashMap换成ConcurrentHashMap？
	public static final int USER_OFFLINE = 6;
	
	private static Map<Integer,String> userSessionMapper;
	private static sessionPool sessions;
	private static ServerSocket ss;
	private static Conn2mysql conn;
	private static boolean working = false;
	
	static {
		userSessionMapper = new HashMap<Integer,String>();
		sessions= new sessionPool(config.sessionExpire,config.clearDelay);
		conn = Conn2mysql.getConn();
		
		//创建exchanger
		ConnectionFactory connectionFactory = new ConnectionFactory();
		connectionFactory.setHost(config.RMQHost);
		Connection connection;
		Channel channel;
		try {
			connection = connectionFactory.newConnection();
			channel = connection.createChannel();
	        channel.exchangeDeclare("directMsgExchanger", "direct");
	        channel.close();
	        connection.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
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
	
	synchronized public static void userOffLine(int userId,String sessionId) {
		//只有session未被继承，完全被清除（即sessionId未改变）时才会删除映射
		if(userSessionMapper.get(userId) == sessionId) {
			userSessionMapper.remove(userId);
		}
	}
	
	public static int getUserState(int userId) {
		String sId = null;
		sId = userSessionMapper.get(userId);
		if(sId != null) {
			return sessions.findSession(sId).getState();
		}else {
			return linkHandler.USER_OFFLINE;
		}
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


