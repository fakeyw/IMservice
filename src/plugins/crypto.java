package plugins;

import java.util.UUID;


public class crypto {
	
	public crypto() {
		
	}
	
	public static String getUUID() {
		return UUID.randomUUID().toString().replace("-","");
	}
	
	//组织加密的注册凭据信息
	public static String mailToken(String credential) {
		return new String();
		
	}
	
	
	
	
}
