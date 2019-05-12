package threads.chatroomsOp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import client.ClientMain;
import enumerations.Operations;
import graphicInterfaces.OperativeGUI;
import graphicInterfaces.ResponseGUI;


/**
 * Classe CRListThread.
 * Thread che richiede al server (e ne gestisce la risposta) la
 * lista di tutte le chatrooms aperte, specificando a quali di
 * esse l'utente sia già iscritto.
 * Se l'operazione va a buon fine viene aggiornata la lista 
 * delle chatrooms presente nell'interfaccia operativa.
 * @author Emilio Panti mat:531844 
 */
public class CRListThread implements Runnable {

	//interfaccia operativa della chat
	private OperativeGUI operativeGUI;
		
		
	/**
	 * Costruttore classe CRListThread.
	 * @param OperativeGUI operativeGUI: interfaccia operativa.
	 */
	public CRListThread(OperativeGUI operativeGUI) {
		this.operativeGUI = operativeGUI;
	}


	/**
	 * Task che esegue l'operazione di richiesta lista chatroom.
	 */
	@SuppressWarnings("unchecked")
	public void run() {
		//prendo gli streams per comunicare con il server
		DataOutputStream writer = ClientMain.WRITER;
		DataInputStream reader = ClientMain.READER;
			
		//creo la richiesta per la lista chatrooms in formato JSON
		JSONObject request = new JSONObject ();
		request.put("OP", "CHATLIST");
				
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
			//prendo la lista chatrooms in formato JSONArray
			JSONArray chatrooms = (JSONArray) responseJSON.get ("CHATROOMS");
			
			if(chatrooms != null) {
				//Stringa dove inserisco il nome delle chatrooms 
				//(specificando a quali l'utente è già iscritto)
				String listChatrooms = "";
				
				//stringa end of line
				String eol = System.getProperty("line.separator");
				
				//creo un iteratore per scorrere la lista delle chatrooms
				Iterator<JSONObject> iterator = chatrooms.iterator();
				
				while (iterator.hasNext()) {
					//prendo un oggetto json della lista
					JSONObject chatroomJSON = iterator.next();
					
					//prendo il nome della chatroom
					String chatroom = (String) chatroomJSON.get("ID");
					//boolean che mi dice se l'utente è iscritto ad essa o meno
					Boolean signed = (Boolean) chatroomJSON.get("SIGNED");
					
					//se l'utente è iscritto
					if(signed) {
						//aggiorno la stringa della lista chatrooms
						listChatrooms = listChatrooms + chatroom + "  (signed up)" + eol;
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
			//se non esistono chatrooms attive
			else {
				operativeGUI.setTextChatrooms("");
			}
		}
		else {
			//prendo il messaggio di errore trasmesso dal server
			String msgErr = (String) responseJSON.get ("MSG");
					
			//apro l'interfaccia grafica per comunicare l'errore
			ResponseGUI responseGUI = new ResponseGUI(msgErr);
			responseGUI.setVisible(true);
		}
				
		//riattivo il bottone per la richiesta della lista chatrooms
		operativeGUI.enabledBtnUpdatesChatrooms();
	}

}
