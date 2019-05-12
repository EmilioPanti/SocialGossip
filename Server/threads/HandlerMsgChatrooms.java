package threads;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import server.ServerMain;

/**
 * Classe HandlerMsgChatrooms.
 * Thread che apre un DatagramSocket e in cui successivamente
 * si mette in ascolto di messaggi destinati alle chatrooms
 * (inviati dagli utenti).
 * Per ogni pacchetto ricevuto preleva l'inet address della
 * chatroom di destinazione e spedisce il messaggio a tale
 * indirizzo multicast.
 * @author Emilio Panti mat:531844 
 */
public class HandlerMsgChatrooms implements Runnable {
	
	
	/**
	 * Costruttore classe HandlerMsgChatrooms.
	 */
	public HandlerMsgChatrooms() {
		
	}

	
	/**
	 * Task che riceve messaggi dai client per poi smistarli
	 * alle chatrooms.
	 */
	public void run() {
		//in che porta aprire il datagram socket
		int portDs = ServerMain.PORT_UDP;
		
		//porta dove sono in ascolto i clients
		int sendPort = ServerMain.PORT_CLIENT;
		
		//utilizzato per ricevere i messaggi UDP dai clients
		//e rispedirli alle chatroom
		DatagramSocket ds = null;
		
		//apro il datagram socket
		try {
			ds = new DatagramSocket(portDs);
		} 
		catch (Exception e) {
			e.printStackTrace();
			ds.close();
			System.exit(0);
		}
		
		//variabile per gestire i messaggi che ricevo
		String msgReiceved = null;
		JSONObject msgJSON = null;
	
		//variabili dove salvo l'indirizzo della chatrooom e il messaggio
		//(in formato json) da inviare ad essa
		String address = null;
		InetAddress ia = null;
		JSONObject msg = null;
		
		while(true){ 
			try {
				//buffer
				byte [ ] buffer = new byte[8000];
				
				//creo il datagram packet
				DatagramPacket dpReceived = new DatagramPacket(buffer,buffer.length);
				
				//aspetto un messaggio
				ds.receive(dpReceived);
				msgReiceved = new String(dpReceived.getData());
				
				//prendo solo la parte della stringa che mi interessa
				int lastIndex = msgReiceved.lastIndexOf('}');
				String msgCorrect = msgReiceved.substring(0, lastIndex+1);
				
				//trasformo in formato JSON il messaggio corretto
				msgJSON = (JSONObject) new JSONParser().parse(msgCorrect);
				
				//prendo il messaggio e l'indirizzo della chatroom destinataria
				address = (String) msgJSON.get ("ADDRESS");
				msg = (JSONObject) msgJSON.get ("MESSAGE");
				
				//prendo l'inet address della chatroom
				ia = InetAddress.getByName(address);
				
				//creo ed invio il pacchetto a tutta la chatroom
				byte [ ] data = (msg.toJSONString()).getBytes();
				DatagramPacket dpToSend = new DatagramPacket(data,data.length,ia,sendPort);
				ds.send(dpToSend);
			} 
			catch (Exception e) {
				//se occorre un'eccezione nel blocco sopra, non faccio niente, così da 
				//far continuare il lavoro al thread
				e.printStackTrace();
			}
		}
	}
}
