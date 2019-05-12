package threads.usersOp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import client.ClientMain;
import enumerations.Operations;
import graphicInterfaces.ChatHandlerGUI;
import graphicInterfaces.OperativeGUI;
import graphicInterfaces.RegistrationGUI;
import graphicInterfaces.ResponseGUI;
import notificationRMI.NotifyEventImpl;
import notificationRMI.NotifyEventInterface;
import notificationRMI.ServerInterface;
import threads.listeners.ListenerCR;
import threads.listeners.ListenerFile;
import threads.listeners.ListenerMsg;


/**
 * Classe RegistrationThread.
 * Thread che richiede al server (e ne gestisce la risposta)
 * la registrazione di un nuovo utente al servizio SocialGossip.
 * Se l'operazione va a buon fine vengono create l'interfaccia
 * operativa (oggetto della classe OperativeGUI) e l'interfaccia
 * che gestisce le chat aperte (oggetto della classe ChatHandlerGUI).
 * Il server, in caso di esito positivo, allega alla risposta 
 * anche la lista di tutte le chatrooms aperte.
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
public class RegistrationThread implements Runnable {

	//nickname e psw
	private String nickname;
	private String psw;
	private String language;
		
	//Interfacce attive al momento in cui viene invocato questo thread
	private RegistrationGUI registrationGUI;
	
	
	/**
	 * Costruttore classe RegistrationThread.
	 * @param RegistrationGUI registrationGUI: interfaccia di registrazione 
	 * 		  che ha fatto partire questo thread.
	 * @param String nickname: nickname che ha inserito l'utente per registrarsi.
	 * @param String psw: password che ha inserito l'utente per registrarsi.
	 * @param String language: lingua selezionata dall'utente.
	 */
	public RegistrationThread(RegistrationGUI registrationGUI,String nickname,
			String psw, String language) {
		this.registrationGUI = registrationGUI;
		this.nickname = nickname;
		this.psw = psw;
		this.language = language;
	}
	
	
	/**
	 * Task che esegue l'operazione di registrazione
	 */
	@SuppressWarnings("unchecked")
	public void run() {
		
		//prendo gli streams per comunicare con il server
		DataOutputStream writer = ClientMain.WRITER;
		DataInputStream reader = ClientMain.READER;
				
		//creo la richiesta di registrazione in formato JSON
		JSONObject request = new JSONObject ();
		request.put("OP", "REG");
		request.put("ID", nickname);
		request.put("PSW", psw);
		request.put("LANGUAGE", language);
				
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
			ChatHandlerGUI chatHandlerGUI = new ChatHandlerGUI();
			OperativeGUI operativeGUI = new OperativeGUI(chatHandlerGUI);
					
			//prendo la lista delle chatrooms e la appendo nell'interfaccia operativa
			JSONArray chatrooms = (JSONArray) responseJSON.get("CHATROOMS"); 
			if (chatrooms!=null) getListChatrooms(chatrooms,operativeGUI);
			
			//creazione e registrazione dello stub per ricevere le notifiche dal server
			NotifyEventInterface callbackObj;
			try {
				callbackObj = new NotifyEventImpl(chatHandlerGUI,operativeGUI);
				NotifyEventInterface stub = 
						(NotifyEventInterface) UnicastRemoteObject.exportObject(callbackObj, 0);
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
			
			//mostro all'utente l'interfaccia operativa e chiudo quella di registrazione
			operativeGUI.setVisible(true);
			registrationGUI.dispose();
					
			//comunico all'utente la riuscita dell'operazione
			ResponseGUI responseGUI = new ResponseGUI("Your registration was successful");
			responseGUI.setVisible(true);
					
		}
		//c'è stato un errore
		else {
			//prendo il messaggio di errore trasmesso dal server
			String msgErr = (String) responseJSON.get ("MSG");
					
			//riattivo il bottone per la registrazione nella interfaccia di registrazione
			registrationGUI.enabledBtnRegister();
					
			//apro l'interfaccia grafica per comunicare l'errore
			ResponseGUI responseGUI = new ResponseGUI(msgErr);
			responseGUI.setVisible(true);
		}
	}
	
	
	/**
	 * Metodo che prende una lista di chatrooms in formato JSON.
	 * Ne ricava una lista da appendere nella text area relativa alle chatroom
	 * nella interfaccua operativa
	 * @param JSONArray chatrooms : lista delle chatrooms in formato JSON.
	 * @param OperativeGUI operativeGUI : interfaccia dove appendere la lista.
	 */
	private void getListChatrooms(JSONArray chatrooms,OperativeGUI operativeGUI) {
		//Stringa dove inserisco il nome delle chatrooms
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
			
			//aggiorno la stringa della lista chatrooms
			listChatrooms = listChatrooms + chatroom + eol;
		} 
		
		//appendo la lista delle chatrooms nell'area di text dell'interfaccia operativa
		operativeGUI.setTextChatrooms(listChatrooms);
	}
}
