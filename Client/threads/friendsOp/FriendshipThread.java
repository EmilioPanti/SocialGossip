package threads.friendsOp;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import client.ClientMain;
import enumerations.Operations;
import graphicInterfaces.ResponseGUI;


/**
 * Classe FriendshipThread.
 * Thread che richiede al server (e ne gestisce la risposta) di 
 * instaurare una nuova amicizia tra l'utente ed un'altra persona 
 * che utilizza SocialGossip.
 * Se l'operazione va a buon fine il nickname del nuovo amico viene 
 * aggiunto alla lista amici nell'interfaccia operativa.
 * @author Emilio Panti mat:531844 
 */
public class FriendshipThread implements Runnable {
	
	//nickname con cui l'utente vuole stringere amicizia
	private String nickname;
	
	
	/**
	 * Costruttore classe FriendshipThread.
	 * @param String nickname: nickname con cui l'utente vuole stringere amicizia.
	 */
	public FriendshipThread(String nickname) {
		this.nickname = nickname;
	}


	/**
	 * Task che esegue l'operazione di richiesta amicizia.
	 */
	@SuppressWarnings("unchecked")
	public void run() {
		//prendo gli streams per comunicare con il server
		DataOutputStream writer = ClientMain.WRITER;
		DataInputStream reader = ClientMain.READER;
		
		//creo la richiesta di amicizia
		JSONObject request = new JSONObject();
		request.put("OP", "FRIENDSHIP");
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
		if (esito!=Operations.OP_OK) {
			//prendo il messaggio di errore trasmesso dal server
			String msgErr = (String) responseJSON.get ("MSG");
					
			//apro l'interfaccia grafica per comunicare l'errore
			ResponseGUI responseGUI = new ResponseGUI(msgErr);
			responseGUI.setVisible(true);
		}
	}
}
