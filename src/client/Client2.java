package client;

import java.awt.Dimension;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.BorderFactory;
import javax.swing.Box;

public class Client2 extends JFrame {
	String clientName = null;
	JTextPane text = new JTextPane();
	JScrollPane chatpart = new JScrollPane(text);// 聊天记录
	Box box = null;
	JButton b_icon, b_send, b_remove = null;
	JTextField typepart = new JTextField();// 文本输入
	Vector<String> cVector = new Vector<>();
	JList<String> jList = new JList<>(cVector);// 在线列表

	public Client2() {
		this.setSize(700,700);
		this.setTitle("<聊天室>--");
		this.setVisible(true);
		
		text.setEditable(false);
		chatpart.setPreferredSize(new Dimension(400, 400));
		typepart = new JTextField(18);

		b_icon = new JButton("图片");
		b_send = new JButton("发送");
		b_remove = new JButton("清空");

		box = Box.createVerticalBox();
		Box box_1 = Box.createHorizontalBox();
		Box box_2 = Box.createHorizontalBox();
		box.add(box_1);
		box_1.add(Box.createHorizontalStrut(4));
		box.add(box_2);
		box_2.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

		box_1.add(chatpart);
		box_1.add(jList);
		
		box_2.add(text);
		box_2.add(b_icon);
		box_2.add(b_remove);
		box_2.add(b_send);
        this.add(box);
	}

	public static void main(String args[]) {
		new Client2();
	}
}
