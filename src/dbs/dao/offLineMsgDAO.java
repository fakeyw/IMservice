package dbs.dao;

import java.util.List;
import dbs.tables.offLineMsg;
import org.springframework.stereotype.Component;

@Component("offLineMsgDAO")
public interface offLineMsgDAO {
	
	//取出用户的离线消息
	public List<offLineMsg> getNewOLM(Object user_id);
	
	//存入某用户的离线消息
	//serial_num由java组织
	public void addBatchOLM(List<offLineMsg> OLMs);
	
	//清空一个用户的离线消息
	public void clearOLM(int user_id);
	
}
