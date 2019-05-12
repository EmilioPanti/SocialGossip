package notificationRMI;

import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.util.concurrent.ConcurrentHashMap;

import dataStructures.User;


/**
 * Classe ServerImpl.
 * Contiene le implementazioni dei metodi dichiarati nell'interfaccia 
 * ServerInterface.
 * @author Emilio Panti mat:531844 
 */
public class ServerImpl extends RemoteObject implements ServerInterface{

	private static final long serialVersionUID = 1L;

	//hash map degli utenti
	private ConcurrentHashMap <String,User> hashUsers;
	
	
	/**
	 * Costruttore classe ServerImpl.
	 * @param ConcurrentHashMap hashUsers: hash map degli utenti.
	 */
	public ServerImpl(ConcurrentHashMap<String,User> hashUsers) 
			throws RemoteException {
		this.hashUsers = hashUsers;
	}

	
	/**
	 * Metodo che può essere richiamato dal client per registrarsi alla callback
	 * e poter ricevere le notifiche che lo riguardano.
	 * @param NotifyEventInterface clientInterface: stub del client utilizzato 
	 * 			dal server per richiamare i metodi che notificano l'eventi.
	 * @param String nickname: nickname dell'utente che ha richiesto la registrazione
	 * 			 alla callback
	 * @throws RemoteException
	 */
	 public void registerForCallback (NotifyEventInterface clientInterface,
			 String nickname) throws RemoteException {
		
		//cerco l'utente nella hash table utenti
		User user = hashUsers.get(nickname);
		
		//salvo lo stub nella sua struttura dati 
		if (user!=null) user.setStubNotify(clientInterface);
	}
	
}
