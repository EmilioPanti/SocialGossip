package threads.listeners;

import java.io.IOException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import client.ClientMain;
import graphicInterfaces.ChatHandlerGUI;


/**
 * Classe ListenerFile.
 * Thread in attesa (sul ServerSocketChannel aperto dal ClientMain)
 * di nuove connessioni da parte di utenti che vogliono inviare 
 * dei file a questo client.
 * Per ogni nuova connessione viene creato un thread (della classe 
 * UnloaderFileThread) che si occupa di scaricare il file inviatogli
 * dal client dell'altro utente.
 * @author Emilio Panti mat:531844 
 */
public class ListenerFile implements Runnable {
	
	//interfaccia che gestisce le chats
	private ChatHandlerGUI chatHandlerGUI;

		
	/**
	 * Costruttore classe ListenerFile.
	 * @param ChatHandlerGUI chatHandlerGUI: interfaccia che gestisce le chat.
	 */
	public ListenerFile(ChatHandlerGUI chatHandlerGUI) {
		this.chatHandlerGUI = chatHandlerGUI;
	}
		

	/**
	 * Task che è in ascolto sul socket server channel
	 * per ricevere eventuali file da altri utenti
	 */
	public void run() {
		//prendo il server socket channel e lo metto in modalità bloccante
		ServerSocketChannel serverSocket = ClientMain.SERVER_SOCKET_FILE;
		try {
			serverSocket.configureBlocking(true);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		//mi metto in attesa di ricevere connessioni
		while (true) {
			
			SocketChannel socketChannel = null;
			try {
				//ricevo una nuova connessione 
				socketChannel = serverSocket.accept();
				socketChannel.configureBlocking(true);
			} catch (IOException e) {
				//se la connessione verso un altro client va male
				socketChannel = null;
			}
			
			if(socketChannel!=null) {
				//passo la connessione al thread che si occupa di scaricare il file
				UnloaderFileThread unloaderFileThread = 
						new UnloaderFileThread(chatHandlerGUI, socketChannel);
				Thread thread = new Thread(unloaderFileThread);
				thread.start();
			}
			
		}
	}
		
}
