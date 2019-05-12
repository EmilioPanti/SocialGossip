package threads;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ConcurrentHashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import dataStructures.Chatroom;
import dataStructures.HashChatrooms;
import dataStructures.User;
import enumerations.Operations;


/**
 * Classe Worker.
 * Thread che gestisce una connessione verso un
 * client finchè essa non viene chiusa.
 * @author Emilio Panti mat:531844 
 */
public class Worker implements Runnable {
		
	//hash map per gli utenti
	private ConcurrentHashMap <String,User> hashUsers;
				
	//hash map per le chatrooms
	private HashChatrooms hashChatrooms;
	
	//socket e streams per la connessione
	private Socket socket;
	private DataOutputStream writer = null;
	private DataInputStream reader = null;
		
	//utente che si è registrato/connesso
	private User user = null;
	
	
	/**
	 * Costruttore classe Worker.
	 * @param Socket socket: socket della connessione verso il client
	 * @param ConcurrentHashMap<String,User> hashUsers: hash map degli utenti.
	 * @param HashChatrooms hashChatrooms: hash map delle chatrooms.
	 */
	public Worker(Socket socket,ConcurrentHashMap <String,User> hashUsers,
			HashChatrooms hashChatrooms) {
		this.socket= socket;
		this.hashUsers = hashUsers;
		this.hashChatrooms = hashChatrooms;
	}

	
	/**
	 * Task che gestisce un intera connessione verso un client
	 */
	public void run() {
		
		//variabile settata a true quando la connessione verso il client deve essere chiusa
		boolean close = false;
		
		//variabile settata a true se è stata chiamata l'operazione per l'apertura della seconda 
		//connessione TCP verso il client
		boolean secondConn = false;
		
		//apro gli streams
		try {
			writer = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
			reader = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			close = true;
		}
		
		
		//gestisco le richieste del client
		while(!close) {
			try {
				//ricevo la richiesta
				String request = reader.readUTF();
				
				//trasformo in formato JSON la richiesta ricevuta dal server
				JSONObject requestJSON = (JSONObject) new JSONParser().parse(request);
				
				//conterrà la risposta da inviare al client
				JSONObject response = null;
				
				//prendo l'operazione richiesta
				Operations requestOp = Operations.valueOf((String) requestJSON.get("OP"));
				
				//in base all'operazione richiesta
				switch(requestOp) {

					case REG:
						//chiamo la funzione che esegue la registrazione
						response = registration(requestJSON);
						break;

					case LOG:
						//chiamo la funzione che esegue il login
						response = login(requestJSON);
						break;
			
					case CONN_MSG:
						//chiamo la funzione che apre la seconda connessione TCP verso il client
						response = secondConnection(requestJSON);
						//il thread che esegue questa operazione dopo termina
						close = true;
						secondConn = true;
						break; 
			
					case LISTFRIEND:
						//chiamo la funzione che resituisce la lista amici dell'utente al client
						response = friendsList(requestJSON);
						break;
						
					case FRIENDSHIP:
						//chiamo la funzione che esegue la richiesta di amicizia
						response = friendship(requestJSON);
						break;
						
					case LOOKUP:
						//chiamo la funzione che esegue l'operazione di look up
						response = lookUp(requestJSON);
						break;
						
					case STARTCHAT:
						//chiamo la funzione che gestisce la richiesta di apertura di una chat
						response = startChat(requestJSON);
						break;
						
					case MSG_FRIEND:
						//chiamo la funzione che manda un messaggio ad un amico
						response = msgFriend(requestJSON);
						break;
						
					case FILE_FRIEND:
						//chiamo la funzione che manda un file ad un amico
						response = fileFriend(requestJSON);
						break;
						
					case CHATLIST:
						//chiamo la funzione che resituisce la lista delle chatrooms al client
						response = chatroomsList(requestJSON);
						break;
						
					case CREATE_CHATROOM:
						//chiamo la funzione che crea una nuova chatroom
						response = createChatroom(requestJSON);
						break;
						
					case ADDME_CHATROOM:
						//chiamo la funzione che aggiunge l'utente ad una chatroom
						response = addToChatroom(requestJSON);
						break;
						
					case CLOSE_CHATROOM:
						//chiamo la funzione che chiude una chatroom
						response = closeChatroom(requestJSON);
						break;
						
					case MSG_CHATROOM:
						//chiamo la funzione che dà l'ok per inviare un messaggio ad una
						//chatroom
						response = msgChatroom(requestJSON);
						break;
					
					case CLOSE:
						//setto close a true
						close = true;
						//setto response a null così non viene inviata nessuna risposta 
						response = null;
						break;
						
					default: 
						close = true;
						response = null;
						break; 
				}
				
				//spedisco la risposta al client
				if(response != null) {
					writer.writeUTF(response.toJSONString());
					writer.flush();
				}
				
			} catch (Exception e) {
				//se viene rilevato qualche problema di connessione con il client
				close = true;
			}
		}
		
		//se il thread non ha gestito l'operazione di apertura della seconda 
		//connessione verso il client chiudo la connessione verso il client
		if (!secondConn) closeConnection();
	}
	
	
	/**
	 * Metodo che esegue l'operazione di registrazione.
	 * @param: JSONObject request: richiesta fatta dal client.
	 * @return: JSONObject: contiene la risposta (con esito positivo o
	 * 						negativo) da inviare al client.
	 */
	@SuppressWarnings("unchecked")
	private JSONObject registration(JSONObject request) {
		//response da restituire
		JSONObject response = new JSONObject();
		
		//prendo i vari campi di interesse dalla richiesta del client
		String nickname = (String) request.get("ID");
		String psw = (String) request.get("PSW");
		String language = (String) request.get("LANGUAGE");
		
		//creo un nuovo utente
		User us = new User(nickname, psw, language);
		
		//se non esiste già un utente registrato con tale nickname
		if(hashUsers.putIfAbsent(nickname, us)==null) {
			//salvo  la struttura dell'utente per le successive richieste
			user = us;
			
			//prendo la lista di tutte le chatrooms
			JSONArray allChatrooms = hashChatrooms.getListChatrooms();
			
			response.put("OP", "OP_OK");
			response.put("CHATROOMS", allChatrooms);
		}
		//se esiste già un utente registrato con tale nickname
		else {
			response.put("OP", "OP_ERR");
			response.put("MSG", "ERR: This nickname is already used");
		}
		
		return response;
	}
	
	
	/**
	 * Metodo che esegue l'operazione di login.
	 * @param: JSONObject request: richiesta fatta dal client.
	 * @return: JSONObject: contiene la risposta (con esito positivo o
	 * 						negativo) da inviare al client.
	 */
	@SuppressWarnings("unchecked")
	private JSONObject login(JSONObject request) {
		//response da restituire
		JSONObject response = new JSONObject();
				
		//prendo i vari campi di interesse dalla richiesta del client
		String nickname = (String) request.get("ID");
		String psw = (String) request.get("PSW");
				
		//cerco un utente con tale nickname
		User us = hashUsers.get(nickname);
				
		//se esiste un utente con tale nickname
		if(us != null) {
			//controllo che la password sia corretta
			if(us.checkPsw(psw)) {
				//controllo se l'utente è già loggato su un altro client
				if(us.getOnline()) {
					response.put("OP", "OP_ERR");
					response.put("MSG", "ERR: This user is already logged in");
				}
				//se non è già online 
				else {
					//salvo  la struttura dell'utente per le successive richieste
					user = us;
					
					//prendo la lista amici dell'utente
					String listFriends = us.getFriendList();
					
					//prendo la lista chatrooms dell'utente
					JSONArray allChatrooms = hashChatrooms.getListChatrooms();
					JSONArray listChatrooms = us.getChatroomsList(allChatrooms);
					
					//inserisco lista amici e lista chatrooms nella riposta
					response.put("OP", "OP_OK");
					response.put("FRIENDS",listFriends);
					response.put("CHATROOMS",listChatrooms);
				}
			}
			//se la psw non coincide
			else {
				response.put("OP", "OP_ERR");
				response.put("MSG", "ERR: The password is incorrect");
			}
		}
		//se non esiste un utente con tale nickname
		else {
			response.put("OP", "OP_ERR");
			response.put("MSG", "ERR: There is no user with this nickname");
		}
				
		return response;
	}
	
	
	/**
	 * Metodo che esegue l'operazione di apertura della seconda connessione 
	 * TCP verso il client per permettere agli altri utenti di inviargli messaggi.
	 * @param: JSONObject request: richiesta fatta dal client.
	 * @return: JSONObject: contiene la risposta (con esito positivo o
	 * 						negativo) da inviare al client.
	 */
	@SuppressWarnings("unchecked")
	private JSONObject secondConnection(JSONObject request) {
		//response da restituire
		JSONObject response = new JSONObject();
				
		//prendo i vari campi di interesse dalla richiesta del client
		String nickname = (String) request.get("ID");
		long portFile = (long) request.get("PORT_FILE");
				
		//cerco un utente con tale nickname
		User us = hashUsers.get(nickname);
				
		//se esiste un utente con tale nickname
		if(us!=null && writer!=null && reader!=null) {
			//salvo nella struttura dell'utente il socket e lo stream
			//di output per mandare i messaggi all'utente e la porta
			//dove il client è in ascolto per eventuali file
			us.setSocketMsg(socket, writer, reader, portFile);
			response.put("OP", "OP_OK");
		}
		//se non esiste un utente con tale nickname
		else {
			response.put("OP", "OP_ERR");
			response.put("MSG", "ERR: There is no user with this nickname");
		}
				
		return response;
	}

	
	/**
	 * Metodo che restituisce la lista amici dell'utente.
	 * @param: JSONObject request: richiesta fatta dal client.
	 * @return: JSONObject: contiene la risposta (con esito positivo o
	 * 						negativo) da inviare al client.
	 */
	@SuppressWarnings("unchecked")
	private JSONObject friendsList(JSONObject request) {
		//response da restituire
		JSONObject response = new JSONObject();
				
		//se l'utente è loggato
		if(user != null) {
			//prendo la lista amici dell'utente
			String listFriends = user.getFriendList();
					
			//inserisco lista amici nella risposta
			response.put("OP", "OP_OK");
			response.put("FRIENDS",listFriends);
		}
		//se non è loggato (cosa che non dovrebbe verificarsi)
		else {
			response.put("OP", "OP_ERR");
			response.put("MSG", "ERR: An error occurred");
		}
				
		return response;
	}

	
	/**
	 * Metodo che esegue l'operazione di richiesta amicizia.
	 * @param: JSONObject request: richiesta fatta dal client.
	 * @return: JSONObject: contiene la risposta (con esito positivo o
	 * 						negativo) da inviare al client.
	 */
	@SuppressWarnings("unchecked")
	private JSONObject friendship(JSONObject request) {
		//response da restituire
		JSONObject response = new JSONObject();
		
		//prendo il nickname dell'utente con cui vuole diventare amico
		String nickname = (String) request.get("ID");
						
		//cerco un utente con tale nickname
		User friend = hashUsers.get(nickname);
				
		//eventuale messaggio di errore
		String msgErr = null;
		
		//se l'utente è loggato
		if(user != null) {
			//se esiste l'utente con cui vuole stringere amicizia
			if(friend != null) {
				//se i due non erano già amici
				if(user.doFriendship(friend)) {
					response.put("OP", "OP_OK");
				}
				//se erano già amici
				else {
					response.put("OP", "OP_ERR");
					msgErr = "ERR: You and \""+nickname+"\" are already friends";
					response.put("MSG", msgErr);
				}
			}
			//se non esiste 
			else {
				response.put("OP", "OP_ERR");
				msgErr="ERR: \""+nickname+"\" does not exists";
				response.put("MSG", msgErr);
			}
		}
		//se non è loggato (cosa che non dovrebbe verificarsi)
		else {
			response.put("OP", "OP_ERR");
			response.put("MSG", "ERR: An error occurred");
		}
				
		return response;
	}
	
	
	/**
	 * Metodo che esegue l'operazione di look up.
	 * @param: JSONObject request: richiesta fatta dal client.
	 * @return: JSONObject: contiene la risposta (con esito positivo o
	 * 						negativo) da inviare al client.
	 */
	@SuppressWarnings("unchecked")
	private JSONObject lookUp(JSONObject request) {
		//response da restituire
		JSONObject response = new JSONObject();
		
		//prendo il nickname dell'utente che vuole cercare il client
		String nickname = (String) request.get("ID");
						
		//cerco un utente con tale nickname
		User us = hashUsers.get(nickname);
				
		//messaggio che il client comunicherà all'utente
		String msg = null;
		
		//se l'utente è loggato
		if(user != null) {
			//se esiste l'utente cercato
			if(us != null) {
				response.put("OP", "OP_OK");
				msg = "\""+nickname+"\" exists";
				response.put("MSG", msg);
			}
			//se non esiste 
			else {
				response.put("OP", "OP_OK");
				msg = "\""+nickname+"\" does not exists";
				response.put("MSG", msg);
			}
		}
		//se non è loggato (cosa che non dovrebbe verificarsi)
		else {
			response.put("OP", "OP_ERR");
			response.put("MSG", "ERR: An error occurred");
		}
				
		return response;
	}
	
	
	/**
	 * Metodo che esegue l'operazione di start chat.
	 * @param: JSONObject request: richiesta fatta dal client.
	 * @return: JSONObject: contiene la risposta (con esito positivo o
	 * 						negativo) da inviare al client.
	 */
	@SuppressWarnings("unchecked")
	private JSONObject startChat(JSONObject request) {
		//response da restituire
		JSONObject response = new JSONObject();
		
		//prendo il nickname dell'utente con cui vuole aprire una chat
		String nickname = (String) request.get("ID");
				
		//eventuale messaggio di errore
		String msgErr = null;
		
		//se l'utente è loggato
		if(user != null) {
			//cerco il nickname nella lista amici dell'utente
			User us = user.getFriend(nickname);
			
			//se l'utente con cui vuole aprire la chat è suo amico
			if(us != null) {
				//se l'utente con cui vuole aprire la chat è online
				if(us.getOnline()) {
					response.put("OP", "OP_OK");
				}
				//se non è online
				else {
					response.put("OP", "OP_ERR");
					msgErr = "ERR: \""+nickname+"\" is not online";
					response.put("MSG", msgErr);
				}
			}
			//se non sono amici
			else {
				response.put("OP", "OP_ERR");
				msgErr="ERR: You and \""+nickname+"\" are not friends";
				response.put("MSG", msgErr);
			}
		}
		//se non è loggato (cosa che non dovrebbe verificarsi)
		else {
			response.put("OP", "OP_ERR");
			response.put("MSG", "ERR: An error occurred");
		}
				
		return response;
	}
	
	
	/**
	 * Metodo che esegue l'operazione di invio messaggio.
	 * NB: non viene controllato che il nickname a cui l'utente vuole
	 * 	   inviare il messaggio sia suo amico perchè questo viene 
	 * 	   controllato nella operazione di start chat.
	 * @param: JSONObject request: richiesta fatta dal client.
	 * @return: JSONObject: contiene la risposta (con esito positivo o
	 * 						negativo) da inviare al client.
	 */
	@SuppressWarnings("unchecked")
	private JSONObject msgFriend(JSONObject request) {
		//response da restituire
		JSONObject response = new JSONObject();
		
		//prendo i campi di interesse della richiesta fatta dal client
		String receiverNickname = (String) request.get("TO");
		String msg = (String) request.get("MSG");
		
		//prendo l'utente destinatario del messaggio
		User receiver = hashUsers.get(receiverNickname);
		
		//eventuale messaggio di errore
		String msgErr = null;
		
		//se l'utente è loggato e il destinatario esiste
		if(user != null && receiver != null) {
			//oggetto json da inviare al receiver
			JSONObject msgToSendObj = new JSONObject();
			
			//prendo le lingue dei due utenti
			String l1 = user.getLanguage();
			String l2 = receiver.getLanguage();
			
			String msgToSend = null;
			
			//se le due lingue solo uguali
			if(l1.equals(l2)) {
				//non effettuo nessuna traduzione
				msgToSend = "[" + user.getNickname() + "]: " + msg;
			}
			else {
				//tento di tradurre il messaggio
				msgToSend = translate(msg, l1, l2);
				
				//se la traduzione è andata a buon fine
				if(msgToSend!=null) {
					msgToSend = "[" + user.getNickname() + "]: " + msgToSend;
				}
				//altrimenti mando il messaggio originale senza traduzione
				else {
					msgToSend = "[" + user.getNickname() + "]: " + msg;
				}
			}
			
			msgToSendObj.put("OP", "MSG_FRIEND");
			msgToSendObj.put("FROM", user.getNickname());
			msgToSendObj.put("MSG", msgToSend);
			
			//se il messaggio viene spedito correttamente
			if(receiver.sendMsg(msgToSendObj.toJSONString())) {
				response.put("OP", "OP_OK");
			}
			//se il messagggio non viene inviato
			else {
				response.put("OP", "OP_ERR");
				msgErr="ERR: \""+receiverNickname+"\" is offline now";
				response.put("MSG", msgErr);
			}
		}
		//se non è loggato o se il receiver non esiste (cosa che non dovrebbe verificarsi)
		else {
			response.put("OP", "OP_ERR");
			response.put("MSG", "ERR: An error occurred");
		}
				
		return response;
	}
	
	
	/**
	 * Metodo che traduce un messaggio da una lingua ad un altra utilizzando
	 * il servizio rest api del sito mymemory.
	 * @param String msg: messaggio da tradurre.
	 * @param String l1: lingua originale del messaggio.
	 * @param String l2: lingua in cui deve essere tradotto il messaggio.
	 * @return String: il messaggio tradotto,
	 * 		   null: se non è possibile tradurlo o se si è verificato un errore.
	 */
	private String translate(String msg, String l1, String l2) {
		//url base per fare la richiesta di traduzione a mymemory
		String pathName = "https://api.mymemory.translated.net/get?";
		
		//messaggio da tradurre
		String message = msg.replace(" ", "%20");
		
		//come inserire il messaggio nella richiesta
		String msgToTranslate = "q=" + message;
		
		//come inserire le ligue nella richiesta
		String languages = "&langpair="+ l1 + "|" + l2;
		
		try {
			//creo l'url per la richiesta e apro la connessione
			URL url = new URL(pathName + msgToTranslate + languages);
			URLConnection uc = url.openConnection();
			uc.connect();
			
			//prendo la risposta
			BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
			String line=null;
			StringBuffer sb=new StringBuffer();
			while((line=in.readLine())!=null){
				sb.append(line);
			}
			
			//trasformo in formato json la riposta e prendo il campo di interesse
			JSONObject response = (JSONObject) new JSONParser().parse(sb.toString());
			JSONObject responseData = (JSONObject) response.get("responseData");
			String msgTranslated = (String) responseData.get("translatedText");
			
			return msgTranslated;
			
		} catch (Exception e) {
			//se il sito non è reperibile o se non è traducibile il messaggio
			return null;
		}
	}
	
	
	/**
	 * Metodo che esegue l'operazione di richiesta di invio file.
	 * NB: il worker restituisce al client solo il numero di porta e l'address
	 *     dell'amico a cui l'utente vuole trasferire il file.
	 * @param: JSONObject request: richiesta fatta dal client.
	 * @return: JSONObject: contiene la risposta (con esito positivo o
	 * 						negativo) da inviare al client.
	 */
	@SuppressWarnings("unchecked")
	private JSONObject fileFriend(JSONObject request) {
		//response da restituire
		JSONObject response = new JSONObject();
		
		//prendo i campi di interesse della richiesta fatta dal client
		String receiverNickname = (String) request.get("ID");
		
		//prendo l'utente destinatario del messaggio
		User receiver = hashUsers.get(receiverNickname);
		
		//eventuale messaggio di errore
		String msgErr = null;
		
		//se l'utente è loggato e il destinatario esiste
		if(user != null && receiver != null) {
			//prendo le info del receiver da inviare al client
			JSONObject infoFile = receiver.getInfoFile();
			
			//se il receiver è online
			if(infoFile!=null) {
				response = infoFile;
				response.put("OP", "OP_OK");
			}
			//se è offline
			else {
				response.put("OP", "OP_ERR");
				msgErr="ERR: \""+receiverNickname+"\" is offline now";
				response.put("MSG", msgErr);
			}
		}
		//se non è loggato o se il receiver non esiste (cosa che non dovrebbe verificarsi)
		else {
			response.put("OP", "OP_ERR");
			response.put("MSG", "ERR: An error occurred");
		}
				
		return response;
	}
	
	
	/**
	 * Metodo che restituisce la lista delle chatrooms all'utente.
	 * @param: JSONObject request: richiesta fatta dal client.
	 * @return: JSONObject: contiene la risposta (con esito positivo o
	 * 						negativo) da inviare al client.
	 */
	@SuppressWarnings("unchecked")
	private JSONObject chatroomsList(JSONObject request) {
		//response da restituire
		JSONObject response = new JSONObject();
				
		//se l'utente è loggato
		if(user != null) {
			//prendo la lista chatrooms dell'utente
			JSONArray allChatrooms = hashChatrooms.getListChatrooms();
			JSONArray listChatrooms = user.getChatroomsList(allChatrooms);
			
			//inserisco lista amici e lista chatrooms nella riposta
			response.put("OP", "OP_OK");
			response.put("CHATROOMS",listChatrooms);
		}
		//se non è loggato (cosa che non dovrebbe verificarsi)
		else {
			response.put("OP", "OP_ERR");
			response.put("MSG", "ERR: An error occurred");
		}
				
		return response;
	}
	
	
	/**
	 * Metodo che crea una nuova chatroom.
	 * @param: JSONObject request: richiesta fatta dal client.
	 * @return: JSONObject: contiene la risposta (con esito positivo o
	 * 						negativo) da inviare al client.
	 */
	@SuppressWarnings("unchecked")
	private JSONObject createChatroom(JSONObject request) {
		//response da restituire
		JSONObject response = new JSONObject();
		
		//prendo i vari campi di interesse dalla richiesta del client
		String id = (String) request.get("ID");
		
		//se l'utente è loggato
		if(user != null) {
			//creo una nuova chatroom
			Chatroom chatroom = new Chatroom(id,user);
			
			//tento di inserire la chatroom nella hash chatrooms
			InetAddress address = hashChatrooms.add(chatroom);
					
			//se l'inserimento è andato bene
			if(address!=null) {
				//aggiorno la lista chatroom dell'utente
				user.addChatroom(chatroom);
				
				//prendo l'host address in formato String 
				String hostAddress = address.getHostAddress().toString();
				
				response.put("OP", "OP_OK");
				response.put("ADDRESS", hostAddress);
			}
			//se esiste già una chatroom con tale id
			else {
				response.put("OP", "OP_ERR");
				String msgErr = "ERR: There is already a chatroom called \""+id+
						"\" or the maximum number of open chatrooms has been reached";
				response.put("MSG", msgErr);
			}
		}
		//se non è loggato (cosa che non dovrebbe verificarsi)
		else {
			response.put("OP", "OP_ERR");
			response.put("MSG", "ERR: An error occurred");
		}
		
		return response;
	}
	
	
	/**
	 * Metodo che aggiunge l'utente ad una chatroom.
	 * @param: JSONObject request: richiesta fatta dal client.
	 * @return: JSONObject: contiene la risposta (con esito positivo o
	 * 						negativo) da inviare al client.
	 */
	@SuppressWarnings("unchecked")
	private JSONObject addToChatroom(JSONObject request) {
		//response da restituire
		JSONObject response = new JSONObject();
		
		//prendo i vari campi di interesse dalla richiesta del client
		String id = (String) request.get("ID");
		
		//se l'utente è loggato
		if(user != null) {
			//cerco la chatroom
			Chatroom chatroom = hashChatrooms.get(id);
					
			//se la chatroom esiste
			if(chatroom!=null) {
				//se l'utente non era già iscritto alla chatroom
				if(user.addChatroom(chatroom)) {
					//aggiungo l'utente alla lista iscritti della chatroom
					chatroom.addSubscriber(user);
					
					//prendo l'address della chatroom
					InetAddress address = chatroom.getAddress();
					
					//prendo l'host address in formato String 
					String hostAddress = address.getHostAddress().toString();
					
					response.put("OP", "OP_OK");
					response.put("ADDRESS", hostAddress);
				}
				//se era già iscritto a tale chatroom
				else {
					response.put("OP", "OP_ERR");
					String msgErr = "ERR: You are already registered to the chatroom \""+id+"\"";
					response.put("MSG", msgErr);
				}
			}
			//se non esiste nessuna chatroom con tale id
			else {
				response.put("OP", "OP_ERR");
				String msgErr = "ERR: There is no chatroom called \""+id+"\"";
				response.put("MSG", msgErr);
			}
		}
		//se non è loggato (cosa che non dovrebbe verificarsi)
		else {
			response.put("OP", "OP_ERR");
			response.put("MSG", "ERR: An error occurred");
		}
		
		return response;
	}
	
	
	/**
	 * Metodo che chiude una chatroom.
	 * @param: JSONObject request: richiesta fatta dal client.
	 * @return: JSONObject: contiene la risposta (con esito positivo o
	 * 						negativo) da inviare al client.
	 */
	@SuppressWarnings("unchecked")
	private JSONObject closeChatroom(JSONObject request) {
		//response da restituire
		JSONObject response = new JSONObject();
		
		//prendo i vari campi di interesse dalla richiesta del client
		String id = (String) request.get("ID");
		
		//se l'utente è loggato
		if(user != null) {
			//provo a rimuovere la chatroom
			if(hashChatrooms.remove(id, user.getNickname())) {
				response.put("OP", "OP_OK");
			}
			//se la chatroom non esiste o l'utente non è il creatore
			else {
				response.put("OP", "OP_ERR");
				String msgErr = "ERR: The chatroom \""+id+
						"\" does not exist or you are not the creator";
				response.put("MSG", msgErr);
			}
		}
		//se non è loggato (cosa che non dovrebbe verificarsi)
		else {
			response.put("OP", "OP_ERR");
			response.put("MSG", "ERR: An error occurred");
		}
		
		return response;
	}
	
	
	/**
	 * Metodo che controlla che ci siano almeno due utenti online 
	 * in ascolto su una chatroom.
	 * @param: JSONObject request: richiesta fatta dal client.
	 * @return: JSONObject: contiene la risposta (con esito positivo o
	 * 						negativo) da inviare al client.
	 */
	@SuppressWarnings("unchecked")
	private JSONObject msgChatroom(JSONObject request) {
		//response da restituire
		JSONObject response = new JSONObject();
		
		//prendo i vari campi di interesse dalla richiesta del client
		String id = (String) request.get("ID");
		
		//se l'utente è loggato
		if(user != null) {
			//cerco la chatroom
			Chatroom chatroom = hashChatrooms.get(id);
					
			//se nella chatroom ci sono almeno due utenti online
			if(chatroom.checkUsersConn()) {
				response.put("OP", "OP_OK");
			}
			//se non ci sono
			else {
				response.put("OP", "OP_ERR");
				String msgErr = "ERR: You are the only one online";
				response.put("MSG", msgErr);
			}
		}
		//se non è loggato (cosa che non dovrebbe verificarsi)
		else {
			response.put("OP", "OP_ERR");
			response.put("MSG", "ERR: An error occurred");
		}
		return response;
	}
	
	
	/**
	 * Metodo che esegue la chiusura della connessione verso il client.
	 */			
	private void closeConnection() {
		//setto offline l'utente (se si era loggato)
		if(user!=null) user.setOffline();
				
		//chiudo il socket e gli streams della connessione
		try {
			if(writer!=null) writer.close();
			if(reader!=null) reader.close();
			if(socket!=null) socket.close();
		} catch (Exception e) {}
	}
}
