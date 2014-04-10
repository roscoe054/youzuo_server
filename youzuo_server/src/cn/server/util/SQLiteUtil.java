package cn.server.util;

import SQLite.*;

import java.io.File;
import java.sql.*;

public class SQLiteUtil {

	public SQLiteUtil() {
		super();
	}

	Database db = new Database();
	
	//sae
//	private String dbDriver = "com.mysql.jdbc.Driver"; // 与本地设置相同
//	private String dbUrl = "jdbc:mysql://w.rdc.sae.sina.com.cn:3307/app_youzuo?characterEncoding=UTF-8"; // app_yanzel为新浪app数据库名称
//	private String dbUser = "3wyy4jn2w2"; // 为[应用信息]->[汇总信息]->[key]中的access key
//	private String dbPassword = "ilzkijh4ymzx1l5kh2hzjj2j51l5140k13xkymhm";
	
	//local
	private String dbDriver = "com.mysql.jdbc.Driver"; // 与本地设置相同
	private String dbUrl = "jdbc:mysql://localhost/youzuo?characterEncoding=UTF-8"; // app_yanzel为新浪app数据库名称
	private String dbUser = "root"; // 为[应用信息]->[汇总信息]->[key]中的access key
	private String dbPassword = "youzuo";

	public Connection connect() {

//		 连接MySQL
		 try {
			 
		 Class.forName(dbDriver).newInstance();
		 Connection connection= DriverManager.getConnection(dbUrl,
		 dbUser,dbPassword);
		
		 return connection;
		 }catch (ClassNotFoundException e) {
		 // TODO Auto-generated catch block
		 e.printStackTrace();
		 } catch (SQLException e) {
		 // TODO Auto-generated catch block
		 e.printStackTrace();
		 } catch (InstantiationException e) {
		 // TODO Auto-generated catch block
		 e.printStackTrace();
		 } catch (IllegalAccessException e) {
		 // TODO Auto-generated catch block
		 e.printStackTrace();
		 }

		return null;
	}

	public void closeConnection(Connection con) {
		try {
			con.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
