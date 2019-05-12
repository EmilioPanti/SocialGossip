package client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import graphicInterfaces.LoginGUI;
import notificationRMI.ServerInterface;


/**
 * Classe ClientMain.
 * Contiene il main che gestisce il client, prende come argomento 
 * l'host del server SocialGossip.
 * Apre: 
 * - la connessione TCP verso il server,
 * - il multicast socket per la ricezione di messaggi dalle chatroom,
 * - il server socket channel per la ricezione di eventuali file inviati
 *   da altri client,
 * - infine l'interfaccia grafica per il login all'applicazione SocialGossip.
 * Offre anche un metodo per la chiusura di tutte le connessioni aperte.
 * @author Emilio Panti mat:531844 
 */
public class ClientMain {
	
	//porte per connessioni e hostname del server
	public static int PORT_TCP = 6012; 
	public static int PORT_UDP = 6013; 
	public static int PORT_RMI = 6014; 
	public static String HOSTNAME = null;
	
	//prima connssione TCP
	//socket e streams utilizzati per fare richieste e ricevere i relativi responsi da/verso il server
	public static Socket SOCKET = null;
	public static DataOutputStream WRITER = null;
	public static DataInputStream READER = null;
	
	//seconda connessione TCP
	//socket e stream utilizzati per ricevere i messaggi dagli altri utenti
	public static Socket SOCKET_MSG = null;
	public static DataInputStream READER_MSG = null;
	public static DataOutputStream WRITER_MSG = null;
	
	//Porta e multicast socket per unirmi alle chatrooms e riceverne i messaggi 
	public static int PORT_MS = 6000; 
	public static MulticastSocket MS = null;
	
	//servizio remoto offerto dal server
	public static ServerInterface SERVER_RMI;
	
	//nickname dell'utente dopo che si è loggato
	public static String NICKNAME = null;
		
	//Porta e server socket channel per ricevere i file da altri utenti
	public static long PORT_FILE = 0; 
	public static ServerSocketChannel SERVER_SOCKET_FILE = null;
		
	//dove cercare i file da inviare 
	public static String PATH_MY_FILE = 
			"UserFiles" + File.separator + "MyFiles" + File.separator;
	//dove salvare i file ricevuti
	public static String PATH_FILE_REICEVED = 
			"UserFiles" + File.separator + "ReceivedFiles"+File.separator;
	
	
	public static void main(String[] args) {
		//l'host del server è passato come argomento
		if (args.length != 1) {
			System.out.println("Usage: java -jar SocialGossipClient.jar HostServer");
			return;
		}
		
		//prendo l'host del server
		HOSTNAME = args[0];
		
		try { 
			//apro la prima connessione TCP verso il server
			SOCKET = new Socket(HOSTNAME, PORT_TCP);
			WRITER = new DataOutputStream(new BufferedOutputStream(SOCKET.getOutputStream()));
			READER = new DataInputStream(new BufferedInputStream(SOCKET.getInputStream()));
			
			//apro il multicast socket, utilizzato per le chatrooms
			MS = new MulticastSocket(PORT_MS);
			
			//prendo dal registry l'oggetto esportato dal server per 
			//la registrazione del client alle notifiche
			Registry registry = LocateRegistry.getRegistry(PORT_RMI);
			SERVER_RMI = (ServerInterface) registry.lookup("Server");
			 
			//apro il server socket channel per la ricezione dei file
			SERVER_SOCKET_FILE = ServerSocketChannel.open();
			ServerSocket ss = SERVER_SOCKET_FILE.socket();
			InetSocketAddress address = new InetSocketAddress(0);
			ss.bind(address);
			PORT_FILE = ss.getLocalPort();
			
			//faccio partire la schermata di login
			LoginGUI loginGUI = new LoginGUI();
			loginGUI.setVisible(true);
		}
		catch (Exception e) { 
			//per qualsiasi cosa non vada bene
			e.printStackTrace();
			//termino il client
			cleanUp();
		}
	}
	
	
	/**
	 * Metodo statico per chiudere tutte le connessioni verso il server
	 * e far terminare il client.
	 */
	public static void cleanUp() {
		//chiudo tutti gli strems ed i socket
		try {
			if (WRITER!=null) WRITER.close();
			if (READER!=null) READER.close();
			if (SOCKET!=null) SOCKET.close();
			if (WRITER_MSG!=null) WRITER.close();
			if (READER_MSG!=null) READER_MSG.close();
			if (SOCKET_MSG!=null) SOCKET_MSG.close();
			if (MS!=null) MS.close();
			if (SERVER_SOCKET_FILE!=null) SERVER_SOCKET_FILE.close();
		}
		catch (Exception e) {}
		
		//termino tutti gli eventuali threads
		System.exit(0);
	}

}
