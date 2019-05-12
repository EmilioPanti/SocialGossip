package threads.listeners;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import client.ClientMain;
import enumerations.Operations;
import graphicInterfaces.ChatHandlerGUI;


/**
 * Classe ListenerMsg.
 * Thread che apre una seconda connessione TCP verso il
 * server di SocialGossip e in cui si mette in ascolto.
 * Questa nuova connessione è dedicata alla ricezione di
 * messaggi testuali inviati dagli altri utenti.
 * Posta i messaggi ricevuti nelle relative chat aperte
 * nell'interfaccia della classe ChatHandlerGUI.
 * NB: al momento della richiesta di apertura della seconda 
 * 	   connessione viene comunicato al server anche la porta
 * 	   in cui il client è in ascolto per la ricezione di file.
 * @author Emilio Panti mat:531844 
 */
public class ListenerMsg implements Runnable {
	
	//interfaccia che gestisce le chats
	private ChatHandlerGUI chatHandlerGUI;

	//nickname dell'utente
	private String nickname;
	
	
	/**
	 * Costruttore classe ListenerMsg.
	 * @param ChatHandlerGUI chatHandlerGUI: interfaccia che gestisce le chat.
	 * @param String nickname: nickame dell'utente, serve per aprire la 
	 * 						   seconda connessione verso il server.
	 */
	public ListenerMsg(ChatHandlerGUI chatHandlerGUI, String nickname) {
		this.chatHandlerGUI = chatHandlerGUI;
		this.nickname = nickname;
	}
	

	/**
	 * Task che ascolta i messaggi in arrivo da altri utenti
	 */
	@SuppressWarnings("unchecked")
	public void run() {
	
		//socket e strems per la seconda connessione TCP
		Socket socket = null;
		DataOutputStream writer = null;
		DataInputStream reader = null;
		
		//variabili per gestire la riposta del server
		String response = null;
		JSONObject responseJSON = null;
		
		//creo una seconda connessione verso il server 
		try {
				
			//apro il socket e gli streams di input ed output
			socket = new Socket(ClientMain.HOSTNAME, ClientMain.PORT_TCP);
			writer = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
			reader = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
				
			//creo la richiesta per la connessione dedicata all'ascolto dei messaggi
			//e specifico anche la porta dove il client è in ascolto di files
			JSONObject request = new JSONObject ();
			request.put("OP", "CONN_MSG");
			request.put("ID", nickname);
			request.put("PORT_FILE", ClientMain.PORT_FILE);
				
			//mando la richiesta
			writer.writeUTF(request.toJSONString());
			writer.flush();
			
			//ricevo la risposta
			response = reader.readUTF();
				
			//trasformo in formato JSON la risposta ricevuta dal server
			responseJSON = 	(JSONObject) new JSONParser().parse(response);
				
			//controllo l'esito della risposta
			Operations esito = Operations.valueOf((String) responseJSON.get("OP"));
			//se non è ok chiudo il Client
			if (esito!=Operations.OP_OK) ClientMain.cleanUp();
			
			//Salvo il nuovo socket creato e lo strem in input nella classe ClientMain
			ClientMain.SOCKET_MSG = socket;
			ClientMain.READER_MSG= reader;
			ClientMain.WRITER_MSG= writer;
			
		} catch (Exception e) {
			//se vi è una qualsiasi eccezione nel blocco sopra devo chiudere 
			//il client. Questo perchè l'apertura della seconda connessione TCP 
			//verso il server è fondamentale
			e.printStackTrace();
			//termino il client
			ClientMain.cleanUp();
		}
		
			
		//inizio l'ascolto ciclico di messaggi
		while(true) {
				
			try {
				//ricevo un messaggio
				response = reader.readUTF();
				
				//trasformo in formato JSON ciò che ho ricevuto
				responseJSON = 	(JSONObject) new JSONParser().parse(response);
			} catch (IOException e) {
				//termino il client nel caso ci sia un problema di comunicazione
				//con il server 
				ClientMain.cleanUp();
			} catch (Exception e) {
				//nel caso si presentasse una eccezione nel parsing del messaggio
				//o altro non faccio nulla,così da far continuare il lavoro del listener
				e.printStackTrace();
			}
				
			//controllo cosa ho ricevuto
			Operations esito = Operations.valueOf((String) responseJSON.get("OP"));
			//se è un messaggio (dovrebbe ricevere solo messaggi)
			if (esito==Operations.MSG_FRIEND) {
				//prendo il mittente ed il messaggio
				String sender = (String) responseJSON.get ("FROM");
				String msg = (String) responseJSON.get ("MSG");
					
				//passo il messaggio all'interfaccia che gestisce le chat
				chatHandlerGUI.postMsgChatNickname(sender,msg);
			}
		}
	}
	
}
