package dataStructures;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


/**
 * Classe HashChatrooms.
 * Struttura dati per la gestione delle chatrooms, che
 * utilizza due 'sotto-strutture dati':
 * - una hash map (oggetto della classe HashMap <String,Chatroom>),
 * - una lista di InetAddress (oggetto della classe LinkedList <InetAddress>).
 * Nella hash map vengono inserite e rimosse le chatrooms. I metodi
 * che operano su di essa sono sincronizzati in modo manuale per 
 * evitare inconsistenze. E' previsto un numero massimo di chatrooms
 * esistenti, deciso dalla variabile statica MAX.
 * La lista contiene MAX InetAddress di indirizzi multicast.
 * Ogni volta che viene inserita una chatroom nella hash map le viene
 * assegnato un indirizzo multicast, prelevato dalla lista.
 * Se la lista è vuota e se la hash map ha raggiunto MAX elementi
 * non è possibile inserire nuove chatrooms.
 * Quando una chatroom viene chiusa e rimossa dalla hash map,
 * viene reinserito nella lista degli InetAddress l'indirizzo
 * che utilizzava, così da renderlo nuovamente disponibile.
 * @author Emilio Panti mat:531844 
 */
public class HashChatrooms {

	//numero massimo di chatrooms
	public static int MAX = 256; 
	
	//base address (le possibili 256 chatrooms hanno una parte di indirizzo 
	//multicast uguale)
	public static String baseAddress = "224.0.0.";
	
	//Lista che contiene gli indirizzi multicast disponibili per le chtrooms
	private LinkedList<InetAddress> listAddress;
	
	//hash map che contiene le chatrooms
	private HashMap <String,Chatroom> hc;
	
	
	/**
	 * Costruttore classe HashChatrooms.
	 */
	public HashChatrooms() {
		hc = new HashMap <String,Chatroom>();
		listAddress = new LinkedList<InetAddress>();
		
		//inserisco MAX address nella lista address
		int i = 0;
		for(i=0;i<MAX;i++) {
			String addressStr = baseAddress + i;
			try {
				InetAddress address = InetAddress.getByName(addressStr);
				//aggiungo il nuovo address alla lista
				listAddress.add(address);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	/**
	 * Metodo per aggiungere una nuova chatroom.
	 * @param Chatroom chatroom: chatroom da aggiungere.
	 * @return InetAddress: l'indirizzo della chatroom appena aggiunta
	 * 		   null : se è già stato raggiunto il numero max o 
	 * 				  se già esiste una chatroom con lo stesso nome.
	 */
	public synchronized InetAddress add(Chatroom chatroom) {
		//se ho raggiunto il numero max
		if ((hc.size() >= MAX) || (listAddress.isEmpty())) return null;
		
		//altrimenti prendo il nome della chatroom
		String id = chatroom.getId();
		
		//se esiste già una chatroom con tale nome
		if (hc.containsKey(id)) return null;
		else {
			//prelevo un indirizzo disponibile dalla list address 
			//e lo assegno alla chatroom
			InetAddress address = listAddress.remove();
			chatroom.setAddress(address);
			
			//la aggiungo alla hash map
			hc.put(id,chatroom);
			
			return address;
		}
	}
	
	
	/**
	 * Metodo per cercare e restituire una chatroom.
	 * @param String id: stringa che contiene il nome della chatroom.
	 * @return Chatroom: la chatroom cercata
	 * 		   null : se non esiste una chatroom con tale nome.
	 */
	public synchronized Chatroom get(String id) {
		return hc.get(id);
	}
	
	
	/**
	 * Metodo per rimuovere e cancellare una chatroom.
	 * @param String id: nome della chatroom da cancellare .
	 * @param String id: nome dell'utente che ha chiesto di cancellarla.
	 * @return true: se la chatroom viene cancellata correttamente
	 * 		   false: se la chatroom non esiste o se il creatore è un altro.
	 */
	public boolean remove(String id, String nickname) {
		//cerco la chatroom
		Chatroom chatroom = this.get(id);
		
		//se esiste
		if(chatroom!=null) {
			//se il creatore è lo stesso che vuole cancellarla
			if(chatroom.checkCreator(nickname)) {
				
				synchronized (this) {
					//chiamo la funzione di chiusura nella chatroom
					//che mi restituisce l'address che utilizzava
					InetAddress address = chatroom.close();
					
					//rimuovo la chatroom dalla hash map
					hc.remove(id);
					
					//rendo nuovamente disponibile l'address
					listAddress.add(address);
				}
				
				return true;
			}
			//se non è il creatore
			else return false;
		}
		else return false;
	}
	
	
	/**
	 * Metodo per rimuovere e cancellare una chatroom.
	 * @return JSONArray: se la hashmap ha almeno un elemento,
	 * 		   null: altrimenti.
	 */
	@SuppressWarnings("unchecked")
	public synchronized JSONArray getListChatrooms() {
		//controllo che non sia vuota la hash map
		if(hc.isEmpty()) return null;
		
		JSONArray listChatrooms = new JSONArray();
		
		//inserisco tutte le chatroom nel JSONarry
		for(Chatroom chatroom : hc.values()) {
			//prendo le info della chatroom
			JSONObject infoJSON = chatroom.getInfoJSON();
			
			//lo aggiungo al JSONArray
			if(infoJSON != null) listChatrooms.add(infoJSON);
		}
		
		return listChatrooms;
	}
}
