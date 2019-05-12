package graphicInterfaces;

import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.InetAddress;
import java.awt.Color;
import javax.swing.SwingConstants;

import threads.senders.MsgCRThread;


/**
 * Classe ChatroomGUI.
 * Offre una interfaccia grafica della chat riguardante una chatroom
 * a cui l'utente è iscritto.
 * Gli oggetti di questa classe vengono mostrati all'utente tramite 
 * l'interfaccia del gestore delle chat (ChatHandlerGUI).
 * Permette di inviare messaggi testuali alla chatroom.
 * @author Emilio Panti mat:531844 
 */
public class ChatroomGUI extends JPanel {

	private static final long serialVersionUID = 1L;
	private JTextArea textChat;

	//caratteri max per un messaggio
	protected static int MAX_CAR = 150;
		
		
	/**
	 * Costruttore classe ChatroomGUI.
	 * @param String chatroom: nome della chatroom.
	 * @param InetAddress address: inet address del gruppo multicast della chatroom.
	 */
	public ChatroomGUI(String chatroom,InetAddress address) {
		setLayout(null);
		
		//LABEL CHATROOM
		JLabel lblChatroom = new JLabel("Chatroom ["+chatroom+"]:");
		lblChatroom.setBounds(10, 11, 430, 22);
		add(lblChatroom);
		
		//TEXT AREA CHAT
		textChat = new JTextArea();
		textChat.setFont(new Font("Monospaced", Font.PLAIN, 11));
		textChat.setEditable(false);
		textChat.setLineWrap(true);
		JScrollPane scrolltextChat = new JScrollPane(textChat);
		scrolltextChat.setBounds(10, 32, 430, 192);
		add(scrolltextChat);
		
		//LABEL INFO
		JLabel lblInfo = new JLabel("Text message:");
		lblInfo.setFont(new Font("Tahoma", Font.PLAIN, 9));
		lblInfo.setBounds(10, 245, 105, 14);
		add(lblInfo);
		
		//TEXT AREA MSG
		JTextArea textMsg = new JTextArea();
		textMsg.setFont(new Font("Monospaced", Font.PLAIN, 11));
		textMsg.setLineWrap(true);
		JScrollPane scrolltextMsg = new JScrollPane(textMsg);
		scrolltextMsg.setBounds(10, 261, 315, 60);
		add(scrolltextMsg);
		
		//BUTTON SEND
		JButton btnSend = new JButton("SEND");
		btnSend.setBounds(335, 278, 105, 23);
		add(btnSend);
		
		JLabel lblCheckMsg = new JLabel("");
		lblCheckMsg.setFont(new Font("Sylfaen", Font.PLAIN, 9));
		lblCheckMsg.setHorizontalAlignment(SwingConstants.RIGHT);
		lblCheckMsg.setForeground(Color.RED);
		lblCheckMsg.setBounds(162, 245, 163, 14);
		add(lblCheckMsg);
		
		//------------------------ALL LISTENER---------------------------------
		
		//LISTENER FIELD NICKNAME
		textMsg.addMouseListener(new MouseAdapter() {
			//quando 'entro' nell'area di textMsg con il mouse
			@Override
			public void mouseEntered(MouseEvent e) {
				lblCheckMsg.setText("(max "+MAX_CAR+" characters)");
			}
			//quando 'esco' dall'area di textMsg con il mouse
			@Override
			public void mouseExited(MouseEvent e) {
				//controllo che il messaggio scritto non sia più lungo di MAX_CAR caratteri
				if (textMsg.getText().length() <= MAX_CAR) lblCheckMsg.setText("");
				else lblCheckMsg.setText("Message too long!");
			}
		});
				
		//LISTENER SEND
		btnSend.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				String msg = textMsg.getText();
				//controllo che il messaggio scritto non sia più lungo di MAX_CAR
				//caratteri e che non sia vuoto
				if (msg.length()<=MAX_CAR && msg.length()>0) {
					//avvio il thread che si occupa di inviare il messaggio
					//alla chatroom
					MsgCRThread msgCRThread = 
							new MsgCRThread(chatroom, address, msg);
					Thread thread = new Thread(msgCRThread);
					thread.start();
					
					//ripulisco la textMsg
					textMsg.setText("");
				}
			}
		});
	}
	
	/**
	 * Metodo per appendere una stringa all'area di testo della chat.
	 * @param String txt: stringa da appendere.
	 */
	public void appendTextChat(String txt) {
		//stringa end of line
		String eol = System.getProperty("line.separator");
		txt = txt + eol;
				
		synchronized(textChat) {
			textChat.append(txt);
		}
	}

}
