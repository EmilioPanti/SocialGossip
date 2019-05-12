package graphicInterfaces;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;

import javax.swing.JFrame;
import javax.swing.border.EmptyBorder;

import client.ClientMain;
import threads.usersOp.CloseThread;

import javax.swing.JTabbedPane;


/**
 * Classe ChatHandlerGUI.
 * Offre una interfaccia grafica che permette all'utente di visionare e
 * controllare tutte le chat che ha aperto verso altri utenti o verso
 * le chatrooms a cui è iscritto.
 * Le chat verso gli utenti sono oggetti della classe ChatNicknameGUI.
 * Le chat verso le chatrooms sono oggetti della classe ChatroomGUI.
 * Un oggetto di questa classe viene creato dopo una operazione di login o 
 * di registrazione effettuata dall'utente.
 * Questa interfaccia grafica diventa visibile all'utente solamente quando
 * c'è almeno una chat attiva.
 * Se una chatroom a cui l'utente è iscritto viene chiusa dal suo creatore
 * questa classe procede alla chiusura della chat relativa.
 * Se l'utente invia un messaggio ad un utente che non è più online la chat
 * verso tale utente viene chiusa in automatico.
 * NB: la chiusura di questa interfaccia grafica da parte dell'utente 
 * 	   causa la terminazione del client.
 * @author Emilio Panti mat:531844 
 */
public class ChatHandlerGUI extends JFrame {

	private static final long serialVersionUID = 1L;
	private JTabbedPane tabbedPane;
		
	//numero delle chat aperte, se = 0 l'interfaccia diventa invisibile
	private int numberChats = 0;
	
	//variabile dove salvo questo gestore chats per passarla come parametro ai listener 
	private ChatHandlerGUI chatHandlerGUI;
		
	//multicast socket del client, usato per disiscrivermi dalle chatroom quando 
	//vengono chiuse
	private MulticastSocket ms = null;
		
	
	/**
	 * Costruttore classe ChatHandlerGUI.
	 */
	public ChatHandlerGUI() {
		chatHandlerGUI = this;
		ms = ClientMain.MS;
		
		setTitle("CHAT HANDLER: [" + ClientMain.NICKNAME + "]");
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		//creo un window listener per personalizzare la gestione della chiusura dell'interfaccia
		WindowListener exitListener = new WindowAdapter() {
		    @Override
		    public void windowClosing(WindowEvent e) {
		    	//faccio partire il thread che si occupa di chiudere la connessione e 
		    	//terminare il client
		    	CloseThread closeThread = new CloseThread();
				Thread thread = new Thread(closeThread);
				thread.start();
		    }
		};
		addWindowListener(exitListener);
		
		setBounds(100, 100, 483, 407);
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(tabbedPane);
	}
	
	
	/**
	 * Aggiunge una chat verso l'utente che ha per nickname il parametro passato.
	 * @param String nickname: stringa contenente il nickname dell'utente.
	 * @param String msg: primo messaggio ricevuto (se è l'utente a mandare il primo messaggio è null)
	 */
	synchronized public void addChatNickname (String nickname,String msg) {
		int index = tabbedPane.indexOfTab("U:["+nickname+"]");
		//se non c'è già una chat aperta verso tale utente
		if (index==-1) {
			tabbedPane.addTab("U:["+nickname+"]",new ChatNicknameGUI(chatHandlerGUI,nickname,msg));
			if(numberChats==0) this.setVisible(true);
			numberChats++;
		}
	}
	
	
	/**
	 * Aggiunge una chat verso la chatroom passata da parametro.
	 * @param String chatroom: stringa contenente il nome della chatroom.
	 * @param InetAddress address: indirizzo multicast della chatroom.
	 */
	synchronized public void addChatroom (String chatroom, InetAddress address) {
		int index = tabbedPane.indexOfTab("C:["+chatroom+"]");
		//se non c'è già una chat aperta verso tale chatroom
		if (index==-1) {
			tabbedPane.addTab("C:["+chatroom+"]",new ChatroomGUI(chatroom,address));
			if(numberChats==0) this.setVisible(true);
			numberChats++;
		}
	}
	
	
	/**
	 * Rimuove una chat verso l'utente che ha per nickname il parametro passato.
	 * @param String nickname: stringa contenente il nickname dell'utente.
	 */
	synchronized public void removeChatNickname (String nickname) {
		int index = tabbedPane.indexOfTab("U:["+nickname+"]");
		//se esiste una chat verso tale utente
		if (index!=-1) {
			tabbedPane.remove(index);
			numberChats--;
			if(numberChats==0) this.setVisible(false);
		}
	}
	
	
	/**
	 * Rimuove la chat relativa alla chatroom passata da parametro.
	 * @param String chatroom: stringa contenente il nome della chatroom.
	 * @param InetAddress address: address della chatroom.
	 */
	synchronized public void removeChatroom (String chatroom,InetAddress address) {
		int index = tabbedPane.indexOfTab("C:["+chatroom+"]");
		//se esiste una chat verso tale chatroom
		if (index!=-1) {
			try {
				ms.leaveGroup(address);
			} catch (IOException e) {
				e.printStackTrace();
			}
			tabbedPane.remove(index);
			numberChats--;
			if(numberChats==0) this.setVisible(false);
		}
	}
	
	
	/**
	 * Posta il messaggio ricevuto da un utente nella relativa chat.
	 * Se la chat non è stata ancora aperta viene creata ed aggiunta a tabbedPane
	 * @param String nickname: stringa contenente il nickname dell'utente.
	 * @param String msg: messaggio da postare nella chat.
	 */
	public void postMsgChatNickname (String nickname,String msg) {
		ChatNicknameGUI chat = null;
		synchronized (this) {
			//controllo se esiste o meno una chat aperta verso tale utente
			int index = tabbedPane.indexOfTab("U:["+nickname+"]");
			if (index!=-1) chat = (ChatNicknameGUI) tabbedPane.getComponentAt(index);
		}
		//se ho già una chat verso tale utente mando il messaggio
		if (chat!=null) chat.appendTextChat(msg);
		//altrimenti creo una chat verso tale utente
		else  addChatNickname (nickname,msg);
	}
	
	
	/**
	 * Posta il messaggio ricevuto da una chatroom nella relativa chat.
	 * E' possibile che un messaggio ricevuto da una chatroom venga gestito 
	 * dopo una sua eventuale chiusura, in questo caso scarto il messaggio.
	 * @param String chatroom: stringa contenente il nome della chatroom.
	 * @param String msg: messaggio da postare nella chat.
	 */
	public void postMsgChatroom (String chatroom,String msg) {
		ChatroomGUI chat = null;
		synchronized (this) {
			//controllo se esiste o meno una chat aperta della relativa chatroom
			int index = tabbedPane.indexOfTab("C:["+chatroom+"]");
			if (index!=-1) chat = (ChatroomGUI) tabbedPane.getComponentAt(index);
		}
		//se ho una chat aperta relativa a tale chatroom
		if (chat!=null) chat.appendTextChat(msg);
	}
	
}
