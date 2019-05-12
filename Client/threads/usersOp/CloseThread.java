package threads.usersOp;

import java.io.DataOutputStream;

import org.json.simple.JSONObject;

import client.ClientMain;


/**
 * Classe CloseThread.
 * Thread che comunica al server che il client sta per
 * chiudere la connessione con esso. Successivamente chiama
 * il metodo di cleanUp della classe ClientMain.
 * @author Emilio Panti mat:531844 
 */
public class CloseThread implements Runnable {
		
		
	/**
	 * Costruttore classe CloseThread.
	 */
	public CloseThread() {
		
	}
	
	
	/**
	 * Task che esegue l'operazione di chiusura connessione e chiusura client
	 */
	@SuppressWarnings("unchecked")
	public void run() {
		//streams per comunicare con il server
		DataOutputStream writer = ClientMain.WRITER;
		
		//creo la richiesta di chiusura in formato JSON
		JSONObject request = new JSONObject ();
		request.put("OP", "CLOSE");
		
		try {
			//mando la richiesta
			writer.writeUTF(request.toJSONString());
			writer.flush();
		} catch (Exception e) {
			//termino il client se c'è qualche problema di
			//comunicazione con il server
			ClientMain.cleanUp();
			e.printStackTrace();
		}
		
		//chiamo la funzione per chiudere connessione e terminare client
		ClientMain.cleanUp();
	}
}
