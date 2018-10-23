package plugins;

import java.util.ArrayList;
import java.util.List;

import dbs.tables.offLineMsg;

public class protocol {
	
	public static List<String> packBatchOLM(List<offLineMsg> messages) {
		List<String> strs = new ArrayList<String>(); 
		for(offLineMsg m:messages) {
			strs.add(packOLM(m.getFrom(),m.getTimestamp(),m.getContent()));
		}
		return strs;
	}
	
	public static String packOLM(int from,String timestamp,String content) {
		//TODO pack
		return "";
	}
	
	public static List<offLineMsg> loadBatchOLM(int to,List<String> messages){
		List<offLineMsg> res = new ArrayList<offLineMsg>();
		int i = 1;
		for(String str:messages) {
			res.add(loadOLM(str,i++));
		}
		return res;
	}
	
	public static offLineMsg loadOLM(String str,int serial_num) {
		//TODO load
		offLineMsg OLM = new offLineMsg(1,serial_num,2,"timestamp","content");
		return OLM;
	}
	

}
