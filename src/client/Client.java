package client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Scanner;
import java.util.Vector;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import javax.print.attribute.standard.Severity;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.synth.SynthPasswordFieldUI;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.text.Element;
import javax.swing.text.ParagraphView;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.InlineView;

import server.Server;


public class Client extends JFrame implements Runnable {
	static Socket clientsocket = null;
	String clientName = null;
	String record = "";
	BufferedReader bReader = null;
	PrintStream pStream = null;
	JPanel main = new JPanel();
	Vector<String> cVector = new Vector<>();
	String msg = null;
	JList<String> jList = new JList<>(cVector);
	HashMap<String, privateChat> pcMap = new HashMap<>();
	HashMap<String, groupChat> gpMap = new HashMap<>();
	// JTextArea chat = new JTextArea(15, 40);
	JTextArea type = new JTextArea(3, 40);

	JTextPane chat = new JTextPane();

	JScrollPane jchat = new JScrollPane(chat);
	JScrollPane jtype = new JScrollPane(type);

	ImageIcon sendicon = new ImageIcon("plus.png");
	JButton startChat = new JButton("发起私聊");
	JButton startGROUP = new JButton("创建群聊");
	JButton send = new JButton(sendicon);
	JLabel list = new JLabel("在线列表", JLabel.CENTER);

	Client self = this;
	boolean flag = true;

