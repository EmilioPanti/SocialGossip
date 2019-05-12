package threads.listeners;

import java.net.DatagramPacket;
import java.net.MulticastSocket;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import client.ClientMain;
import graphicInterfaces.ChatHandlerGUI;


/**
 * Classe ListenerCR.
 * Thread in ascolto (sul multicast socket aperto dal ClientMain)
 * di nuovi messaggi provenienti dalle varie chatroom a cui
 * l'utente è iscritto.
 * Posta i messaggi ricevuti nelle relative chat aperte
 * nell'interfaccia della classe ChatHandlerGUI.
 * @author Emilio Panti mat:531844 
 */
public class ListenerCR implements Runnable {
	
	//interfaccia che gestisce le chat
	private ChatHandlerGUI chatHandlerGUI;

	
	/**
	 * Costruttore classe ListenerChatrooms.
	 * @param ChatHandlerGUI chatHandlerGUI: interfaccia che gestisce le chat.
	 */
	public ListenerCR(ChatHandlerGUI chatHandlerGUI) {
		this.chatHandlerGUI = chatHandlerGUI;
	}

	
	/**
	 * Task che ascolta i messaggi in arrivo dalle varie chatrooms
	 */
	public void run() {
		//multicast socket per ricevere i messaggi dalle chatrooms
		MulticastSocket ms = ClientMain.MS;
		
		//buffer
		byte [ ] buffer = new byte[8000];
		
		//variabile per gestire i messaggi che ricevo
		String msgReiceved = null;
		JSONObject msgJSON = null;
	
		//variabili dove salvo il messaggio ricevuto e la chatroom di destinazione
		String chatroom = null;
		String msg = null;
		
		while(true){ 
			try {
				
				//creo il datagram packet
				DatagramPacket dp = new DatagramPacket(buffer,buffer.length);
				
				//aspetto un messaggio
				ms.receive(dp);
				msgReiceved = new String(dp.getData());
				
				//prendo solo la parte della stringa che mi interessa
				int lastIndex = msgReiceved.lastIndexOf('}');
				String msgCorrect = msgReiceved.substring(0, lastIndex+1);
				
				//trasformo in formato JSON il messaggio corretto
				msgJSON = (JSONObject) new JSONParser().parse(msgCorrect);
				
				//prendo il messaggio e la chat destinataria
				chatroom = (String) msgJSON.get ("TO");
				msg = (String) msgJSON.get ("MSG");
				
				//passo il messaggio all'interfaccia che gestisce le chat
				chatHandlerGUI.postMsgChatroom(chatroom,msg);
			} 
			catch (Exception e) {
			//nel caso si presentasse una eccezione nella ricezione o nel parsing 
			//o altro non faccio nulla,così da far continuare il lavoro del listener
			}
		}
	}
}
