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
import graphicInterfaces.OperativeGUI;
import graphicInterfaces.ResponseGUI;


/**
 * Classe CreateCRThread.
 * Thread che richiede al server (e ne gestisce la risposta) di 
 * creare una nuova chatroom.
 * Se l'operazione va a buon fine la chatroom appena creata viene
 * aggiunta alla lista delle chatrooms nell'interfaccia operativa.
 * Se l'operazione va a buon fine viene aperta una chat
 * verso la nuova chatroom.
 * @author Emilio Panti mat:531844 
 */
public class CreateCRThread implements Runnable {

	//interfacce grafiche aperte
	private OperativeGUI operativeGUI;
	private ChatHandlerGUI chatHandlerGUI;
	
	//nome della chatroom che si vuole aprire
	private String id;
	
	
	/**
	 * Costruttore classe CreateCRThread.
	 * @param OperativeGUI operativeGUI: interfaccia operativa.
	 * @param ChatHandlerGUI chatHandlerGUI: interfaccia che gestisce le chat.
     * @param String id: nome della chatroom che vuole creare l'utente.
	 */
	public CreateCRThread(OperativeGUI operativeGUI,ChatHandlerGUI chatHandlerGUI,String id) {
		this.operativeGUI = operativeGUI;
		this.chatHandlerGUI = chatHandlerGUI;
		this.id = id;
	}


	/**
	 * Task che esegue l'operazione di richiesta apertura chatroom.
	 */
	@SuppressWarnings("unchecked")
	public void run() {
		//prendo gli streams per comunicare con il server
		DataOutputStream writer = ClientMain.WRITER;
		DataInputStream reader = ClientMain.READER;
		
		//creo la richiesta di creazione chatroom
		JSONObject request = new JSONObject();
		request.put("OP", "CREATE_CHATROOM");
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
				//se fallisce la ricerca dell'indirizzo ip delll'host 
				//o se fallisce l'iscrizione al gruppo multicast
				e.printStackTrace();
				check = false;
			}
			
			//se la registrazione al gruppo è andata bene
			if(check) {
				//apro una chat per la chatroom
				chatHandlerGUI.addChatroom(id, ia);
				
				//aggiorno la lista chatroom nella operative interface
				String txt = id + " (signed up)";
				operativeGUI.appendTextChatrooms(txt);
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
