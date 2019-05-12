package dataStructures;

import java.net.InetAddress;

import org.json.simple.JSONObject;


/**
 * Classe Chatroom.
 * Contiene i metodi per creare ed eseguire operazioni
 * sulle chatroom.
 * @author Emilio Panti mat:531844 
 */
public class Chatroom {
	
	//nome chatroom
	private String id;
	
	//creatore della chatroom
	private String creator;
		
	//utenti connessi
	private int usersConn;
		
	//lista iscritti
	private ListUsers subscribers;
		
	//multicast address della chatroom
	private InetAddress address;

	//variabile per stabilire se una chatroom è attiva o in chiusura
	private boolean active;
	
	
	/**
	 * Costruttore classe Chatroom.
	 * @param String id: nome della chatroom.
	 * @param User user: utente creatore della chatroom.
	 */
	public Chatroom(String id, User user) {
		this.id = id;
		creator = user.getNickname();
		
		active = true;
		usersConn = 0;
		
		subscribers = new ListUsers();
		//aggiungo il creatore agli iscritti
		addSubscriber(user);
	}

	
	/**
	 * Override del metodo equals.
	 * @param: Object obj: oggetto da confrontare.
	 * @return true: se le due chatrooms hanno lo stesso id,
	 * 		   false: altrimenti.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj==null) return false;
		Chatroom chatroom = (Chatroom) obj;
		return id.equals(chatroom.getId());
	}
	
	
	/**
	 * Override del metodo hashCode.
	 * Ho effettuato l'override di hashCode solo per formalità 
	 * dato che ho fatto l'ovveride di equals.
	 * @return int: restituisce il risultato del metodo hashCode chiamato
	 * 				sull'id della chatroom (anche il metodo equals di cui ho 
	 * 				fatto l'override sopra si basa sul confronto del campo id)
	 */
	@Override
	public int hashCode() {
		return id.hashCode();
	}

	
	/**
	 * Metodo per settare l'indirizzo multicast della chatroom.
	 * @param InetAddress: indirizzo multicast della chatroom
	 */
	public void setAddress(InetAddress address) {
		this.address = address;
	}

	
	/**
	 * Metodo che restituisce l'address della chatroom
	 * @return InetAddress: address della chatroom
	 */
	public InetAddress getAddress() {
		return address;
	}
	

	/**
	 * Metodo che restituisce l'id della chatroom
	 * @return String: nome della chatroom
	 */
	public String getId() {
		return id;
	}

	
	/**
	 * Metodo per incrementare gli utenti online nella chatroom.
	 */
	public synchronized void increaseUsersConn() {
		usersConn++;
	}

	
	/**
	 * Metodo per decrementare gli utenti online nella chatroom.
	 */
	public synchronized void decreaseUsersConn() {
		usersConn--;
	}
	
	
	/**
	 * Metodo per sapere se ci sono almeno due utenti online
	 * iscritti alla chatroom.
	 * @return true: se ci sono,
	 * 		   false: se non ci sono o se la chatroom è in chiusura
	 */
	public synchronized boolean checkUsersConn() {
		if(active && usersConn>1) return true;
		else return false;
	}
	
	
	/**
	 * Metodo che restituisce un oggetto in formato JSON contente l'id
	 * e l'indirizzo della chatroom.
	 * @return JSONObject: se la chatroom è ancora attiva
	 * 		   null: se la chatroom è in chiusura.
	 */
	@SuppressWarnings("unchecked")
	public synchronized JSONObject getInfoJSON() {
		if(active) {
			JSONObject info = new JSONObject();
			info.put("ID", id);
			info.put("ADDRESS", address.getHostAddress().toString());
			return info;
		}
		else return null;
	}


	/**
	 * Metodo per controllare se il nickname passato da parametro
	 * corrisponde al nome del creatore della chatroom.
	 * @param String nickname: nickname da controllare.
	 * @return true: se i due nomi corrispondono,
	 * 		   false: altrimenti.
	 */
	public boolean checkCreator (String nickname) {
		return (creator.equals(nickname));
	}
	
	
	/**
	 * Metodo per aggiungere un utente alla lista iscritti.
	 * @param User user: user da inserire nella lista iscritti.
	 * @return true: se non era già presente tale utente nella lista,
	 * 		   false: se era già iscritto oppure se la chatroom 
	 * 				  non è più attiva ma ancora non è stata eliminata
	 * 				  dalla hash map.
	 */
	public boolean addSubscriber (User user) {
		boolean check = false;
		
		synchronized (this) {
			if (active) check = true;
		}
		
		if (check) {
			if(subscribers.add(user)) {
				//aumento di 1 gli utenti connessi
				increaseUsersConn();
				return true;
			}
			else return false;
		}
		else return false;
	}
	
	
	/**
	 * Metodo per disattivare una chatroom e notificare a tutti i suoi
	 * iscritti che sta per essere chiusa.
	 * @return InetAddress: l'address che utilizzava.
	 */
	public InetAddress close() {
		//"disattivo" la chatroom in attesa che venga eliminata dalla hash 
		//map che la contiene
		synchronized (this) {
			active = false;
		}
		
		//notifico a tutti i suoi iscritti che sta per essere cancellata
		subscribers.notifyAllClose(this);
		
		return address;
	}
}
