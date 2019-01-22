package Server;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;
import java.io.Serializable;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import Common.Book;
import Common.Copy;
import Common.IEntity;
import Common.ObjectMessage;
import Common.ReaderAccount;
import clientCommonBounderies.AClientCommonUtilities;
import clientCommonBounderies.LogInController;


public abstract class  ABookDBController 
{
	/**
	 * This function sorts the request in the 'msg' to the relevant function and returns the answer
	 * @param msg - the object from the client
	 * @param connToSQL - the connection to the MySQL created in the Class OBLServer
	 * @return ObjectMessage with the answer to the client
	 */
	public static ObjectMessage selection(ObjectMessage msg, Connection connToSQL)
	{

		if (((msg.getMessage()).equals("addBook")))
		{
			return tryToAddBook(msg, connToSQL);
		}
		else if (((msg.getMessage()).equals("searchBookID")))
		{
			return searchBookID(msg, connToSQL);
		}
		else if (((msg.getMessage()).equals("SearchBook")))
		{
			return searchBook(msg, connToSQL);
		}
		else if((msg.getMessage()).equals("setLocation"))
		{
			return tryToSetLocationOfBook(msg, connToSQL);
		}
		else if((msg.getMessage()).equals("showBookInfo"))
		{
			return showBookInfo(msg, connToSQL);
		}
		
		else if((msg.getMessage()).equals("changeBookInfo"))
		{
			return changeBookInfo(msg, connToSQL);
		}
		else
			return null; 
	}


	private static ObjectMessage changeBookInfo(ObjectMessage msg, Connection connToSQL)
	{
		
		return null;
	}


	/**
	 * The function check if book exist in DB and return all data about specific book
	 * @param msg- the object from the client
	 * @param connToSQL - the connection to the MySQL created in the Class OBLServer
	 * @return ObjectMessage with the answer to the client
	 */
	private static ObjectMessage showBookInfo(ObjectMessage msg, Connection connToSQL)
	{

		Book tempBook=(Book)msg.getObjectList().get(0);
		PreparedStatement checkBook = null; 
		ResultSet rs1 = null;
		try 
		{
			String query= "SELECT * FROM book WHERE bookId = ?  ";
			checkBook = connToSQL.prepareStatement(query);
			checkBook.setInt(1,tempBook.getBookID()); 
			rs1 =checkBook.executeQuery();

			//send to client answer if there is book already exist in DB with all information about book 
			if(rs1.next())
			{
				tempBook.setBookName(rs1.getString(2));
				tempBook.setAuthorName(rs1.getString(3));
				tempBook.setDateOfBook(rs1.getInt(4));
				tempBook.setTopic(rs1.getString(5));
				tempBook.setDesired(rs1.getBoolean(6));
				tempBook.setEdition(rs1.getInt(7));
				tempBook.setBookLocation(rs1.getString(8));
				return new ObjectMessage(tempBook, "FoundBook");
				
			}
			else //if there is no book with the same bookId
			{
				return new ObjectMessage("Book not exist in DB","Wrong");
			}


		}
		catch (SQLException e)
		{
			e.printStackTrace();

		}
		return new ObjectMessage("Book not exist in DB","Wrong");
	}
	


	/**
	 * 
	 */
	private static ObjectMessage tryToSetLocationOfBook(ObjectMessage msg, Connection connToSQL) 
	{

		ObjectMessage massegeRes;
		PreparedStatement checkBook = null; 
		ResultSet rs1 = null;
		Book tempBook=(Book)msg.getObjectList().get(0);
		try 
		{
			String query= "SELECT * FROM book WHERE bookName = ? AND authorName = ? AND year = ? AND edition = ? ";
			checkBook = connToSQL.prepareStatement(query);
			checkBook.setString(1,tempBook.getBookName()); 
			checkBook.setString(2, tempBook.getAuthorName());
			checkBook.setInt(3, tempBook.getDateOfBook());
			checkBook.setInt(4,tempBook.getEdition());
			rs1 =checkBook.executeQuery();

			//send to client answer if there is book already exist in db and have location
			if(rs1.next())
			{
				return new ObjectMessage(rs1.getString(8),"LocationFound");
			}
			else
			{
				return new ObjectMessage(" ","LocationNotFound");
			}

		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return new ObjectMessage("Unexpected Error.","Unsucessfull");

		}

	}



