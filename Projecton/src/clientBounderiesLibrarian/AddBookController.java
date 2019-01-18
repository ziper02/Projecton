package clientBounderiesLibrarian;

import com.jfoenix.controls.JFXButton;
import java.net.*;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXTextField;

import Common.Book;
import Common.IGUIController;
import Common.ObjectMessage;
import clientCommonBounderies.AClientCommonUtilities;
import clientCommonBounderies.StartPanelController;
import clientConrollers.AValidationInput;
import clientConrollers.OBLClient;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.input.InputMethodEvent;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class AddBookController implements IGUIController
{
	OBLClient client;
	@FXML
	private ResourceBundle resources;

	@FXML
	private URL location;

	@FXML 
	private JFXTextField TopicTextField; 
	@FXML
	private JFXTextField BookTitleTextField;

	@FXML 
	private JFXTextField numberOfCopies; 

	@FXML
	private JFXTextField BookAuthorTextField;

	@FXML
	private JFXTextField PublishedYearTextField;

	@FXML
	private JFXTextField EditionTextField;


	@FXML
	private JFXCheckBox DesiredCheckBox;

	@FXML
	private DatePicker PurchaseDateDatePicker;

	@FXML
	private Button UploadBtn;

	@FXML
	private JFXButton CancelBtn;

	@FXML
	private JFXButton SaveBtn;

	@FXML
	private Label fileLabel;

	@FXML
	private Button cancelUploadBtn;

	@FXML
	private ComboBox<String> bookLocationLetter;

	@FXML
	private ComboBox<String> BookLocationNumber;

	ObservableList<String> list1;
	ObservableList<String> list2;
	
	private boolean isUploaded =false;
	private static File f;

	//function for cancel button. if librarian doesn't want  continue to add a book
	@FXML
	void cancelBtnClicked(ActionEvent event) 
	{
		AClientCommonUtilities.backToStartPanel();
	}


	@FXML
	void cancelUpload(ActionEvent event)
	{
		isUploaded=false;
		fileLabel.setText(" " );
		cancelUploadBtn.setVisible(false);
	}
	 
	public void combo() 
	{
		ArrayList <String> s=new ArrayList<String>();
		
		for(int i=1; i<=6; i++)
		{
			s.add(Integer.toString(i));
		}
			
		list1 = FXCollections.observableArrayList(s);
		BookLocationNumber.setItems( list1);
		
		ArrayList <String> b=new ArrayList<String>();
		char ch = 'A';
		
		for(int i= 0; i<= ('Z'- 'A'); i++)
		{
			b.add(String.valueOf(ch));
			ch++;
		}
		list2 = FXCollections.observableArrayList(b);
		bookLocationLetter.setItems((ObservableList) list2);
	}


	//add new book or only copy
	@FXML
	void saveAddNewBook(ActionEvent event) 
	{ 
		String checkResult = validationFields();

		if(checkResult.equals("correct"))//if all fields correctly
		{
			String bookLocation;
			
			if( ( bookLocationLetter.getValue() == null  || (bookLocationLetter.getValue().toString()).equals(null) ) 
					|| ( BookLocationNumber.getValue() == null  || (BookLocationNumber.getValue().toString()).equals(null) ) )
			{
				bookLocation = "";
			}
			else
			{
				bookLocation =  bookLocationLetter.getValue() + "-" + BookLocationNumber.getValue();
			}
			
			
			boolean isDesired= DesiredCheckBox.isSelected();
			Book book=new Book(BookTitleTextField.getText(), BookAuthorTextField.getText(),PublishedYearTextField.getText(),TopicTextField.getText(),String.valueOf(isDesired),EditionTextField.getText(),numberOfCopies.getText(), bookLocation);
			System.out.println(String.valueOf(isDesired));
			ObjectMessage msg= new ObjectMessage(book,"addBook","Book");

			book.setFileIsLoaded(isUploaded);
			client.handleMessageFromClient(msg);
		}
		else
		{
			AClientCommonUtilities.alertErrorWithOption(checkResult,"Wrong","Back");		
		}



		/*if(isUploaded)
		{

			File myFile = new File(f.getAbsolutePath());
			Socket sock;
			ServerSocket servsock;
			BufferedInputStream bis;
			try
			{
				servsock = new ServerSocket(5643);
				client.handleMessageFromClient(msg);
				sock = servsock.accept();
				byte[] mybytearray = new byte[(int) myFile.length()];
				bis = new BufferedInputStream(new FileInputStream(myFile));
				bis.read(mybytearray, 0, mybytearray.length);
				OutputStream os = sock.getOutputStream();
				os.write(mybytearray, 0, mybytearray.length);
				os.flush();
				sock.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}

		}*/



	}


	//function upload file and send it to server/ sending- still not writing
	@FXML
	void uploadTableOfContents(ActionEvent event) 
	{
		FileChooser fc=new FileChooser();
		fc.getExtensionFilters().add(new ExtensionFilter("PDF Files","*.pdf"));
		f=fc.showOpenDialog(null);
		if(f!=null)
		{
			isUploaded=true;
			fileLabel.setText("Selected File::" + f.getAbsolutePath());
			cancelUploadBtn.setVisible(true);
		}


	}


	@FXML
	void initialize() 
	{
		client=StartPanelController.connToClientController;
		client.setClientUI(this);
		combo();
		
		//fillFileds();

	}

	private void fillFileds() 
	{
		BookTitleTextField.setText("");
		BookAuthorTextField.setText("");
		PublishedYearTextField.setText("");
		EditionTextField.setText("");
		
	}


	//check validation
	private String validationFields()
	{
		String result,finalResult="";

		result=AValidationInput.checkValidationBook("bookName",BookTitleTextField.getText());
		if(!result.equals("correct"))
		{
			finalResult+=result+'\n';
		}


		result=AValidationInput.checkValidationBook("authorName",BookAuthorTextField.getText());
		if(!result.equals("correct"))
		{
			finalResult+=result+'\n';
		}

		result=AValidationInput.checkValidationBook("dateOfBook",PublishedYearTextField.getText());
		if(!result.equals("correct"))
		{
			finalResult+=result+'\n';
		}

		result=AValidationInput.checkValidationBook("topic",TopicTextField.getText());
		if(!result.equals("correct"))
		{
			finalResult+=result+'\n';
		}

		if(EditionTextField.getText().equals("") || null == EditionTextField.getText())
		{
			finalResult+="Insert number of edition";

		}
		else
		{
			for(int i=0;i<EditionTextField.getText().length();i++)//check validation for edition text field
			{

				if(EditionTextField.getText().charAt(i)<'0' ||EditionTextField.getText().charAt(i)>'9')        
				{
					finalResult+="Edition must contain only numbers";
					break;
				}

			}
		}

		if(numberOfCopies.getText().equals("") || null == numberOfCopies.getText())
		{
			finalResult+="Insert number of copies";

		}
		else
		{
			for(int i=0;i<numberOfCopies.getText().length();i++)//check validation for number of copies text field
			{

				if(numberOfCopies.getText().charAt(i)<'0' ||numberOfCopies.getText().charAt(i)>'9')        
				{
					finalResult+="Edition must contain only numbers";
					break;
				}

			}
		}


		if(finalResult.equals(""))
		{
			return "correct";
		}

		else
		{
			return finalResult;
		}

	}

	/**
	 * @param event
	 */
	@FXML
	void checkLocation(ActionEvent  event) 
	{
		System.out.println("here1");
		String bookName, authorName; 
		int year, edition;
		
		if(BookTitleTextField.getText().equals("") || null == BookTitleTextField.getText())
		{
			bookName= " ";
		}
		else
		{
			bookName=BookTitleTextField.getText();
		}
		
		if(BookTitleTextField.getText().equals("") || null == BookTitleTextField.getText())
		{
			authorName= " ";
		}
		else
		{
			authorName=BookAuthorTextField.getText();
		}
		
		if(BookTitleTextField.getText().equals("") || null == BookTitleTextField.getText())
		{
			year=0;
		}
		else
		{
			year=Integer.parseInt(PublishedYearTextField.getText());
		}
		
		if(BookTitleTextField.getText().equals("") || null == BookTitleTextField.getText())
		{
			edition=0;
		}
		else
		{
			edition=Integer.parseInt(EditionTextField.getText());
		}
		
		Book book = new Book(bookName, authorName, year,edition );
		System.out.println(book);
		ObjectMessage msg= new ObjectMessage(book,"setLocation","Book");
		client.handleMessageFromClient(msg);
		
		
	}

	@Override
	public void display(ObjectMessage msg)
	{
		
		if (msg.getNote().equals("Successfull"))
		{
			//if file was uploaded and the "BookAdd" was successful add the file
			if(isUploaded)
			{
				sendFile();
			}

			AClientCommonUtilities.infoAlert(msg.getMessage(), msg.getNote());
			AClientCommonUtilities.backToStartPanel();

		}

		else if (msg.getNote().equals("Unsuccessfull"))
		{
			AClientCommonUtilities.alertErrorWithExit(msg.getMessage(), msg.getNote());
		}
		
		else if (msg.getNote().equals("Wrong"))
		{
			AClientCommonUtilities.alertErrorWithOption(msg.getMessage(), msg.getNote(),"Back");
		}
		
		else if(msg.getNote().equals("LocationFound")) 
		{
			System.out.println("here2");
			setLocation(msg);
			
		}
		
		else if(msg.getNote().equals("LocationNotFound")) 
		{
			System.out.println("here3");
			bookLocationLetter.setDisable(false);
			BookLocationNumber.setDisable(false);
		}

	}
	private void setLocation(ObjectMessage msg)
	{
		
		bookLocationLetter.setValue(String.valueOf(msg.getMessage().charAt(0)));
		BookLocationNumber.setValue(String.valueOf(msg.getMessage().charAt(2)));
		bookLocationLetter.setDisable(true);
		BookLocationNumber.setDisable(true);
	}


	public void sendFile()
	{
		File myFile = new File(f.getAbsolutePath());
		Socket sock;
		ServerSocket servsock;
		BufferedInputStream bis;
		try
		{
			ObjectMessage m = new ObjectMessage();
			m.setNote("AddPDF");
			m.setMessage(Integer.toString((int) myFile.length()));
			servsock = new ServerSocket(5643);
			client.handleMessageFromClient(m);
			sock = servsock.accept();
			byte[] mybytearray = new byte[(int) myFile.length()];
			bis = new BufferedInputStream(new FileInputStream(myFile));
			bis.read(mybytearray, 0, mybytearray.length);
			OutputStream os = sock.getOutputStream();
			os.write(mybytearray, 0, mybytearray.length);
			os.flush();
			sock.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

}
