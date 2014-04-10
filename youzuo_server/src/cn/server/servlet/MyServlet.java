package cn.server.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import cn.server.util.SQLiteUtil;
import cn.server.model.*;

public class MyServlet extends HttpServlet {
//	String dbDriver = "com.mysql.jdbc.Driver"; // 与本地设置相同
//	String dbUrl = "jdbc:mysql://w.rdc.sae.sina.com.cn:3307/app_youzuo?characterEncoding=UTF-8"; // app_yanzel为新浪app数据库名称
//	String dbUser = "3wyy4jn2w2"; // 为[应用信息]->[汇总信息]->[key]中的access key
//	String dbPassword = "ilzkijh4ymzx1l5kh2hzjj2j51l5140k13xkymhm";
	//local
	private String dbDriver = "com.mysql.jdbc.Driver"; // 与本地设置相同
	private String dbUrl = "jdbc:mysql://w.rdc.sae.sina.com.cn:3307/app_youzuo?characterEncoding=UTF-8"; // app_yanzel为新浪app数据库名称
	private String dbUser = "3wyy4jn2w2"; // 为[应用信息]->[汇总信息]->[key]中的access key
	private String dbPassword = "ilzkijh4ymzx1l5kh2hzjj2j51l5140k13xkymhm";
	
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		PrintWriter out = null;

		response.setContentType("text/html;charset=UTF-8");
		out = response.getWriter();
//		byte[] resultKey = request.getParameter("key").getBytes("ISO-8859-1");
//		String key = new String(resultKey, "UTF-8");
		String key = request.getParameter("key");
		
		if (!key.equals("reservation")&&!key.equals("deleteReservation")) {//查询餐厅
			try {
				System.out.println("用户输入:key=" + key);
				// 执行查询(本地)
//				out.println(MyServlet.searchRes(key));

				// 执行查询(SAE)
				out.println(searchInSAE(key));

			} catch (Exception ex) {
				out.print("error");
			} finally {
				out.flush();
				out.close();
			}
		}
		
