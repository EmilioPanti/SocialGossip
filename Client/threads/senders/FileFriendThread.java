package threads.senders;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import client.ClientMain;
import enumerations.Operations;
import graphicInterfaces.ChatHandlerGUI;
import graphicInterfaces.ChatNicknameGUI;
import graphicInterfaces.ResponseGUI;


/**
 * Classe FileFriendThread.
 * Thread che si occupa di inviare un file ad un altro utente.
 * Per prima cosa controlla che il file esista all'interno della
 * apposita cartella specificata nella classe ClientMain.
 * Se esiste, il server invia l'address dell'utente destinatario 
 * e la porta in cui è in ascolto per eventuali file.
 * Dopo aver raccolto le informazioni necessarie invia il file 
 * tramite un socket channel.
 * Se l'operazione va a buon fine aggiorna la chat verso l'utente
 * destinatario, altrimenti la chat viene chiusa.
 * @author Emilio Panti mat:531844 
 */
public class FileFriendThread implements Runnable {

	//interfaccia della chat verso l'utente
	private ChatNicknameGUI chatNicknameGUI;
	
	//interfaccia che gestisce le chat aperte
	private ChatHandlerGUI chatHandlerGUI;
		
	//nickname dell'amico a cui si vuole inviare il file
	private String receiver;
	
	//nome del file da inviare
	private String fileName;
	
	
	/**
	 * Costruttore classe FileFriendThread.
	 * @param ChatNicknameGUI chatNicknameGUI: chat aperta verso l'utente destinatario.
	 * @param ChatHandlerGUI chatHandlerGUI: interfaccia che gestisce le chat.
	 * @param String receiver: nickname dell'utente destinatario.
	 * @param String fileName: nome del file da inviare.
	 */
	public FileFriendThread(ChatNicknameGUI chatNicknameGUI,ChatHandlerGUI chatHandlerGUI,
			String receiver, String fileName) {
		this.chatNicknameGUI = chatNicknameGUI;
		this.chatHandlerGUI = chatHandlerGUI;
		this.receiver = receiver;
		this.fileName = fileName;
	}


	/**
	 * Task che esegue l'operazione di invio file ad un utente.
	 */
	@SuppressWarnings("unchecked")
	public void run() {
		//path della cartella in cui cerco il file da inviare
		String path = ClientMain.PATH_MY_FILE;
		
		//variabile di controllo
		boolean check = true;
		
		//controllo che esista il file nella cartella apposita cercando di 
		//aprire un file channel verso esso
		FileChannel fileChannel = null;
		try {
			fileChannel = FileChannel.open(Paths.get(path+fileName),StandardOpenOption.READ);
		} catch (IOException e) {
			//se non esiste il file 
			check = false;
		}
		
		
		//se il file che vuole inviare l'utente esiste
		if(check) {
			//prendo gli streams per comunicare con il server
			DataOutputStream writer = ClientMain.WRITER;
			DataInputStream reader = ClientMain.READER;
			
			//creo la richiesta di invio file
			JSONObject request = new JSONObject();
			request.put("OP", "FILE_FRIEND");
			request.put("ID", receiver);
							
			//variabili per gestire la riposta del server
			String response = null;
			JSONObject responseJSON = null;
											
			try {
				//mando la richiesta
				writer.writeUTF(request.toJSONString());
				writer.flush();
												
				//ricevo la risposta
				response = reader.readUTF();
											
				//trasformo in formato JSON la risposta ricevuta dal server
				responseJSON = 	(JSONObject) new JSONParser().parse(response);
												
			} catch (Exception e) {
				//se c'è qualche problema di comunicazione con il server
				//o se non è possibile parsare il messaggio ricevuto
				e.printStackTrace();
				//termino il client
				ClientMain.cleanUp();
			}
	
											
			//controllo l'esito della risposta
			Operations esito = Operations.valueOf((String) responseJSON.get("OP"));
			if (esito==Operations.OP_OK) {
				//prendo l'indirizzo e la porta del destinatario
				String address = (String) responseJSON.get ("ADDRESS");
				long port = (long) responseJSON.get ("PORT_FILE");
				
				//chiamo il metodo per inviare il file all'address mandato dal server
				try {
					sendFile(address,port,fileChannel);
					
					//comunico all'utente l'invio del file 
					String txt = "["+ClientMain.NICKNAME+"]: you have sent the file \""+
							fileName+"\" to \""+receiver+"\"";
					chatNicknameGUI.appendTextChat(txt);
				} catch (IOException e) {
					//eccezione se l'utente a cui si invia il file si disconnette
					String msgErr = "ERR: \""+receiver+"\" is offline now";
										
					//chiudo la chat verso l'utente
					chatHandlerGUI.removeChatNickname(receiver);
					
					//apro l'interfaccia grafica per comunicare l'errore
					ResponseGUI responseGUI = new ResponseGUI(msgErr);
					responseGUI.setVisible(true);
				}
			}
			else {
				//prendo il messaggio di errore trasmesso dal server
				String msgErr = (String) responseJSON.get ("MSG");
									
				//chiudo la chat verso l'utente
				chatHandlerGUI.removeChatNickname(receiver);
				
				//apro l'interfaccia grafica per comunicare l'errore
				ResponseGUI responseGUI = new ResponseGUI(msgErr);
				responseGUI.setVisible(true);
			}
		}
		//se non esiste il file
		else {
			String msgErr = "ERR: The file \""+fileName+
					"\" does not exist in "+path;
			
			//apro l'interfaccia grafica per comunicare l'errore
			ResponseGUI responseGUI = new ResponseGUI(msgErr);
			responseGUI.setVisible(true);
		}
	}

	
	/**
	 * Metodo che invia all'address passato da parametro il file collegato
	 * al file channel (passato anch'esso da parametro)
	 * @param: String address: host address del client a cui inviare il file.
	 * @param: long port: porta in cui il ricevente è in ascolto di eventuali file.
	 * @param: FileChannel fileChannel: channel aperto verso il file da inviare.
	 * @throws IOException : se l'utente a cui vuole inviare il file si è disconnesso.
	 */
	@SuppressWarnings("unchecked")
	private void sendFile(String address, long port, FileChannel fileChannel) 
			throws IOException {
		
		//prendo il nickname dell'utente che invia il file
		String nickname = ClientMain.NICKNAME;
	
		//apro la connessione
		SocketAddress isa = new InetSocketAddress(address,(int) port);
		SocketChannel socketChannel = SocketChannel.open(isa);
		socketChannel.configureBlocking(true);
		ByteBuffer buffer = ByteBuffer.allocate(400);
		
		long size = fileChannel.size();
		
		//creo l'oggetto json dove inserisco il nome dell'utente, il nome file
		//e la size del file
		JSONObject obj = new JSONObject();
		obj.put("SENDER", nickname);
		obj.put("FILE", fileName);
		obj.put("SIZE", size);
		
		//invio l'oggetto json al receiver
		buffer.put(obj.toJSONString().getBytes());
		buffer.flip();
		while(buffer.hasRemaining()) socketChannel.write(buffer);
		buffer.clear();
		
		//ricevo dal receiver un segnale che ha ricevuto le info
		socketChannel.read(buffer);
		buffer.flip();
		
		//mando il file
		fileChannel.transferTo(0, size, socketChannel);
	}
}
