package plugins;

import java.util.Date;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

public class token {

	//精确到毫秒的时间戳
	public static long timeNow() {
		return System.currentTimeMillis();
	}
	
	//组织加密的注册凭据信息，带有时间信息
	public static String getMailToken(String credential,String password) {
		
		return new String();
		
	}
	
	public static Map<String,String> resolveMailToken(String token){
		Map<String,String> resMap = null;
		try {
			JSONObject jobj = new JSONObject(token);
			resMap = (Map<String,String>) jobj;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return resMap;
	}
}
