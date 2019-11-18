package server;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.lang.*;
import javax.swing.*;
import javax.swing.border.Border;

public class Server extends JFrame implements Runnable {
	private ServerSocket server;
	HashMap<String, ListenerClient> clientMap = new HashMap<>();//�洢 �û�������Ӧ ListenerClient��
	HashMap<String, ArrayList<String>> groupMap = new HashMap<>();//�洢 Ⱥ��������ӦȺ��Ա
	JPanel jPanel = new JPanel();
	JPanel southpanel = new JPanel();
	JTextArea jTextArea = new JTextArea(12, 25);
	JScrollPane jScrollPane = new JScrollPane(jTextArea);
	JButton broadcast = new JButton("�㲥");
	JButton exit = new JButton("����");
	JList<String> list = new JList<>();
	String record = "RECORD";
	

	public Server() {
		setTitle("������");
		setSize(300, 300);
		setLocation(300, 300);
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setResizable(false);
		jScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		jScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		this.add(jPanel);
		jPanel.setLayout(new BorderLayout());
		jPanel.add(jScrollPane, BorderLayout.NORTH);
		jPanel.add(list, BorderLayout.NORTH);
		jPanel.add(southpanel, BorderLayout.SOUTH);
		southpanel.add(exit, BorderLayout.SOUTH);
		southpanel.add(broadcast, BorderLayout.NORTH);
		list.setFixedCellWidth(5);
		Vector<String> cVector = new Vector<>(clientMap.keySet());
		list.setListData(cVector);
		broadcast.setBackground(Color.LIGHT_GRAY);
		exit.setBackground(Color.LIGHT_GRAY);
		broadcast.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				String broMsg = JOptionPane.showInputDialog("������㲥��Ϣ��");
				record += "#ϵͳ�㲥@" + broMsg;
				broMsg = "BROADCAST#" + broMsg;
				for (ListenerClient lc : clientMap.values()) {
					lc.pStream.println(broMsg);
				}//��ÿһ���û�����ϵͳ�㲥��Ϣ
			}
		});
		exit.addMouseListener(new MouseAdapter() {//ע�������
			@Override
			public void mouseClicked(MouseEvent e) {
				list.getSelectionModel();
				String select = list.getSelectedValue();//ѡ���б���ĳһ��
				ListenerClient lcc = clientMap.get(select);
				lcc.pStream.println("ATTENTION#ϵͳҪ��5s��ǿ������");//���Ӧ�Ŀͻ��˷���������Ϣ
				clientMap.remove(select);//�Ƴ����û�
				String names = "UPDATE#";
				for (String n : clientMap.keySet()) {
					names = names + n + "@";
				}
				for (ListenerClient lc : clientMap.values()) {
					lc.pStream.println(names);
				}
				Vector<String> cVector = new Vector<>(clientMap.keySet());
				list.setListData(cVector);//�����б�

			}
		});

		try {
			server = new ServerSocket(55533);
			new Thread(this).start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while (true) {
			Socket socket;
			try {
				socket = server.accept();
				new ListenerClient(socket, this);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		new Server();
	}

}

class ListenerClient implements Runnable {
	Server server = null;
	private Socket socket;
	private String name;
	BufferedReader bReader = null;
	PrintStream pStream = null;
	String userName = null;
	String msg = null;

	public ListenerClient(Socket s, Server se) {
		socket = s;
		server = se;
		try {
			bReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			pStream = new PrintStream(socket.getOutputStream());
			userName = bReader.readLine();
			server.clientMap.put(userName, this);
			server.jTextArea.append(userName + "����" + '\n');
			String tempMsg = "USERLIST#";
			for (String s1 : server.clientMap.keySet()) {
				tempMsg = tempMsg + s1 + "@";
			}
			for (ListenerClient lc : server.clientMap.values()) {
				lc.pStream.println(tempMsg);
			}
			Vector<String> cVector = new Vector<>(server.clientMap.keySet());
			server.list.setListData(cVector);
			new Thread(this).start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void groupchat() throws Exception {
		for (ListenerClient lc : server.clientMap.values()) {
			lc.pStream.println(msg);
		}
	}

	public void run() {
		try {
			while (true) {
				msg = bReader.readLine();
				server.jTextArea.append(msg + "\n");
				String[] msgs = msg.split("#");
				if (msgs[0].equals("PRIMSG")) {
					ListenerClient lc = server.clientMap.get(msgs[1]);
					lc.pStream.println(msg);
				} else if (msgs[0].equals("GROUPSET")) {
					String[] names = msgs[2].split("@");
					ArrayList<String> memlist = new ArrayList<>();
					for (String nn : names) {
						if (!nn.equals("")) {
							memlist.add(nn);
						}
					}
					server.groupMap.put(msgs[1], memlist);
				} else if (msgs[0].equals("GROUPMSG")) {
					ArrayList<String> memlist = server.groupMap.get(msgs[1]);
					for (String mem : memlist) {
						ListenerClient lClient = server.clientMap.get(mem);
						lClient.pStream.println(msg);
					}
				} else if (msgs[0].equals("GPMEMLIST")) {
					ArrayList<String> memlist = server.groupMap.get(msgs[1]);
					String names = msgs[0] + "#" + msgs[1] + "#";
					for (String mem : memlist) {
						names = names + mem + "@";
					}
					ListenerClient listenerClient = server.clientMap.get(msgs[2]);
					listenerClient.pStream.println(names);
					System.out.println(names);
				} else if (msgs[0].equals("BROADCAST1")) {
					for (ListenerClient lc : server.clientMap.values()) {
						lc.pStream.println(msg);
					}
					server.record += ("#" + msgs[1] + "@" + msgs[2]);
				} else if (msgs[0].equals("UPDATE")) {
					server.clientMap.remove(msgs[1]);
					String names = msgs[0] + "#";
					for (String n : server.clientMap.keySet()) {
						names = names + n + "@";
					}
					for (ListenerClient lc : server.clientMap.values()) {
						lc.pStream.println(names);
					}
					Vector<String> cVector = new Vector<>(server.clientMap.keySet());
					server.list.setListData(cVector);
				} else if (msgs[0].equals("GETRE")) {
					server.clientMap.get(msgs[1]).pStream.println(server.record);
				}
			}
		} catch (Exception e) {
			e.getStackTrace();
		} finally {

		}

	}
}