	char[] specialChars = { '#', '@' };
	@SuppressWarnings("serial")
	public Client() {

		clientName = getClientName();
		/*
		 * while(clientName.length()==0) { JOptionPane.showMessageDialog(this,
		 * "用户名不能为空", "提示", JOptionPane.PLAIN_MESSAGE); clientName = new
		 * JOptionPane().showInputDialog("请设置用户名："); } if(clientName==null) {
		 * System.exit(0); }
		 */
		this.setTitle("Mainpage of " + clientName);
		this.setSize(620, 488);
		this.setLocation(100, 200);
		this.setVisible(true);
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		// 主面板
		this.add(main);
		main.setBackground(Color.WHITE);
		main.setLayout(null);
		main.add(jchat);
		// main.add(type);
		main.add(jList);
		main.add(startChat);
		main.add(startGROUP);
		main.add(send);
		main.add(list);
		main.add(jtype);
		// jlabel
		list.setLocation(500, 0);
		list.setSize(110, 18);
		list.setBorder(BorderFactory.createLineBorder(Color.lightGray, 1));
		list.setBackground(Color.red);
		list.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		// 聊天记录scrollpane
		jchat.setLocation(-1, 0);
		jchat.setSize(502, 350);
		jchat.setBorder(BorderFactory.createLineBorder(Color.lightGray, 1));
		// 聊天记录jtextpane
		// chat.setLineWrap(true);
		chat.setEditable(false);
		// 在线列表
		jList.setLocation(500, 18);
		jList.setSize(110, 394);
		jList.setBorder(BorderFactory.createLineBorder(Color.lightGray, 1));
		jList.setFont(new Font("微软雅黑", Font.PLAIN, 11));
		// 发起私聊和群聊按钮
		startChat.setSize(110, 20);
		startChat.setLocation(500, 411);
		startGROUP.setSize(110, 20);
		startGROUP.setLocation(500, 431);
		startChat.setAlignmentX(0);
		startGROUP.setAlignmentX(0);
		startChat.setBackground(Color.LIGHT_GRAY);
		startGROUP.setBackground(Color.LIGHT_GRAY);
		startChat.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		startGROUP.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		// 文本输入
		jtype.setLocation(0, 350);
		jtype.setSize(500, 70);
		jtype.setBorder(null);
		type.setEditable(true);
		type.setLineWrap(true);
		// 发送按钮
		send.setBounds(450, 420, 50, 30);
		send.setBackground(Color.WHITE);
		send.setBorder(null);
		sendicon.setImage(sendicon.getImage().getScaledInstance(32, 32, java.awt.Image.SCALE_DEFAULT));

		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				String tempmsg = "UPDATE#" + clientName;
				self.pStream.println(tempmsg);//更新在线用户列表
				System.exit(0);//退出
			}
		});
		// 发起私聊
		startChat.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				jList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				@SuppressWarnings("deprecation")
				Object[] name = jList.getSelectedValues();
				System.out.println(name);
				if (name == null) {
					return;
				} else {
					if (name.length == 1) {
						privateChat pChat = new privateChat(clientName, (String) name[0], self);
						pcMap.put((String) name[0], pChat);
					} else {
						JOptionPane.showMessageDialog(self, "请选择一位用户发起私聊", "ERROR", JOptionPane.ERROR_MESSAGE);
					}
				}
			}

		});
		// 创建群聊
		startGROUP.addMouseListener(new MouseAdapter() {
			@SuppressWarnings({ "unused", "deprecation" })
			public void mouseClicked(MouseEvent e) {
				jList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				@SuppressWarnings("deprecation")
				Object[] name = jList.getSelectedValues();
				System.out.println(name);
				if (name == null) {
					return;
				} else {
					if (jList.getSelectedValues().length < 2) {
				JOptionPane.showMessageDialog(self, "请选择至少两个用户", "ERROR", JOptionPane.ERROR_MESSAGE);
					} else {
						//String groupName = JOptionPane.showInputDialog("请输入群聊名称：");
						String groupName=null;
					/*	if (groupName.length() == 0) {
							JOptionPane.showMessageDialog(self, "昵称不能为空", "错误", JOptionPane.ERROR_MESSAGE);
							groupName = JOptionPane.showInputDialog("请输入群聊名称：");
						} else if(groupName == null){
							JOptionPane.showMessageDialog(self, "昵称不能为空", "错误", JOptionPane.ERROR_MESSAGE);
							groupName = JOptionPane.showInputDialog("请输入群聊名称：");
						}*/
						boolean flags=true;
						while(flags) {
							groupName = JOptionPane.showInputDialog("请输入群聊名称：");
							if(groupName==null||groupName.length()==0) {
								JOptionPane.showMessageDialog(self, "昵称不能为空", "错误", JOptionPane.ERROR_MESSAGE);
							}else {
								flags=false;
								
							}
						}
						String tempMsg = "GROUPSET#" + groupName + "#" + clientName + "@";
						for (Object nn : name) {
							tempMsg = tempMsg + (String) nn + "@";
						}
						self.pStream.println(tempMsg);
						groupChat groupChat = new groupChat(groupName, clientName, self);
					}
				}
			}
		});
		// 回车发送
		type.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				if (e.getKeyChar() == KeyEvent.VK_ENTER) {
					String brocaMsg = type.getText();//获取发送消息内容
					brocaMsg = "BROADCAST1#" + clientName + "#" + brocaMsg;
					self.pStream.println(brocaMsg);//发送至服务器
					type.setText("");//重置输入框

				}
			}
		});
		// 设置发送按钮
		send.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				String brocaMsg = type.getText();//获取发送消息内容
				brocaMsg = "BROADCAST1#" + clientName + "#" + brocaMsg;
				self.pStream.println(brocaMsg);//发送至服务器
				type.setText("");//重置输入框

			}
		});
		// jtextpane换行
		chat.setEditorKit(new HTMLEditorKit() {
			@Override
			public ViewFactory getViewFactory() {

				return new HTMLFactory() {
					public View create(Element e) {
						View v = super.create(e);
						if (v instanceof InlineView) {
							return new InlineView(e) {
								public int getBreakWeight(int axis, float pos, float len) {
									return GoodBreakWeight;
								}

								public View breakView(int axis, int p0, float pos, float len) {
									if (axis == View.X_AXIS) {
										checkPainter();
										int p1 = getGlyphPainter().getBoundedPosition(this, p0, pos, len);
										if (p0 == getStartOffset() && p1 == getEndOffset()) {
											return this;
										}
										return createFragment(p0, p1);
									}
									return this;
								}
							};
						} else if (v instanceof ParagraphView) {
							return new ParagraphView(e) {
								protected SizeRequirements calculateMinorAxisRequirements(int axis,
										SizeRequirements r) {
									if (r == null) {
										r = new SizeRequirements();
									}
									float pref = layoutPool.getPreferredSpan(axis);
									float min = layoutPool.getMinimumSpan(axis);
									// Don't include insets, Box.getXXXSpan will include them.
									r.minimum = (int) min;
									r.preferred = Math.max(r.minimum, (int) pref);
									r.maximum = Integer.MAX_VALUE;
									r.alignment = 0.5f;
									return r;
								}

							};
						}
						return v;
					}
				};
			}
		});

		String serverIP = "127.0.0.1";
		try {
			clientsocket = new Socket(serverIP, 55533);
			bReader = new BufferedReader(new InputStreamReader(clientsocket.getInputStream()));
			pStream = new PrintStream(clientsocket.getOutputStream());
			pStream.println(clientName);
			pStream.println("GETRE#" + clientName);
			new Thread(this).start();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String args[]) {
		new Client();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (flag) {
			try {
				msg = bReader.readLine();
				System.out.println(msg);
				String[] msgs = msg.split("#");
				if (msgs[0].equals("USERLIST")) {
					updateList(msgs[1]);
				} else if (msgs[0].equals("PRIMSG")) {
					getPriMsg(msgs[2], msgs[3]);
				} else if (msgs[0].equals("GROUPMSG")) {
					if (msgs.length < 4) {
						continue;
					}
					getGroupMsg(msgs[1], msgs[2], msgs[3]);
				} else if (msgs[0].equals("GPMEMLIST")) {
					gpMap.get(msgs[1]).updateList(msgs[2]);
				} else if (msgs[0].equals("BROADCAST")) {
					showBoardCast(msgs[1]);
				} else if (msgs[0].equals("BROADCAST1")) {
					showMsg(msgs[1], msgs[2]);
				} else if (msgs[0].equals("UPDATE")) {
					updateList(msgs[1]);
				} else if (msgs[0].equals("ATTENTION")) {
					new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								Thread.sleep(5000);
								clientsocket.close();
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

							dispose();
							self.flag = false;
						}
					}).start();
					JOptionPane.showMessageDialog(this, msgs[1], "提示", JOptionPane.PLAIN_MESSAGE);

				} else if (msgs[0].equals("RECORD")) {
					showPastMessage(msgs);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	// 更新列表
	public void updateList(String s) {
		cVector.removeAllElements();
		String[] names = s.split("@");
		for (String name : names) {
			cVector.add(name);
		}
		jList.setListData(cVector);
	}

	// 群聊信息
	public void getGroupMsg(String gn, String n, String m) throws Exception {
		if (gpMap.containsKey(gn)) {
			gpMap.get(gn).jTextArea.append(n + ":" + m + "\n");
		} else {
			groupChat gChat = new groupChat(gn, clientName, self);
			gChat.jTextArea.append(n + ":" + m + "\n");
		}
	}

	// 私聊信息
	public void getPriMsg(String n, String m) {
		if (pcMap.containsKey(n)) {
			pcMap.get(n).jTextArea.append(n + ":" + m + "\n");
		} else {
			privateChat pChat = new privateChat(clientName, n, this);
			pcMap.put(n, pChat);
			pChat.jTextArea.append(n + ":" + m + "\n");
		}
	}

	// 世界聊天室用户，用jtextpane
	public void showMsg(String s, String msg) {
		String show;
		if (s.equals(clientName)) {
			show = "<div><font color='#6A5ACD' face = '微软雅黑' size='4'>" + s + ":</font></div>";
		} else {
			show = "<div><font color='#FFA500' face = '微软雅黑' size='4'>" + s + ":</font></div>";
		}
		String showmsg = "<div><font color='#000000' face = '微软雅黑' size='4'>" + msg + "</font></div>";
		record += show + showmsg;
		chat.setText(record);
	}

	// 世界聊天室系统广播
	public void showBoardCast(String msg) {
		String show = "<div><font color='#8A2BE2' face = '微软雅黑' size='4' style='font-weight:700'>系统广播：" + msg
				+ "</font></div>";
		record += show;
		chat.setText(record);
	}

	// 历史消息
	public void showPastMessage(String[] msgs) {
		for (int i = 0; i < msgs.length; i++) {
			if (msgs[i] != null) {
				String[] texts = msgs[i].split("@");
				if (texts.length == 2) {
					if (texts[0].equals("系统广播")) {
						showBoardCast(texts[1]);
					} else {
						showMsg(texts[0], texts[1]);
					}
				}
			}
		}
	}

	// 获取用户名
	public String getClientName() {
		String clientName = null;
		char[] specialChars = { '#', '@' };
		boolean flag = false;
		clientName = JOptionPane.showInputDialog(this, "请输入用户名");
		while (true) {
			flag = false;
			if (clientName == null) {
				System.exit(0);
			}
			if (clientName.length() == 0) {
				JOptionPane.showMessageDialog(this, "昵称不能为空", "错误", JOptionPane.ERROR_MESSAGE);
			} else {
				for (char c : specialChars) {
					if (clientName.contains(String.valueOf(c))) {
						flag = true;
					}
				}
				if (flag == true) {
					JOptionPane.showMessageDialog(this, "昵称不能包含特殊字符" + specialChars + "，请重新输入", "ERROR",
							JOptionPane.ERROR_MESSAGE);
				} else {
					break;
				}
			}
			clientName = JOptionPane.showInputDialog(this, "请输入用户名");
		}
		return clientName;
	}

	/*public void broadCast(String m) {
		// JOptionPane.showMessageDialog(this, m, "广播", JOptionPane.PLAIN_MESSAGE);
		// chat.append("服务器广播:" + m + '\n');
	}*/

	class privateChat extends JFrame {
		Client client = null;
		String selfName = null;
		String name = null;
		JTextArea jTextArea = new JTextArea(17, 35);
		JTextField jTextField = new JTextField(35);
		JPanel jPanel = new JPanel();

		public privateChat(String s, String n, Client c) {
			selfName = s;
			name = n;
			client = c;
			setLocation(c.getLocation());
			this.setTitle("ChatWith" + name);
			setSize(400, 390);
			setVisible(true);
			//setLocation(400, 400);
			setDefaultCloseOperation(HIDE_ON_CLOSE);
			this.add(jPanel);
			jPanel.add(jTextArea, BorderLayout.NORTH);
			jPanel.add(jTextField, BorderLayout.SOUTH);
			jTextField.setEditable(true);
			jTextArea.setLineWrap(true);
			jTextArea.setEditable(false);

			this.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					client.pcMap.remove(name);
				}
			});
			jTextField.addKeyListener(new KeyAdapter() {
				@Override
				public void keyTyped(KeyEvent e) {
					if (e.getKeyChar() == KeyEvent.VK_ENTER) {
						String tempMsg = "PRIMSG#" + name + "#" + selfName + "#";
						String input = jTextField.getText();
						input.replaceAll("\n", "");
						if (input.equals("")) {
							jTextField.setText("");
							return;
						}
						tempMsg = tempMsg + input;
						client.pStream.println(tempMsg);
						System.out.println(tempMsg);
						jTextArea.append(selfName + ":" + input + "\n");
						jTextField.setText("");
					}
				}
			});
		}
	}

	class groupChat extends JFrame {
		String groupName = null;
		String selfName = null;
		Client self = null;
		String msg = null;
		String[] names = null;
		JTextArea jTextArea = new JTextArea(17, 35);
		JTextArea type = new JTextArea();
		JScrollPane jScrollPane = new JScrollPane(jTextArea);
		JLabel list = new JLabel("群成员列表", JLabel.CENTER);
		JPanel main = new JPanel();
		ImageIcon sendicon = new ImageIcon("plus.png");
		JButton send = new JButton(sendicon);
		Vector<String> mem = new Vector<>();
		JList<String> memlist = new JList<>(mem);

		public groupChat(String n, String sn, Client s) {
			groupName = n;
			selfName = sn;
			self = s;
			self.pStream.println("GPMEMLIST#" + groupName + "#" + selfName);
			self.gpMap.put(groupName, this);
			setLocation(self.getLocation());
			this.setTitle(selfName + " In Groupchat:" + groupName);
			setSize(620, 488);
			setVisible(true);
			//setLocation(300, 300);
			setDefaultCloseOperation(HIDE_ON_CLOSE);
			this.add(main);
			main.setBackground(Color.WHITE);
			main.setLayout(null);

			main.add(jScrollPane);
			main.add(type);
			main.add(memlist);
			main.add(send);
			main.add(list);

			list.setLocation(500, 0);
			list.setSize(110, 18);
			list.setBorder(BorderFactory.createLineBorder(Color.lightGray, 1));
			jScrollPane.setLocation(-1, 0);
			jScrollPane.setSize(502, 350);
			jScrollPane.setBorder(BorderFactory.createLineBorder(Color.lightGray, 1));
			memlist.setLocation(500, 18);
			memlist.setSize(110, 440);
			memlist.setBorder(BorderFactory.createLineBorder(Color.lightGray, 1));

			type.setLocation(0, 350);
			type.setSize(500, 70);
			type.setBorder(null);
			type.setEditable(true);
			type.setLineWrap(true);

			jTextArea.setLineWrap(true);
			jTextArea.setEditable(false);

			send.setBounds(450, 420, 50, 30);
			send.setBackground(Color.WHITE);
			send.setBorder(null);
			sendicon.setImage(sendicon.getImage().getScaledInstance(32, 32, java.awt.Image.SCALE_DEFAULT));

			this.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					self.gpMap.remove(groupName);
				}
			});

			type.addKeyListener(new KeyAdapter() {
				@Override
				public void keyTyped(KeyEvent e) {
					if (e.getKeyChar() == KeyEvent.VK_ENTER) {
						String tempMsg = "GROUPMSG#" + groupName + "#" + selfName + "#";
						String input = type.getText();
						tempMsg = tempMsg + input;
						self.pStream.println(tempMsg);
						System.out.println(tempMsg);
						type.setText("");
					}
				}
			});

			send.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					String tempMsg = "GROUPMSG#" + groupName + "#" + selfName + "#";
					String input = type.getText();
					tempMsg = tempMsg + input;
					self.pStream.println(tempMsg);
					System.out.println(tempMsg);
					type.setText("");
				}
			});
			//self.gpMap.put(groupName, this);
			pStream.println("GPMEMLIST#" + groupName + "#" + clientName);

		}

		public void updateList(String msg) throws Exception {
			mem.removeAllElements();
			String[] names = msg.split("@");
			System.out.println("client" + names);
			for (String name : names) {
				mem.add(name);
			}
			memlist.setListData(mem);
		}
	}
}
