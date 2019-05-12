package graphicInterfaces;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import client.ClientMain;
import enumerations.Operations;
import threads.chatroomsOp.CRListThread;
import threads.friendsOp.FriendsListThread;
import threads.usersOp.CloseThread;

import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.JButton;
import java.awt.Font;
import javax.swing.JScrollPane;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;


/**
 * Classe OperativeGUI.
 * Offre l'interfaccia grafica operativa del servizio SocialGossip.
 * Permette all'utente di effettuare le operazioni di:
 * - aggiornamento lista amici,
 * - look up di un nickname,
 * - amicizia 
 * - apertura chat verso un amico,
 * - aggiornamento lista chatrooms,
 * - creazione chatroom,
 * - iscrizione ad una chatroom,
 * - chiusura di una chatroom.
 * Mostra all'utente:
 * - la lista amici,
 * - la lista delle chatrooms (indicando a quali di esse è iscritto),
 * - le notifiche che lo rigurdano.
 * Un oggetto di questa classe viene creato dopo una operazione di login o 
 * di registrazione effettuata dall'utente.
 * NB: la chiusura di questa interfaccia grafica da parte dell'utente 
 * 	   causa la terminazione del client.
 * @author Emilio Panti mat:531844 
 */
public class OperativeGUI extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextArea textFriendsList;
	private JTextArea textChatrooms;
	private JTextArea textNotifications;
	private JButton btnUpdatesFriends;
	private JButton btnUpdatesChatrooms;
	
	//Interfaccia che gestisce le chat
	@SuppressWarnings("unused")
	private ChatHandlerGUI chatHandlerGUI;
	
	//variabile dove salvo questa interaccia grafica per poi passarla
	//come parametro anche all'interno dei vari listener
	private OperativeGUI operativeGUI;
		
	//nickname dell'utente
	private String nickname;
	
	/**
	 * Costruttore classe OperativeGUI.
	 * @param: ChatHandlerGUI chatHandlerGUI: gestore delle chat aperte.
	 */
	public OperativeGUI(ChatHandlerGUI chatHandlerGUI) {
		this.operativeGUI = this;
		this.chatHandlerGUI = chatHandlerGUI;
		nickname = ClientMain.NICKNAME;
		
		setTitle("SOCIAL GOSSIP: ["+nickname+"]");
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		//creo un window listener per personalizzare la gestione della chiusura dell'interfaccia
		WindowListener exitListener = new WindowAdapter() {
		    @Override
		    public void windowClosing(WindowEvent e) {
		    	//faccio partire il thread che si occupa di chiudere la connessione e il client
		    	CloseThread closeThread = new CloseThread();
				Thread thread = new Thread(closeThread);
				thread.start();
		    }
		};
		addWindowListener(exitListener);
		
		setBounds(100, 100, 765, 404);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		//LABEL FRIENDS LIST
		JLabel lblFriendsList = new JLabel("Friends list:");
		lblFriendsList.setBounds(40, 27, 70, 14);
		contentPane.add(lblFriendsList);
		
		//BUTTON UPDATES LIST FRIENDS
		btnUpdatesFriends = new JButton("Updates");
		btnUpdatesFriends.setBounds(107, 23, 92, 23);
		contentPane.add(btnUpdatesFriends);
		
		//TEXT AREA FRIENDS LIST
		textFriendsList = new JTextArea();
		textFriendsList.setEditable(false);
		JScrollPane scrollFriendsList = new JScrollPane(textFriendsList);
		textFriendsList.setFont(new Font("Monospaced", Font.PLAIN, 11));
		scrollFriendsList.setBounds(40, 52, 159, 200);
		contentPane.add(scrollFriendsList);
		
		//BUTTON ADD FRIEND
		JButton btnAddNewFriend = new JButton("Add new friend");
		btnAddNewFriend.setBounds(40, 260, 159, 23);
		contentPane.add(btnAddNewFriend);
		
		//BUTTON LOOK-UP NICKNAME
		JButton btnLookUpNickname = new JButton("Look-up nickname");
		btnLookUpNickname.setBounds(40, 286, 159, 23);
		contentPane.add(btnLookUpNickname);
		
		//BUTTON START CHAT
		JButton btnStartChat = new JButton("START CHAT");
		btnStartChat.setBounds(40, 313, 159, 23);
		contentPane.add(btnStartChat);
		
		//SEPARATOR
		JSeparator separator_1 = new JSeparator();
		separator_1.setOrientation(SwingConstants.VERTICAL);
		separator_1.setBounds(239, 0, 2, 394);
		contentPane.add(separator_1);
		
		//LABEL CHATROOMS
		JLabel lblChatrooms = new JLabel("Chatrooms:");
		lblChatrooms.setBounds(281, 27, 70, 14);
		contentPane.add(lblChatrooms);
		
		//BUTTON UPDATES CHATROOMS
		btnUpdatesChatrooms = new JButton("Updates");
		btnUpdatesChatrooms.setBounds(348, 23, 92, 23);
		contentPane.add(btnUpdatesChatrooms);
		
		//TEXT AREA CHATROOMS
		textChatrooms = new JTextArea();
		textChatrooms.setEditable(false);
		JScrollPane scrollChatrooms = new JScrollPane(textChatrooms);
		textChatrooms.setFont(new Font("Monospaced", Font.PLAIN, 11));
		scrollChatrooms.setBounds(281, 52, 159, 200);
		contentPane.add(scrollChatrooms);
		
		//BUTTON CREATE CHATROOM
		JButton btnCreateChatroom = new JButton("Create chatroom");
		btnCreateChatroom.setBounds(281, 260, 159, 23);
		contentPane.add(btnCreateChatroom);
		
		//BUTTON ADD TO CHATROOM
		JButton btnAddToChatroom = new JButton("Add to a chatroom");
		btnAddToChatroom.setBounds(281, 286, 159, 23);
		contentPane.add(btnAddToChatroom);
		
		//BUTTON CLOSE CHATROOM
		JButton btnCloseChatroom = new JButton("Close chatroom");
		btnCloseChatroom.setBounds(281, 313, 159, 23);
		contentPane.add(btnCloseChatroom);
		
		//SEPARATOR
		JSeparator separator = new JSeparator();
		separator.setOrientation(SwingConstants.VERTICAL);
		separator.setBounds(481, 0, 2, 394);
		contentPane.add(separator);
		
		//LABEL NOTIFICATIONS
		JLabel lblNewNotifications = new JLabel("New notifications:");
		lblNewNotifications.setBounds(523, 27, 108, 14);
		contentPane.add(lblNewNotifications);
				
		//TEXT AREA NOTIFICATIONS
		textNotifications = new JTextArea();
		textNotifications.setEditable(false);
		JScrollPane scrollNotification = new JScrollPane(textNotifications);
		textNotifications.setFont(new Font("Monospaced", Font.PLAIN, 11));
		scrollNotification.setBounds(523, 52, 188, 284);
		contentPane.add(scrollNotification);
		
		
		//------------------------ALL LISTENER---------------------------------
		
		
		//LISTENER UPDATES FRIENDS LIST
		btnUpdatesFriends.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				//disabilito il bottone 
				btnUpdatesFriends.setEnabled(false);
				
				//attivo il thread che si occupa di richiedere la lista amici
				FriendsListThread friendsListThread = 
						new FriendsListThread(operativeGUI);
				Thread thread = new Thread(friendsListThread);
				thread.start();
			}
		});
		
		
		//LISTENER ADD FRIEND
		btnAddNewFriend.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				//apro l'interfaccia per la richiesta e passo al costruttore la relativa operazione
				RequestGUI frame = new RequestGUI(Operations.FRIENDSHIP,operativeGUI,chatHandlerGUI);
				frame.setVisible(true);
			}
		});
		
		
		//LISTENER LOOK-UP NICKNAME
		btnLookUpNickname.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				//apro l'interfaccia per la richiesta e passo al costruttore la relativa operazione
				RequestGUI frame = new RequestGUI(Operations.LOOKUP,operativeGUI,chatHandlerGUI);
				frame.setVisible(true);
			}
		});
				
				
		//LISTENER START CHAT
		btnStartChat.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				//apro l'interfaccia per la richiesta e passo al costruttore la relativa operazione
				RequestGUI frame = new RequestGUI(Operations.STARTCHAT,operativeGUI,chatHandlerGUI);
				frame.setVisible(true);
			}
		});
				
		
		//LISTENER UPDATES CHATROOMS LIST
		btnUpdatesChatrooms.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				//disabilito il bottone 
				btnUpdatesChatrooms.setEnabled(false);
				
				//attivo il thread che si occupa di chiedere la lista amici
				CRListThread cRListThread = 
						new CRListThread(operativeGUI);
				Thread thread = new Thread(cRListThread);
				thread.start();
			}
		});
				
				
		//LISTENER CREATE CHATROOM
		btnCreateChatroom.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				//apro l'interfaccia per la richiesta e passo al costruttore la relativa operazione
				RequestGUI frame = new RequestGUI(Operations.CREATE_CHATROOM,operativeGUI,chatHandlerGUI);
				frame.setVisible(true);
			}
		});
			
		
		//LISTENER ADD TO CHATROOM
		btnAddToChatroom.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				//apro l'interfaccia per la richiesta e passo al costruttore la relativa operazione
				RequestGUI frame = new RequestGUI(Operations.ADDME_CHATROOM,operativeGUI,chatHandlerGUI);
				frame.setVisible(true);
			}
		});
				
				
		//LISTENER CLOSE CHATROOM
		btnCloseChatroom.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				//apro l'interfaccia per la richiesta e passo al costruttore la relativa operazione
				RequestGUI frame = new RequestGUI(Operations.CLOSE_CHATROOM,operativeGUI,chatHandlerGUI);
				frame.setVisible(true);
			}
		});
		
	}
	
	
	/**
	 * Metodo per scrivere nell'area di testo relativa alle amicizie.
	 * @param String txt: stringa contenente il testo da scrivere.
	 */
	public void setTextFriends (String txt) {
		synchronized(textFriendsList) {
			textFriendsList.setText(txt);
		}
	}
	
	
	/**
	 * Metodo per scrivere nell'area di testo relativa alle chatrooms.
	 * @param String txt: stringa contenente il testo da scrivere.
	 */
	public void setTextChatrooms (String txt) {
		synchronized(textChatrooms) {
			textChatrooms.setText(txt);
		}
	}
	
	
	/**
	 * Metodo per appendere una stringa all'area di testo relativa alle amicizie.
	 * @param String txt: stringa da appendere.
	 */
	public void appendTextFriends (String txt) {
		//stringa end of line
		String eol = System.getProperty("line.separator");
		txt = txt + eol;
				
		synchronized(textFriendsList) {
			textFriendsList.append(txt);
		}
	}
	
	
	/**
	 * Metodo per appendere una stringa all'area di testo relativa alle chatrooms.
	 * @param String txt: stringa da appendere.
	 */
	public void appendTextChatrooms (String txt) {
		//stringa end of line
		String eol = System.getProperty("line.separator");
		txt = txt + eol;
		
		synchronized(textChatrooms) {
			textChatrooms.append(txt);
		}
	}
	

	/**
	 * Metodo per appendere una stringa all'area di testo relativa alle notifiche.
	 * @param String txt: stringa da appendere.
	 */
	public void appendTextNotifications (String txt) {
		//stringa end of line
		String eol = System.getProperty("line.separator");
		txt = txt + eol;
				
		synchronized(textNotifications) {
			textNotifications.append(txt);
		}
	}
	
	
	/**
	 * Attiva il bottone per la richiesta della lista amici.
	 */
	public void enabledBtnUpdatesFriends() {
		btnUpdatesFriends.setEnabled(true);
	}
	
	
	/**
	 * Attiva il bottone per la richiesta della lista chatrooms.
	 */
	public void enabledBtnUpdatesChatrooms() {
		btnUpdatesChatrooms.setEnabled(true);
	}
}
