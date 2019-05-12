package threads.usersOp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import client.ClientMain;
import enumerations.Operations;
import graphicInterfaces.ChatHandlerGUI;
import graphicInterfaces.LoginGUI;
import graphicInterfaces.OperativeGUI;
import graphicInterfaces.ResponseGUI;
import notificationRMI.NotifyEventImpl;
import notificationRMI.NotifyEventInterface;
import notificationRMI.ServerInterface;
import threads.listeners.ListenerCR;
import threads.listeners.ListenerFile;
import threads.listeners.ListenerMsg;


/**
 * Classe LoginThread.
 * Thread che richiede al server (e ne gestisce la risposta)
 * il login dell'utente al servizio SocialGossip.
 * Se l'operazione va a buon fine vengono create l'interfaccia
 * operativa (oggetto della classe OperativeGUI) e l'interfaccia
 * che gestisce le chat aperte (oggetto della classe ChatHandlerGUI).
 * Il server, in caso di esito positivo, allega alla risposta 
 * anche la lista amici dell'utente e la lista di tutte le chatrooms
 * aperte (specificando a quali di esse l'utente sia già iscritto).
 * Infine (sempre in caso di esito positivo) fa partire i thread che
 * si occupano della ricezione di:
 * - messaggi testuali provenienti da altri utenti (oggetto della classe
 *   ListenerMsg),
 * - file inviati da altri utenti (oggetto della classe ListenerFile),
 * - messaggi provenienti dalle varie chatrooms a cui l'utente è
 *   iscritto (oggetto della classe ListenerCR).
 * Crea anche uno stub e lo registra sul server per ricevere le notifiche
 * riguardanti l'utente.
 * @author Emilio Panti mat:531844 
 */
public class LoginThread implements Runnable {
	
	//nickname e psw
	private String nickname;
	private String psw;
	
	//Interfacce attive al momento in cui viene invocato questo thread
	private LoginGUI loginGUI;
	
	//interfacce che verranno create dal thread se l'operazione di login andrà bene
	private ChatHandlerGUI chatHandlerGUI;
	private OperativeGUI operativeGUI;
	
	
	/**
	 * Costruttore classe LoginThread.
	 * @param LoginGUI loginGUI: interfaccia di login che ha fatto partire
	 * 							 questo thread.
	 * @param String nickname: nickname che ha inserito l'utente per loggarsi.
	 * @param String psw: password che ha inserito l'utente per loggarsi.
	 */
	public LoginThread(LoginGUI loginGUI, String nickname, String psw) {
		this.loginGUI = loginGUI;
		this.nickname = nickname;
		this.psw = psw;
	}


