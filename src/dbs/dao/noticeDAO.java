package dbs.dao;

import java.util.List;
import dbs.tables.notice;
import org.springframework.stereotype.Component;

@Component("noticeDAO")
public interface noticeDAO {
	
	//添加一则系统通知
	public void addNotice(notice n);
	
	//获取最新通知
	public List<notice> getLatestNotice(Object serial_num);
}
