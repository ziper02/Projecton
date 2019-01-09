package clientBounderiesLibrarian;

import com.jfoenix.controls.JFXRadioButton;
import com.jfoenix.controls.JFXTextField;

import Common.IGUIController;
import Common.ObjectMessage;
import clientConrollers.OBLClient;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;


/**
 *  Controller Class for 'StartPanelLibrarian.fxml' 
 */


public class StartPanelLibrarianController implements IGUIController
{
	//Instance variables **********************************************

	/**
	 * The instance of the client that created this ConsoleChat.
	 */
	OBLClient client;

	
    @FXML // fx:id="logOutBtn"
    private Button logOutBtn; 

    @FXML // fx:id="borrowBookBtn"
    private Button borrowBookBtn; 

    @FXML // fx:id="returnBookBtn"
    private Button returnBookBtn;

    @FXML // fx:id="addBookBtn"
    private Button addBookBtn; 

    @FXML // fx:id="deleteBookBtn"
    private Button deleteBookBtn; 

    @FXML // fx:id="registerNewAccountBtn"
    private Button registerNewAccountBtn; 

    @FXML // fx:id="searchBookTab"
    private Tab searchBookTab; 

    @FXML // fx:id="searchBookTextField"
    private JFXTextField searchBookTextField; 

    @FXML // fx:id="searchBookBtn"
    private Button searchBookBtn; 

    @FXML // fx:id="bookNameRB"
    private JFXRadioButton bookNameRB; 

    @FXML // fx:id="authorNameRB"
    private JFXRadioButton authorNameRB; 

    @FXML // fx:id="topicRB"
    private JFXRadioButton topicRB; 

    @FXML // fx:id="freeSearchBookRB"
    private JFXRadioButton freeSearchBookRB; 

    @FXML // fx:id="searchResultTable"
    private TableView<?> searchResultTable; 

    @FXML // fx:id="bookNameColumn"
    private TableColumn<?, ?> bookNameColumn; 

    @FXML // fx:id="authorNameColumn"
    private TableColumn<?, ?> authorNameColumn; 

    @FXML // fx:id="bookYearColumn"
    private TableColumn<?, ?> bookYearColumn; 

    @FXML // fx:id="BookTopicColumn"
    private TableColumn<?, ?> BookTopicColumn; 

    @FXML // fx:id="isDesiredBookColumn"
    private TableColumn<?, ?> isDesiredBookColumn; 

    @FXML // fx:id="viewIntroColumn"
    private TableColumn<?, ?> viewIntroColumn; 

    @FXML // fx:id="searchReaderAccountTab"
    private Tab searchReaderAccountTab; 

    @FXML // fx:id="searchReaderAccountSearchField"
    private JFXTextField searchReaderAccountSearchField; 

    @FXML // fx:id="searchReaderAccountBtn"
    private Button searchReaderAccountBtn; 

    @FXML // fx:id="iDRB"
    private JFXRadioButton iDRB; 

    @FXML // fx:id="firstNameRB"
    private JFXRadioButton firstNameRB; 

    @FXML // fx:id="lastNameRB"
    private JFXRadioButton lastNameRB; 

    @FXML // fx:id="freeSearchReaderAccountRB"
    private JFXRadioButton freeSearchReaderAccountRB; 

    @FXML // fx:id="searchReaderAccountTable"
    private TableView<?> searchReaderAccountTable; 

    @FXML // fx:id="accountIDColumn"
    private TableColumn<?, ?> accountIDColumn; 

    @FXML // fx:id="accountLastNameColumn"
    private TableColumn<?, ?> accountLastNameColumn; 

    @FXML // fx:id="accountStatusColumn"
    private TableColumn<?, ?> accountStatusColumn; 

    @FXML // fx:id="accountPhoneColumn"
    private TableColumn<?, ?> accountPhoneColumn;

    @FXML // fx:id="borrowsAndReservesColumn"
    private TableColumn<?, ?> borrowsAndReservesColumn; 
    
    //for the Library Director only
    ///////////////////////////////////////////////////////////////////////////////////
  
    @FXML // fx:id="reportBtn"
    private Button reportBtn; 
    
    @FXML
    private TableColumn<?, ?> freezeColumn;
    
    @FXML
    void openReportWindow(ActionEvent event) 
    {

    }
    ///////////////////////////////////////////////////////////////////////////////////
    
    
    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize(String []connectionDetails) 
    {
    	
    	String ip = connectionDetails[0];
    	int port = Integer.parseInt(connectionDetails[0]);
    	
    	try 
        {
          client= new OBLClient(ip, port, this);
        } 
        catch(IOException exception) 
        {
        	System.out.println("Error: Can't setup connection!"+ " Terminating client.");
        	System.exit(1);
        }
    	
    }
    

    @FXML
    void makeLogOut(ActionEvent event) 
    {

    }

    @FXML
    void makeSearch(ActionEvent event) 
    {

    }

    @FXML
    void openAddBook(ActionEvent event) 
    {
    	loadWindow("/clientBounderiesLibrarian/AddBook.fxml","Add book copy");
    }

    @FXML
    void openBorrowBook(ActionEvent event) 
    {
    	loadWindow("/clientBounderiesLibrarian/BorrowBook.fxml","Borrow book");
    }

    @FXML
    void openDeleteBookBtn(ActionEvent event) 
    {
    	loadWindow("/clientBounderiesLibrarian/DeleteBook.fxml","Delete book");
    }

    @FXML
    void openRegisterNewAccount(ActionEvent event) 
    {
    	loadWindow("/clientBounderiesLibrarian/RegisterNewAccount.fxml","Registrate new reader account");
    }

    @FXML
    void openReturnBook(ActionEvent event) 
    {
    	loadWindow("/clientBounderiesLibrarian/ReturnBook.fxml","Return book");
    }
    
    /**
     * this function open new the new window from current one 
     * @param loc is the location of the fxml file 
     * @param title is the title of the new window
     */
    void loadWindow(String loc, String title)
    {
    	try 
    	{
			Parent parent = FXMLLoader.load(getClass().getResource(loc));
			Stage stage = new Stage(StageStyle.DECORATED);
			stage.setTitle(title);
			stage.setScene(new Scene(parent));
			stage.show();
		} 
    	catch (IOException e) 
    	{
    		//-----TO_DO: open error message
			e.printStackTrace();
		}
    	
    }


	@Override
	public void display(ObjectMessage msg) 
	{
		// TODO Auto-generated method stub
		
	}

   
}

