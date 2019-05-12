package graphicInterfaces;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import java.awt.Font;
import javax.swing.JButton;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.SwingConstants;

import threads.senders.FileFriendThread;
import threads.senders.MsgFriendThread;

import java.awt.Color;


/**
 * Classe ChatNicknameGUI.
 * Offre una interfaccia grafica della chat verso un utente.
 * Gli oggetti di questa classe vengono mostrati all'utente tramite 
 * l'interfaccia del gestore delle chat (ChatHandlerGUI).
 * Permette di inviare sia messaggi testuali che file.
 * @author Emilio Panti mat:531844 
 */
public class ChatNicknameGUI extends JPanel {

	private static final long serialVersionUID = 1L;
	
	//caratteri max per un messaggio
	protected static int MAX_CAR = 150;
	
	private JTextArea textChat;
	
	//variabile dove salvo questa chat interface per passarla come parametro ai listener 
	private ChatNicknameGUI chatNicknameGUI;
		
	//interfaccia gestore delle chats
	@SuppressWarnings("unused")
	private ChatHandlerGUI chatHandlerGUI;
		
		
	/**
	 * Costruttore classe ChatNicknameGUI.
	 * @param ChatHandlerGUI chatHandlerGUI: interfaccia che raccoglie tutte le chat aperte dell'utente.
	 * @param String nickname: nickname dell'utente con cui è aperta la chat.
	 * @param String msg: primo messaggio ricevuto (se è l'utente a mandare il primo messaggio è null).
	 */
	public ChatNicknameGUI(ChatHandlerGUI chatHandlerGUI, String nickname,String msg) {
		this.chatHandlerGUI = chatHandlerGUI;
		this.chatNicknameGUI = this;
		
		setLayout(null);
		
		//LABEL NICKNAME
		JLabel lblNickname = new JLabel("Nickname ["+nickname+"]:");
		lblNickname.setBounds(10, 11, 430, 22);
		add(lblNickname);
		
		//TEXT AREA CHAT
		textChat = new JTextArea();
		textChat.setFont(new Font("Monospaced", Font.PLAIN, 11));
		textChat.setEditable(false);
		textChat.setLineWrap(true);
		JScrollPane scrolltextChat = new JScrollPane(textChat);
		scrolltextChat.setBounds(10, 32, 430, 192);
		add(scrolltextChat);
		//appendo il messaggio passato da parametro
		if (msg!=null) appendTextChat(msg);
		
		//LABEL INFO
		JLabel lblInfo = new JLabel("Text message (or file name):");
		lblInfo.setFont(new Font("Tahoma", Font.PLAIN, 9));
		lblInfo.setBounds(10, 245, 158, 14);
		add(lblInfo);
		
		//TEXT AREA MSG
		JTextArea textMsg = new JTextArea();
		textMsg.setFont(new Font("Monospaced", Font.PLAIN, 11));
		textMsg.setLineWrap(true);
		JScrollPane scrolltextMsg = new JScrollPane(textMsg);
		scrolltextMsg.setBounds(10, 261, 315, 60);
		add(scrolltextMsg);
		
		//BUTTON MSG
		JButton btnMsg = new JButton("SEND MSG");
		btnMsg.setBounds(335, 262, 105, 23);
		add(btnMsg);
		
		//BUTTON FILE
		JButton btnFile = new JButton("SEND FILE");
		btnFile.setBounds(335, 296, 105, 23);
		add(btnFile);
		
		JLabel lblCheckMsg = new JLabel("");
		lblCheckMsg.setFont(new Font("Tahoma", Font.PLAIN, 9));
		lblCheckMsg.setForeground(Color.RED);
		lblCheckMsg.setHorizontalAlignment(SwingConstants.RIGHT);
		lblCheckMsg.setBounds(185, 245, 140, 14);
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
		
		//LISTENER SEND MSG
		btnMsg.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				String msg = textMsg.getText();
				//controllo che il messaggio scritto non sia più lungo di MAX_CAR
				//caratteri e che non sia vuoto
				if (msg.length()<=MAX_CAR && msg.length()>0) {
					//avvio il thread che si occupa di inviare il messaggio all'amico
					MsgFriendThread msgFriendThread = 
							new MsgFriendThread(chatNicknameGUI,chatHandlerGUI, nickname, msg);
					Thread thread = new Thread(msgFriendThread);
					thread.start();
					
					//ripulisco la textMsg
					textMsg.setText("");
				}
			}
		});
		
		//LISTENER SEND FILE
		btnFile.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				String file = textMsg.getText();
				//controllo che il file name non sia più lungo di MAX_CAR
				//caratteri e che non sia vuoto
				if (file.length()<=MAX_CAR && file.length()>0) {
					//avvio il thread che si occupa di inviare il file all'amico
					FileFriendThread fileFriendThread = 
							new FileFriendThread(chatNicknameGUI,chatHandlerGUI,nickname, file);
					Thread thread = new Thread(fileFriendThread);
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
	public void appendTextChat (String txt) {
		//stringa end of line
		String eol = System.getProperty("line.separator");
		txt = txt + eol;
		
		synchronized(textChat) {
			textChat.append(txt);
		}
	}
}
