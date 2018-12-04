package Server;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import com.mysql.jdbc.Driver;

import RMI.MethodRMI;


public class Server extends JFrame{
	private static final long serialVersionUID = 1L;
	public static JTextField statusText;
	public static JTextArea LogText;
	public static JTextField NumberOfClientText;
	public static ArrayList<Socket> threads = new ArrayList<Socket>();
	public static ServerSocket svsk = null;
	public static Socket sk = null;
	public static String message="";
	public static ServerSocket svskTran;
	public static Socket skTran;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Server sv = new Server();
		sv.doShow();
		try {
			MethodRMIImpl mri = new MethodRMIImpl();
			Registry r =  LocateRegistry.createRegistry(12345);
			r.bind("MethodClient", mri);
			statusText.setText("Server Online...");
//			while(true) {
//				LogText.append("hello abc xyz\n");
//				try {
//					Thread.sleep(3000);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AlreadyBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					svskTran = new ServerSocket(12347);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				while(true) {
					try {
						final Socket sktran = svskTran.accept();
						DataInputStream is = new DataInputStream(sktran.getInputStream());
						DataOutputStream os = new DataOutputStream(sktran.getOutputStream());
						String s = is.readUTF();
						String[] sa = s.split("-");
						final String path = "src//File//"+sa[2];
						final String name = sa[2];
						if(sa[1].equals("@getfile")) {
							LogText.append("[Client][System] "+sa[0]+" is get file "+sa[2]+"!\n");
							new Thread(new Runnable() {
								
								@Override
								public void run() {
									// TODO Auto-generated method stub
									File file = new File(path);
									try {
										os.writeUTF("get-"+sa[2]+"-"+file.length());
										os.flush();
										String status = is.readUTF();
										if(status.equals("ready")) {
											BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
											int data;
											while((data=bis.read()) != -1) {
												os.writeInt(data);
												os.flush();
											}
											bis.close();
										}
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							}).start();
						}
						else if(sa[1].equals("@uploadfile"))
						{
							LogText.append("[Client][System] "+sa[0]+" is upload file "+sa[2]+"!\n");
							new Thread(new Runnable() {
								
								@Override
								public void run() {
									// TODO Auto-generated method stub
									File file = new File(path);
									try {
										file.createNewFile();
									} catch (IOException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
									try {
										os.writeUTF("upload-"+sa[2]);
										os.flush();
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									try {
										Long filesize = is.readLong();
										os.writeUTF("ready");
										os.flush();
										BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
										for(int i = 0; i < filesize; i++) {
											bos.write(is.readInt());
											bos.flush();
										}
										bos.close();
										try {
											Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/humanmanager","root","");
											con.createStatement().executeUpdate("insert into File(filename,path) values('"+sa[2]+"','src//File//"+sa[2]+"')");
										} catch (SQLException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}	
								}
							}).start();
						}
						else if(sa[1].equals("@@@")) {
							os.writeUTF("@@@-a");
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}).start();
		
		
		try {
			svsk = new ServerSocket(12346);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		while(true) {
			try {
				
				final Socket sk = svsk.accept();
				threads.add(sk);
				NumberOfClientText.setText(threads.size()+"");
				DataInputStream is = new DataInputStream(sk.getInputStream());
//				DataOutputStream os = new DataOutputStream(sk.getOutputStream());
				String join = is.readUTF();
				LogText.append("[Client][System] " +join+" is connect!"+"\n");
				for(Socket ss : threads)
				{
					if(!ss.equals(sk))
					{
						DataOutputStream os = new DataOutputStream(ss.getOutputStream());
						os.writeUTF(join+" join the room!\n");
					}
					else
					{
						DataOutputStream os = new DataOutputStream(ss.getOutputStream());
						os.writeUTF("You join the room!\n");
					}
				}
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						String[] sa;
						while(true)
						{
							try {
								message = is.readUTF();
//								LogText.append(message);
								sa = message.split("-");
								if(sa[1].equals("@@@\n"))
								{
									LogText.append("[Client][System] " + sa[0] + " is logout!\n");
									DataOutputStream os = new DataOutputStream(sk.getOutputStream());
									os.writeUTF("@@@");
									threads.remove(sk);
									break;
								}
								else if(sa[1].equals("@changepass")) {
									LogText.append("[Client][System] "+sa[0]+" change password!\n");
									continue;
									
								}
								else if(sa[1].equals("@offerday")) {
									LogText.append("[Client][System] "+sa[0]+" offer day off!\n");
									continue;
								}
								else if(sa[1].equals("@checkin")) {
									LogText.append("[Client][System] "+sa[0]+" was check in!\n");
									continue;
								}
								else if(sa[1].equals("@checkout")) {
									LogText.append("[Client][System] "+sa[0]+" was check out!\n");
									continue;
								}
								for(Socket s : threads)
								{
									if(!s.equals(sk))
									{
										LogText.append("[Client][Chat]["+sa[0]+"] "+sa[1]);
										DataOutputStream os = new DataOutputStream(s.getOutputStream());
										os.writeUTF("["+sa[0]+" said]: "+sa[1]);
									}
									else
									{
										if(NumberOfClientText.getText().equals("1"))
										{
											LogText.append("[Client][Chat]["+sa[0]+"] "+sa[1]);
										}
									}
								}
								sa = null;
								
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						for(Socket s : threads)
						{
								DataOutputStream os;
								try {
									os = new DataOutputStream(s.getOutputStream());
									os.writeUTF("["+sa[0]+"] out the room!\n");
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}

						}
						NumberOfClientText.setText(threads.size()+"");
					}
				}).start();				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public void doShow() {
		setTitle("Server");
		setSize(1200, 800);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		addControl();
		setResizable(false);
		setVisible(true);
	}
	public void addControl() {
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		
		JPanel topPanel = new JPanel();
		JLabel helloLabel = new JLabel("ADMIN");
		helloLabel.setFont(new Font("Arial", Font.PLAIN, 20));
		helloLabel.setForeground(Color.white);
		topPanel.setBackground(Color.GRAY);
		topPanel.add(helloLabel);
		
		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
		JButton logServer = new JButton("Server Log               ");
		JButton addEmployee = new JButton    ("Add Employee          ");
		JButton addEmployeeType = new JButton("Add Employee Type");
		JButton acceptDayOff = new JButton   ("Accept DayOff          ");
		JButton showSalary = new JButton     ("Salary Of Employee ");
//		JButton fileOnServer = new JButton   ("Public File                 ");
//		JButton chat = new JButton           ("Chat                           ");
		leftPanel.add(Box.createVerticalStrut(20));
		leftPanel.add(logServer);
		leftPanel.add(Box.createVerticalStrut(20));
		leftPanel.add(addEmployeeType);
		leftPanel.add(Box.createVerticalStrut(20));
		leftPanel.add(addEmployee);
		leftPanel.add(Box.createVerticalStrut(20));
		leftPanel.add(acceptDayOff);
		leftPanel.add(Box.createVerticalStrut(20));
		leftPanel.add(showSalary);
//		leftPanel.add(Box.createVerticalStrut(20));
//		leftPanel.add(fileOnServer);
//		leftPanel.add(Box.createVerticalStrut(20));
//		leftPanel.add(chat);
		leftPanel.setBackground(Color.LIGHT_GRAY);
		
		JPanel centerPanel = new JPanel();
		JPanel InfoPanel = new JPanel();
		JLabel statusLanel = new JLabel("Status:");
		JLabel numberOfClientLabel = new JLabel("Number Of Client:");
		NumberOfClientText = new JTextField(5);
		NumberOfClientText.setText("0");
		NumberOfClientText.setEditable(false);
		LogText = new JTextArea(20,50);
		LogText.setEditable(false);
		JScrollPane logTextScroll = new JScrollPane(LogText);
		JPanel LogPanel = new JPanel();
		LogPanel.add(logTextScroll);
		statusText = new JTextField(10);
		statusText.setText("Server Offline...");
		statusText.setEditable(false);
		InfoPanel.add(statusLanel);
		InfoPanel.add(statusText);
		InfoPanel.add(Box.createHorizontalStrut(180));
		InfoPanel.add(numberOfClientLabel);
		InfoPanel.add(NumberOfClientText);	
		centerPanel.add(InfoPanel);
		centerPanel.add(Box.createHorizontalStrut(2));
		centerPanel.add(LogPanel);
		
		mainPanel.add(topPanel,BorderLayout.NORTH);
		mainPanel.add(centerPanel,BorderLayout.CENTER);
		mainPanel.add(leftPanel,BorderLayout.WEST);
		
		logServer.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				getContentPane().remove(mainPanel);
				centerPanel.removeAll();
				JPanel InfoPanel = new JPanel();
				JLabel statusLanel = new JLabel("Status:");
				JLabel numberOfClientLabel = new JLabel("Number Of Client:");
				JScrollPane logTextScroll = new JScrollPane(LogText);
				JPanel LogPanel = new JPanel();
				LogPanel.add(logTextScroll);
				InfoPanel.add(statusLanel);
				InfoPanel.add(statusText);
				InfoPanel.add(Box.createHorizontalStrut(180));
				InfoPanel.add(numberOfClientLabel);
				InfoPanel.add(NumberOfClientText);	
				centerPanel.add(InfoPanel);
				centerPanel.add(Box.createHorizontalStrut(2));
				centerPanel.add(LogPanel);
				mainPanel.add(centerPanel,BorderLayout.CENTER);
				getContentPane().add(mainPanel);
				setVisible(false);
				setVisible(true);
				
			}
		});
		
		addEmployeeType.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				getContentPane().remove(mainPanel);
				centerPanel.removeAll();
				JPanel empType = new JPanel();
				JPanel empSalary = new JPanel();
				JPanel button = new JPanel();
				JLabel nameLabel = new JLabel("Employee Type Name: ");
				JTextField nameText = new JTextField(15);
				JLabel salaryLabel = new JLabel("Salary: ");
				JTextField salaryText = new JTextField(15);
				JButton submitButton = new JButton("Submit");
				salaryLabel.setPreferredSize(nameLabel.getPreferredSize());
				empType.add(nameLabel);
				empType.add(nameText);
				empSalary.add(salaryLabel);
				empSalary.add(salaryText);
				button.add(submitButton);
				centerPanel.add(empType);
				centerPanel.add(Box.createHorizontalStrut(700));
				centerPanel.add(empSalary);
				centerPanel.add(Box.createHorizontalStrut(700));
				centerPanel.add(button);
				centerPanel.add(Box.createHorizontalStrut(700));
				
				submitButton.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent arg0) {
						// TODO Auto-generated method stub
						Object[] options = {"OK"};
						if(nameText.getText().equals("")) {
							JOptionPane.showOptionDialog(null, "Employee type name is empty", "Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
						}
						else
						{
							if(salaryText.getText().equals("")) {
								JOptionPane.showOptionDialog(null, "Salary is empty", "Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
							}
							else
							{
								try {
									double salary = Double.parseDouble(salaryText.getText());
									boolean check = true;
									Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/humanmanager","root","");
									Statement sta = con.createStatement();
									ResultSet res = sta.executeQuery("select * from positiion");
									int count = 0;
									while(res.next()) {
										if(res.getString(2).equals(nameText.getText())) {
											check = false;
										}
										count++;
									}
									if(check == false)
									{
										JOptionPane.showOptionDialog(null, "Employee type name was exist", "Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
									}
									else
									{	
										con.createStatement().executeUpdate("insert into positiion values("+(count+1)+",'"+nameText.getText()+"')");
										con.createStatement().executeUpdate("insert into salaryforpositionbyday values("+(count+1)+","+salary+")");
										JOptionPane.showOptionDialog(null, "Add employee type is success", "Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
										nameText.setText("");
										salaryText.setText("");
										LogText.append("[Admin][System] Add new employee type!");
									}
								} catch (Exception e) {
									// TODO: handle exception
									JOptionPane.showOptionDialog(null, "Salary is wrong", "Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
								}
							}
						}
					}
				});
				
				mainPanel.add(centerPanel, BorderLayout.CENTER);
				getContentPane().add(mainPanel);
				setVisible(false);
				setVisible(true);
			}
		});
		
		addEmployee.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				getContentPane().remove(mainPanel);
				centerPanel.removeAll();
				JPanel empName = new JPanel();
				JPanel empBirth = new JPanel();
				JPanel empAddress = new JPanel();
				JPanel empGender = new JPanel();
				JPanel empCountry = new JPanel();
				JPanel empUsername = new JPanel();
				JPanel empPassword = new JPanel();
				JPanel empPosition = new JPanel();
				JPanel empButton = new JPanel();
				
				JLabel nameLabel = new JLabel("Name: ");
				JTextField nameText = new JTextField(15);
				empName.add(nameLabel);
				empName.add(nameText);
				
				JLabel birthLabel = new JLabel("Birth: ");
				String[] day = {"1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24","25","26","27","28","29","30","31"};
				String[] month = {"1","2","3","4","5","6","7","8","9","10","11","12"};
				String[] year = new String[30];
				int count = 0;
				for(int i = 0; i < 30;i++) {
					year[i] = LocalDate.now().getYear() - 18 - 30 + count + "";
					count++;
				}
				JComboBox monthList = new JComboBox(month);
				JComboBox dayList = new JComboBox<>(day);
				JComboBox yearList = new JComboBox<>(year);
				empBirth.add(birthLabel);
				empBirth.add(dayList);
				empBirth.add(monthList);
				empBirth.add(yearList);
				
				JLabel addressLabel = new JLabel("Address: ");
				JTextField addressText = new JTextField(15);
				empAddress.add(addressLabel);
				empAddress.add(addressText);
				
				JLabel genderLabel = new JLabel("Gender: ");
				String[] gender = {"Male","Female"};
				JComboBox genderList = new JComboBox<>(gender);
				empGender.add(genderLabel);
				empGender.add(genderList);
				
				JLabel countryLabel = new JLabel("Country: ");
				JTextField countryText = new JTextField(15);
				empCountry.add(countryLabel);
				empCountry.add(countryText);
				
				JLabel usernameLabel = new JLabel("Username: ");
				JTextField usernameText = new JTextField(15);
				empUsername.add(usernameLabel);
				empUsername.add(usernameText);
				
				JLabel passwordLabel = new JLabel("Password: ");
				JPasswordField passwordText = new JPasswordField(15);
				empPassword.add(passwordLabel);
				empPassword.add(passwordText);
				
				JLabel positionLabel = new JLabel("Position: ");
				String item = "";
				try {
					Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/humanmanager","root","");
					ResultSet res = con.createStatement().executeQuery("select * from positiion");
					int num = 0;
					while(res.next()) {
						if(res.getInt(1) != 1)
						{
							if(num == 0) {
								item += res.getString(2);
								num = 1;
							}
							else
							{
								item += "-" + res.getString(2);
							}
						}
					}
					num = 0;
					con.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				String[] position = item.split("-");
				JComboBox positionList =  new JComboBox<>(position);
				empPosition.add(positionLabel);
				empPosition.add(positionList);
				
				JButton submitButton = new JButton("Submit");
				empButton.add(submitButton);
				
				centerPanel.add(empName);
				centerPanel.add(Box.createHorizontalStrut(700));
				centerPanel.add(empBirth);
				centerPanel.add(Box.createHorizontalStrut(700));
				centerPanel.add(empAddress);
				centerPanel.add(Box.createHorizontalStrut(710));
				centerPanel.add(empGender);
				centerPanel.add(Box.createHorizontalStrut(790));
				centerPanel.add(empCountry);
				centerPanel.add(Box.createHorizontalStrut(710));
				centerPanel.add(empUsername);
				centerPanel.add(Box.createHorizontalStrut(720));
				centerPanel.add(empPassword);
				centerPanel.add(Box.createHorizontalStrut(720));
				centerPanel.add(empPosition);
				centerPanel.add(Box.createHorizontalStrut(790));
				centerPanel.add(empButton);
				centerPanel.add(Box.createHorizontalStrut(700));
				
				submitButton.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent arg0) {
						// TODO Auto-generated method stub
						Object[] options = {"OK"};
						if(nameText.getText().equals(""))
						{
							JOptionPane.showOptionDialog(null, "Name is empty", "Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
						}
						else
						{
							if(addressText.getText().equals(""))
							{
								JOptionPane.showOptionDialog(null, "Address is empty", "Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
							}
							else
							{
								if(countryText.getText().equals("")) {
									JOptionPane.showOptionDialog(null, "Country is empty", "Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
								}
								else
								{
									if(usernameText.getText().equals("")) {
										JOptionPane.showOptionDialog(null, "Username is empty", "Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
									}
									else
									{
										if(passwordText.getText().equals("")) {
											JOptionPane.showOptionDialog(null, "Password is empty", "Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
										}
										else
										{
											try {
												Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/humanmanager","root","");
												//Check username duplicate
												ResultSet res = con.createStatement().executeQuery("select * from account");
												boolean check = true;
												while(res.next()) {
													if(res.getString(2).equals(usernameText.getText()))
													{
														check = false;
														break;
													}
												}
												if(check == true)
												{
													//process birth
													if((monthList.getSelectedItem().equals("2") && dayList.getSelectedItem().equals("29")) ||
														(monthList.getSelectedItem().equals("2") && dayList.getSelectedItem().equals("30")) || 
														(monthList.getSelectedItem().equals("2") && dayList.getSelectedItem().equals("31")) ||
														(monthList.getSelectedItem().equals("4") && dayList.getSelectedItem().equals("31")) ||
														(monthList.getSelectedItem().equals("6") && dayList.getSelectedItem().equals("31")) ||
														(monthList.getSelectedItem().equals("9") && dayList.getSelectedItem().equals("31")) ||
														(monthList.getSelectedItem().equals("11") && dayList.getSelectedItem().equals("31"))) 
													{
														JOptionPane.showOptionDialog(null, dayList.getSelectedItem() + "/" +monthList.getSelectedItem() + " doesn't exist", "Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
													}
													else
													{
														String birth = yearList.getSelectedItem() + "-" + monthList.getSelectedItem() + "-" + dayList.getSelectedItem();
														//process position
														int positionID = 0;
														ResultSet res1 = con.createStatement().executeQuery("select * from positiion");
														while(res1.next()) {
															if(res1.getString(2).equals(positionList.getSelectedItem())) {
																positionID = res1.getInt(1);
																break;
															}
														}
														//get largest ID of employee table
														int largestIDemp = 0;
														ResultSet res2 = con.createStatement().executeQuery("select * from emlpoyee");
														while(res2.next()) {
															largestIDemp++;
														}
														//insert DB
														con.createStatement().executeUpdate("insert into account values('"+usernameText.getText()+"','"+passwordText.getText()+"',"+positionID+")");
														con.createStatement().executeUpdate("insert into emlpoyee values("+(largestIDemp+1)+",'"+nameText.getText()+"','"+birth+"','"+addressText.getText()+"','"+genderList.getSelectedItem()+"','"+countryText.getText()+"',"+positionID+",'"+usernameText.getText()+"')");
														JOptionPane.showOptionDialog(null, "Add employee success", "Success", JOptionPane.PLAIN_MESSAGE, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
														nameText.setText("");
														addressText.setText("");
														countryText.setText("");
														usernameText.setText("");
														passwordText.setText("");
														LogText.append("[Admin][System] Add new employee!");
													}
												}
												else
												{
													check = true;
													JOptionPane.showOptionDialog(null, "Username already exist", "Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
													
												}
												con.close();
											} catch (SQLException e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
											}
										}
									}
								}
							}
						}
					}
				});
				
				mainPanel.add(centerPanel,BorderLayout.CENTER);
				getContentPane().add(mainPanel);
				setVisible(false);
				setVisible(true);
			}
		});
		
		acceptDayOff.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				getContentPane().remove(mainPanel);
				centerPanel.removeAll();
				JPanel title = new JPanel();
				JPanel tablePanel = new JPanel();
				JPanel buttonPanel = new JPanel();
				
				JLabel titleLabel = new JLabel("List Offer DayOff");
				titleLabel.setFont(new Font("Arial", Font.BOLD, 15));
				titleLabel.setForeground(Color.blue);
				title.add(titleLabel);
				
				JTable dayoffTable = new JTable();
				dayoffTable.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
				JScrollPane tableScroll = new JScrollPane(dayoffTable);
				String data = "";
				try {
					int check = 0;
					Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/humanmanager","root","");
					ResultSet res = con.createStatement().executeQuery("select DO.ID, DO.DayOff, E.Name from dayoff as DO inner join emlpoyee as E on E.ID = DO.EmployeeID where DO.status = 1");
					while(res.next()) {
						String[] date = (res.getDate(2)+"").split("-");
						if(check == 0) {
							data += res.getInt(1)+":"+date[2]+"/"+date[1]+"/"+date[0]+":"+res.getString(3);
							check = 1;
						}
						else
						{
							data += "-"+res.getInt(1)+":"+date[2]+"/"+date[1]+"/"+date[0]+":"+res.getString(3);
						}
					}
					check = 0;
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//Add data to table
				String[] dataList = data.split("-");
				String[][] tableModelData = new String[dataList.length][3];
				if(data.equals(""))
				{
					//
				}
				else
				{
					for(int i = 0; i < dataList.length; i++)
					{
						String[] dataRow = dataList[i].split(":");
						tableModelData[i][0] = dataRow[0];
						tableModelData[i][1] = dataRow[1];
						tableModelData[i][2] = dataRow[2];
					}
				}
				DefaultTableModel tableModel = (DefaultTableModel)dayoffTable.getModel();
				tableModel.setDataVector(tableModelData, new String[] {"ID","Day Off","Employee Name"});
				tablePanel.add(tableScroll);
				
				JButton acceptButton = new JButton("Accept");
				JButton removeButton = new JButton("Remove");
				buttonPanel.add(acceptButton);
				buttonPanel.add(removeButton);
				
				centerPanel.add(Box.createHorizontalStrut(450));
				centerPanel.add(titleLabel);
				centerPanel.add(Box.createHorizontalStrut(450));
				centerPanel.add(tablePanel);
				centerPanel.add(Box.createHorizontalStrut(700));
				centerPanel.add(buttonPanel);
				centerPanel.add(Box.createHorizontalStrut(150));
				Object[] options = {"OK"};
				
				acceptButton.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent arg0) {
						// TODO Auto-generated method stub
						if(dayoffTable.getSelectedRowCount() == 0)
						{
							JOptionPane.showOptionDialog(null, "No one row was select", "Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
						}
						else
						{
							int choose = JOptionPane.showConfirmDialog(null, "Are you sure to accept?", "Accept", JOptionPane.YES_NO_OPTION);
							if(choose == JOptionPane.YES_OPTION)
							{
								int offerID = Integer.parseInt(dayoffTable.getValueAt(dayoffTable.getSelectedRow(), 0)+"");
								String nameEmp = dayoffTable.getValueAt(dayoffTable.getSelectedRow(), 2)+"";
								try {
									Connection con = DriverManager.getConnection("jdbc:mysql://localhost/humanmanager","root","");
									con.createStatement().executeUpdate("update DayOff set status = 2 where ID = "+offerID);
									JOptionPane.showOptionDialog(null, "Accept offer success", "Success", JOptionPane.PLAIN_MESSAGE, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
									//Update table
									ResultSet res = con.createStatement().executeQuery("select DO.ID, DO.DayOff, E.Name from dayoff as DO inner join emlpoyee as E on E.ID = DO.EmployeeID where DO.status = 1");
									int check = 0;
									String data = "";
									while(res.next()) {
										String[] date = (res.getDate(2)+"").split("-");
										if(check == 0) {
											data += res.getInt(1)+":"+date[2]+"/"+date[1]+"/"+date[0]+":"+res.getString(3);
											check = 1;
										}
										else
										{
											data += "-"+res.getInt(1)+":"+date[2]+"/"+date[1]+"/"+date[0]+":"+res.getString(3);
										}
									}
									check = 0;
									String[] dataList = data.split("-");
									String[][] tableModelData = new String[dataList.length][3];
									if(data.equals(""))
									{
										//
									}
									else
									{
										for(int i = 0; i < dataList.length; i++)
										{
											String[] dataRow = dataList[i].split(":");
											tableModelData[i][0] = dataRow[0];
											tableModelData[i][1] = dataRow[1];
											tableModelData[i][2] = dataRow[2];
										}
									}
									tableModel.setDataVector(tableModelData, new String[] {"ID","Day Off","Employee Name"});
									LogText.append("[Admin][System] Accept offer day off of "+ nameEmp + "!");
									con.close();
								} catch (SQLException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
					}
				});
				
				removeButton.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent arg0) {
						// TODO Auto-generated method stub
						if(dayoffTable.getSelectedRowCount() == 0)
						{
							JOptionPane.showOptionDialog(null, "No one row was select", "Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
						}
						else
						{
							int choose = JOptionPane.showConfirmDialog(null, "Are you sure to remove?", "Remove", JOptionPane.YES_NO_OPTION);
							if(choose == JOptionPane.YES_OPTION)
							{
								int offerID = Integer.parseInt(dayoffTable.getValueAt(dayoffTable.getSelectedRow(), 0)+"");
								String nameEmp = dayoffTable.getValueAt(dayoffTable.getSelectedRow(), 2)+"";
								try {
									Connection con = DriverManager.getConnection("jdbc:mysql://localhost/humanmanager","root","");
									con.createStatement().executeUpdate("update DayOff set status = 3 where ID = "+offerID);
									JOptionPane.showOptionDialog(null, "Remove offer success", "Success", JOptionPane.PLAIN_MESSAGE, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
									//Update table
									ResultSet res = con.createStatement().executeQuery("select DO.ID, DO.DayOff, E.Name from dayoff as DO inner join emlpoyee as E on E.ID = DO.EmployeeID where DO.status = 1");
									int check = 0;
									String data = "";
									while(res.next()) {
										String[] date = (res.getDate(2)+"").split("-");
										if(check == 0) {
											data += res.getInt(1)+":"+date[2]+"/"+date[1]+"/"+date[0]+":"+res.getString(3);
											check = 1;
										}
										else
										{
											data += "-"+res.getInt(1)+":"+date[2]+"/"+date[1]+"/"+date[0]+":"+res.getString(3);
										}
									}
									check = 0;
									String[] dataList = data.split("-");
									String[][] tableModelData = new String[dataList.length][3];
									if(data.equals(""))
									{
										//
									}
									else
									{
										for(int i = 0; i < dataList.length; i++)
										{
											String[] dataRow = dataList[i].split(":");
											tableModelData[i][0] = dataRow[0];
											tableModelData[i][1] = dataRow[1];
											tableModelData[i][2] = dataRow[2];
										}
									}
									tableModel.setDataVector(tableModelData, new String[] {"ID","Day Off","Employee Name"});
									LogText.append("[Admin][System] Accept offer day off of "+ nameEmp + "!");
									con.close();
								} catch (SQLException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
					}
				});
				
				mainPanel.add(centerPanel,BorderLayout.CENTER);
				getContentPane().add(mainPanel);
				setVisible(false);
				setVisible(true);
			}
		});
		
		showSalary.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				getContentPane().remove(mainPanel);
				centerPanel.removeAll();
				
				JPanel titlePanel = new JPanel();
				JPanel tablePanel = new JPanel();
				
				JLabel title = new JLabel("List Salary "+ LocalDateTime.now().getMonthValue() + "/" + LocalDateTime.now().getYear());
				title.setFont(new Font("Arial", Font.PLAIN, 15));
				title.setForeground(Color.blue);
				titlePanel.add(title);
				
				JTable table = new JTable();
				DefaultTableModel tableModel = (DefaultTableModel)table.getModel();
				JScrollPane tableScroll = new JScrollPane(table);
				String data = "";
				try {
					int check = 0;
					Connection con = DriverManager.getConnection("jdbc:mysql://localhost/humanmanager","root","");
					ResultSet res = con.createStatement().executeQuery("select DISTINCT E.Name, P.Name, S.salary , COUNT(*) as NumofDay, (SELECT COUNT(*) FROM timekeeping WHERE EmployeeID = E.ID and Late = 1) as NumofLate from timekeeping as TK inner join (emlpoyee as E inner join (positiion as P inner join salaryforpositionbyday as S on S.ID = P.ID) on E.PositionID = P.ID) on TK.EmployeeID = E.ID where MONTH(TK.TimeIn) = "+LocalDateTime.now().getMonthValue());
					while(res.next()) {
						if(check == 0) {
							data += res.getString(1)+":"+res.getString(2)+":"+res.getDouble(3)+":"+res.getInt(4)+":"+res.getInt(5);
							check = 1;
						}
						else
						{
							data += "-"+res.getString(1)+":"+res.getString(2)+":"+res.getDouble(3)+":"+res.getInt(4)+":"+res.getInt(5);
						}
						check = 0;
					}
					con.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				String[] dataList = data.split("-");
				String[][] dataTable = new String[dataList.length][5];
				if(data.equals(""))
				{
					//
				}
				else
				{
					for(int i = 0; i < dataList.length; i++) {
						String[] tempData = dataList[i].split(":");
						dataTable[i][0] = tempData[0];
						dataTable[i][1] = tempData[1];
						dataTable[i][2] = tempData[2];
						dataTable[i][3] = tempData[3];
						dataTable[i][4] = tempData[4];
					}
				}
				tableModel.setDataVector(dataTable, new String[] {"Employee Name","Position","Salary/Day","Work Day","Late Day"});
				tablePanel.add(tableScroll);
				
				centerPanel.add(Box.createHorizontalStrut(430));
				centerPanel.add(titlePanel);
				centerPanel.add(Box.createHorizontalStrut(450));
				centerPanel.add(tablePanel);
				
				mainPanel.add(centerPanel,BorderLayout.CENTER);
				getContentPane().add(mainPanel);
				setVisible(false);
				setVisible(true);
			}
		});
		
		getContentPane().add(mainPanel);
	}
}
