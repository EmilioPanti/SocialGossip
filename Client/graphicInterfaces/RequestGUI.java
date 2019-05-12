package graphicInterfaces;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import client.ClientMain;
import enumerations.Operations;
import threads.chatroomsOp.AddToCRThread;
import threads.chatroomsOp.CloseCRThread;
import threads.chatroomsOp.CreateCRThread;
import threads.friendsOp.FriendshipThread;
import threads.friendsOp.LookUpThread;
import threads.friendsOp.StartChatThread;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


/**
 * Classe RequestGUI.
 * Interfaccia grafica per effettuare una richiesta al server
 * di SocialGossip.
 * @author Emilio Panti mat:531844 
 */
public class RequestGUI extends JFrame {

	//lunghezza massima per i campi nickname e password
	static int MAX_LENGHT = 16;
		
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField textField;
	
	
	/**
	 * Costruttore classe RequestGUI.
	 * @param Operations op: operazione richiesta dall'utente.
	 * @param OperativeGUI operativeGUI: interfaccia operativa del servizio.
	 * @param ChatHandlerGUI chatHandlerGUI: interfaccia gestore delle chat.
	 */
	public RequestGUI(Operations op,OperativeGUI operativeGUI,
			ChatHandlerGUI chatHandlerGUI) {
		
		setTitle("Request");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 268, 126);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		//LABEL
		JLabel lbl = new JLabel("Nickname:");
		lbl.setBounds(20, 25, 85, 14);
		contentPane.add(lbl);
		
		//FIELD 
		textField = new JTextField();
		textField.setBounds(98, 22, 133, 20);
		contentPane.add(textField);
		textField.setColumns(10);
		
		//BUTTON
		JButton btnNewButton = new JButton("New button");
		btnNewButton.setBounds(59, 53, 133, 23);
		contentPane.add(btnNewButton);
		
		//in base alla operazione richiesta viene generata un interfaccia grafica diversa
		switch(op) {

			case FRIENDSHIP:
				btnNewButton.setText("ADD");
				break;

			case LOOKUP:
				btnNewButton.setText("LOOK UP");
				break;
		
			case STARTCHAT:
				btnNewButton.setText("START CHAT");
				break; 
				
			case CREATE_CHATROOM:
				lbl.setText("Chatroom:");
				btnNewButton.setText("CREATE");
				break;

			case ADDME_CHATROOM:
				lbl.setText("Chatroom:");
				btnNewButton.setText("ADD ME");
				break;
		
			case CLOSE_CHATROOM:
				lbl.setText("Chatroom:");
				btnNewButton.setText("CLOSE");
				break; 
		
			default: 
				dispose();
				break; 
		}
		
		//------------------------ALL LISTENER---------------------------------
		
		//LISTENER BUTTON 
		btnNewButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				//prendo cosa ha scritto l'utente
				String txt = textField.getText();
				
				//se txt non è vuoto
				if(txt.length()>0) {
					
					//se txt è più lungo di MAX_LENGHT
					if(txt.length()>MAX_LENGHT) {
						//comunico all'utente l'errore
						if(op==Operations.FRIENDSHIP || op==Operations.LOOKUP ||
								op==Operations.STARTCHAT) {
							ResponseGUI responseGUI = new ResponseGUI("The nickname is too long!");
							responseGUI.setVisible(true);
						}
						else {
							ResponseGUI responseGUI = new ResponseGUI("The chatroom is too long!");
							responseGUI.setVisible(true);
						}
					}
					//se l'utente prova ad aggiungere, cercare, chattare con sè stesso
					else if(checkTxt(op,txt)==false) {
						ResponseGUI responseGUI = new ResponseGUI("You can't insert your nickname!");
						responseGUI.setVisible(true);
					}
					//se tutto è corretto
					else {
						//in base alla operazione richiesta attivo il thread relativo
						switch(op) {
		
							case FRIENDSHIP:
								//faccio partire il thread che esegue la richiesta di amicizia
								FriendshipThread friendshipThread = new FriendshipThread(txt);
								Thread thread = new Thread(friendshipThread);
								thread.start();
								break;
		
							case LOOKUP:
								//faccio partire il thread che esegue la richiesta di 
								//ricerca utente
								LookUpThread lookUpThread = new LookUpThread(txt);
								Thread thread2 = new Thread(lookUpThread);
								thread2.start();
								break;
						
							case STARTCHAT:
								//faccio partire il thread che esegue la richiesta di 
								//aprere una nuova chat verso un utente
								StartChatThread startChatThread = 
									new StartChatThread(chatHandlerGUI,txt);
								Thread thread3 = new Thread(startChatThread);
								thread3.start();
								break; 
								
							case CREATE_CHATROOM:
								//faccio partire il thread che esegue la richiesta 
								//di creare una nuova chatroom
								CreateCRThread createCRThread = 
									new CreateCRThread(operativeGUI,chatHandlerGUI,txt);
								Thread thread4 = new Thread(createCRThread);
								thread4.start();
								break;
		
							case ADDME_CHATROOM:
								//faccio partire il thread che esegue la richiesta di 
								//aggiunta ad una chatroom
								AddToCRThread addToCRThread = 
									new AddToCRThread(chatHandlerGUI,txt);
								Thread thread5 = new Thread(addToCRThread);
								thread5.start();
								break;
						
							case CLOSE_CHATROOM:
								//faccio partire il thread che esegue la richiesta di chiusura
								//chatroom
								CloseCRThread closeCRThread = new CloseCRThread(txt);
								Thread thread6 = new Thread(closeCRThread);
								thread6.start();
								break; 
						
							default: 
								break; 
						}
					}
					dispose();
				}
			}
		});
	}
	
	
	/**
	 * Metodo per controllare che il testo passato da parametro non sia 
	 * uguale al nickname dell'utente, per evitare che tenti di aggiungere,
	 * chattare o cercare sè stesso.
	 * @param Operations op: operazione richiesta dall'utente.
	 * @param String txt: testo inserito dall'utente nella richiesta.
	 * @return boolean: true, se non ha inserito il suo stesso nickname nella richiesta,
	 * 					false, altrimenti.
	 */
	private boolean checkTxt(Operations op, String txt) {
		//se è una operazione verso un altro utente
		if(op==Operations.FRIENDSHIP || op==Operations.LOOKUP ||
				op==Operations.STARTCHAT) {
			if(ClientMain.NICKNAME.equals(txt)) return false;
			else return true;
		}
		//se è una operazione verso una chatroom
		else return true;
	}
}
