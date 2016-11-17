/**
  * CCReader é um utilitário concebido pela Rumos e disponibilizada à comunidade para demonstrar o uso da API do cartão do cidadão da República Portuguesa.
  * Desenvolvida por Gonçalo Silva.
  * 
  * Esta aplicação tambem guarda os dados do cartão em dois ficheiros no computador, na mesma pasta que o jar.
  * ---------------------------------------------------------------------------------------------------------------------------------------------------------------
  * CCReader is a utility designed for Rumos and made available to the community to demonstrate the use of the citizen card of the Portuguese Republic API.
  * Developed by Gonçalo Silva.
  *
  * This application also stores the card data in two files on the computer, in the same folder as the jar.
  */

import java.awt.EventQueue;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.TerminalFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;

public class Main 
{
	private JFrame mainFrame;
	private CartaoCidadao cardReader;
	private boolean cardPrestent = false;
	private JLabel text;

	private CardTerminal smartCardReader = null;
	public static void main(String[] args) 
	{
		EventQueue.invokeLater(new Runnable() 
		{
			public void run() 
			{
				try 
				{
					Main window = new Main();
					window.mainFrame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public Main() { initialize(); }

	/**
	 * Responsável por inicializar a aplicação.
	 * ---------------------------------------------
	 * Responsible for initializing the application.
	 */
	private void initialize() 
	{
		cardReader = new CartaoCidadao(false);

		mainFrame = new JFrame("Leitor de Cartões do Cidadão");
		mainFrame.setResizable(false);//Sets the window size fixed with no possibility to change the size.
		
		mainFrame.setSize(350, 60);//Sets the window size
		mainFrame.setLocationRelativeTo(null);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//Allows to close the window and close all processes related with the application.
		mainFrame.getContentPane().setLayout(new MigLayout("", "[]", "[]"));//Defines the main content layout.
		
		text = new JLabel();
		
		text.setText("A aguardar por um cartão.");
		mainFrame.add(text);

		Timer clock = new Timer();
		
		TerminalFactory factory = TerminalFactory.getDefault();//Gets the smart card readers on the computer.
		List<CardTerminal> terminals;
		try 
		{
			terminals = factory.terminals().list();//Creates a list with the smart card readers.
			smartCardReader = terminals.get(0);//Uses the first terminal on the list

			clock.schedule(new TimerTask() 
			{
				@Override
				public void run() 
				{
					try {
						if (!smartCardReader.isCardPresent()) 
						{//If there is not a card present on the reader
							if (cardPrestent) cardReader.dataGetted = false;
							cardPrestent = false;
							text.setText("A aguardar por um cartão.");
						} else {
							if (cardPrestent == false) 
							{//If the last statement of the reader was with no card, avoiding double loading of the card.
								text.setText("A lêr cartão.");
								cardReader = new CartaoCidadao();//Will read the citizen card, and initialize the card class.
								cardReader.saveData("./");//Stores the data and the photo on the project folder.
								cardPrestent = true;
								text.setText("Cartão lido com sucesso.");
							}
						}
					} catch (CardException exception) { 
						cardReader.errorMessage("Lertor de cartões removido.");
						System.exit(0);					
					}
				}
			}, 0, 1000);//Will check the card Statement every second.
		} catch (CardException exception) { cardReader.errorCC(CartaoCidadao.NO_READERS_FOUND, exception.toString()); }
	}
}