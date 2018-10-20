package dbs.dao;

import java.util.List;
import dbs.tables.relationship;
import org.springframework.stereotype.Component;

@Component("relationshipDAO")
public interface relationshipDAO {
	
	//在关系操作前 需要确认两人的关系状态
	public relationship relaStat(int p1,int p2);
	
	//建立关系
	public void createRela(relationship rela);
	
	//judged 0 查询未确认关系 1 查询已确认关系 2+查询所有关系
	//查找用户主动建立的关系
	public List<relationship> getRelaByFrom(Object user_id,int judged);
	
	//查找用户被动建立的关系，两者不可重复
	public List<relationship> getRelaByTo(Object user_id,int judged);
	
	//取消一个关系
	public void deleteRela(relationship rela);
	
	//确认一个关系
	public void judgeRela(relationship rela);
}
