package dataStructures;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.rmi.RemoteException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import notificationRMI.NotifyEventInterface;

/**
 * Classe User.
 * Contiene i metodi per creare ed eseguire operazioni
 * sugli utenti.
 * @author Emilio Panti mat:531844 
 */
public class User {
	
	//nickname, password e lingue dell'utente
	private String nickname;
	private String psw;
	private String language;
	
	//status utente
	private boolean online;
	
	//lista amici dell'utente
	private ListUsers friends;
	
	//lista delle chatrooms a cui l'utente è iscritto
	private ListChatrooms chatrooms;
	
	//stub per notificare gli eventi al client 
	private NotifyEventInterface stub;
	
	//socket e stream per inviare messaggi all'utente
	private Socket socketMsg;
	private DataOutputStream writerMsg;
	private DataInputStream readerMsg;
	
	//porta dove il client dell'user è in ascolto per ricevere file
	private long portFile;
	
	
	/**
	 * Costruttore classe User.
	 * @param String nickname: nickname scelto dall'utente
	 * @param String psw: password scelta dall'utente.
	 * @param String language: lingua dell'utente.
	 */
	public User(String nickname, String psw, String language) {
		this.nickname = nickname;
		this.psw = psw;
		this.language = language;
		online = false;
		friends = new ListUsers();
		chatrooms = new ListChatrooms();
		stub = null;
		socketMsg = null;
		writerMsg = null;
		portFile = 0;
	}
	
	
	/**
	 * Override del metodo equals.
	 * @param: Object obj: oggetto da confrontare.
	 * @return true: se i due utenti hanno lo stesso nickname,
	 * 		   false: altrimenti.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj==null) return false;
		User user = (User) obj;
		return nickname.equals(user.getNickname());
	}
	
	
	/**
	 * Override del metodo hashCode.
	 * Ho effettuato l'override di hashCode solo per formalità (non verrà 
	 * utilizzato) dato che ho fatto l'ovveride di equals.
	 * @return int: restituisce il risultato del metodo hashCode chiamato
	 * 				sul nickname dell'utente (anche il metodo equals
	 * 				di cui ho fatto l'override sopra si basa sul confronto 
	 * 				del campo nickname)
	 */
	@Override
	public int hashCode() {
		return nickname.hashCode();
	}


	/**
	 * Metodo che restituisce lo status dell'utente.
	 * @return boolean: true se l'utente è online
	 * 					false altrimenti.
	 */
	public synchronized boolean getOnline() {
		return online;
	}
	
