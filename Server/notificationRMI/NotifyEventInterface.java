package notificationRMI;

import java.net.InetAddress;
import java.rmi.Remote;
import java.rmi.RemoteException;


/**
 * Interfaccia NotifyEventInterface.
 * Contiene le dichiarazioni dei metodi utilizzati dal server per notificare
 * al client remoto alcuni eventi, fra cui:
 * - cambiamento di status di un amico (online/offline),
 * - nuova amicizia,
 * - chiusura di una chatroom.
 * @author Emilio Panti mat:531844 
 */
public interface NotifyEventInterface extends Remote {
	
	/**
	 * Metodo che può essere richiamato dal server per notificare al client
	 * che un suo amico è adesso online.
	 * @param String Nickname: nickname dell'utente che è online
	 * @throws RemoteException
	 */
	public void notifyFriendOnline(String nickname) throws RemoteException;
	
	
	/**
	 * Metodo che può essere richiamato dal server per notificare al client
	 * che un suo amico è adesso offline.
	 * @param String Nickname: nickname dell'utente che è andato offline
	 * @throws RemoteException
	 */
	public void notifyFriendOffline(String nickname) throws RemoteException;
	
	
	/**
	 * Metodo che può essere richiamato dal server per notificare al client
	 * una nuova amicizia .
	 * @param String Nickname: nickname dell'utente che ha stretto l'amicizia
	 * @throws RemoteException
	 */
	public void notifyNewFriend(String nickname) throws RemoteException;
	
	
	/**
	 * Metodo che può essere richiamato dal server per notificare al client
	 * la chiusura di una chatroom da parte del suo creatore.
	 * @param String chatroom: nome della chatroom che è stata chiusa
	 * @param InetAddress address: address della chatroom.
	 * @throws RemoteException
	 */
	public void notifyCloseChatroom(String chatroom,InetAddress address) 
			throws RemoteException;
}
