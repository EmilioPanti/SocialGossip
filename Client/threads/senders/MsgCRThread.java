package threads.senders;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import client.ClientMain;
import enumerations.Operations;
import graphicInterfaces.ResponseGUI;


/**
 * Classe MsgCRThread.
 * Thread che si occupa di inviare un messaggio testuale ad una 
 * chatroom.
 * Per prima cosa controlla che la chat sia ancora attiva 
 * mandando una richiesta di controllo al server.
 * Se la risposta è positiva spedisce al server un 
 * pacchetto UDP contenente il messaggio e le info sulla 
 * chatroom di destinazione.
 * Sarà poi compito del server inoltrare il messaggio agli
 * altri utenti iscritti alla chatroom.
 * @author Emilio Panti mat:531844 
 */
public class MsgCRThread implements Runnable {
	
	//nome e address della chatroom
	private String chatroom;
	private InetAddress ia;
	
	//messaggio da inviare
	private String msg;
	
	
	/**
	 * Costruttore classe MsgCRThread.
	 * @param String chatroom: nome della chatroom.
	 * @param InetAddress ia: inet address del gruppo multicast relativo
	 * 						  alla chatroom.
	 * @param String msg: messaggio da inviare.
	 */
	public MsgCRThread(String chatroom,InetAddress ia, String msg) {
		this.chatroom = chatroom;
		this.ia = ia;
		this.msg = msg;
	}


	/**
	 * Task che esegue l'operazione di invio messaggio ad una chatroom
	 */
	@SuppressWarnings("unchecked")
	public void run() {
		//prendo gli streams per comunicare con il server
		DataOutputStream writer = ClientMain.WRITER;
		DataInputStream reader = ClientMain.READER;
				
		//creo la richiesta di invio messaggio ad una chatroom
		JSONObject request = new JSONObject();
		request.put("OP", "MSG_CHATROOM");
		request.put("ID", chatroom);
								
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
			//mando il messaggio UDP al server
			sendMsg();
		}
		else {
			//prendo il messaggio di errore trasmesso dal server
			String msgErr = (String) responseJSON.get ("MSG");
									
			//apro l'interfaccia grafica per comunicare l'errore
			ResponseGUI responseGUI = new ResponseGUI(msgErr);
			responseGUI.setVisible(true);
		}
	}

	
	/**
	 * Metodo che invia il messaggio scritto dall'utente al server 
	 * con un pacchetto UDP.
	 */
	@SuppressWarnings("unchecked")
	private void sendMsg() {
		//aggiungo il nickname dell'utente al messaggio
		msg = "[" + ClientMain.NICKNAME + "]: " + msg;
		
		//creo l'oggetto json che contiene il messaggio e il nome della chatroom
		JSONObject msgJSON = new JSONObject();
		msgJSON.put("TO", chatroom);
		msgJSON.put("MSG", msg);
		
		//prendo l'host address della chatroom
		String address = ia.getHostAddress().toString();
				
		//creo l'oggetto json finale da inviare al server
		JSONObject msgToSendJSON = new JSONObject();
		msgToSendJSON.put("ADDRESS", address);
		msgToSendJSON.put("MESSAGE", msgJSON);
		
		//trasformo in stringa il messaggio da inviare al server
		String msgToSend = msgToSendJSON.toJSONString();
		
		//boolean per controllare che nell'invio del messaggio non avvengano errori
		boolean error = false;
				
		//apro un datagram socket ed invio il messaggio al server
		try {
			//prendo l'inet address del server 
			InetAddress iaServer = InetAddress.getByName(ClientMain.HOSTNAME);
			
			//apro il datagram socket
			@SuppressWarnings("resource")
			DatagramSocket ds = new DatagramSocket();
					
			byte [ ] data = msgToSend.getBytes();
					
			//creo il datagram packet e lo invio
			DatagramPacket dp = new DatagramPacket(data,data.length,iaServer,ClientMain.PORT_UDP);
			ds.send(dp);
		} 
		catch (Exception e) {
			error = true;
		}
			
		//se c'è stato un errore lo comunico
		if(error) {
			//apro l'interfaccia grafica per comunicare l'errore
			ResponseGUI responseGUI = new ResponseGUI("ERR: An error occurred");
			responseGUI.setVisible(true);
		}
	}
}
