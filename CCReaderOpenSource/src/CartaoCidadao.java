import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import com.idrsolutions.image.jpeg2000.Jpeg2000Decoder;

import javax.swing.JFrame;

import pteidlib.PTEID_ADDR;
import pteidlib.PTEID_ID;
import pteidlib.PTEID_PIC;
import pteidlib.PTEID_Pin;
import pteidlib.PteidException;
import pteidlib.pteid;

public class CartaoCidadao 
{
	static 
	{
		try 
		{
			System.loadLibrary("pteidlibj");
		} catch (UnsatisfiedLinkError error) {
			System.err.println("Não foi possível carregar a biblioteca do Cartão do Cidadão.\n" + error);
			System.exit(1);
		}
	}
	
	//Personal Information
	protected String birthDate, //Data de nascimento
					 cardNumber, //Número do cartão 
					 cardNumberPAN, 
					 cardVersion, //Versão do cartão
					 country, //País
					 deliveryDate, //Data de Emissão
					 deliveryEntity, //Entidade Emissora
					 documentType, //Tipo de documento
					 firstName, //Nomes própios
					 name, //Apelidos
					 firstNameMother, //Nomes própios da mãe
					 nameMother, //Apelidos da mãe
					 firstNameFather, //Nomes própios do pai
					 nameFather, //Apelidos do pai
					 height, //Lltura
					 locale,//Natalidade
					 mrz1, 
					 mrz2, 
					 mrz3, 
					 notes, //Notas sobre a pessoa
					 numBI, //Número do bilhete de identidade mais digito de confirmação
					 numSS, //Número de segurança social
					 numNIF, //Número de identificação cívil
					 numSNS, //Número do serviço nacional de saúde
					 sex, //sexo
					 validityDate; //Data de validade do cartão
	
	//Address Information
	protected String district, //Código do destrito
					 districtDesc, //Destrito
					 municipality, //Código do Município
					 municipalityDesc, //Município
					 freguesia, //Código da Freguesia
					 freguesiaDesc, //Freguesia
					 streetTypeAbbr, //Abreviatura do tipo de morada
					 streetType, //Tipo de morada
					 street, //Nome da rua/avenida/praceta/largo
					 buildingAbbr, //Abreviatura do edifício
					 building, //Nome do edifício
					 door, //Numero da porta
					 floor, //Andar
					 side, //Letra da porta
					 place, //vila
					 postal, //
					 locality, //Localidade
					 cp4, //Código postal 4
					 cp3, //Código postal 3
					 addrType,//Tipo de morada(Nacional ou Internacional)
					 localityF, //Localidade de fora
					 countryAddr, //País da morada de fora
					 addressF, //Endereço da morada de fora
					 cityF, //Nome da cidade de fora
					 numMor, //Numero da morada
					 regioF; //Região de fora
	//Card Image of the Person
	protected byte[] picture; //Fotografia do cidadão Português
	//Default Address key
	
	public static final int NO_READERS_FOUND = 1101;
	public static final int CARD_NOT_PRESENT = 1104;
	public static final int KEYPAD_CANCELLED = 1109;
	public static final int CARD_UNRESPONSIVE = 1113;
	public static final int NOT_ALLOWED = 1209;
	public static final int INVALID_CARD = 1210;
	public static final int AUTH_METHOD_BLOCKED = 1212;
	public static final int PIN_CODE_INCORRECT = 1214;
	public static final int INVALID_PIN_LENGTH = 1304;
	
	public static final int HTTP_OK = 200;
	public static final int HTTP_CREATED = 201;
	public static final int HTTP_BAD_REQUEST = 502;
	
	
	protected int addrAtempts; //Número de tentativas do código pin da morada
	private static final String DEFAULT_ADDR_PIN = "0000"; //Pin genérico de obtenção dos dados de morada
	protected String pin = DEFAULT_ADDR_PIN; // Pin da morada
	protected boolean validAddrPin = true, //Indicador da viabialidade da útilização do pin de morada
					  dataGetted = false; //Indicador de dados carregados na classe

	public CartaoCidadao() { initialize(); }
	public CartaoCidadao(boolean run) { if( run ) { initialize(); } }//used for initialize this class with out read the Citizen Card
	
