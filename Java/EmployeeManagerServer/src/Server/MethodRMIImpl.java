package Server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;

import RMI.MethodRMI;

public class MethodRMIImpl extends UnicastRemoteObject implements MethodRMI {
	private static final long serialVersionUID = 1L;

	public MethodRMIImpl() throws RemoteException {
		// TODO Auto-generated constructor stub
	}

	@Override
	public int Login(String username, String password) throws RemoteException {
		// TODO Auto-generated method stub
		try {
			Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/humanmanager","root","");
			Statement sta = con.createStatement();
			ResultSet res = sta.executeQuery("select * from account");
			while(res.next()) {
				if(res.getString(1).equals(username) && res.getString(2).equals(password))
				{
					return res.getInt(3);
				}
			}
			con.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public int ChangePassword(String username, String oldpassword, String newpassword) throws RemoteException {
		// TODO Auto-generated method stub
		try {
			Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/humanmanager","root","");
			Statement sta = con.createStatement();
			ResultSet res = sta.executeQuery("select * from account");
			while(res.next()) {
				if(res.getString(1).equals(username) && res.getString(2).equals(oldpassword))
				{
					con.createStatement().executeUpdate("update account set password = '"+newpassword+"' where username = '"+username+"'");
					con.close();
					return 1;
				}
			}
			return 0;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public String getDataDayOff(String username) throws RemoteException {
		// TODO Auto-generated method stub
		String data = "";
		try {
			Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/humanmanager","root","");
			ResultSet res = con.createStatement().executeQuery("select DO.ID, DO.DayOff, DO.status from DayOff as DO inner join emlpoyee as E on E.ID = DO.EmployeeID where E.username = '"+username+"'");
			int check = 0;
			while(res.next()) {
				String[] dateFormat = (res.getDate(2)+"").split("-");
				if(check == 0) {
					data += res.getInt(1)+":"+dateFormat[2]+"/"+dateFormat[1]+"/"+dateFormat[0]+":"+res.getInt(3);
					check = 1;
				}
				else
				{
					data += "-"+res.getInt(1)+":"+dateFormat[2]+"/"+dateFormat[1]+"/"+dateFormat[0]+":"+res.getInt(3);
				}
			}
			check = 0;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return data;
	}

	@Override
	public void InsertDayOff(String date, String username) throws RemoteException {
		// TODO Auto-generated method stub
		int empID = 0;
		try {
			Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/humanmanager","root","");
			ResultSet res = con.createStatement().executeQuery("select * from emlpoyee");
			while(res.next()) {
				if(res.getString(8).equals(username)) {
					empID = res.getInt(1);
					break;
				}
				
			}
			if(empID != 0) {
				con.createStatement().executeUpdate("insert into DayOff(DayOff,EmployeeID,status) values('"+date+"',"+empID+",1)");
				con.close();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void CheckInOut(String username, String action) throws RemoteException {
		// TODO Auto-generated method stub
		try {
			Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/humanmanager","root","");
			int empID = 0;
			ResultSet res = con.createStatement().executeQuery("select * from emlpoyee");
			while(res.next()) {
				if(res.getString(8).equals(username)) {
					empID = res.getInt(1);
					break;
				}
			}
			int late = 0;
			if(LocalDateTime.now().getHour() > 6) {
				late = 1;
			}
			else
			{
				if(LocalDateTime.now().getMinute() > 30) {
					late = 1;
				}
			}
			if(action.equals("in")) {
				String dateTime = LocalDateTime.now().getYear()+"-"+LocalDateTime.now().getMonthValue()+"-"+LocalDateTime.now().getDayOfMonth()+" "+LocalDateTime.now().getHour()+":"+LocalDateTime.now().getMinute();
				con.createStatement().executeUpdate("insert into timekeeping(TimeIn,Late,EmployeeID,status) values('"+dateTime+"',"+late+","+empID+",1)");
			}
			else
			{
				String dateTime = LocalDateTime.now().getYear()+"-"+LocalDateTime.now().getMonthValue()+"-"+LocalDateTime.now().getDayOfMonth()+" "+LocalDateTime.now().getHour()+":"+LocalDateTime.now().getMinute();
				con.createStatement().executeUpdate("update timekeeping set TimeOut = '"+dateTime+"',status = 2 where DAY(TimeIn) = "+LocalDateTime.now().getDayOfMonth()+" and MONTH(TimeIn) = "+LocalDateTime.now().getMonthValue()+" and YEAR(TimeIn) = "+LocalDateTime.now().getYear()+" and EmployeeID = "+empID);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public String GetDataTimeKeeping(String username) throws RemoteException {
		// TODO Auto-generated method stub
		int day = LocalDateTime.now().getDayOfMonth(), month = LocalDateTime.now().getMonthValue(), year = LocalDateTime.now().getYear();
		try {
			Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/humanmanager","root","");
			int empID = 0;
			ResultSet res1 = con.createStatement().executeQuery("select * from emlpoyee");
			while(res1.next()) {
				if(res1.getString(8).equals(username))
				{
					empID = res1.getInt(1);
					break;
				}
			}
			if(empID != 0) {
				ResultSet res = con.createStatement().executeQuery("select * from TimeKeeping where DAY(TimeIn) = "+day+" and MONTH(TimeIn) = "+month+" and YEAR(TimeIn) = "+year+" and EmployeeID = "+empID);
				int count = 0;
				while(res.next())
				{
					count++;
				}
				if(count == 0) {
					return "1";
				}
				else
				{
					String data = "";
					res.beforeFirst();
					while(res.next()) {
						String[] DayTimeInSplit = (res.getDate(2)+"").split("-");
						String DayTimeIn = DayTimeInSplit[2]+"/"+DayTimeInSplit[1]+"/"+DayTimeInSplit[0];
						String[] TimeTimeInSplit = (res.getTime(2)+"").split(":");
						String TimeTimeIn = TimeTimeInSplit[0]+":"+TimeTimeInSplit[1];
						String TimeIn = DayTimeIn+" "+TimeTimeIn;
						
						String TimeOut = "";
						if((res.getDate(3)+"").equals("null"))
						{
							TimeOut = "NULL";
						}
						else
						{
							String[] DayTimeOutSplit = (res.getDate(3)+"").split("-");
							String DayTimeOut = DayTimeOutSplit[2]+"/"+DayTimeOutSplit[1]+"/"+DayTimeOutSplit[0];
							String[] TimeTimeOutSplit = (res.getTime(3)+"").split(":");
							String TimeTimeOut = TimeTimeOutSplit[0]+":"+TimeTimeOutSplit[1];
							TimeOut = DayTimeOut+" "+TimeTimeOut;
						}
						data = TimeIn + "-" + TimeOut;
					}
					return data;
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return " abc";
	}

	@Override
	public String GetDataFile() throws RemoteException {
		// TODO Auto-generated method stub
		String data = "";
		try {
			int check = 0;
			Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/humanmanager","root","");
			ResultSet res = con.createStatement().executeQuery("select * from File");
			while(res.next()) {
				if(check == 0) {
					data += res.getString(2);
					check = 1;
				}
				else
				{
					data += "-"+res.getString(2);
				}
			}
			check = 0;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return data;
	}
	
}
