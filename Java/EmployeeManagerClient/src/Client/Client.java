package Client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalDateTime;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;


import RMI.MethodRMI;

public class Client extends JFrame{
	private static final long serialVersionUID = 1L;
	public static MethodRMI rmi;
	public static String usernameGlobal = "";
	public static Socket sk = null;
	public static DataInputStream is = null;
	public static DataOutputStream os = null;
	public static DataInputStream isTran = null;
	public static DataOutputStream osTran = null;
	public static JTextArea chatLogText;
	public static JList<String> list;
	public static File file;
	public static DefaultListModel<String> listModel;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Client cl = new Client();
		cl.doShow1();
		try {
			Registry r = LocateRegistry.getRegistry("127.0.0.1",12345);
			rmi = (MethodRMI)r.lookup("MethodClient");
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void doShow1() {
		setTitle("Login");
		setSize(300, 130);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		addControl1();
		setResizable(false);
		setVisible(true);
	}
	public void addControl1() {
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		JPanel bottomPanel = new JPanel();
		JPanel centerPanel = new JPanel();
		JLabel username = new JLabel("Username:");
		JLabel password = new JLabel("Password:");
		password.setPreferredSize(username.getPreferredSize());
		JTextField usernameText = new JTextField(15);
		JPasswordField passwordText = new JPasswordField(15);
		JButton login = new JButton("Login");
		
		bottomPanel.add(login);
		centerPanel.add(username);
		centerPanel.add(usernameText);
		centerPanel.add(password);
		centerPanel.add(passwordText);
		mainPanel.add(centerPanel, BorderLayout.CENTER);
		mainPanel.add(bottomPanel,BorderLayout.SOUTH);
		
		login.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				Object[] options = {"OK"};
				if(usernameText.getText().equals(""))
				{
					JOptionPane.showOptionDialog(null, "Username is empty", "Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
				}
				else if(passwordText.getText().equals(""))
				{
					JOptionPane.showOptionDialog(null, "Password is empty", "Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
				}
				else
				{
					try {
						int i = rmi.Login(usernameText.getText(), passwordText.getText());
						if(i == -1)
						{
							JOptionPane.showOptionDialog(null, "Username or Password is wrong", "Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
						}
						else
						{
							usernameGlobal = usernameText.getText();
							setVisible(false);
							Client cl = new Client();
							cl.doShowClient();
							try {
								sk = new Socket("localhost", 12346);
								is = new DataInputStream(sk.getInputStream());
								os = new DataOutputStream(sk.getOutputStream());
								os.writeUTF(usernameGlobal);
								new Thread(new Runnable() {
									
									@Override
									public void run() {
										// TODO Auto-generated method stub
										while(true)
										{
											String s;
											try {
												s = is.readUTF();
												if(s.equals("@@@"))
													break;
//												chatLogText.append(s);
											} catch (IOException e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
											}
										}
										try {
											is.close();
											os.close();
											sk.close();
											sk = null;
//											System.exit(1);
										} catch (Exception e) {
											// TODO: handle exception
										}
									}
								}).start();
								try {
									final Socket skTran = new Socket("localhost", 12347);
									osTran = new DataOutputStream(skTran.getOutputStream());
									isTran = new DataInputStream(skTran.getInputStream());
									new Thread(new Runnable() {
										
										@Override
										public void run() {
											// TODO Auto-generated method stub
											while(true) {
												String s = "";
												try {
													s = isTran.readUTF();
												} catch (IOException e1) {
													// TODO Auto-generated catch block
													e1.printStackTrace();
												}
												String[] sa = s.split("-");
												fileLog.append("[GetFile] Waiting for get file "+sa[1]+"...\n");
												if(sa[0].equals("get")) {
													Long filesize = Long.parseLong(sa[2]);
													try {
														osTran.writeUTF("ready");
														osTran.flush();
													} catch (IOException e) {
														// TODO Auto-generated catch block
														e.printStackTrace();
													}
													try {
														BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
														for(int i =0; i<filesize; i++) {
															bos.write(isTran.readInt());
															bos.flush();
														}
														bos.close();
														fileLog.append("[GetFile] Get file "+sa[1]+" done!");
													} catch (FileNotFoundException e) {
														// TODO Auto-generated catch block
														e.printStackTrace();
													} catch (IOException e) {
														// TODO Auto-generated catch block
														e.printStackTrace();
													}
													
												}
												else if(sa[0].equals("upload"))
												{
													fileLog.append("[UploadFile] Waitting for upload file "+sa[1]+" to server...\n");
													Long filesize = file.length();
													try {
														osTran.writeLong(filesize);
														osTran.flush();
														String status = isTran.readUTF();
														if(status.equals("ready")) {
															BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
															int data;
															while((data=bis.read()) != -1) {
																osTran.writeInt(data);
																osTran.flush();
															}
															bis.close();
															fileLog.append("[UploadFile] Upload file "+sa[1]+" to server done!");
														}
													} catch (IOException e) {
														// TODO Auto-generated catch block
														e.printStackTrace();
													}
													listModel.addElement(sa[1]);
												}
												else if(sa[0].equals("@@@"))
												{
													try {
														isTran.close();
														osTran.close();
														skTran.close();
													} catch (IOException e) {
														// TODO Auto-generated catch block
														e.printStackTrace();
													}
												}
											}
										}
									}).start();
								} catch (UnknownHostException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								} catch (IOException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
							} catch (UnknownHostException e) {
								// TODO Auto-generated catch block
								JOptionPane.showOptionDialog(null, "Server Offline", "Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								JOptionPane.showOptionDialog(null, "Server Offline", "Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
							}
						}
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}	
				}	
			}
		});
		
		getContentPane().add(mainPanel);
	}
	public void doShowClient() {
		setTitle("Client");
		setSize(1200, 800);
		setDefaultCloseOperation(0);
		setLocationRelativeTo(null);
		addControlClient();
		setResizable(false);
		setVisible(true);
	}
	public void addControlClient() {
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
		JButton logOut = new JButton("Logout");
		JLabel helloLabel = new JLabel("Hello "+usernameGlobal+"   /   ");
		helloLabel.setFont(new Font("Arial", Font.PLAIN, 20));
		helloLabel.setForeground(Color.white);
		topPanel.setBackground(Color.GRAY);
		topPanel.add(Box.createHorizontalStrut(920));
		topPanel.add(helloLabel);
		topPanel.add(logOut);
		
		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
		JButton changePassword = new JButton ("Change Password   ");
		JButton offerDayoff = new JButton("Offer Day Off            ");
		JButton checkInOut = new JButton     ("Check In/Out            ");
		JButton fileOnServer = new JButton   ("Public File                 ");
		JButton chat = new JButton           ("Chat                           ");
		leftPanel.add(Box.createVerticalStrut(20));
		leftPanel.add(changePassword);
		leftPanel.add(Box.createVerticalStrut(20));
		leftPanel.add(offerDayoff);
		leftPanel.add(Box.createVerticalStrut(20));
		leftPanel.add(checkInOut);
		leftPanel.add(Box.createVerticalStrut(20));
		leftPanel.add(fileOnServer);
		leftPanel.add(Box.createVerticalStrut(20));
		leftPanel.add(chat);
		leftPanel.setBackground(Color.LIGHT_GRAY);
		
		chatLogText = new JTextArea(20,50);
		chatLogText.setEditable(false);
		
		JPanel centerPanel = new JPanel();
		
		mainPanel.add(topPanel,BorderLayout.NORTH);
		mainPanel.add(leftPanel,BorderLayout.WEST);
		
		changePassword.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				getContentPane().remove(mainPanel);
				centerPanel.removeAll();
//				JPanel mainPanelChangePassword = new JPanel();
//				mainPanelChangePassword.setLayout(new BorderLayout());
				JLabel oldPasswordLabel = new JLabel("Old PassWord:");
				JLabel newPasswordLabel = new JLabel("New Password:");
				JLabel confirmPasswordLabel = new JLabel("Confirm New Password:");
				JPasswordField oldPasswordText = new JPasswordField(20);
				JPasswordField newPasswordText = new JPasswordField(20);
				JPasswordField confirmNewPasswordText = new JPasswordField(20);
				JPanel oldPasswordPanel = new JPanel();
				oldPasswordPanel.add(oldPasswordLabel);
				oldPasswordPanel.add(oldPasswordText);
				JPanel newPasswordPanel = new JPanel();
				newPasswordPanel.add(newPasswordLabel);
				newPasswordPanel.add(newPasswordText);
				JPanel confirmNewPasswordPanel = new JPanel();
				confirmNewPasswordPanel.add(confirmPasswordLabel);
				confirmNewPasswordPanel.add(confirmNewPasswordText);
				JPanel buttonSubmit = new JPanel();
				JButton submit = new JButton("Submit");
				buttonSubmit.add(submit);
				
				oldPasswordLabel.setPreferredSize(confirmPasswordLabel.getPreferredSize());
				newPasswordLabel.setPreferredSize(confirmPasswordLabel.getPreferredSize());
				
				centerPanel.add(oldPasswordPanel);
				centerPanel.add(Box.createHorizontalStrut(600));
				centerPanel.add(newPasswordPanel);
				centerPanel.add(Box.createHorizontalStrut(600));
				centerPanel.add(confirmNewPasswordPanel);
				centerPanel.add(Box.createHorizontalStrut(600));
				centerPanel.add(buttonSubmit);
				centerPanel.add(Box.createHorizontalStrut(600));
				
				
				submit.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent arg0) {
						// TODO Auto-generated method stub
						Object[] options = {"OK"};
						if(oldPasswordText.getText().equals(""))
						{
							JOptionPane.showOptionDialog(null, "Old password is empty", "Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
						}
						else if(newPasswordText.getText().equals(""))
						{
							JOptionPane.showOptionDialog(null, "New password is empty", "Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
						}
						else if(confirmNewPasswordText.getText().equals("")) {
							JOptionPane.showOptionDialog(null, "Confirm new password is empty", "Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
						}
						else if(!newPasswordText.getText().equals(confirmNewPasswordText.getText())) {
							JOptionPane.showOptionDialog(null, "Confirm new password and new password isn't correct", "Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
						}
						else
						{
							try {
								int i = rmi.ChangePassword(usernameGlobal, oldPasswordText.getText(), newPasswordText.getText());
								if(i == -1) {
									JOptionPane.showOptionDialog(null, "Can not connect database", "Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
								}
								else if(i == 0) {
									JOptionPane.showOptionDialog(null, "Old password isn't correct", "Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
								}
								else
								{
									JOptionPane.showOptionDialog(null, "Request successful", "Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
									oldPasswordText.setText("");
									newPasswordText.setText("");
									confirmNewPasswordText.setText("");
									try {
										os.writeUTF(usernameGlobal+"-@changepass");
										os.flush();
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							} catch (RemoteException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
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
		
		offerDayoff.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				getContentPane().remove(mainPanel);
				centerPanel.removeAll();
				
				JPanel dayPanel = new JPanel();
				JPanel buttonPanel = new JPanel();
				JPanel titlePanel = new JPanel();
				JPanel tablePanel = new JPanel();
				
				JLabel dayLabel = new JLabel("Day: ");
				String[] day = {"1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24","25","26","27","28","29","30","31"};
				String[] month = {"1","2","3","4","5","6","7","8","9","10","11","12"};
				String[] year = {LocalDateTime.now().getYear()+"",((Integer.parseInt(LocalDateTime.now().getYear() + "") + 1)) + ""};
				JComboBox dayDrop = new JComboBox<>(day);
				JComboBox monthDrop = new JComboBox<>(month);
				JComboBox yearDrop = new JComboBox<>(year);
				dayPanel.add(dayLabel);
				dayPanel.add(dayDrop);
				dayPanel.add(monthDrop);
				dayPanel.add(yearDrop);
				
				JButton submitButton = new JButton("Submit");
				buttonPanel.add(submitButton);
				
				JLabel title = new JLabel("List My Offer");
				title.setFont(new Font("Arial", Font.PLAIN, 15));
				title.setForeground(Color.blue);
				titlePanel.add(title);
				
				JTable table = new JTable();
				DefaultTableModel tableModel = (DefaultTableModel)table.getModel();
				JScrollPane tableScroll = new JScrollPane(table);
				String data = "";
				try {
					data = rmi.getDataDayOff(usernameGlobal);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				String[] dataSplit = data.split("-");
				String[][] dataTable = new String[dataSplit.length][3];
				if(data.equals("")) {
					//
				}
				else
				{
					for(int i = 0 ; i<dataSplit.length; i++) {
						String[] tempData = dataSplit[i].split(":");
						dataTable[i][0] = tempData[0];
						dataTable[i][1] = tempData[1];
						if(tempData[2].equals("1"))
						{
							dataTable[i][2] = "wait";
						}
						else if(tempData[2].equals("2"))
						{
							dataTable[i][2] = "accept";
						}
						else
						{
							dataTable[i][2] = "remove";
						}
					}
				}
				tableModel.setDataVector(dataTable, new String[] {"ID","Day off","Status"});
				tablePanel.add(tableScroll);
				
				centerPanel.add(dayPanel);
				centerPanel.add(Box.createHorizontalStrut(810));
				centerPanel.add(buttonPanel);
				centerPanel.add(Box.createHorizontalStrut(900));
				centerPanel.add(titlePanel);
				centerPanel.add(Box.createHorizontalStrut(1000));
				centerPanel.add(tablePanel);
				centerPanel.add(Box.createHorizontalStrut(600));
				
				submitButton.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent arg0) {
						// TODO Auto-generated method stub
						Object[] options = {"OK"};
						if((monthDrop.getSelectedItem().equals("2") && dayDrop.getSelectedItem().equals("29")) ||
								(monthDrop.getSelectedItem().equals("2") && dayDrop.getSelectedItem().equals("30")) ||
								(monthDrop.getSelectedItem().equals("2") && dayDrop.getSelectedItem().equals("31")) ||
								(monthDrop.getSelectedItem().equals("4") && dayDrop.getSelectedItem().equals("31")) ||
								(monthDrop.getSelectedItem().equals("6") && dayDrop.getSelectedItem().equals("31")) ||
								(monthDrop.getSelectedItem().equals("9") && dayDrop.getSelectedItem().equals("31")) ||
								(monthDrop.getSelectedItem().equals("11") && dayDrop.getSelectedItem().equals("31")))
						{
							JOptionPane.showOptionDialog(null, dayDrop.getSelectedItem() + "/" + monthDrop.getSelectedItem() + " doesn't exist", "Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
						}
						else
						{
							if(Integer.parseInt(monthDrop.getSelectedItem()+"") > LocalDateTime.now().getMonthValue())
							{
								//date process
								String date = yearDrop.getSelectedItem() + "-" + monthDrop.getSelectedItem() + "-" + dayDrop.getSelectedItem();
								try {
									rmi.InsertDayOff(date, usernameGlobal);
									//refersh table
									String data = "";
									try {
										data = rmi.getDataDayOff(usernameGlobal);
									} catch (RemoteException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									String[] dataSplit = data.split("-");
									String[][] dataTable = new String[dataSplit.length][3];
									if(data.equals("")) {
										//
									}
									else
									{
										for(int i = 0 ; i<dataSplit.length; i++) {
											String[] tempData = dataSplit[i].split(":");
											dataTable[i][0] = tempData[0];
											dataTable[i][1] = tempData[1];
											if(tempData[2].equals("1"))
											{
												dataTable[i][2] = "wait";
											}
											else if(tempData[2].equals("2"))
											{
												dataTable[i][2] = "accept";
											}
											else
											{
												dataTable[i][2] = "remove";
											}
										}
									}
									tableModel.setDataVector(dataTable, new String[] {"ID","Day off","Status"});
									JOptionPane.showOptionDialog(null, "Offer day off success", "Success", JOptionPane.PLAIN_MESSAGE, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
								} catch (RemoteException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
							else
							{
								if(Integer.parseInt(dayDrop.getSelectedItem()+"") > LocalDateTime.now().getDayOfMonth()) {
									//date process
									String date = yearDrop.getSelectedItem() + "-" + monthDrop.getSelectedItem() + "-" + dayDrop.getSelectedItem();
									try {
										rmi.InsertDayOff(date, usernameGlobal);
										//refersh table
										String data = "";
										try {
											data = rmi.getDataDayOff(usernameGlobal);
										} catch (RemoteException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
										String[] dataSplit = data.split("-");
										String[][] dataTable = new String[dataSplit.length][3];
										if(data.equals("")) {
											//
										}
										else
										{
											for(int i = 0 ; i<dataSplit.length; i++) {
												String[] tempData = dataSplit[i].split(":");
												dataTable[i][0] = tempData[0];
												dataTable[i][1] = tempData[1];
												if(tempData[2].equals("1"))
												{
													dataTable[i][2] = "wait";
												}
												else if(tempData[2].equals("2"))
												{
													dataTable[i][2] = "accept";
												}
												else
												{
													dataTable[i][2] = "remove";
												}
											}
										}
										tableModel.setDataVector(dataTable, new String[] {"ID","Day off","Status"});
										JOptionPane.showOptionDialog(null, "Offer day off success", "Success", JOptionPane.PLAIN_MESSAGE, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
										try {
											os.writeUTF(usernameGlobal+"-@offerday");
											os.flush();
										} catch (IOException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									} catch (RemoteException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
								else
								{
									JOptionPane.showOptionDialog(null, "Day off can't today or older", "Success", JOptionPane.PLAIN_MESSAGE, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
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
		
		checkInOut.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				getContentPane().remove(mainPanel);
				centerPanel.removeAll();
				
				JPanel buttonPanel = new JPanel();
				JPanel titlePanel = new JPanel();
				JPanel tablePanel = new JPanel();
				
				JButton checkIn = new JButton("Check In");
				JButton checkOut = new JButton("Check Out");
				buttonPanel.add(checkIn);
				buttonPanel.add(checkOut);
				
				JLabel title = new JLabel("List Check In/Out");
				title.setFont(new Font("Arial", Font.PLAIN, 15));
				title.setForeground(Color.blue);
				titlePanel.add(title);
				
				JTable table = new JTable();
				DefaultTableModel tableModel = (DefaultTableModel)table.getModel();
				JScrollPane tableScroll = new JScrollPane(table);
				String data = "";
				try {
					data = rmi.GetDataTimeKeeping(usernameGlobal);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				String[] dataSplit = data.split("-");
				String[][] dataTable = new String[1][2];
				if(data.equals("1"))
				{
					//
				}
				else
				{
					dataTable[0][0] = dataSplit[0];
					dataTable[0][1] = dataSplit[1];
				}
				tableModel.setDataVector(dataTable, new String[] {"Time In","Time Out"});
				tablePanel.add(tableScroll);
				
				if(data.equals("1"))
				{
					checkOut.hide();
				}
				else
				{
					if(dataSplit[1].equals("NULL"))
					{
						checkIn.hide();
					}
					else
					{
						checkIn.hide();
						checkOut.hide();
					}
				}
				
				centerPanel.add(buttonPanel);
				centerPanel.add(Box.createHorizontalStrut(950));
				centerPanel.add(titlePanel);
				centerPanel.add(Box.createHorizontalStrut(950));
				centerPanel.add(tablePanel);
				
				checkIn.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent arg0) {
						// TODO Auto-generated method stub
						Object[] options = {"OK"};
						try {
							rmi.CheckInOut(usernameGlobal, "in");
						} catch (RemoteException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						JOptionPane.showOptionDialog(null, "Check In success", "Success", JOptionPane.PLAIN_MESSAGE, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
						// refesh table
						String data = "";
						try {
							data = rmi.GetDataTimeKeeping(usernameGlobal);
						} catch (RemoteException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						String[] dataSplit = data.split("-");
						String[][] dataTable = new String[1][2];
						if(data.equals("1"))
						{
							//
						}
						else
						{
							dataTable[0][0] = dataSplit[0];
							dataTable[0][1] = dataSplit[1];
						}
						tableModel.setDataVector(dataTable, new String[] {"Time In","Time Out"});
						checkIn.hide();
						checkOut.show();
						try {
							os.writeUTF(usernameGlobal+"-@checkin");
							is.read();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
				
				checkOut.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent arg0) {
						// TODO Auto-generated method stub
						Object[] options = {"OK"};
						try {
							rmi.CheckInOut(usernameGlobal, "out");
						} catch (RemoteException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						JOptionPane.showOptionDialog(null, "Check Out success", "Success", JOptionPane.PLAIN_MESSAGE, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
						// refesh table
						String data = "";
						try {
							data = rmi.GetDataTimeKeeping(usernameGlobal);
						} catch (RemoteException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						String[] dataSplit = data.split("-");
						String[][] dataTable = new String[1][2];
						if(data.equals("1"))
						{
							//
						}
						else
						{
							dataTable[0][0] = dataSplit[0];
							dataTable[0][1] = dataSplit[1];
						}
						tableModel.setDataVector(dataTable, new String[] {"Time In","Time Out"});
						checkIn.hide();
						checkOut.hide();
						try {
							os.writeUTF(usernameGlobal+"-@checkout");
							os.flush();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
				
				mainPanel.add(centerPanel, BorderLayout.CENTER);
				getContentPane().add(mainPanel);
				setVisible(false);
				setVisible(true);
			}
		});
		
		chat.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				getContentPane().remove(mainPanel);
				centerPanel.removeAll();
				
				JPanel chatLogPanel = new JPanel();
				JPanel inputTextPanel = new JPanel();
				
				JScrollPane chatLogScroll = new JScrollPane(chatLogText);
				chatLogPanel.add(chatLogScroll);
				
				JTextArea chatInput = new JTextArea(3,30);
				JScrollPane chatInputScroll = new JScrollPane(chatInput);
				JButton send = new JButton("Send");
				inputTextPanel.add(chatInputScroll);
				inputTextPanel.add(send);
				
				centerPanel.add(chatLogPanel);
				centerPanel.add(Box.createHorizontalStrut(800));
				centerPanel.add(inputTextPanel);
				centerPanel.add(Box.createHorizontalStrut(800));
				
				send.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent arg0) {
						// TODO Auto-generated method stub
						String s = usernameGlobal+"-"+chatInput.getText()+"\n";
						try {
							os.writeUTF(s);
							chatLogText.append("[You said]: "+chatInput.getText()+"\n");
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						chatInput.setText("");
						chatInput.requestFocus();
					}
				});
				
				mainPanel.add(centerPanel,BorderLayout.CENTER);
				getContentPane().add(mainPanel);
				setVisible(false);
				setVisible(true);	
			}
		});
		
		logOut.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				try {
					int choose = JOptionPane.showConfirmDialog(null, "Are you sure to logout?", "Logout", JOptionPane.YES_NO_OPTION);
					if(choose == JOptionPane.YES_OPTION)
					{
						os.writeUTF(usernameGlobal+"-@@@\n");
						osTran.writeUTF(usernameGlobal+"-@@@-a");
						setVisible(false);
						Client cl = new Client();
						cl.doShow1();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
		fileOnServer.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				getContentPane().remove(mainPanel);
				centerPanel.removeAll();
				
				JPanel listFilePanel = new JPanel();
				JPanel buttonPanel = new JPanel();
				
				String data = "";
				try {
					data = rmi.GetDataFile();
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Border bd = BorderFactory.createLineBorder(Color.blue);
				TitledBorder tbd = new TitledBorder(bd,"File");
				listModel = new DefaultListModel();
				list = new JList(listModel);
				list.setBorder(tbd);
				list.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
				JScrollPane listScroll = new JScrollPane(list);
				listScroll.setPreferredSize(new Dimension(500, 300));
				if(data.equals(""))
				{
					//
				}
				else
				{
					String[] dataSplit = data.split("-");
					for(int i = 0; i < dataSplit.length; i++) {
						listModel.addElement(dataSplit[i]);
					}
				}
				listFilePanel.add(listScroll);
				
				JButton getFile = new JButton("Get File");
				JButton uploadFile = new JButton("Upload File");
				buttonPanel.add(getFile);
				buttonPanel.add(uploadFile);
				
				fileLog = new JTextArea(20,50);
				fileLog.setEditable(false);
				JScrollPane logFile = new JScrollPane(fileLog);
				
				centerPanel.add(listFilePanel);
				centerPanel.add(Box.createHorizontalStrut(900));
				centerPanel.add(buttonPanel);
				centerPanel.add(Box.createHorizontalStrut(900));
				centerPanel.add(logFile);
				
				getFile.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent arg0) {
						// TODO Auto-generated method stub
						Object[] options = {"OK"};
						if(list.isSelectionEmpty())
						{
							JOptionPane.showOptionDialog(null, "No one file is select", "Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
						}
						else
						{
							file = new File("src//File//"+list.getSelectedValue());
							try {
								if(!file.createNewFile())
								{
									JOptionPane.showOptionDialog(null, "File already exist!", "Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
								}
								else
								{
									try {
										osTran.writeUTF(usernameGlobal+"-@getfile-"+list.getSelectedValue());
										osTran.flush();
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							} catch (HeadlessException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}							
						}
					}
				});
				
				uploadFile.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent arg0) {
						// TODO Auto-generated method stub
						Object[] options = {"OK"};
						JFileChooser fileChoose = new JFileChooser();
						int value = fileChoose.showOpenDialog(null);
						if(value == fileChoose.APPROVE_OPTION)
						{
							file = fileChoose.getSelectedFile();
							int check = 0;
							String name = file.getName();
							for(int i = 0; i < listModel.size(); i++)
							{
								if(name.equals(listModel.getElementAt(i))) {
									check = 1;
									break;
								}
							}
							if(check == 1) {
								JOptionPane.showOptionDialog(null, "File already exist in server!", "Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
							}
							else
							{
								try {
									osTran.writeUTF(usernameGlobal+"-@uploadfile-"+name);
									osTran.flush();
								} catch (IOException e) {
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
		
		getContentPane().add(mainPanel);
	}
}