	/**
	 * Responsável por executar a leitura do Cartão do Cidadão.
	 * --------------------------------------------------------
	 * Responsible for reading the Citizen Card.
	 */
	private void initialize()
	{
		try 
		{
			pteid.Init("");
			pteid.SetSODChecking(false);

			int cardtype = pteid.GetCardType();
			switch (cardtype) {
			case pteid.CARD_TYPE_IAS07:
				System.out.println("IAS 0.7 card\r\n");
				break;
			case pteid.CARD_TYPE_IAS101:
				System.out.println("IAS 1.0.1 card\r\n");
				break;
			case pteid.CARD_TYPE_ERR:
				System.out.println("O cartão inserido não é um Cartão do Cidadão\r\n");
				break;
			default:
				System.out.println("Tipo de cartão desconhecido\r\n");
			}
			this.getData(true);//It will collect Citizen Card information and also attempt to collect the address information.
			pteid.Exit(0);
		} catch (PteidException exception) {
			setErrorCode(exception);
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println(ex.getMessage());
		}
	}

	/**
	 * Este método guarda os dados do Cartão do Cidadão na pasta indicada.
	 * @param folderName - Localização da pasta de destino.
	 * -----------------------------------------------------------------------------------------
	 * This method stores the Citizen Card data in the indicated folder.
	 * @param folderName - Destination folder location.
	 */
	protected void saveData(String folderName) 
	{
		File folderPointer = new File(folderName);
		if (!folderPointer.exists()) folderPointer.mkdirs();

		String stringText = "birthDate\t" + birthDate + "\r\n" 
						  + "cardNumber\t" + cardNumber + "\r\n"
						  + "cardNumberPAN\t" + cardNumberPAN + "\r\n" 
						  + "cardVersion\t" + cardVersion + "\r\n" 
						  + "country\t" + country + "\r\n" 
						  + "deliveryDate\t" + deliveryDate + "\r\n" 
						  + "deliveryEntity\t" + deliveryEntity + "\r\n" 
						  + "documentType\t" + documentType + "\r\n" 
						  + "firstName\t" + firstName + "\r\n"
						  + "name\t" + name + "\r\n" 
						  + "firstNameMother\t" + firstNameMother + "\r\n"
						  + "nameMother\t" + nameMother + "\r\n" 
						  + "firstNameFather\t" + firstNameFather + "\r\n"
						  + "nameFather\t" + nameFather + "\r\n" 
						  + "height\t" + height + "\r\n" 
						  + "locale\t" + locale + "\r\n"
						  + "mrz1\t" + mrz1 + "\r\n" 
						  + "mrz2\t" + mrz2 + "\r\n" 
						  + "mrz3\t" + mrz3 + "\r\n" 
						  + "notes\t" + notes + "\r\n"
						  + "numBI\t" + numBI + "\r\n" 
						  + "numSS\t" + numSS + "\r\n" 
						  + "numNIF\t" + numNIF + "\r\n"
						  + "numSNS\t" + numSNS + "\r\n" 
						  + "sex\t" + sex + "\r\n" 
						  + "validityDate\t" + validityDate + "\r\n"
						  + "\r\n"
						  + "district\t" + district + "\r\n" 
						  + "districtDesc\t" + districtDesc + "\r\n" 
						  + "municipality\t" + municipality + "\r\n" 
						  + "municipalityDesc\t" + municipalityDesc + "\r\n" 
						  + "freguesia\t" + freguesia + "\r\n"
						  + "freguesiaDesc\t" + freguesiaDesc + "\r\n" 
						  + "streetTypeAbbr\t" + streetTypeAbbr + "\r\n"
						  + "streetType\t" + streetType + "\r\n" 
						  + "street\t" + street + "\r\n" 
						  + "buildingAbbr\t" + buildingAbbr + "\r\n" 
						  + "building\t" + building + "\r\n" 
						  + "door\t" + door + "\r\n" 
						  + "floor\t" + floor + "\r\n" 
						  + "side\t" + side + "\r\n" 
						  + "place\t" + place + "\r\n" 
						  + "cp4\t" + cp4 + "\r\n" 
						  + "cp3\t" + cp3 ;

		String fileName = numBI.substring(0, numBI.length() - 1);
		this.createTxtFile(fileName, stringText, "txt", folderPointer);
		this.savePhoto(folderPointer, fileName);
		this.infoMessage("Dados gravados com sucesso", "Os dados foram gravados na pasta '"+folderName+"'\ncom o nome: '"+fileName+"'");
	}

