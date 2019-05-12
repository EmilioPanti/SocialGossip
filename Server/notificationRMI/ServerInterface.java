package notificationRMI;

import java.rmi.Remote;
import java.rmi.RemoteException;


/**
 * Interfaccia ServerInterface.
 * Contiene le dichiarazioni dei metodi offerti in remoto dal server 
 * per permettere ai clients di registrarsi alla callback e ricevere
 * così le notifiche che li riguardano.
 * @author Emilio Panti mat:531844 
 */
public interface ServerInterface extends Remote {

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
			 String nickname) throws RemoteException;
}
