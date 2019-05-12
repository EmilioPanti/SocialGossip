package threads.chatroomsOp;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import client.ClientMain;
import enumerations.Operations;
import graphicInterfaces.ResponseGUI;


/**
 * Classe CloseCRThread.
 * Thread che richiede al server (e ne gestisce la risposta) di 
 * chiudere una specifica chatroom.
 * Se l'operazione va a buon fine viene chiusa la chat che era 
 * aperta verso tale chatroom.
 * NB: la lista chatrooms presente nell'interfaccia operativa non 
 * 	   viene aggiornata (è lasciato all'utente il compito di 
 *     aggiornarla quando lo ritiene più opportuno).
 * @author Emilio Panti mat:531844 
 */
public class CloseCRThread implements Runnable {
			
	//nome della chatroom che si vuole chiudere.
	private String id;
			
			
	/**
	 * Costruttore classe CloseCRThread.
	 * @param String id: nome della chatroom che l'utente vuole chiudere.
	 */
	public CloseCRThread(String id) {
		this.id = id;
	}


	/**
	 * Task che esegue l'operazione di chiusura chatroom.
	 */
	@SuppressWarnings("unchecked")
	public void run() {
		//prendo gli streams per comunicare con il server
		DataOutputStream writer = ClientMain.WRITER;
		DataInputStream reader = ClientMain.READER;
			
		//creo la richiesta di cancellazione di una chatroom
		JSONObject request = new JSONObject();
		request.put("OP", "CLOSE_CHATROOM");
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
		if (esito!=Operations.OP_OK) {
			//prendo il messaggio di errore trasmesso dal server
			String msgErr = (String) responseJSON.get ("MSG");
									
			//apro l'interfaccia grafica per comunicare l'errore
			ResponseGUI responseGUI = new ResponseGUI(msgErr);
			responseGUI.setVisible(true);
		}
	}

}
