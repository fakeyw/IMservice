package handler.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.ui.ModelMap;
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
	
	//先把注册凭据发到这
	//把注册数据构造一个加密信息串 带着连接一起发到邮箱
	//两轮注册都需要服务端验证账户是否存在
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET,value="/preRegister")
	public String preRegister(@RequestParam("credential") String credential) {
		return "preRegister";
	}
	
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET,value="/register")
	public String register(@RequestParam("iden")String iden,@RequestParam("password") String password) {
		//iden是带有时间、账户凭据信息的加密串，需要解密后使用
		return "register";
	}
	
	@ResponseBody
	@RequestMapping("/signin")
	public String signIn(@RequestParam("credential") String credential,@RequestParam("password") String password) {
		
		return "signin";
	}
	
	@ResponseBody
	@RequestMapping("/userinfo")
	public String userInfo(ModelMap model) {
		return "userinfo";
	}
	
	@ResponseBody
	@RequestMapping("/changepassword")
	public String changePassword(ModelMap model) {
		return "changepassword";
	}
	
	//关系处理类操作需要带上自己的token
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