	/**
	 * Task che esegue l'operazione di login
	 */
	@SuppressWarnings("unchecked")
	public void run() {
		
		//prendo gli streams per comunicare con il server
		DataOutputStream writer = ClientMain.WRITER;
		DataInputStream reader = ClientMain.READER;
		
		//creo la richiesta di login in formato JSON
		JSONObject request = new JSONObject ();
		request.put("OP", "LOG");
		request.put("ID", nickname);
		request.put("PSW", psw);
		
		//variabili per gestire la riposta del server
		String response = null;
		JSONObject responseJSON = null;
		
		
		try {
			//mando la richiesta
			writer.writeUTF(request.toJSONString());
			writer.flush();
			
			//ricevo la risposta
			response = reader.readUTF();
			
			//trasformo in formato JSON la risposta ricevuta dal server
			responseJSON = 	(JSONObject) new JSONParser().parse(response);
			
		} catch (Exception e) {
			//se c'è qualche problema di comunicazione con il server
			//o se non è possibile parsare il messaggio ricevuto
			e.printStackTrace();
			//termino il client
			ClientMain.cleanUp();
		}

		
		//controllo l'esito della risposta
		Operations esito = Operations.valueOf((String) responseJSON.get("OP"));
		if (esito==Operations.OP_OK) {
			
			//salvo il nickname in ClientMain
			ClientMain.NICKNAME = nickname;
			
			//creo la operative interface e l'interfaccia gestore delle chat
			chatHandlerGUI = new ChatHandlerGUI();
			operativeGUI = new OperativeGUI(chatHandlerGUI);
			
			//mostro all'utente l'interfaccia operativa e chiudo quella di login
			operativeGUI.setVisible(true);
			loginGUI.dispose();
			
			//prendo la lista amici e la appendo nell'area di text dell'interfaccia operativa
			String friends = (String) responseJSON.get ("FRIENDS");
			if (friends!=null) operativeGUI.setTextFriends(friends);
			
			//prendo la lista delle chatrooms,la appendo nell'area di text dell'interfaccia
			//operativa e mi connetto a quelle a cui sono già iscritto 
			JSONArray chatrooms = (JSONArray) responseJSON.get("CHATROOMS"); 
			if (chatrooms!=null) connectToChatrooms(chatrooms);
			
			//creazione e registrazione dello stub per ricevere le notifiche dal server
			NotifyEventInterface callbackObj;
			try {
				callbackObj = new NotifyEventImpl(chatHandlerGUI,operativeGUI);
				NotifyEventInterface stub = (NotifyEventInterface) UnicastRemoteObject.exportObject(callbackObj, 0);
				ServerInterface serverRMI = ClientMain.SERVER_RMI;
				serverRMI.registerForCallback(stub,nickname);
			} catch (RemoteException e) {
				e.printStackTrace();
				//termino il client
				ClientMain.cleanUp();
			}
			
			//faccio partire il thread che si occupa di ricevere i messaggi dagli altri utenti
			ListenerMsg listenerMsg = new ListenerMsg(chatHandlerGUI, nickname);
			Thread thread = new Thread(listenerMsg);
			thread.start();
			
			//faccio partire il thread che si occupa di ricevere i file dagli altri utenti
			ListenerFile listenerFile = new ListenerFile(chatHandlerGUI);
			Thread thread2 = new Thread(listenerFile);
			thread2.start();
			
			//faccio partire il thread che si occupa di ricevere i messaggi dalle chatrooms
			ListenerCR listenerChatrooms = new ListenerCR(chatHandlerGUI);
			Thread thread3 = new Thread(listenerChatrooms);
			thread3.start();
			
			//comunico all'utente la riuscita dell'operazione
			ResponseGUI responseGUI = new ResponseGUI("Your login was successful");
			responseGUI.setVisible(true);
			
		}
		//c'è stato un errore
		else {
			//prendo il messaggio di errore trasmesso dal server
			String msgErr = (String) responseJSON.get ("MSG");
			
			//riattivo il bottone per il login nella interfaccia di login
			loginGUI.enabledBtnLogin();
			
			//apro l'interfaccia grafica per comunicare l'errore
			ResponseGUI responseGUI = new ResponseGUI(msgErr);
			responseGUI.setVisible(true);
		}
	}
	
	
	/**
	 * Metodo che prende una lista di chatrooms in formato JSON.
	 * Per quelle a cui è già iscritto l'utente si registra ai relativi gruppi multicast.
	 * Alla fine appende nell'interfaccia operativa la lista delle chatroom 
	 * esistenti al momento del login (specificando a quali è iscritto).
	 * @param JSONArray chatrooms : lista delle chatrooms in formato JSON.
	 */
	private void connectToChatrooms(JSONArray chatrooms) {
		//multicast socket per registrare l'utente alle chatrooms a cui è già iscritto
		MulticastSocket ms = ClientMain.MS;
		
		//Stringa dove inserisco il nome delle chatrooms (e a quali l'utente è iscritto)
		String listChatrooms = "";
		
		//stringa end of line
		String eol = System.getProperty("line.separator");
		
		//creo un iteratore per scorrere la lista delle chatrooms
		@SuppressWarnings("unchecked")
		Iterator<JSONObject> iterator = chatrooms.iterator();
		
		while (iterator.hasNext()) {
			//prendo un oggetto json della lista
			JSONObject chatroomJSON = iterator.next();
			
			//prendo il nome della chatroom
			String chatroom = (String) chatroomJSON.get("ID");
			//indirizzo multicast della chatroom
			String address = (String) chatroomJSON.get("ADDRESS");
			//boolean che mi dice se l'utente è iscritto ad esso o meno
			Boolean signed = (Boolean) chatroomJSON.get("SIGNED");
			
			//se l'utente è iscritto
			if(signed) {
				//per sapere se la registrazione va bene
				Boolean check = true;
				
				//mi registro al gruppo multicast 
				InetAddress ia = null;
				try {
					ia = InetAddress.getByName(address);
					ms.joinGroup (ia); 
				} catch (Exception e) {
					e.printStackTrace();
					check = false;
				}
				
				//se la registrazione è andata bene
				if(check) {
					//apro la chat verso tale chatrooms
					chatHandlerGUI.addChatroom(chatroom,ia);
					
					//aggiorno la stringa della lista chatrooms
					listChatrooms = listChatrooms + chatroom + "  (signed up)" + eol;
				}
			}
			//se l'utente non è iscritto
			else {
				//aggiorno la stringa della lista chatrooms
				listChatrooms = listChatrooms + chatroom + eol;
			}
		} 
		
		//appendo la lista delle chatrooms nell'area di text dell'interfaccia operativa
		operativeGUI.setTextChatrooms(listChatrooms);
	}
}