	/**
	 * Este método serve para recolher os dados do Cartão do Cidadão.
	 * @param getAddress - (true) Se pretender recolher também os dados da morada. Caso contário (false).
	 * @return (true) Se conseguir obter os dados com sucesso. Caso contrárion devolve (false).
	 * --------------------------------------------------------------------------------------------------
	 * This method is used to collect the data of the Citizen's Card.
	 * @param getAddress - (true) If you want to retrieve the address data as well. Otherwise (false).
	 * @return (true) If you can get the data successfully. Otherwise it returns (false).
	 */
	public boolean getData(boolean getAddress) 
	{
		try 
		{
			pteid.Init("");
			if (pteid.IsActivated() == 0) this.errorMessage("Este cartão ainda não está ativo.");
			pteid.SetSODChecking(false);

			PTEID_ID userData = pteid.GetID();
			if (null != userData) getPersonalInfo(userData);

			PTEID_PIC photoData = pteid.GetPic();
			if (null != photoData) picture = photoData.picture;
			dataGetted = true;
			if (getAddress) {
				PTEID_Pin[] pins = pteid.GetPINs();
				addrAtempts = pins[2].triesLeft;//Collect address attempts. 
				pteid.VerifyPIN(pins[2].id, pin);

				PTEID_ADDR addrData = pteid.GetAddr();
				if (null != userData) getAddress(addrData);
			}
		} catch (PteidException ex) {
			setErrorCode(ex);
			return false;
		} finally {
			try {
				pteid.Exit(0);
			} catch (PteidException e) {
				setErrorCode(e);
			}
		}
		return true;
	}

	/**
	 * Este método recolhe os dados pessoais do cartão.
	 * @param userData - Utiliza a API desenvolvida pelo estado Português, nomiadamente PTEID_ID
	 * ----------------------------------------------------------------------------------------
	 * This method collects the personal data of the card.
	 * @param userData - Uses the API developed by the Portuguese state, namely PTEID_ID
	 */
	public void getPersonalInfo(PTEID_ID userData) 
	{
		birthDate = userData.birthDate;
		cardNumber = userData.cardNumber.replaceFirst("^0+(?!$)", "");
		cardNumberPAN = userData.cardNumberPAN.replaceFirst("^0+(?!$)", "");
		cardVersion = userData.cardVersion;
		country = userData.country;
		deliveryDate = userData.deliveryDate;
		deliveryEntity = userData.deliveryEntity;
		documentType = userData.documentType;
		firstName = userData.firstname;
		name = userData.name;
		firstNameMother = userData.firstnameMother;
		nameMother = userData.nameMother;
		firstNameFather = userData.firstnameFather;
		nameFather = userData.nameFather;
		height = userData.height;
		locale = userData.locale;
		mrz1 = userData.mrz1;
		mrz2 = userData.mrz2;
		mrz3 = userData.mrz3;
		notes = userData.notes;
		numBI = userData.numBI.replaceFirst("^0+(?!$)", "");
		numSS = userData.numSS.replaceFirst("^0+(?!$)", "");
		numNIF = userData.numNIF.replaceFirst("^0+(?!$)", "");
		numSNS = userData.numSNS.replaceFirst("^0+(?!$)", "");
		sex = userData.sex;
		validityDate = userData.validityDate;
	}


