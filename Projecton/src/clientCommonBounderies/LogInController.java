package clientCommonBounderies;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;

import Common.IGUIController;
import Common.ObjectMessage;
import Common.User;
import clientBounderiesLibrarian.StartPanelLibrarianController;
import clientConrollers.OBLClient;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.stage.Stage;

public class LogInController  implements IGUIController
{
	OBLClient client;

	public static String currentID;
	protected static Object startPanelController;
	

    @FXML
    private JFXTextField logInIDTextField;

    @FXML
    private JFXPasswordField logInPasswordTextField;

    @FXML
    private JFXButton LogInBtn;

    @FXML
    private JFXButton cancelLogInBtn;

    
    @FXML
    void initialize()
    {
    	//get the client from the StartPanelController
    	client=StartPanelController.connToClientController;
    	client.setClientUI(this);
    }
    
    
    @FXML
    void cancelLogIn(ActionEvent event) 
    {

    }

    @FXML
    void makeLogIn(ActionEvent event) 
    {
    	String id = logInIDTextField.getText();
    	String password = logInPasswordTextField.getText();
    	
    	currentID=id;
    	
    	System.out.println(id + "  " + password);
    	
    	User user = new User(id,password);
    	ObjectMessage msg = new ObjectMessage(user,"user try to log in","User");
    	
    	client.handleMessageFromClient(msg); 
    }

    
    @Override
	public void display(ObjectMessage msg) 
    {
    	//if successful go to the relevant start panel
		if((msg.getMessage()).equals("successful"))
		{
			try 
			{
				Platform.runLater(()->
				{ 
					AClientCommonUtilities.stage.close();
				});
			}
			catch (Exception e) 
			{
		
				e.printStackTrace();
			}
			 // 1 = Library Director , 2 = Librarian , 3 = reader account
			if((msg.getNote()).equals("1"))
			{
				
				AClientCommonUtilities.loadStartPanelWindow(startPanelController,"/clientBounderiesLibrarian/StartPanelLibraryDirector.fxml","Librarian Start Panel");
			}
			else if((msg.getNote()).equals("2"))
			{
				AClientCommonUtilities.loadStartPanelWindow(startPanelController,"/clientBounderiesLibrarian/StartPanelLibrarian.fxml","Librarian Start Panel");
			}
			else if((msg.getNote()).equals("3"))
			{
				AClientCommonUtilities.loadStartPanelWindow(startPanelController,"/clientBounderiesReaderAccount/StartPanelReaderAccount.fxml","Librarian Start Panel");
			}
		}
		
		else
		{
			if((msg.getMessage()).equals("unsuccessful"))
			{
				
				AClientCommonUtilities.alertError("User already connected", "Error");
			}
			else
			{
				AClientCommonUtilities.alertError("ID or Password not match", "Error");
			}
		}
		
	}
}