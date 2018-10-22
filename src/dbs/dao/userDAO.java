package dbs.dao;

import dbs.tables.user;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

@Component("userDAO")
public interface userDAO{
	
	//由用户标识获取用户，用于密码验证以及确定是否存在
	//type: 0 id及密码 1 全部信息
	public user getUserByCred(String credential,int type) throws DataAccessException;
	
	//获取单个用户
	public user getUserById(Object user_id) throws DataAccessException;
	
	//在业务层，不会暴露以下函数操作时不因接触的参数
	//添加一个用户，id在数据库中从一基础值开始递增
	public void addUser(user u) throws DataAccessException;
	
	//更新用户信息
	public void updateUser(user u) throws DataAccessException;
	
	//删除用户
	public void deleteUser(Object user_id) throws DataAccessException;
}