	/**
	 * Este método recolhe os dados da morada do cartão.
	 * @param userData - Utiliza a API desenvolvida pelo estado Português, nomiadamente PTEID_ADDR
	 * ------------------------------------------------------------------------------------------
	 * This method collects the card address data.
	 * @param userData - Uses the API developed by the Portuguese state, namely PTEID_ADDR
	 */
	public void getAddress(PTEID_ADDR addrData) 
	{
		district = addrData.district;
		districtDesc = addrData.districtDesc;
		municipality = addrData.municipality;
		municipalityDesc = addrData.municipalityDesc;
		freguesia = addrData.freguesia;
		freguesiaDesc = addrData.freguesiaDesc;
		streetTypeAbbr = addrData.streettypeAbbr;
		streetType = addrData.streettype;
		street = addrData.street;
		buildingAbbr = addrData.buildingAbbr;
		building = addrData.building;
		door = addrData.door;
		floor = addrData.floor;
		side = addrData.side;
		cp4 = addrData.cp4;
		cp3 = addrData.cp3;
		place = addrData.place;

		if ("N".equals(addrData.addrType)) 
		{
			addrType = "Nacional";
			locality = addrData.locality;
			postal = addrData.postal;
			countryAddr = addrData.country;
			numMor = addrData.numMor;
		} else {
			addrType = "Internacional";
			locality = addrData.localityF;
			postal = addrData.postalF;
			countryAddr = addrData.countryDescF;
			numMor = addrData.numMorF;
			addressF = addrData.addressF;
			cityF = addrData.cityF;
			regioF = addrData.regioF;
		}
	}

	/**
	 * Este método é utilizado para reportar os erros lançados pela API do estado Português.
	 * @param exception - O erro lançado pela PteidException da API
	 * -------------------------------------------------------------------------------------
	 * This method is used to report bugs released by the API of the Portuguese state.
	 * @param exception - Error thrown by PteidException of API
	 */
	public void setErrorCode(PteidException exception) 
	{
		exception.printStackTrace();
		String errorMessage = exception.getMessage();
		String[] exceptionSplited = errorMessage.split("-");
		int errorCode = Integer.parseInt(exceptionSplited[1]);
		if (PIN_CODE_INCORRECT == errorCode && DEFAULT_ADDR_PIN.equals(pin)) 
		{
			validAddrPin = false;
			addrAtempts--;
			errorMessage("Não foi possível ler os dados de morada com o pin genérico.\nRestam-lhe "+addrAtempts+" tentativa(s).");
			pin = JOptionPane.showInputDialog(new JFrame(), "Por favor indique o pin de morada.", "Pin da Morada", JOptionPane.WARNING_MESSAGE);
			if (pin != null) 
			{
				getData(true);//Will read the citizen card
				validAddrPin = true;
			} else {
				validAddrPin = false;
			}
		} else {
			errorCC(errorCode, errorMessage);
		}
	}

	/**
	 * Responsável por mandar imprimir os erros devolvidos pela API segundo um código de erro.
	 * @param errorNumber - Código de erro
	 * @param exceptionMessage - Mensagem de erro.
	 * ---------------------------------------------------------------------------------------
	 * Responsible for sending the errors returned by the API according to an error code.
	 * @param errorNumber - Error code
	 * @param exceptionMessage - error message.
	 */
	void errorCC(int errorNumber, String exceptionMessage) 
	{
		switch (errorNumber) {
		case NO_READERS_FOUND:
			errorMessage("Não foi detetado nenhum leitor de cartões.");
			System.exit(0);
			break;
		case CARD_NOT_PRESENT:
			errorMessage("Não foi possível aceder ao Cartão do Cidadão.\nVerifique se está corretamente inserido no leitor.");
			break;
		case KEYPAD_CANCELLED:
			errorMessage("Ação cancelada pelo utilizador.");
			break;
		case CARD_UNRESPONSIVE:
			errorMessage("Cartão danificado.");
			break;
		case NOT_ALLOWED:
			errorMessage("Não foi permitido.");
			break;
		case INVALID_CARD:
			errorMessage("O cartão inserido não corresponde a um Cartão do Cidadão válido.");
			break;
		case AUTH_METHOD_BLOCKED:
			errorMessage("O pin da morada encontra-se bloqueado.\nDesloque-se a uma Loja do Cidadão para solicitar o desbloqueamento.");
			break;
		case PIN_CODE_INCORRECT:
			errorMessage("Pin de morada inválido.\nRestam-lhe "+addrAtempts+" tentativa(s).");
			break;
		case INVALID_PIN_LENGTH:
			errorMessage("Pin inválido, não tente novamente.");
			break;
		default:
			errorMessage("Erro desconhecido.\n" + exceptionMessage);
			break;
		}
	}

