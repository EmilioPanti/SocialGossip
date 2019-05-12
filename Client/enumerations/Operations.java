package enumerations;


/**
 * Enumerazione Operations.
 * Contiene l'enumerazione di tutte le operazioni richieste dal client
 * ed i possibili responsi inviati dal server.
 * @author Emilio Panti mat:531844 
 */
public enum Operations {
	
	//POSSIBILI RESPONSI DAL SERVER
	OP_OK,
	OP_ERR,
	
	//OPERAZIONI RIGUARDANTI L'UTENTE
	REG,             //registrazione
	LOG,			 //login
	CLOSE,			 //chiusura connessione
	CONN_MSG,        //connessione per ricevere messaggi dagli altri utenti
	
	//OPERAZIONI VERSO GLI AMICI
	LOOKUP,          //ricerca di un nickname
	FRIENDSHIP,      //richiedere l'amicizia verso un utente
	LISTFRIEND,      //richiedere la lista degli amici
	STARTCHAT,       //aprire una chat con un amico
	MSG_FRIEND,      //mandare un messaggio testuale ad un amico
	FILE_FRIEND,     //mandare un file ad un amico
	
	//OPERAZIONI VERSO LE CHATROOMS
	CREATE_CHATROOM, //creare una chatroom
	ADDME_CHATROOM,  //aggiungersi ad una chatroom
	CHATLIST,        //ottenere la lista delle chatrooms
	CLOSE_CHATROOM,  //chiudere una chatrooms
	MSG_CHATROOM     //mandare un messaggio testuale su una chatroom
}