	/**
	 * This function add book to the DB in MySQL. It is check if it is must be added like new book or copy for book that already exist
	 * @param msg- the object from the client
	 * @param connToSQL -the connection to the MySQL created in the Class OBLServer
	 * @return ObjectMessage with the answer to the client if book was added successfully or not or it has wrong .
	 */
	private static ObjectMessage tryToAddBook(ObjectMessage msg, Connection connToSQL)
	{
		ObjectMessage massegeRes;
		PreparedStatement checkBook = null; 
		PreparedStatement addCopy=null ;
		PreparedStatement addBook=null ;
		ResultSet rs1 = null; 


		Book tempBook=(Book)msg.getObjectList().get(0);

		try 
		{
			String query= "SELECT * FROM book WHERE bookName = ? AND authorName = ? AND year = ? AND edition = ? ";
			checkBook = connToSQL.prepareStatement(query);
			checkBook.setString(1,tempBook.getBookName()); 
			checkBook.setString(2, tempBook.getAuthorName());
			checkBook.setInt(3, tempBook.getDateOfBook());
			checkBook.setInt(4,tempBook.getEdition());
			rs1 =checkBook.executeQuery();

			//add like copy
			if(rs1.next())
			{
				if(tempBook.isFileIsLoaded())
				{
					return new ObjectMessage("Error! You can't add file to existing book. Delete file.","Wrong");
				}
				else
				{
					if(Boolean.parseBoolean(rs1.getString(6))==(tempBook.isDesired()))//check if same desire
					{
						if (rs1.getString(5).equals(tempBook.getTopic()))//if it is same book 
						{
							for(int i=0;i<tempBook.getNumberOfCopies();i++)//add copies for book
							{
								addCopy  = connToSQL.prepareStatement(" INSERT INTO `Copy` (`bookId`) VALUES (?)"); 
								addCopy.setInt(1, Integer.parseInt(rs1.getString(1)));
								addCopy.executeUpdate();
							}

							return new ObjectMessage("This Book is already exist in the system,so successfully added it like copy.","Successfull");
						}
						else //if there is different topics
						{
							return new ObjectMessage("Error!Please change topic. There is also book with the same name,author ,year and edition.","Wrong");
						}

					}
					else  
					{
						//if there is same book but different desired
						return new ObjectMessage("Error!Please change desired choise. There is book with the same name, author, year and edition.","Wrong");

					}
				}
			}

			//add like book
			else 
			{   
				if (tempBook.getBookLocation()==null ||tempBook.getBookLocation().equals(""))
				{

					return new ObjectMessage("Error!Please enter book`s location.","Wrong");
				}
				else
				{
					if(!tempBook.isFileIsLoaded()) 
					{
						return new ObjectMessage("Error!You need upload table of contekst for new book.","Wrong");
					}
					else 
					{  //add new book in to the table
						addBook=connToSQL.prepareStatement("INSERT INTO `Book` (`bookName`,`authorName`,`year`,`topic`,`isDesired`,`edition`,`bookLocation`) VALUES (?,?,?,?,?,?,?)");
						addBook.setString(1, (String)tempBook.getBookName());
						addBook.setString(2, (String)tempBook.getAuthorName());
						addBook.setInt(3, (int)tempBook.getDateOfBook());
						addBook.setString(4, tempBook.getTopic());//tempBook.getTopic()
						addBook.setBoolean(5,(Boolean) tempBook.isDesired());//(Boolean) tempBook.isDesired()
						addBook.setInt(6, (int)tempBook.getEdition());
						addBook.setString(7, tempBook.getBookLocation());//tempBook.getTopic()
						addBook.executeUpdate();

						//add copy of this book
						String queryForNewBook= "SELECT * FROM book WHERE bookName = ? AND authorName = ? AND year = ? AND edition = ?";
						checkBook = connToSQL.prepareStatement(queryForNewBook);
						checkBook.setString(1,tempBook.getBookName()); 
						checkBook.setString(2, tempBook.getAuthorName());
						checkBook.setInt(3, tempBook.getDateOfBook());
						checkBook.setInt(4,tempBook.getEdition());
						rs1 =checkBook.executeQuery();
					}


				}


				if(rs1.next())
				{
					for(int i=0; i<tempBook.getNumberOfCopies(); i++)//add copies for book
					{
						addCopy  = connToSQL.prepareStatement(" INSERT INTO `Copy` (`bookId`) VALUES (?)"); 
						addCopy.setInt(1, Integer.parseInt(rs1.getString(1)));
						addCopy.executeUpdate();
					}

				}
				return new ObjectMessage("This Book was successfully added like book and like copy.","Successfull");

			}	
		}	
		catch (SQLException e)
		{
			e.printStackTrace();
			return new ObjectMessage("Unexpected Error.","Unsucessfull");

		}

	}



