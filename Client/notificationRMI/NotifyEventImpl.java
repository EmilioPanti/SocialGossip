package notificationRMI;

import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;

import graphicInterfaces.ChatHandlerGUI;
import graphicInterfaces.OperativeGUI;
import graphicInterfaces.ResponseGUI;


/**
 * Classe NotifyEventImpl.
 * Contiene le implementazioni dei metodi dichiarati nell'interfaccia
 * NotifyEventInterface.
 * @author Emilio Panti mat:531844 
 */
public class NotifyEventImpl extends RemoteObject implements NotifyEventInterface {
	
	private static final long serialVersionUID = 1L;
	
	//interfacce grafiche dove verrano segnalati gli eventi
	private ChatHandlerGUI chatHandlerGUI;
	private OperativeGUI operativeGUI;
	
	
	/**
	 * Costruttore classe NotifyEventImpl.
	 * crea un nuovo callback client.
	 * @param ChatHandlerGUI chatHandlerGUI: interfaccia che gestisce
	 * 			le chat aperte.
	 * @param OperativeGUI operativeGUI: interfaccia operativa.
	 * @throws RemoteException
	 */
	public NotifyEventImpl(ChatHandlerGUI chatHandlerGUI, 
			OperativeGUI operativeGUI) throws RemoteException { 
		this.chatHandlerGUI = chatHandlerGUI;
		this.operativeGUI = operativeGUI;
	}

	
	/**
	 * Metodo che può essere richiamato dal server per notificare al client
	 * che un suo amico è adesso online.
	 * @param String Nickname: nickname dell'utente che è online
	 * @throws RemoteException
	 */
	public void notifyFriendOnline(String nickname) throws RemoteException {
		//scrivo l'evento nell'area di testo delle notifiche
		operativeGUI.appendTextNotifications(nickname + " is now online");
	}
	
	
	/**
	 * Metodo che può essere richiamato dal server per notificare al client
	 * che un suo amico è adesso offline.
	 * @param String Nickname: nickname dell'utente che è andato offline
	 * @throws RemoteException
	 */
	public void notifyFriendOffline(String nickname) throws RemoteException {
		//scrivo l'evento nell'area di testo delle notifiche
		operativeGUI.appendTextNotifications(nickname + " is now offline");
	}
	
	
	/**
	 * Metodo che può essere richiamato dal server per notificare al client
	 * una nuova amicizia.
	 * @param String Nickname: nickname dell'utente diventato amico.
	 * @throws RemoteException
	 */
	public void notifyNewFriend(String nickname) throws RemoteException {
		//scrivo l'evento nell'area di testo delle notifiche
		operativeGUI.appendTextNotifications("You and "+ nickname + " are now friends");
		
		//aggiungo il nuovo amico nell'area di testo riservata alla lista amici
		operativeGUI.appendTextFriends(nickname);
	}
	
	
	/**
	 * Metodo che può essere richiamato dal server per notificare al client
	 * la chiusura di una chatroom da parte del suo creatore.
	 * @param String chatroom: nome della chatroom che è stata chiusa
	 * @param InetAddress address: address della chatroom.
	 * @throws RemoteException
	 */
	public void notifyCloseChatroom(String chatroom, InetAddress address)
			throws RemoteException {
		//scrivo l'evento nell'area di testo delle notifiche
		operativeGUI.appendTextNotifications("The chatroom '"+ chatroom + "' has been closed");
		
		//cancello la chat aperta verso la chatroom chiusa
		chatHandlerGUI.removeChatroom(chatroom, address);
		
		/**
		 * apro anche una interfaccia grafica per comunicare che la chat è stata rimossa 
		 * dalla interfaccia che gestisce le chat aperte, questo perchè ritengo opportuno 
		 * richiamare l'attenzione dell'utente in modo più rilevante rispetto alle notifiche
		 * di cambiamento status di un amico e di nuova amicizia.
		 */
		ResponseGUI responseGUI = new ResponseGUI("The chatroom '"+ chatroom + "' has been closed");
		responseGUI.setVisible(true);
	}
}
