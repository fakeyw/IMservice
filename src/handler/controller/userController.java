package handler.controller;

import dbs.Conn2mysql;
import dbs.tables.user;
import handler.linkHandler;

import java.util.Map;

import org.json.JSONObject;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.ui.ModelMap;
import plugins.crypto;
import plugins.requests;
import plugins.token;
import service.config;
/**
 * 用户账户 
 * 用户信息
 * 用户关系
 * @author F4k3yw
 *
 */
@Controller
@RequestMapping("/user")
public class userController {
	private Conn2mysql conn = Conn2mysql.getConn();
	
	//先把注册凭据发到这
	//把注册数据构造一个加密信息串 带着连接一起发到邮箱
	//两轮注册都需要服务端验证账户是否存在
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET,value="/preregister")
	public String preRegister(@RequestParam("credential") String credential,@RequestParam("password") String password) {
		user u = conn.userHandler.getUserByCred(credential, 0);
		if(u == null) {
			String t = token.getMailToken(credential, password);
			//TODO content的组织
			requests.sendMail("target","content");
			return "验证邮件已发送到邮箱";
		}else {
			return "用户已存在";
		}
	}
	
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET,value="/register")
	public String register(@RequestParam("iden")String iden) {
		Map<String,String> infoMap = token.resolveMailToken(iden);
		//token无法解析
		if(infoMap == null) {
			return "注册失败页面";
		}
		long time_ori = Long.parseLong(infoMap.get("timestamp"));
		long time_now = token.timeNow();
		if(time_now-time_ori > config.mailExpire) {
			return "页面过期";
		}
		user newUser = new user(0,infoMap.get("credential"),0,infoMap.get("hashed_password"),1,infoMap.get("credential"),"",0);
		try {
			conn.userHandler.addUser(newUser);
		}catch(DataAccessException e){
			e.printStackTrace();
		}
		return "注册成功";
	}
	
	@ResponseBody
	@RequestMapping("/signin")
	public String signIn(@RequestParam("credential") String credential,@RequestParam("password") String password) {
		user u = conn.userHandler.getUserByCred(credential, 0);
		if(u != null) {
			String hashedPassword = crypto.getHash(password);
			if(hashedPassword == u.getHashed_password()) {
				String sessionId = linkHandler.userSigned(credential);
				return "";
			}
		}
		return "用户名或密码错误";
	}
	
	//token用于身份和权限验证
	@ResponseBody
	@RequestMapping("/userinfo")
	public String userInfo(@RequestParam("credential") String credential,@RequestParam("token") String token) {
		return "userinfo";
	}
	
	@ResponseBody
	@RequestMapping("/changepassword")
	public String changePassword(	@RequestParam("credential") String credential,
									@RequestParam("token") String token,
									@RequestParam("oldpassword") String oldPassword,
									@RequestParam("newpassword") String newPassword) {
		
		return "changepassword";
	}

	@ResponseBody
	@RequestMapping("/addrelationship")
	public String addRelationship(ModelMap model) {
		return "addrelationship";
	}
	
	@ResponseBody
	@RequestMapping("/cancelrelationship")
	public String cancelRelationship(ModelMap model) {
		return "cancelrelationship";
	}
	
	@ResponseBody
	@RequestMapping("/getrelationship")
	public String getRelationship() {
		return "getrelationship";
	}
}



