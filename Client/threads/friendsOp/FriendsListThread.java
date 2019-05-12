package threads.friendsOp;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import client.ClientMain;
import enumerations.Operations;
import graphicInterfaces.OperativeGUI;
import graphicInterfaces.ResponseGUI;


/**
 * Classe FriendsListThread.
 * Thread che richiede al server (e ne gestisce la risposta) la 
 * lista degli amici dell'utente.
 * Se l'operazione va a buon fine viene aggiornata la lista degli
 * amici presente nell'interfaccia operativa.
 * NB: la richiesta di aggiornamento della lista amici da parte 
 * 	   dell'utente è futile. Questo perchè essa viene aggiornata
 * 	   ogni volta che l'utente stringe una nuova amicizia
 * 	   (sia in modo attivo che passivo).
 * @author Emilio Panti mat:531844 
 */
public class FriendsListThread implements Runnable {

	//interfaccia operativa della chat
	private OperativeGUI operativeGUI;
	
	
	/**
	 * Costruttore classe FriendsListThread.
	 * @param OperativeGUI operativeGUI: interfaccia operativa.
	 */
	public FriendsListThread(OperativeGUI operativeGUI) {
		this.operativeGUI = operativeGUI;
	}


	/**
	 * Task che esegue l'operazione di richiesta lista amici.
	 */
	@SuppressWarnings("unchecked")
	public void run() {
		//prendo gli streams per comunicare con il server
		DataOutputStream writer = ClientMain.WRITER;
		DataInputStream reader = ClientMain.READER;
		
		//creo la richiesta per la lista amici in formato JSON
		JSONObject request = new JSONObject ();
		request.put("OP", "LISTFRIEND");
		
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
			//prendo la lista amici e la appendo nell'area di text dell'interfaccia operativa
			String friends = (String) responseJSON.get ("FRIENDS");
			if (friends!=null) operativeGUI.setTextFriends(friends);
		}
		else {
			//prendo il messaggio di errore trasmesso dal server
			String msgErr = (String) responseJSON.get ("MSG");
			
			//apro l'interfaccia grafica per comunicare l'errore
			ResponseGUI responseGUI = new ResponseGUI(msgErr);
			responseGUI.setVisible(true);
		}
		
		//riattivo il bottone per la richiesta della lista amici
		operativeGUI.enabledBtnUpdatesFriends();
	}

}
