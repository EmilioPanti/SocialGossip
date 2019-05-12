package threads.chatroomsOp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.MulticastSocket;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import client.ClientMain;
import enumerations.Operations;
import graphicInterfaces.ChatHandlerGUI;
import graphicInterfaces.ResponseGUI;


/**
 * Classe AddToCRThread.
 * Thread che richiede al server (e ne gestisce la risposta) di 
 * iscrivere l'utente ad una specifica chatroom.
 * Se l'operazione va a buon fine viene aperta una chat verso
 * tale chatroom.
 * NB: la lista chatrooms presente nell'interfaccia operativa non 
 * 	   viene aggiornata (è lasciato all'utente il compito di 
 *     aggiornarla quando lo ritiene più opportuno).
 * @author Emilio Panti mat:531844 
 */
public class AddToCRThread implements Runnable {

	//interfaccia che gestisce le chat
	private ChatHandlerGUI chatHandlerGUI;
		
	//nome della chatroom a cui si vuole unire l'utente
	private String id;
		
		
	/**
     * Costruttore classe AddToCRThread.
     * @param ChatHandlerGUI chatHandlerGUI: interfaccia che gestisce le chat.
     * @param String id: nome della chatroom a cui si vuole unire l'utente.
	 */
	public AddToCRThread(ChatHandlerGUI chatHandlerGUI,String id) {
		this.chatHandlerGUI = chatHandlerGUI;
		this.id = id;
	}


	/**
	 * Task che esegue l'operazione di iscrizione ad una chatroom.
	 */
	@SuppressWarnings("unchecked")
	public void run() {
		//prendo gli streams per comunicare con il server
		DataOutputStream writer = ClientMain.WRITER;
		DataInputStream reader = ClientMain.READER;
		
		//creo la richiesta di iscrizione ad una chatroom
		JSONObject request = new JSONObject();
		request.put("OP", "ADDME_CHATROOM");
		request.put("ID", id);
						
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
			//prendo l'indirizzo della chatroom
			String address = (String) responseJSON.get ("ADDRESS");
									
			//prendo il multicast socket
			MulticastSocket ms = ClientMain.MS;
					
			//boolean per sapere se la registrazione al gruppo è andata bene
			boolean check = true;
			
			//mi registro al gruppo multicast della chatroom
			InetAddress ia = null;
			try {
				ia = InetAddress.getByName(address);
				ms.joinGroup (ia); 
			} catch (Exception e) {
				//se fallisce la ricerca dell'indirizzo ip dell'host 
				//o se fallisce l'iscrizione al gruppo multicast
				e.printStackTrace();
				check = false;
			}
			
			//se la registrazione al gruppo è andata bene
			if(check) {
				//apro una chat per la chatroom
				chatHandlerGUI.addChatroom(id, ia);
			}
			//se c'è stato qualche errore
			else {
				//apro l'interfaccia grafica per comunicare l'errore
				ResponseGUI responseGUI = 
						new ResponseGUI("ERR: An error occurred");
				responseGUI.setVisible(true);
			}
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
