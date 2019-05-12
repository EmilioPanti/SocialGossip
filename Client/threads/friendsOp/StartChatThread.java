package threads.friendsOp;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import client.ClientMain;
import enumerations.Operations;
import graphicInterfaces.ChatHandlerGUI;
import graphicInterfaces.ResponseGUI;


/**
 * Classe StartChatThread.
 * Thread che richiede al server (e ne gestisce la risposta) 
 * il 'permesso' di aprire una chat verso l'utente specificato.
 * @author Emilio Panti mat:531844 
 */
public class StartChatThread implements Runnable {

	//interfaccia che gestisce le chat
	private ChatHandlerGUI chatHandlerGUI;
	
	//nickname dell'utente con cui il client vuole aprire una chat
	private String nickname;
	
	
	/**
	 * Costruttore classe StartChatThread.
	 * @param ChatHandlerGUI chatHandlerGUI: interfaccia gestore delle chat.
	 * @param String nickname: nickname dell'utente con cui il client vuole
	 * 						   aprire una chat.
	 */
	public StartChatThread(ChatHandlerGUI chatHandlerGUI,String nickname) {
		this.chatHandlerGUI = chatHandlerGUI;
		this.nickname = nickname;
	}


	/**
	 * Task che esegue l'operazione di apertura chat.
	 */
	@SuppressWarnings("unchecked")
	public void run() {
		//prendo gli streams per comunicare con il server
		DataOutputStream writer = ClientMain.WRITER;
		DataInputStream reader = ClientMain.READER;
		
		//creo la richiesta di apertura chat
		JSONObject request = new JSONObject();
		request.put("OP", "STARTCHAT");
		request.put("ID", nickname);
						
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
			//apro una chat verso l'utente
			chatHandlerGUI.addChatNickname(nickname, null);
		}
		else {
			//prendo il messaggio di errore trasmesso dal server
			String msgErr = (String) responseJSON.get ("MSG");
									
			//apro l'interfaccia grafica per comunicare l'errore
			ResponseGUI responseGUI = new ResponseGUI(msgErr);
			responseGUI.setVisible(true);
		}
	}
}