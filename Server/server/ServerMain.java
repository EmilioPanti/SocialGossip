package server;

import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;

import dataStructures.HashChatrooms;
import dataStructures.User;
import notificationRMI.ServerImpl;
import notificationRMI.ServerInterface;
import threads.HandlerMsgChatrooms;
import threads.ThreadPool;


/**
 * Classe ServerMain.
 * Contiene il main che gestisce il server dell'applicazione
 * SocialGossip.
 * Crea una hash map per gli utenti registrati (ConcurrentHashMap
 *  <String,User>) e una per le chatrooms (oggetto della classe
 * HashChatrooms).
 * Offre un servizio remoto per permettere ai clients di registrarsi
 * e ricevere le notifiche che li riguardano.
 * Fa partire un thread (oggetto della classe HandlerMsgChatrooms) 
 * che si occupa di smistare alle chatrooms di competenza i messaggi
 * ricevuti dagli utenti destinati ad esse.
 * Infine si mette in attesa ciclica di nuove connessioni; che vengono
 * passate e gestite da un thread pool (oggetto della classe ThreadPool,
 * creato precedentemente).
 * @author Emilio Panti mat:531844 
 */
public class ServerMain {
	
	//porta per ricevere le connessioni TCP
	public static int PORT_TCP = 6012;
	
	//porta per ricevere i messaggi da girare alle chatrooms
	public static int PORT_UDP = 6013;
	
	//porta dove creare il registry ed esposrtare oggetti remoti
	public static int PORT_RMI = 6014; 
	
	//porta dove i client aprono il multicast socket per 
	//ricevere i messaggi dalle chatrooms
	public static int PORT_CLIENT = 6000; 
	
	//numero max delle connessioni contemporanee
	public static int MAX_CONN = 12; 
	
	public static void main(String[] args) {
		
		//hash map per gli utenti
		ConcurrentHashMap <String,User> hashUsers = new ConcurrentHashMap <String,User>();
		
		//hash map per le chatrooms
		HashChatrooms hashChatrooms = new HashChatrooms();
		
		//creo il thread pool che gestirà le connessioni
		ThreadPool threadPool = new ThreadPool(hashUsers,hashChatrooms);
		
		//faccio partire il thread che si occupa di smistare i messaggi alle chatrooms
		HandlerMsgChatrooms handlerMsgChatrooms = new HandlerMsgChatrooms();
		Thread thread = new Thread(handlerMsgChatrooms);
		thread.start();
		
		//listening socket
		ServerSocket serverSocket;
				
		try {
			//registrazione del servizio remoto per le notifiche presso il registry
			ServerImpl server = new ServerImpl(hashUsers);
			ServerInterface stub = (ServerInterface) UnicastRemoteObject.exportObject (server,39000);
			LocateRegistry.createRegistry(PORT_RMI);
			Registry registry = LocateRegistry.getRegistry(PORT_RMI);
			registry.bind ("Server", stub);
			 
			//apro il server socket
			serverSocket = new ServerSocket(PORT_TCP);
			
			System.out.println("Server ready");
			
			//ciclo di vita del server
			while(true){
				
				//active socket verso un client 
				Socket socket = serverSocket.accept();
				  
				//passo la nuova connessione al thread pool
				threadPool.newConnection(socket);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		}
	}
}
