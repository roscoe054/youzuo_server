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
//	String dbDriver = "com.mysql.jdbc.Driver"; // �뱾��������ͬ
//	String dbUrl = "jdbc:mysql://w.rdc.sae.sina.com.cn:3307/app_youzuo?characterEncoding=UTF-8"; // app_yanzelΪ����app���ݿ�����
//	String dbUser = "3wyy4jn2w2"; // Ϊ[Ӧ����Ϣ]->[������Ϣ]->[key]�е�access key
//	String dbPassword = "ilzkijh4ymzx1l5kh2hzjj2j51l5140k13xkymhm";
	//local
	private String dbDriver = "com.mysql.jdbc.Driver"; // �뱾��������ͬ
	private String dbUrl = "jdbc:mysql://w.rdc.sae.sina.com.cn:3307/app_youzuo?characterEncoding=UTF-8"; // app_yanzelΪ����app���ݿ�����
	private String dbUser = "3wyy4jn2w2"; // Ϊ[Ӧ����Ϣ]->[������Ϣ]->[key]�е�access key
	private String dbPassword = "ilzkijh4ymzx1l5kh2hzjj2j51l5140k13xkymhm";
	
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		PrintWriter out = null;

		response.setContentType("text/html;charset=UTF-8");
		out = response.getWriter();
//		byte[] resultKey = request.getParameter("key").getBytes("ISO-8859-1");
//		String key = new String(resultKey, "UTF-8");
		String key = request.getParameter("key");
		
		if (!key.equals("reservation")&&!key.equals("deleteReservation")) {//��ѯ����
			try {
				System.out.println("�û�����:key=" + key);
				// ִ�в�ѯ(����)
//				out.println(MyServlet.searchRes(key));

				// ִ�в�ѯ(SAE)
				out.println(searchInSAE(key));

			} catch (Exception ex) {
				out.print("error");
			} finally {
				out.flush();
				out.close();
			}
		}
		
		//�����ݿ���ɾ��Ԥ��
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
				//��reservation����ɾ��
				String sqlDeleteReservation = "DELETE FROM reservation "
						+ "WHERE name = '"+name+"' AND code = '"+code+"'";
				if(st.execute(sqlDeleteReservation)){
					resultset=st.getResultSet();
				}
				//��restaurant����ɾ��
				String sqlGetWaitingNum = "select waitingNum,queue from restaurant WHERE name='"
						+ name + "'";//�õ����к͵ȴ�����
				if(st.execute(sqlGetWaitingNum)){
					resultset=st.getResultSet();
				}
				int tempWaitingNum = 0;//�ò����ȴ�����
				String tempQueue="";//�ò����ȴ�����
				while (resultset.next()) {
					tempWaitingNum = resultset.getInt(1);
					tempQueue=resultset.getString(2);
				}
				tempWaitingNum--;//�ȴ�������1
				int position=tempQueue.indexOf(code);
				if(position==-1){
					return;
				}
				if(position+2<tempQueue.length()){
					tempQueue=tempQueue.substring(0,position)+
						tempQueue.substring(position+2,tempQueue.length());//�Ӷ�����ɾ��
				}
				else{
					tempQueue=tempQueue.substring(0,position);//�Ӷ�����ɾ��
				}
				String sqlUpdateWaitingNum = "Update restaurant SET waitingNum="//���µȴ�����
						+ tempWaitingNum + " WHERE name='" + name+ "'";
				if(st.execute(sqlUpdateWaitingNum)){
					resultset=st.getResultSet();
				}
				String sqlUpdateQueue= "Update restaurant SET queue='"//���¶���
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
		
		//�ύԤ��
		else {
//			byte[] resultName = request.getParameter("name").getBytes("ISO-8859-1");
//			String keyName = new String(resultName, "UTF-8");
			String keyName = request.getParameter("name");
			
			String sqlGetWaitingNum = "select waitingNum,queue from restaurant WHERE name='"
					+ keyName + "'";//�õ���ǰ�����ĵȴ�����

			try {
				Class.forName(dbDriver).newInstance();
				Connection connection = DriverManager.getConnection(dbUrl, dbUser,
						dbPassword);
				Statement st = connection.createStatement();
				
				ResultSet resultset = null;
				if(st.execute(sqlGetWaitingNum)){
					resultset=st.getResultSet();
				}
				String code;//�����
				int tempWaitingNum = 0;//�ò����ȴ�����
				String tempQueue="";//�ò����ȴ�����
				while (resultset.next()) {
					tempWaitingNum = resultset.getInt(1);
					tempQueue=resultset.getString(2);
				}
				String sqlUpdateWaitingNum = "Update restaurant SET waitingNum="//���µȴ�����(+1)
						+ (tempWaitingNum + 1) + " WHERE name='" + keyName+ "'";
				if(st.execute(sqlUpdateWaitingNum)){
					resultset=st.getResultSet();
				}
				
				do{
					code = getCharAndNumr(2);//����2λ�����
				}while(tempQueue.contains(code));//����������Ѿ����ڸú�������������
				tempQueue+=code;//�������
				
				//���µȴ�����
				String sqlUpdateQueue="Update restaurant SET queue='"
				+ tempQueue + "' WHERE name='" + keyName+ "'";
				if(st.execute(sqlUpdateQueue)){
					resultset=st.getResultSet();
				}
				out.print(code);
				
				//���뵽reservation��
				SimpleDateFormat currentTime = new SimpleDateFormat("MM-dd HH:mm");//�������ڸ�ʽ
				String currentString=currentTime.format(new Date());
				String sqlInsertQueue="INSERT INTO reservation (id,code,name,time,status) VALUES "
						+ "('10','"+code+"','"+keyName+"','"+currentString+"' ,'�ȴ���')";
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

	public static String searchRes(String key) { //���ز�ѯ����
		String sql = "select name,evaluation,waitingNum,location,tel,introduct"
				+ " from restaurant WHERE location LIKE '%" + key
				+ "%' or name LIKE '%" + key + "%'";
//		String sql="select name,evaluation,waitingNum,location,tel,introduct from restaurant"+
//				" WHERE location LIKE BINARY '%��%'";
		
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
	
	public String searchInSAE(String key) {		//ִ�в�ѯSAE
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
	
	private String getCharAndNumr(int length) {// ���������
		String val = "";
		String charOrNum = "char"; // �����ĸ��������
		Random random = new Random();
		for (int i = 0; i < length; i++) {
			if ("char".equalsIgnoreCase(charOrNum)) // �ַ���
			{
				int choice = 65;// ȡ�ô�д��ĸ
				val += (char) (choice + random.nextInt(26));
				charOrNum = "num";
			} else if ("num".equalsIgnoreCase(charOrNum)) // ����
			{
				val += String.valueOf(random.nextInt(10));
			}
		}
		return val;
	}
}
