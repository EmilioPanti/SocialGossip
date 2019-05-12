package graphicInterfaces;

import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import threads.usersOp.CloseThread;
import threads.usersOp.RegistrationThread;

import javax.swing.JLabel;
import java.awt.Color;
import javax.swing.SwingConstants;


/**
 * Classe RegistrationGUI.
 * Interfaccia grafica per la registrazione dell'utente al servizio
 * Social Gossip.
 * Quando l'utente clicca sul bottone per la registrazione viene 
 * creato e fatto partire il thread (oggetto della classe RegistrationThread)
 * che si occuperà di effettuare la richiesta al server.
 * Offre anche la possibilità di accedere all'interfaccia grafica di login.
 * NB: la chiusura di questa interfaccia grafica da parte dell'utente 
 * 	   causa la terminazione del client.
 * @author Emilio Panti mat:531844 
 */
public class RegistrationGUI extends JFrame {

	//lunghezza massima per i campi nickname e password
	static int MAX_LENGHT = 16;
	
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField fieldNickname;
	private JPasswordField fieldPassword;
	private JPasswordField fieldPassword2;
	private JButton btnRegister;
	private JComboBox<String> comboBox;
	
	/**
	 * variabile dove salvo l'interfaccia di registrazione creata per 
	 * passarla ai metodi invocati dai vari listener.
	 */
	private RegistrationGUI registrationGUI;
	
	
	/**
	 * Costruttore classe RegistrationGUI.
	 */
	public RegistrationGUI() {
		registrationGUI = this;
		
		setTitle("SOCIAL GOSSIP : registration");
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
		
		setBounds(100, 100, 397, 336);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
				
		//LABEL NICKNAME
		JLabel lblNickname = new JLabel("Nickname:");
		lblNickname.setHorizontalAlignment(SwingConstants.RIGHT);
		lblNickname.setBounds(34, 21, 100, 24);
		contentPane.add(lblNickname);
		
		//FIELD NICKNAME
		fieldNickname = new JTextField();
		fieldNickname.setBounds(144, 23, 172, 20);
		contentPane.add(fieldNickname);
		fieldNickname.setColumns(10);
				
		//LABEL CHECK NICKNAME
		JLabel lblCheckNickname = new JLabel("");
		lblCheckNickname.setForeground(Color.RED);
		lblCheckNickname.setFont(new Font("Tahoma", Font.PLAIN, 9));
		lblCheckNickname.setBounds(134, 43, 172, 14);
		contentPane.add(lblCheckNickname);
		
		//LABEL PASSWORD
		JLabel lblPassword = new JLabel("Password:");
		lblPassword.setHorizontalAlignment(SwingConstants.RIGHT);
		lblPassword.setBounds(20, 62, 114, 14);
		contentPane.add(lblPassword);
		
		//FIELD PASSWORD
		fieldPassword = new JPasswordField();
		fieldPassword.setBounds(144, 59, 172, 20);
		contentPane.add(fieldPassword);
				
		//LABEL CHECK PASSWORD
		JLabel lblCheckPassword = new JLabel("");
		lblCheckPassword.setForeground(Color.RED);
		lblCheckPassword.setFont(new Font("Tahoma", Font.PLAIN, 9));
		lblCheckPassword.setBounds(134, 79, 172, 14);
		contentPane.add(lblCheckPassword);
		
		//LABEL PASSWORD2
		JLabel lblPassword2 = new JLabel("Confirm password:");
		lblPassword2.setHorizontalAlignment(SwingConstants.RIGHT);
		lblPassword2.setBounds(10, 98, 124, 14);
		contentPane.add(lblPassword2);
		
		//FIELD PASSWORD2
		fieldPassword2 = new JPasswordField();
		fieldPassword2.setBounds(144, 95, 172, 20);
		contentPane.add(fieldPassword2);
		
		//LABEL CHECK PASSWORD2
		JLabel lblCheckPassword2 = new JLabel("");
		lblCheckPassword2.setForeground(Color.RED);
		lblCheckPassword2.setFont(new Font("Tahoma", Font.PLAIN, 9));
		lblCheckPassword2.setBounds(134, 114, 172, 14);
		contentPane.add(lblCheckPassword2);
				
		//LABEL LANGUAGE
		JLabel lblLanguage = new JLabel("Language:");
		lblLanguage.setBounds(71, 135, 63, 14);
		contentPane.add(lblLanguage);
		
		//JCOMBOBOX LANGUAGES
		comboBox = new JComboBox<String>();
		//prendo un array di String delle lingue in formato ISO e le inserisco nella combo box
		String[] languages = java.util.Locale.getISOLanguages();
		comboBox.setModel(new DefaultComboBoxModel<String>(languages));
		comboBox.setBounds(144, 132, 48, 20);
		contentPane.add(comboBox);
				
		//BUTTON REGISTER
		btnRegister = new JButton("REGISTER NOW");
		btnRegister.setBounds(124, 170, 135, 23);
		contentPane.add(btnRegister);
		
		//LINE SEPARATOR
		JSeparator separator = new JSeparator();
		separator.setBounds(-11, 219, 404, 2);
		contentPane.add(separator);
		
		//INFO LABEL ("Are you already registered? Please login")
		JLabel lblInfo = new JLabel("Are you already registered? Please login");
		lblInfo.setFont(new Font("Tahoma", Font.PLAIN, 10));
		lblInfo.setBounds(98, 232, 192, 14);
		contentPane.add(lblInfo);
		
		//BUTTON LOGIN
		JButton btnLogin = new JButton("LOGIN");
		btnLogin.setBounds(124, 257, 135, 23);
		contentPane.add(btnLogin);
		
		
		
		//------------------------ALL LISTENER---------------------------------
			
		
		//LISTENER FIELD NICKNAME
		fieldNickname.addMouseListener(new MouseAdapter() {
			//quando 'entro' nell'area di fieldNickname con il mouse
			@Override
			public void mouseEntered(MouseEvent e) {
				lblCheckNickname.setText("(max "+MAX_LENGHT+" characters)");
			}
			//quando 'esco' dall'area di fieldNickname con il mouse
			@Override
			public void mouseExited(MouseEvent e) {
				//controllo che il nickname inserito dall'utente sia della lunghezza giusta
				String nickname = fieldNickname.getText();
				if (nickname.length()<=MAX_LENGHT) lblCheckNickname.setText("");
				else lblCheckNickname.setText("Nickname too long!");
			}
		});
		
		
		//LISTENER FIELD PASSWORD
		fieldPassword.addMouseListener(new MouseAdapter() {
			//quando 'entro' nell'area di fieldPassword con il mouse
			@Override
			public void mouseEntered(MouseEvent e) {
				lblCheckPassword.setText("(max "+MAX_LENGHT+" characters)");
			}
			//quando 'esco' dall'area di fieldPassword con il mouse
			@Override
			public void mouseExited(MouseEvent e) {
				//controllo che la password inserita dall'utente sia della lunghezza giusta
				String psw = new String(fieldPassword.getPassword());
				if (psw.length()<=MAX_LENGHT) lblCheckPassword.setText("");
				else lblCheckPassword.setText("Password too long!");
			}
		});
		
		
		//LISTENER FIELD PASSWORD2
		fieldPassword2.addMouseListener(new MouseAdapter() {
			//quando 'entro' nell'area di fieldPassword2 con il mouse
			@Override
			public void mouseEntered(MouseEvent e) {
				lblCheckPassword2.setText("(max "+MAX_LENGHT+" characters)");
			}
			//quando 'esco' dall'area di fieldPassword2 con il mouse
			@Override
			public void mouseExited(MouseEvent e) {
				//controllo che le due password siano uguali
				if (checkEqualsPassword(fieldPassword.getPassword(),fieldPassword2.getPassword())) lblCheckPassword2.setText("");
				else lblCheckPassword2.setText("Passwords must match!");
			}
		});
		
		
		//LISTENER BUTTON REGISTER NOW
		btnRegister.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				//prendo il nickname, le passwords e la lingua
				String nickname = fieldNickname.getText();
				String psw = new String(fieldPassword.getPassword());
				String language = (String) comboBox.getSelectedItem();
				
				//chiamo l'operazioe di registrazione se tutti i campi sono corretti
				if(checkLenghtNickname(nickname) && checkLenghtPassword(fieldPassword.getPassword())
					&& checkEqualsPassword(fieldPassword.getPassword(),fieldPassword2.getPassword())
				  ) {
					//disabilito il bottone 
					btnRegister.setEnabled(false);
					
					//chiama l'operazione di registrazione
					RegistrationThread registrationThread = 
							new RegistrationThread(registrationGUI, nickname, psw, language);
					Thread thread = new Thread(registrationThread);
					thread.start();
				}
				//altrimenti comunico l'errore all'utente senza fare richieste inutili al server
				else {
					ResponseGUI responseGUI = new ResponseGUI("ERR: Nickname or password incorrect");
					responseGUI.setVisible(true);
				}
			}
		});
		
		
		//LISTENER BUTTON LOGIN
		btnLogin.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				//apro l'interfaccia per il login
				LoginGUI frame = new LoginGUI();
				frame.setVisible(true);
				
				//chiudo la registration interface
				dispose();
			}
		});
		
	}
	
	
	
	/**
	 * Controlla la lunghezza del nickname.
	 * @param String nickname: stringa contenente il nickname
	 * @return boolean: true se il nickname è più corto di MAX_LENGHT, 
	 * 					false altrimenti
	 */
	private boolean checkLenghtNickname (String nickname){
		return (nickname.length()<=MAX_LENGHT && nickname.length()>0);
	}
	
	
	/**
	 * Controlla la lunghezza di una password.
	 * @param char[] password: rappresenta la password
	 * @return boolean: true se la password è più corta di MAX_LENGHT, 
	 * 					false altrimenti
	 */
	private boolean checkLenghtPassword (char[] password){
		String psw = new String(password);
		return (psw.length()<=MAX_LENGHT && psw.length()>0);
	}
	
	
	/**
	 * Controlla che due passwords siano uguali.
	 * @param char[] password1: rappresenta la prima password
	 * @param char[] password1: rappresenta la seconda password
	 * @return boolean: true se le due passwords sono uguali, 
	 * 					false altrimenti
	 */
	private boolean checkEqualsPassword (char[] password1, char[] password2){
		String psw1 = new String(password1);
		String psw2 = new String(password2);
		return (psw1.equals(psw2));
	}
	
	
	/**
	 * Attiva il bottone per la registrazione.
	 */
	public void enabledBtnRegister() {
		btnRegister.setEnabled(true);
	}
}
