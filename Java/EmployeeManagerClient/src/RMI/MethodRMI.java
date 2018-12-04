package RMI;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MethodRMI extends Remote{
	public int Login (String username, String password) throws RemoteException;
	public int ChangePassword (String username, String oldpassword, String newpassword) throws RemoteException;
	public String getDataDayOff (String username) throws RemoteException;
	public void InsertDayOff (String date, String username) throws RemoteException;
	public void CheckInOut(String username, String action) throws RemoteException;
	public String GetDataTimeKeeping (String username) throws RemoteException;
	public String GetDataFile () throws RemoteException;
}