package graphicInterfaces;


import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import threads.usersOp.CloseThread;
import threads.usersOp.LoginThread;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JButton;
import javax.swing.JSeparator;
import java.awt.Font;
import javax.swing.SwingConstants;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;


/**
 * Classe LoginGUI.
 * Interfaccia grafica per il login dell'utente al servizio Social Gossip.
 * Quando l'utente clicca sul bottone per il login viene 
 * creato e fatto partire il thread (oggetto della classe LoginThread)
 * che si occuperà di effettuare la richiesta al server.
 * Offre anche la possibilità di accedere all'interfaccia grafica 
 * per la registrazione a SocialGossip.
 * NB: la chiusura di questa interfaccia grafica da parte dell'utente 
 * 	   causa la terminazione del client.
 * @author Emilio Panti mat:531844 
 */
public class LoginGUI extends JFrame {

	private static final long serialVersionUID = 1L;
	
	//lunghezza massima per i campi nickname e password
	static int MAX_LENGHT = 16;
		
	private JPanel contentPane;
	private JTextField fieldNickname;
	private JPasswordField fieldPassword;
	private JButton btnLogin;
	
	//variabile dove salvo l'interfaccia di login creata per 
	//passarla ai metodi invocati dai vari listener.
	private LoginGUI loginGUI;
	
	
	/**
	 * Costruttore classe LoginGUI.
	 */
	public LoginGUI() {
		loginGUI = this;
		
		setTitle("SOCIAL GOSSIP : login");
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		//creo un window listener per personalizzare la gestione della chiusura dell'interfaccia
		WindowListener exitListener = new WindowAdapter() {
		    @Override
		    public void windowClosing(WindowEvent e) {
		    	//faccio partire il thread che si occupa di chiudere la connessione e il client
		    	CloseThread closeThread = new CloseThread();
				Thread thread = new Thread(closeThread);
				thread.start();
		    }
		};
		addWindowListener(exitListener);
		
		setBounds(100, 100, 397, 310);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		//LABEL WELCOME
		JLabel lblWelcome = new JLabel("WELCOME TO SOCIAL GOSSIP");
		lblWelcome.setHorizontalAlignment(SwingConstants.CENTER);
		lblWelcome.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblWelcome.setBounds(87, 26, 200, 14);
		contentPane.add(lblWelcome);
				
		//LABEL NICKNAME
		JLabel lblNickname = new JLabel("Nickname:");
		lblNickname.setBounds(61, 61, 63, 24);
		contentPane.add(lblNickname);
		
		//FIELD NICKNAME
		fieldNickname = new JTextField();
		fieldNickname.setBounds(134, 63, 173, 20);
		contentPane.add(fieldNickname);
		fieldNickname.setColumns(10);
		
		//LABEL PASSWORD
		JLabel lblPassword = new JLabel("Password:");
		lblPassword.setBounds(61, 96, 63, 14);
		contentPane.add(lblPassword);
		
		//FIELD PASSWORD
		fieldPassword = new JPasswordField();
		fieldPassword.setBounds(134, 94, 173, 20);
		contentPane.add(fieldPassword);
		
		//BUTTON LOGIN
		btnLogin = new JButton("LOGIN");
		btnLogin.setBounds(121, 127, 135, 23);
		contentPane.add(btnLogin);
		
		//LINE SEPARATOR
		JSeparator separator = new JSeparator();
		separator.setBounds(-11, 175, 404, 2);
		contentPane.add(separator);
		
		//INFO LABEL ("Not yet registered? Register now")
		JLabel lblInfo = new JLabel("Not yet registered? Register now!");
		lblInfo.setFont(new Font("Tahoma", Font.PLAIN, 10));
		lblInfo.setBounds(113, 197, 155, 14);
		contentPane.add(lblInfo);
		
		//BUTTON REGISTER
		JButton btnRegister = new JButton("REGISTER NOW");
		btnRegister.setBounds(121, 222, 135, 23);
		contentPane.add(btnRegister);
		
		
		
		//------------------------ALL LISTENER---------------------------------
		
		
		//LISTENER BUTTON LOGIN
		btnLogin.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				
				//prendo il nickname e la psw
				String nickname = fieldNickname.getText();
				String psw = new String(fieldPassword.getPassword());
				
				//controllo che il nickname e la password siano della lunghezza giusta
				if (checkLenght(nickname)&&checkLenght(psw)) {
					
					//disabilito il bottone 
					btnLogin.setEnabled(false);
					
					//chiama l'operazione di login
					LoginThread loginThread = new LoginThread(loginGUI, nickname, psw);
					Thread thread = new Thread(loginThread);
					thread.start();
					
				}
				//comunico l'errore all'utente senza fare richieste inutili al server
				else {
					ResponseGUI responseGUI = new ResponseGUI("ERR: Nickname or password incorrect");
					responseGUI.setVisible(true);
				}
				
			}
		});
		
		
		//LISTENER BUTTON REGISTER NOW
		btnRegister.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				//apro l'interfaccia per la registrazione
				RegistrationGUI frame = new RegistrationGUI();
				frame.setVisible(true);
				
				//chiudo la login interface
				dispose();
			}
		});
		
	}
	
	
	/**
	 * Controlla la lunghezza della stringa passata.
	 * @param String str: stringa 
	 * @return boolean: true se la stringa è più corta di MAX_LENGHT, 
	 * 					false altrimenti
	 */
	private boolean checkLenght(String str){
		return (str.length()<=MAX_LENGHT && str.length()>0);
	}
	
	
	/**
	 * Attiva il bottone per il login.
	 */
	public void enabledBtnLogin() {
		btnLogin.setEnabled(true);
	}
}
