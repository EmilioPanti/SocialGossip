package threads;

import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import dataStructures.HashChatrooms;
import dataStructures.User;
import server.ServerMain;


/**
 * Classe ThreadPool.
 * Crea un thread pool di N threads (dove N è il 
 * numero massimo di connessioni accettate dal server).
 * Ogni thread del pool (oggetti della classe Worker)
 * gestisce una connessione verso un client finchè non 
 * viene chiusa.
 * @author Emilio Panti mat:531844 
 */
public class ThreadPool {
	
	//thread pool esecutore di task
	private ThreadPoolExecutor executor;
	
	//hash map per gli utenti
	private ConcurrentHashMap <String,User> hashUsers;
			
	//hash map per le chatrooms
	private HashChatrooms hashChatrooms;
	
	
	/**
	 * Costruttore classe ThreadPoolContatore.
	 * @param ConcurrentHashMap<String,User> hashUsers: hash map degli utenti.
	 * @param HashChatrooms hashChatrooms: hash map delle chatrooms.
	 */
	public ThreadPool(ConcurrentHashMap<String,User> hashUsers, 
			HashChatrooms hashChatrooms){
		this.hashUsers = hashUsers;
		this.hashChatrooms = hashChatrooms;
		executor=(ThreadPoolExecutor)Executors.newFixedThreadPool(ServerMain.MAX_CONN);
	}
	
	
	/**
	 * Metodo che dà in gestione ad un thread del pool una nuova connessione.
	 * @param Socket socket: socket della nuova connessione.
	 */
	public void newConnection(Socket socket){
		try{
			executor.execute((Runnable)new Worker(socket,hashUsers,hashChatrooms));
		} 
		catch (Exception e) {
 			e.printStackTrace();
 		}
	}
}
