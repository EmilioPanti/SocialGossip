package threads.listeners;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import client.ClientMain;
import graphicInterfaces.ChatHandlerGUI;


/**
 * Classe UnloaderFileThread.
 * Thread a cui viene passato (al momento della sua
 * creazione) un socket channel, in cui verrà inviato 
 * un file.
 * Compito di questo thread è scaricare tale file e 
 * salvarlo nella cartella specificata nel ClientMain.
 * @author Emilio Panti mat:531844 
 */
public class UnloaderFileThread implements Runnable {
	
	//interfaccia che gestisce le chats
	private ChatHandlerGUI chatHandlerGUI;

	//socket channel in cui verrà spedito il file 
	private SocketChannel socketChannel;
		
	/**
	 * Costruttore classe UnloaderFileThread.
	 * @param ChatHandlerGUI chatHandlerGUI: interfaccia che gestisce le chat.
	 * @param SocketChannel socketChannel: socket channel in cui verrà spedito
	 * 									   il file.
	 */
	public UnloaderFileThread(ChatHandlerGUI chatHandlerGUI, 
			SocketChannel socketChannel) {
		this.chatHandlerGUI = chatHandlerGUI;
		this.socketChannel = socketChannel;
	}
		

	/**
	 * Task che riceve un file dal socket channel e lo 
	 * salva nella apposita cartella.
	 */
	public void run() {
		//prendo il path della cartella dove salvare il file
		String path = ClientMain.PATH_FILE_REICEVED;
		
		ByteBuffer buffer = ByteBuffer.allocate(400);
		
		try {
			//leggo l'oggetto json contenente le info sul file
			socketChannel.read(buffer);
			buffer.flip();
			
			//creo un byte array della lunghezza del numero di bytes presenti in 'input'
			byte[] bytes = new byte[buffer.remaining()];
			buffer.get(bytes);
			 
			//trasformo in stringa il bytes array
			String info = new String(bytes);
			
			//trasformo in formato JSON la stringa letta e prendo i campi d'interesse
			JSONObject infoJSON = 	(JSONObject) new JSONParser().parse(info);
			String sender = (String) infoJSON.get("SENDER");
			String fileName = (String) infoJSON.get("FILE");
			long size = (long) infoJSON.get("SIZE");
			
			//mando al sender un segnale che ho ricevuto le info
			buffer.clear();
			buffer.put("ok".getBytes());
			buffer.flip();
			while(buffer.hasRemaining()) socketChannel.write(buffer);
			
			//creo il file e apro il file channel (se esiste di già lo sovrascrivo)
			FileChannel fileChannel = FileChannel.open(Paths.get(path+fileName),
					StandardOpenOption.WRITE,StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING);
			
			//collego direttamente il file channel al socket channel per il trasferimento
			fileChannel.transferFrom(socketChannel, 0, size);
			
			//faccio sapere all'utente che ha ricevuto un file dal sender
			String txt = "["+sender+"]: has sent you the file \""+fileName+"\"";
			chatHandlerGUI.postMsgChatNickname(sender, txt);
			
		} catch (Exception e) {
			//se l'utente che sta inviando il file si disconnette 
			//e vengono alzate delle eccezioni a seguito di ciò 
			//non faccio niente.
			e.printStackTrace();
		}
	}

}
