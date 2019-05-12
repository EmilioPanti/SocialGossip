package graphicInterfaces;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextArea;
import java.awt.Font;


/**
 * Classe ResponseGUI.
 * Interfaccia grafica per mostrare all'utente l'esito di una 
 * sua richiesta al server.
 * @author Emilio Panti mat:531844 
 */
public class ResponseGUI extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;


	/**
	 * Costruttore classe ResponseGUI.
	 * @param string: messaggio di risposta
	 */
	public ResponseGUI(String string) {
		setTitle("Response");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 304, 100);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		//TEXT AREA
		JTextArea textArea = new JTextArea(string);
		textArea.setFont(new Font("Tahoma", Font.BOLD, 11));
		textArea.setBounds(10, 11, 268, 67);
		contentPane.add(textArea);
		textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setOpaque(false);
        textArea.setEditable(false);
	}
}
