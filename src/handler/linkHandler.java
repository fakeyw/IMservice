package handler;

import java.util.Map;
import java.net.ServerSocket;
import java.util.HashMap;
import plugins.session;
import dbs.Conn2mysql;
import dbs.tables.user;

/**
 * springMVC接收到登录信息后传给这个组件
 * 完成session建立（或转移）后返回token
 * 维护user-token-session映射
 * 维护等待新连接的socket
 * @author F4k3yw
 */
public class linkHandler {
	private static Map<Integer,String> userSessionMapper;
	private static Map<String,session> sessions;
	private static ServerSocket ss;
	
	static {
		userSessionMapper = new HashMap<Integer,String>();
		sessions = new HashMap<String,session>();
		try {
			ss = new ServerSocket();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void userSigned(String credential,String password) {
		Conn2mysql conn = Conn2mysql.getConn();
		user u = conn.userHandler.getUserByCred(credential, 0);
		int user_id = u.getUser_id();
		String ori_hashedPassword = u.getHashed_password();
	}
}