	/**
	 * Imprime no ecrã do utilizador dentro de uma janela o erro a descriminar.
	 * @param message - mensagem do erro obtido.
	 * -----------------------------------------------------------------------
	 * Prints on the user's screen inside a window the error to be discriminated. 
	 * @param message - error message obtained.
	 */
	public void errorMessage(String message) 
	{
		JOptionPane.showMessageDialog(new JFrame(), message, "Error", JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Imprime no ecrã do utilizador dentro de uma janela a informação a descriminar.
	 * @param title - Titulo descritivo
	 * @param message - Menssagem com a informação para o utilizador
	 * ------------------------------------------------------------------------------
	 * Print on the user's screen in a window the information to be discriminated. 
	 * @param title - Descriptive title 
	 * @param message - Message with information for the user
	 */
	public void infoMessage(String title, String message) 
	{
		JTextArea messageBox = new JTextArea();
		messageBox.setText(message);
		JOptionPane.showMessageDialog(new JFrame(), messageBox, title, JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Cria um ficheiro com os seguintes parametros:
	 * @param fileName - Nome para o ficheiro
	 * @param text - Texto para o ficheiro 
	 * @param extension - Estenção para o ficheiro
	 * @param folderPointer - Apontador para a pasta de destino utilizando a class java.io.File
	 * ----------------------------------------------------------------------------------------
	 * Creates a file with the following parameters:
	 * @param fileName - Name for the file
	 * @param text - Text to file
	 * @param extension - Exception to file
	 * @param folderPointer - Pointer to the destination folder using the class java.io.File
	 */
	public void createTxtFile(String fileName, String text, String extension, File folderPointer) 
	{
		File outputFile = new File(folderPointer, fileName + "." + extension);
		PrintWriter outputWriter;
		try 
		{
			//String textOutput = new String(text.getBytes(),"Cp1252");
			outputWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(outputFile),Charset.forName("Cp1252")),true);
			outputWriter.write(text);			
			outputWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Este método é utilizado para redimencionar a fotografia da pessoa.
	 * @param width - largura da foto pretendida.
	 * @param height - altura da foto pretendida.
 	 * @return devolve a mesma imagem redimensionada.
	 * -----------------------------------------------------------------------------------------------
	 * This method is used to resize the photograph of the person.
	 * @param Width - width of the desired photo.
	 * @param Height - height of the desired photo.
	 * @return The same image resized.
	 */
	public BufferedImage getPhotoRedim(int width, int height) 
	{
		Jpeg2000Decoder imageDecoder = new Jpeg2000Decoder();

		BufferedImage file = null;
		try 
		{
			file = imageDecoder.read(picture);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (width == 0 && height == 0) 
		{
			width = file.getWidth();
			height = file.getHeight();
		}

		Image scaledPhoto = file.getScaledInstance(width, height, Image.SCALE_SMOOTH);
		BufferedImage photoRedimensioned = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		Graphics2D graphics2D = photoRedimensioned.createGraphics();
		graphics2D.drawImage(scaledPhoto, 0, 0, null);
		graphics2D.dispose();
		return photoRedimensioned;
	}

	/**
	 * Este método é utilizado para guardar a fotografia do cartão num ficheiro na direturia indicada.
	 * @param folderPointer - O caminho do arquivo com o diretório.
 	 * @return devolve a mesma imagem que foi armazenada na pasta.
	 * -----------------------------------------------------------------------------------------------
	 * This method is used to save the photo card in a file indicated direturia.
	 * @param folderPointer - The file path with the directory.
	 * @return the same image that is been stored in a folder.
	 */
	public BufferedImage savePhoto(File folderPointer, String fileName) 
	{
		Jpeg2000Decoder imageDecoder = new Jpeg2000Decoder();

		BufferedImage bufferedPhoto = null;
		try 
		{
			bufferedPhoto = imageDecoder.read(picture);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try 
		{
			ImageIO.write(bufferedPhoto, "jpg", new File(folderPointer, fileName + ".jpg"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		return bufferedPhoto;
	}
}