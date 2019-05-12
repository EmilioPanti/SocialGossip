package dataStructures;

import java.util.ArrayList;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


/**
 * Classe ListChatrooms.
 * Contiene i metodi per creare, gestire ed eseguire
 * operazioni sulle liste di chatrooms.
 * La struttura base utilizzata è un oggetto della classe
 * ArrayList <Chatroom>. 
 * @author Emilio Panti mat:531844 
 */
public class ListChatrooms {

	private ArrayList<Chatroom> listChatrooms;
	
	/**
	 * Costruttore classe ListChatrooms.
	 */
	public ListChatrooms() {
		listChatrooms = new ArrayList<Chatroom>();
	}
	
	
	/**
	 * Metodo che aggiunge una chatroom alla lista.
	 * @param Chatroom chatroom: chatroom da aggiungere.
	 * @return true: se non era già presente tale chatroom nella lista,
	 * 		   false: altrimenti.
	 */
	public synchronized boolean add(Chatroom chatroom) {
		if(!listChatrooms.contains(chatroom)) {
			listChatrooms.add(chatroom);
			return true;
		}
		else return false;
	}
	
	
	/**
	 * Metodo restituisce la chatroom che ha come id la stringa
	 * passata da parametro.
	 * @param String id: id della chatroom da cercare.
	 * @return Chatroom: se viene trovato tale chatroom,
	 * 		   null: altrimenti.
	 */
	public synchronized Chatroom get(String id) {
		//creo un iteratore per scorrere la lista chatroom
		Iterator<Chatroom> iterator = listChatrooms.iterator();
		
		//chatroom che restituisco
		Chatroom found = null;
		
		while (iterator.hasNext() && found==null) {
			//prendo una chatroom dalla lista
			Chatroom chatroom = iterator.next();
			
			//se ha l'id che cercavo
			if(id.equals(chatroom.getId())) found = chatroom;
			
		}
		
		return found;
	}
	
	
	/**
	 * Metodo che rimuove la chatroom passata come parametro dalla lista.
	 * @param Chatroom chatroom: chatroom da rimuovere.
	 */
	public synchronized void remove(Chatroom chatroom) {
		listChatrooms.remove(chatroom);
	}
	
	
	/**
	 * Metodo che prende come argomento un JSONArray contenente oggetti JSON. 
	 * Ogni oggetto JSON contiene il nome e l'indirizzo di una chatroom.
	 * A tali oggetti JSON viene aggiunto un booleano in base al fatto o meno
	 * che la chatroom sia presente anche in listChatrooms.
	 * @param JSONArray allChatrooms: JSONArray contenente le info su tutte 
	 * 								  le chatrooom registrate sul server.
	 * @return JSONArray: contiene allChatrooms aggiornata come sopra descritto
	 * 		   null: se il allChatrooms è null
	 */
	@SuppressWarnings("unchecked")
	public synchronized JSONArray listChatroomSigned(JSONArray allChatrooms) {
		
		if(allChatrooms==null) return null;
		
		//creo un iteratore per scorrere il JSONArray
		Iterator<JSONObject> iterator = allChatrooms.iterator();
				
		while (iterator.hasNext()) {
			//prendo un oggetto json della lista
			JSONObject objJSON = iterator.next();
					
			//prendo il nome della chatroom
			String id = (String) objJSON.get("ID");
					
			//se la chatroom è presente in listChatrooms
			if(this.get(id) != null) {
				objJSON.put("SIGNED", true);
			}
			//altrimenti
			else objJSON.put("SIGNED", false);
		}
		
		return allChatrooms;
	}
	
	
	/**
	 * Metodo che incrementa il numero degli utenti connessi di 1 per ogni
	 * chatroom presente nella lista.
	 */
	public synchronized void increaseAll() {
		//creo un iteratore per scorrere la lista chatroom
		Iterator<Chatroom> iterator = listChatrooms.iterator();
				
		while (iterator.hasNext()) {
			//prendo una chatroom dalla lista
			Chatroom chatroom = iterator.next();
			chatroom.increaseUsersConn();	
		}
	}
	
	
	/**
	 * Metodo che decrementa il numero degli utenti connessi di 1 per ogni
	 * chatroom presente nella lista.
	 */
	public synchronized void decreaseAll() {
		//creo un iteratore per scorrere la lista chatroom
		Iterator<Chatroom> iterator = listChatrooms.iterator();
						
		while (iterator.hasNext()) {
			//prendo una chatroom dalla lista
			Chatroom chatroom = iterator.next();
			chatroom.decreaseUsersConn();	
		}
	}
}