	/**
	 * Metodo che restituisce la lingua dell'utente
	 * @return String: la lingua dell'utente
	 */
	public String getLanguage() {
		return language;
	}
	
	
	/**
	 * Metodo che restituisce il nickname dell'utente
	 * @return String: il nickname dell'utente
	 */
	public String getNickname() {
		return nickname;
	}

	
	/**
	 * Metodo che restituisce un oggetto json contenente l'inet address e 
	 * la porta dove il client è in ascolto per eventuali file.
	 * @return JSONObject: contenente l'inet address e la port file,
	 * 	       null: se è offline.
	 */
	@SuppressWarnings("unchecked")
	public synchronized JSONObject getInfoFile() {
		if (online) {
			JSONObject infoFile = new JSONObject();
			infoFile.put("ADDRESS", socketMsg.getInetAddress().getHostAddress().toString());
			infoFile.put("PORT_FILE", portFile);
			return infoFile;
		}
		else return null;
	}
	
	
	/**
	 * Metodo per controllare se la password passata da parametro corrisponde
	 * a quella salvata nella struttura dell'user.
	 * @param String password: password da controllare.
	 * @return true: se le due password corrispondono,
	 * 		   false: altrimenti.
	 */
	public boolean checkPsw (String password) {
		return (psw.equals(password));
	}
	
	
	/**
	 * Metodo per settare il socket e gli streams dedicati all'invio di messaggi all'user.
	 * Se lo stub per le notifiche è già stato settato cambio lo stato dell'user a online.
	 * @param Socket socketMsg: socket aperto per inviare i messaggi all'user.
	 * @param DataOutputStream writerMsg: stream per inviare i messaggi all'user.
	 */
	public void setSocketMsg (Socket socketMsg, DataOutputStream writerMsg,
			DataInputStream readerMsg, long portFile) {
		
		//variabile per sapere se è cambiato di stato l'utente
		boolean check = false;
		
		synchronized (this ) {
			this.socketMsg = socketMsg;
			this.writerMsg = writerMsg;
			this.readerMsg = readerMsg;
			this.portFile = portFile;
			if (stub != null) {
				online = true;
				check = true;
			}
		}
		
		//se l'utente è online
		if(check) {
			//notifico a tutti gli amici dell'utente che adesso è online
			friends.notifyAllOnline(nickname);
			
			//aumento di 1 il numero degli utenti connessi a tutte le chatroom
			//a cui l'utente è iscritto
			chatrooms.increaseAll();
		}
	}
	
	
	/**
	 * Metodo per settare lo stub che permette di notificare i vari eventi all'user.
	 * Se il socket per i messaggi è già stato settato cambio lo stato
	 * dell'user a online.
	 * @param NotifyEventInterface stub: stub per le notifiche.
	 */
	public void setStubNotify (NotifyEventInterface stub) {
		
		//variabile per sapere se è cambiato di stato l'utente
		boolean check = false;
				
		synchronized (this ) {
			this.stub = stub;
			if (socketMsg!=null) {
				online = true;
				check = true;
			}
		}
				
		//se l'utente è online
		if(check) {
			//notifico a tutti gli amici dell'utente che adesso è online
			friends.notifyAllOnline(nickname);
					
			//aumento di 1 il numero degli utenti connessi a tutte le chatroom
			//a cui l'utente è iscritto
			chatrooms.increaseAll();
		}
	}
	
	
	/**
	 * Metodo per aggiungere un amico alla lista amici.
	 * @param User friend: user da inserire nella lista amici.
	 */
	public void addFriend (User friend) {
		//aggiungo friend alla lista amici
		if (friends.add(friend)) {
			//notifico l'utente dell'avvenimento
			notifyNewFriend(friend.getNickname());
		}
	}
	
	
	/**
	 * Metodo per stabilire una nuova amicizia.
	 * @param User friend: utente con cui vuole stringere una nuova amicizia
	 * @return true: se l'utente viene aggiunto alle amicizie,
	 * 		   false: se è già amico con esso.
	 */
	public boolean doFriendship (User friend) {
		//se l'inserimento va a buon fine 
		if (friends.add(friend)) {
			//notifico l'utente della nuova amicizia
			notifyNewFriend(friend.getNickname());
			
			//aggiorno anche la lista amici dell'amico aggiunto
			friend.addFriend(this);
			
			return true;
		}
		//se i due utenti erano già amici
		else return false;
	}
	
	
	/**
	 * Metodo che restituisce una stringa contenente i nomi degli amici dell'utente.
	 * @return String: se ha almeno un amico,
	 * 		   null, altrimenti.
	 */
	public String getFriendList () {
		return friends.getList();
	}
	
	
	/**
	 * Metodo che restituisce l'amico cercato (se è nella lista amici).
	 * @param String nickname: nickname dell'utente da cercare.
	 * @return User: se viene trovato tale utente,
	 * 		   null: altrimenti.
	 */
	public User getFriend(String nickname) {
		return friends.get(nickname);
	}
	
	
	/**
	 * Metodo per aggiungere una chatroom alla lista chatrooms.
	 * @param Chatroom chatroom: chatroom da aggiungere.
	 * @return true: se la chatroom viene agggiunta alla lista.
	 * 		   false: se l'utente ha già tale chatroom nella propria lista.
	 */
	public boolean addChatroom (Chatroom chatroom) {
		//aggiungo la chatroom alla lista chatrooms
		return chatrooms.add(chatroom);
	}
	
	
	/**
	 * Metodo per rimuovere una chatroom dalla lista chatrooms.
	 * @param Chatroom chatroom: chatroom da rimuovere.
	 */
	public void removeChatroom (Chatroom chatroom) {
		//rimuovo la chatroom alla lista chatrooms
		chatrooms.remove(chatroom);
	}
	
	
	/**
	 * Metodo che confronta la lista di chatrooms passata da parametro con
	 * la lista chatrooms dell'utente.
	 * Restituisce la lista che ha preso come argomento specificando per ogni
	 * chatrooms al suo interno se l'utente vi è iscritto o meno.
	 * @return JSONArray: se la lista passata da parametro non è null,
	 * 		   null, altrimenti.
	 */
	public JSONArray getChatroomsList (JSONArray allChatrooms) {
		return chatrooms.listChatroomSigned(allChatrooms);
	}
	
	
	/**
	 * Metodo per inviare un messaggio all'utente se online.
	 * @param String msg: stringa da inviare sullo stream.
	 * @return true: se il messaggio viene inviato correttamente.
	 * 		   false: se l'utente non è online oppure se si verifica un errore.
	 */
	public synchronized boolean sendMsg (String msg) {
		//se l'utente è online
		if (online) {
			boolean check = true;
			//mando il messaggio
			try {
				writerMsg.writeUTF(msg);
				writerMsg.flush();
			} catch (IOException e) {
				//se si verifica un errore
				e.printStackTrace();
				check = false;
			}
			return check;
		}
		else return false;
	}
	
	
	/**
	 * Metodo per notificare all'utente (se è online) che un suo amico è adesso online.
	 * @param String name: nome dell'amico che adesso è online.
	 */
	public synchronized void notifyFriendOnline (String name) {
		//se l'utente è online
		if (online) {
			//chiamo il metodo dello stub notifiche
			try {
				stub.notifyFriendOnline(name);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}


	/**
	 * Metodo per notificare all'utente (se è online) che un suo amico è adesso offline.
	 * @param String name: nome dell'amico che adesso è offline.
	 */
	public synchronized void notifyFriendOffline (String name) {
		//se l'utente è online
		if (online) {
			//chiamo il metodo dello stub notifiche
			try {
				stub.notifyFriendOffline(name);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	/**
	 * Metodo per notificare all'utente (se è online) che ha stretto una nuova amicizia.
	 * @param String name: nome del suo nuovo amico.
	 */
	public synchronized void notifyNewFriend (String name) {
		//se l'utente è online
		if (online) {
			//chiamo il metodo dello stub notifiche
			try {
				stub.notifyNewFriend(name);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	/**
	 * Metodo per notificare all'utente (se è online) che una chatroom a cui era 
	 * icritto è stata chiusa.
	 * @param String name: nome della chatroomm chiusa.
	 * @param InetAddress address: address della chatroom.
	 */
	public synchronized void notifyCloseChatroom (String name, InetAddress address) {
		//se l'utente è online
		if (online) {
			//chiamo il metodo dello stub notifiche
			try {
				stub.notifyCloseChatroom(name,address);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	/**
	 * Metodo per rendere l'utente offline.
	 */
	public void setOffline () {
		synchronized (this) {
			online = false;
			stub = null;
			
			//chiudo writerMsg e socketMsg
			try {
				writerMsg.close();
				readerMsg.close();
				socketMsg.close();
			} catch (IOException e) {}
			
			socketMsg = null;
			writerMsg = null;
			readerMsg = null;
			portFile = 0;
		}
		
		//comunico agli amici che l'utente è offline adesso
		friends.notifyAllOffline(nickname);
		
		//decremento di 1 il numero degli utenti connessi a tutte le chatroom
		//a cui l'utente è iscritto
		chatrooms.decreaseAll();
	}
}
