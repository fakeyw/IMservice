package dbs;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import dbs.dao.userDAO;
import dbs.tables.user;

import dbs.dao.relationshipDAO;
import dbs.tables.relationship;

import dbs.dao.offLineMsgDAO;
import dbs.tables.offLineMsg;

import dbs.dao.noticeDAO;
import dbs.tables.notice;

public class Conn2mysql {
	public userDAO userHandler = null;
	public relationshipDAO relaHandler = null;
	public offLineMsgDAO OLMHandler = null;
	public noticeDAO noticeHandler = null;
	private static Conn2mysql conn = null;
	
	public Conn2mysql() {
		ApplicationContext ctx=new ClassPathXmlApplicationContext(new String[] { "applicationContext.xml" });
		this.userHandler = (userDAO)ctx.getBean("userDAO");
		this.relaHandler = (relationshipDAO)ctx.getBean("relationshipDAO");
		this.OLMHandler = (offLineMsgDAO)ctx.getBean("offLineMsgDAO");
		this.noticeHandler = (noticeDAO)ctx.getBean("noticeDAO");
	}
	
	public static Conn2mysql getConn() {
		if(conn == null) {
			conn = new Conn2mysql();
		}
		return conn;
	}
	
	public static void main(String[] args) {
		Conn2mysql conn = new Conn2mysql();
		try { 
			//user
			user uRes = conn.userHandler.getUserById(1);
			System.out.println(uRes.getCredential());
			//conn.userHandler.addUser(new user(0,"fakeyw@163.com",0,"#",0,"fakeyw","",0));
			user uRes2 = conn.userHandler.getUserByCred("fakeyw@163.com",0);
			System.out.println(uRes2.getHashed_password());
			
			//relationship
			conn.relaHandler.createRela(new relationship(1,100000,0,0));
			relationship re = conn.relaHandler.relaStat(1, 100000);
			if(re == null) {
				System.out.println("null");
			}else {
				System.out.println(re.getJudge());
			}
			
			conn.relaHandler.judgeRela(new relationship(1,100000,0,1));
			re = conn.relaHandler.relaStat(1, 100000);
			if(re == null) {
				System.out.println("null");
			}else {
				System.out.println(re.getJudge());
			}
			
			conn.relaHandler.deleteRela(new relationship(1,100000,0,1));
			
			//notice
			conn.noticeHandler.addNotice(new notice(0,"123",0,"1"));
			conn.noticeHandler.addNotice(new notice(0,"123",0,"2"));
			conn.noticeHandler.addNotice(new notice(0,"123",0,"3"));
			conn.noticeHandler.addNotice(new notice(0,"123",0,"4"));
			conn.noticeHandler.addNotice(new notice(0,"123",0,"5"));
			List<notice> Ln = conn.noticeHandler.getLatestNotice(3);
			for(notice n:Ln) {
				System.out.println(n.getContent());
			}
			
			//offlinemsg
			List<offLineMsg> Lm = new ArrayList();
			String[] ctts = {"a","b","c","d","e"};
			String[] stmp = {"1","2","3","4","5"};
			for(int i=0;i<5;i++) {
				Lm.add(new offLineMsg(1,i,100000,stmp[i],ctts[i]));
			}
			conn.OLMHandler.addBatchOLM(Lm);
			
			List<offLineMsg> res = conn.OLMHandler.getNewOLM(1);
			for(offLineMsg o:res) {
				System.out.println(o.getContent());
			}
			
			conn.OLMHandler.clearOLM(1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}




