package threads.senders;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import client.ClientMain;
import enumerations.Operations;
import graphicInterfaces.ChatHandlerGUI;
import graphicInterfaces.ChatNicknameGUI;
import graphicInterfaces.ResponseGUI;


/**
 * Classe MsgFriendThread.
 * Thread che si occupa di inviare un messaggio testuale ad
 * un utente.
 * Manda il messaggio al server che poi lo girerà all'utente
 * destinatario.
 * Se l'operazione va a buon fine aggiorna la chat verso l'utente
 * destinatario, altrimenti la chat viene chiusa.
 * @author Emilio Panti mat:531844 
 */
public class MsgFriendThread implements Runnable {

	//interfaccia della chat verso l'utente
	private ChatNicknameGUI chatNicknameGUI;
	
	//interfaccia che gestisce le chat aperte
	private ChatHandlerGUI chatHandlerGUI;
		
	//nickname dell'amico a cui si vuole inviare il file
	private String receiver;
	
	//messaggio da inviare
	private String msg;
	
	
	/**
	 * Costruttore classe MsgFriendThread.
	 * @param ChatNicknameGUI chatNicknameGUI: chat aperta verso l'utente destinatario.
	 * @param ChatHandlerGUI chatHandlerGUI: interfaccia che gestisce le chat.
	 * @param String receiver: nickname dell'utente destinatario.
	 * @param String msg: messaggio da inviare.
	 */
	public MsgFriendThread(ChatNicknameGUI chatNicknameGUI, ChatHandlerGUI chatHandlerGUI,
			String receiver, String msg) {
		this.chatNicknameGUI = chatNicknameGUI;
		this.chatHandlerGUI = chatHandlerGUI;
		this.receiver = receiver;
		this.msg = msg;
	}


	/**
	 * Task che esegue l'operazione di invio messaggio ad un utente amico.
	 */
	@SuppressWarnings("unchecked")
	public void run() {
		//prendo gli streams per comunicare con il server
		DataOutputStream writer = ClientMain.WRITER;
		DataInputStream reader = ClientMain.READER;
		
		//creo la richiesta di invio messaggio
		JSONObject request = new JSONObject();
		request.put("OP", "MSG_FRIEND");
		request.put("TO", receiver);
		request.put("MSG", msg);
						
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
			//scrivo nella chat il messaggio inviato
			String msgToPost = "[" + ClientMain.NICKNAME + "]: " + msg;
			chatNicknameGUI.appendTextChat(msgToPost);
		}
		else {
			//prendo il messaggio di errore trasmesso dal server
			String msgErr = (String) responseJSON.get ("MSG");
								
			//chiudo la chat verso l'utente
			chatHandlerGUI.removeChatNickname(receiver);
			
			//apro l'interfaccia grafica per comunicare l'errore
			ResponseGUI responseGUI = new ResponseGUI(msgErr);
			responseGUI.setVisible(true);
		}
	}

}
