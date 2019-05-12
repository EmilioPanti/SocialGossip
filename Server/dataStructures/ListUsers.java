package dataStructures;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;


/**
 * Classe ListUsers.
 * Contiene i metodi per creare, gestire ed eseguire
 * operazioni sulle liste di utenti.
 * La struttura base utilizzata è un oggetto della classe
 * ArrayList <User>. 
 * @author Emilio Panti mat:531844 
 */
public class ListUsers {

	private ArrayList<User> listUsers;
	
	
	/**
	 * Costruttore classe ListUsers.
	 */
	public ListUsers() {
		listUsers = new ArrayList<User>();
	}
	
	
	/**
	 * Metodo che aggiunge un utente alla lista.
	 * @param User user: utente da aggiungere.
	 * @return true: se non era già presente tale utente nella lista,
	 * 		   false: altrimenti.
	 */
	public synchronized boolean add(User user) {
		if(!listUsers.contains(user)) {
			listUsers.add(user);
			return true;
		}
		else return false;
	}
	
	
	/**
	 * Metodo restituisce l'utente che ha per nickname la stringa
	 * passata da parametro.
	 * @param String nickname: nickname dell'utente da cercare.
	 * @return User: se viene trovato tale utente,
	 * 		   null: altrimenti.
	 */
	public synchronized User get(String nickname) {
		//creo un iteratore per scorrere la lista utenti
		Iterator<User> iterator = listUsers.iterator();
		
		//utente che restituisco
		User found = null;
		
		while (iterator.hasNext() && found==null) {
			//prendo un utente dalla lista
			User user = iterator.next();
			
			//se ha il nickname che cercavo
			if(nickname.equals(user.getNickname())) found = user;
			
		}
		
		return found;
	}
	
	
	/**
	 * Metodo che restituisce una stringa contenente i nomi degli utenti in lista.
	 * @return String: se ha almeno un utente la lista,
	 * 		   null, altrimenti.
	 */
	public synchronized String getList() {
		//se la lista amici è vuota ritorno null
		if(listUsers.isEmpty()) return null;
		
		//altrimenti creo un iteratore per scorrere la lista utenti
		Iterator<User> iterator = listUsers.iterator();
				
		//Stringa contente i nickname
		String str = "";
				
		//stringa end of line
		String eol = System.getProperty("line.separator");
				
		while (iterator.hasNext()) {
			//prendo un utente dalla lista
			User user = iterator.next();
			
			//aggiungo il nikname
			str = str + user.getNickname() + eol;
		}
				
		return str;
	}
	
	
	/**
	 * Metodo per notificare a tutti gli utenti della lista che 
	 * l'utente passato da parametro è adesso online.
	 * @param String name: nome dell'utente.
	 */
	public synchronized void notifyAllOnline(String name) {
		//creo un iteratore per scorrere la lista utenti
		Iterator<User> iterator = listUsers.iterator();
				
		while (iterator.hasNext()) {
			//prendo un utente dalla lista
			User user = iterator.next();
			
			//notifico l'evento all'utente
			user.notifyFriendOnline(name);;
		}
	}
	
	
	/**
	 * Metodo per notificare a tutti gli utenti della lista che 
	 * l'utente passato da parametro è adesso offline.
	 * @param String name: nome dell'utente.
	 */
	public synchronized void notifyAllOffline(String name) {
		//creo un iteratore per scorrere la lista utenti
		Iterator<User> iterator = listUsers.iterator();
				
		while (iterator.hasNext()) {
			//prendo un utente dalla lista
			User user = iterator.next();
			
			//notifico l'evento all'utente
			user.notifyFriendOffline(name);;
		}
	}
	
	
	/**
	 * Metodo per notificare a tutti gli utenti della lista che 
	 * la chatoom passata da parametro è stata chiusa e rimuoverla
	 * dalla loro lista chatrooms.
	 * @param Chatroom chatroom: chatroom.
	 */
	public synchronized void notifyAllClose(Chatroom chatroom) {
		//prendo il nome e l'address della chatroom
		String name = chatroom.getId();
		InetAddress address = chatroom.getAddress();
				
		//creo un iteratore per scorrere la lista utenti
		Iterator<User> iterator = listUsers.iterator();
				
		while (iterator.hasNext()) {
			//prendo un utente dalla lista
			User user = iterator.next();
			
			//rimuovo la chatroom dalla sua lista
			user.removeChatroom(chatroom);
			
			//notifico l'evento all'utente
			user.notifyCloseChatroom(name,address);
		}
	}
}
