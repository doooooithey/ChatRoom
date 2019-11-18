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
	JScrollPane chatpart = new JScrollPane(text);// �����¼
	Box box = null;
	JButton b_icon, b_send, b_remove = null;
	JTextField typepart = new JTextField();// �ı�����
	Vector<String> cVector = new Vector<>();
	JList<String> jList = new JList<>(cVector);// �����б�

	public Client2() {
		this.setSize(700,700);
		this.setTitle("<������>--");
		this.setVisible(true);
		
		text.setEditable(false);
		chatpart.setPreferredSize(new Dimension(400, 400));
		typepart = new JTextField(18);

		b_icon = new JButton("ͼƬ");
		b_send = new JButton("����");
		b_remove = new JButton("���");

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