	private static ObjectMessage searchBook(ObjectMessage msg, Connection connToSQL)
	{
		int isFreeSearch=0;
		PreparedStatement ps = null;
		ObjectMessage answer;
		Book askedBook=(Book)msg.getObjectList().get(0);
		String input = null;
		ArrayList <IEntity> result=new ArrayList<IEntity>();
		try 
		{
			if(askedBook.getAuthorName()!=null)
			{
				ps = connToSQL.prepareStatement("SELECT * FROM obl.book WHERE authorName LIKE ?");
				input=askedBook.getAuthorName();
			}
			else if(askedBook.getBookName()!=null)
			{
				ps = connToSQL.prepareStatement("SELECT * FROM obl.book WHERE bookName LIKE ?");
				input=askedBook.getBookName();
			}
			else if(askedBook.getTopic()!=null)
			{
				ps = connToSQL.prepareStatement("SELECT * FROM obl.book WHERE topic LIKE ?");
				input=askedBook.getTopic();
			}
			else
			{
				isFreeSearch=1;
				String freeSearch=askedBook.getFreeSearch();
				String[] arrFreeSearch = freeSearch.split("\\s+");
				//pass on every word in free search and check if exist in db
				for ( String ss : arrFreeSearch)
				{
					ps = connToSQL.prepareStatement("SELECT * FROM obl.book WHERE bookName LIKE ? OR authorName LIKE ? OR year LIKE ? OR topic LIKE ? ");
					ps.setString(1,"%"+ss+"%");
					ps.setString(2,"%"+ss+"%");
					ps.setString(3,"%"+ss+"%");
					ps.setString(4,"%"+ss+"%");
					ResultSet rs= ps.executeQuery();
					while(rs.next())
					{
						Book book=new Book(rs.getString(1),rs.getString(2),rs.getString(3),rs.getInt(4),rs.getString(5),rs.getBoolean(6),rs.getInt(7),rs.getString(8));
						Copy copy=new Copy(-1,rs.getInt(1),null);
						ObjectMessage message=new ObjectMessage(copy,"checkIfAllBorrowed","Copy");
						ObjectMessage resultOfCopy=ACopyDBController.selection(message,connToSQL);
						if(resultOfCopy.getNote().equals("FoundBook"))
						{
							book.setNumberOfCopies(1);
						}
						else
						{
							book.setNumberOfCopies(0);
							message=new ObjectMessage(copy,"closetReturnDate","Copy");
							resultOfCopy=ACopyDBController.selection(message,connToSQL);
							try 
							{
								book.setClosetReturn(((Copy)(resultOfCopy.getObjectList().get(0))).getReturnDate());
							}
							catch(Exception e)
							{
								e.printStackTrace();
							}
						}
						result.add(book);
					} 

				}
			}
			//if it is not free search
			if(isFreeSearch==0)
			{
				ps.setString(1,"%"+input+"%");
				ResultSet rs = ps.executeQuery();
				while(rs.next())
				{
					Book book=new Book(rs.getString(1),rs.getString(2),rs.getString(3),rs.getInt(4),rs.getString(5),rs.getBoolean(6),rs.getInt(7),rs.getString(8));
					Copy copy=new Copy(-1,rs.getInt(1),null);
					ObjectMessage message=new ObjectMessage(copy,"checkIfAllBorrowed","Copy");
					ObjectMessage resultOfCopy=ACopyDBController.selection(message,connToSQL);
					if(resultOfCopy.getNote().equals("FoundBook"))
					{
						book.setNumberOfCopies(1);
					}
					else
					{
						book.setNumberOfCopies(0);
						message=new ObjectMessage(copy,"closetReturnDate","Copy");
						resultOfCopy=ACopyDBController.selection(message,connToSQL);
						try 
						{
							book.setClosetReturn(((Copy)(resultOfCopy.getObjectList().get(0))).getReturnDate());
						}
						catch(Exception e)
						{
							e.printStackTrace();
						}
					}
					result.add(book);
				} 


			}

			if(!result.isEmpty())
			{
				answer=new ObjectMessage(result,"BookSearch","BooksFound");
			}
			else
			{
				answer=new ObjectMessage("BookSearch","NoBookFound");
			}
		}

		catch (SQLException e) 
		{
			e.printStackTrace();
			answer=new ObjectMessage("BookSearch","NoBookFound");
		}
		return answer;
	}
	
	private static ObjectMessage searchBookID(ObjectMessage msg, Connection connToSQL)
	{
		PreparedStatement ps = null;
		ObjectMessage answer;
		Book askedBook=(Book)msg.getObjectList().get(0);
		try 
		{
			ps = connToSQL.prepareStatement("SELECT * FROM obl.book WHERE bookId= ?");
			ps.setInt(1, askedBook.getBookID());
			ResultSet rs= ps.executeQuery();
			rs.next();
			Book book=new Book(rs.getString(1),rs.getString(2),rs.getString(3),rs.getInt(4),rs.getString(5),rs.getBoolean(6),rs.getInt(7),rs.getString(8));
			answer=new ObjectMessage(book,"BookSearch","BookFound");
		} 
		catch (SQLException e) 
		{
			answer=new ObjectMessage("BookSearch","NoBookFound");
			e.printStackTrace();
		}
		return answer;
	}
	
}