		//从数据库中删除预定
		else if(key.equals("deleteReservation")){
//			byte[] resultName = request.getParameter("name").getBytes("ISO-8859-1");
//			String name = new String(resultName, "UTF-8");
			String name =request.getParameter("name");
			
			String code=request.getParameter("code");
			
			try {
				Class.forName(dbDriver).newInstance();
				Connection connection = DriverManager.getConnection(dbUrl, dbUser,
						dbPassword);
				Statement st = connection.createStatement();
				ResultSet resultset = null;
				//从reservation表中删除
				String sqlDeleteReservation = "DELETE FROM reservation "
						+ "WHERE name = '"+name+"' AND code = '"+code+"'";
				if(st.execute(sqlDeleteReservation)){
					resultset=st.getResultSet();
				}
				//从restaurant表中删除
				String sqlGetWaitingNum = "select waitingNum,queue from restaurant WHERE name='"
						+ name + "'";//得到队列和等待人数
				if(st.execute(sqlGetWaitingNum)){
					resultset=st.getResultSet();
				}
				int tempWaitingNum = 0;//该餐厅等待人数
				String tempQueue="";//该餐厅等待队列
				while (resultset.next()) {
					tempWaitingNum = resultset.getInt(1);
					tempQueue=resultset.getString(2);
				}
				tempWaitingNum--;//等待人数减1
				int position=tempQueue.indexOf(code);
				if(position==-1){
					return;
				}
				if(position+2<tempQueue.length()){
					tempQueue=tempQueue.substring(0,position)+
						tempQueue.substring(position+2,tempQueue.length());//从队列中删除
				}
				else{
					tempQueue=tempQueue.substring(0,position);//从队列中删除
				}
				String sqlUpdateWaitingNum = "Update restaurant SET waitingNum="//更新等待人数
						+ tempWaitingNum + " WHERE name='" + name+ "'";
				if(st.execute(sqlUpdateWaitingNum)){
					resultset=st.getResultSet();
				}
				String sqlUpdateQueue= "Update restaurant SET queue='"//更新队列
						+ tempQueue + "' WHERE name='" + name+ "'";
				if(st.execute(sqlUpdateQueue)){
					resultset=st.getResultSet();
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//提交预定
		else {
//			byte[] resultName = request.getParameter("name").getBytes("ISO-8859-1");
//			String keyName = new String(resultName, "UTF-8");
			String keyName = request.getParameter("name");
			
			String sqlGetWaitingNum = "select waitingNum,queue from restaurant WHERE name='"
					+ keyName + "'";//得到当前餐厅的等待人数

			try {
				Class.forName(dbDriver).newInstance();
				Connection connection = DriverManager.getConnection(dbUrl, dbUser,
						dbPassword);
				Statement st = connection.createStatement();
				
				ResultSet resultset = null;
				if(st.execute(sqlGetWaitingNum)){
					resultset=st.getResultSet();
				}
				String code;//随机码
				int tempWaitingNum = 0;//该餐厅等待人数
				String tempQueue="";//该餐厅等待队列
				while (resultset.next()) {
					tempWaitingNum = resultset.getInt(1);
					tempQueue=resultset.getString(2);
				}
				String sqlUpdateWaitingNum = "Update restaurant SET waitingNum="//更新等待人数(+1)
						+ (tempWaitingNum + 1) + " WHERE name='" + keyName+ "'";
				if(st.execute(sqlUpdateWaitingNum)){
					resultset=st.getResultSet();
				}
				
				do{
					code = getCharAndNumr(2);//生成2位随机码
				}while(tempQueue.contains(code));//如果队列中已经存在该号码则重新生成
				tempQueue+=code;//加入队列
				
				//更新等待队列
				String sqlUpdateQueue="Update restaurant SET queue='"
				+ tempQueue + "' WHERE name='" + keyName+ "'";
				if(st.execute(sqlUpdateQueue)){
					resultset=st.getResultSet();
				}
				out.print(code);
				
				//加入到reservation表
				SimpleDateFormat currentTime = new SimpleDateFormat("MM-dd HH:mm");//设置日期格式
				String currentString=currentTime.format(new Date());
				String sqlInsertQueue="INSERT INTO reservation (id,code,name,time,status) VALUES "
						+ "('10','"+code+"','"+keyName+"','"+currentString+"' ,'等待中')";
				if(st.execute(sqlInsertQueue)){
					resultset=st.getResultSet();
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				out.print("error");
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				out.flush();
				out.close();
			}
		}
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

	public static String searchRes(String key) { //本地查询餐厅
		String sql = "select name,evaluation,waitingNum,location,tel,introduct"
				+ " from restaurant WHERE location LIKE '%" + key
				+ "%' or name LIKE '%" + key + "%'";
//		String sql="select name,evaluation,waitingNum,location,tel,introduct from restaurant"+
//				" WHERE location LIKE BINARY '%清%'";
		
		try {
			SQLiteUtil testcon = new SQLiteUtil();
			Connection con = testcon.connect();
			List<Restaurant> restaurants = new ArrayList<Restaurant>();
			Statement st = con.createStatement();
			ResultSet result = st.executeQuery(sql);
			while (result.next()) {
				String resultName = result.getString(1);
				int resultEva = result.getInt(2);
				int resultWai = result.getInt(3);
				String resultLoc = result.getString(4);
				String resultTel = result.getString(5);
				String resultIntro = result.getString(6);
				restaurants.add(new Restaurant(resultName, resultEva,
						resultWai, resultLoc, resultTel, resultIntro));
			}
			Gson gson = new Gson();
			String results = gson.toJson(restaurants);
			System.out.println(results);
			return results;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public String searchInSAE(String key) {		//执行查询SAE
		try {
			Class.forName(dbDriver).newInstance();
			Connection connection = DriverManager.getConnection(dbUrl, dbUser,
					dbPassword);
			Statement st = connection.createStatement();
			String sql = "select name,evaluation,waitingNum,location,tel,introduct"
					+ " from restaurant WHERE location LIKE '%" + key
					+ "%' or name LIKE '%" + key + "%'";
			ResultSet result = st.executeQuery(sql);
			List<Restaurant> restaurants = new ArrayList<Restaurant>();
			while (result.next()) {
				String resultName = result.getString(1);
				int resultEva = result.getInt(2);
				int resultWai = result.getInt(3);
				String resultLocation = result.getString(4);
				String resultTel = result.getString(5);
				String resultIntro = result.getString(6);
				restaurants.add(new Restaurant(resultName, resultEva,
						resultWai,  resultLocation,resultTel , resultIntro));
			}
			Gson gson = new Gson();
			String results = gson.toJson(restaurants);
			return results;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private String getCharAndNumr(int length) {// 生成随机码
		String val = "";
		String charOrNum = "char"; // 输出字母还是数字
		Random random = new Random();
		for (int i = 0; i < length; i++) {
			if ("char".equalsIgnoreCase(charOrNum)) // 字符串
			{
				int choice = 65;// 取得大写字母
				val += (char) (choice + random.nextInt(26));
				charOrNum = "num";
			} else if ("num".equalsIgnoreCase(charOrNum)) // 数字
			{
				val += String.valueOf(random.nextInt(10));
			}
		}
		return val;
	}
}
