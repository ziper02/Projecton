package Server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;

import Common.Book;
import Common.Copy;
import Common.IEntity;
import Common.ObjectMessage;
import Common.ReaderAccount;
import Common.User;

public abstract class ACopyDBController 
{
	
	/**
	 * This function sorts the request in the 'msg' to the relevant function and returns the answer
	 * @param msg - the object from the client
	 * @param connToSQL - the connection to the MySQL created in the Class OBLServer
	 * @return ObjectMessage with the answer to the client
	 */
	public static ObjectMessage selection(ObjectMessage msg, Connection connToSQL)
	{

		if (((msg.getMessage()).equals("checkIfAllBorrowed")))
		{
			return checkIfAllBorrowed(msg, connToSQL);
		}
		else if (((msg.getMessage()).equals("get borrows")))
		{
			return getBorrows(msg, connToSQL);
		}
		else if (((msg.getMessage()).equals("DeleteBook")))
		{
			return deleteBook(msg, connToSQL);
		}
		else if(((msg.getMessage()).equals("ReturnCopy")))
		{
			return tryToReturnBook(msg, connToSQL);
		} 
		else if(((msg.getMessage()).equals("ask for delay")))
		{
			return askForDelay(msg, connToSQL);
		} 
		else
		{
			return null; 
		}
	}
	 
	private static ObjectMessage askForDelay(ObjectMessage msg, Connection connToSQL) 
	{
		// TODO Auto-generated method stub
		return null;
	}

	private static ObjectMessage tryToReturnBook(ObjectMessage msg, Connection connToSQL) 
	{

		ObjectMessage answer=new ObjectMessage();
		PreparedStatement checkCopy=null;
		PreparedStatement updateCopy=null;
		PreparedStatement checkReaderAccountDelays=null;
		PreparedStatement updateReaderAccount=null;
		
		ResultSet query1 = null;
		ResultSet query2 = null;
		ResultSet query3=null;
		int numOfDelay;

		Copy tempCopy=(Copy)msg.getObjectList().get(0);
		try
		{

			checkCopy= (PreparedStatement) connToSQL.prepareStatement("SELECT * FROM copy WHERE copyId = ? AND borrowerId IS NOT NULL");
			checkCopy.setInt(1,tempCopy.getCopyID());
			query1 =checkCopy.executeQuery();
			Boolean temp=query1.next();
			if(temp==false)
			{
				answer.setNote("The copyID is wrong or the copyID does not borrowed");
			}
			else
			{
				checkCopy=(PreparedStatement) connToSQL.prepareStatement("SELECT * FROM copy WHERE copyId = ?");
				checkCopy.setInt(1,tempCopy.getCopyID());
				query2=checkCopy.executeQuery();
				query2.next();
				String id=query2.getString(3);
				checkReaderAccountDelays=(PreparedStatement) connToSQL.prepareStatement("SELECT * FROM readeraccount WHERE ID = ?");
				checkReaderAccountDelays.setString(1,id);
				query3=checkReaderAccountDelays.executeQuery();
			
				query3.next();
				numOfDelay=query3.getInt(9);
				String status=query3.getString(8);
				if(numOfDelay<3 && status.equals("Frozen"))
				{
					updateReaderAccount = connToSQL.prepareStatement("UPDATE `readeraccount` SET `status`=? WHERE ID=?");
					updateReaderAccount.setString(1,"Active");
					updateReaderAccount.setString(2,id);
					updateReaderAccount.executeUpdate();
				}
				updateCopy = connToSQL.prepareStatement("UPDATE `copy` SET `borrowerId`=NULL WHERE copyId=?");
				updateCopy.setInt(1,tempCopy.getCopyID());
				updateCopy.executeUpdate();
				answer.setNote("successful ReturnCopy");
				answer.addObject(msg.getObjectList().get(0));
			}

		}
		catch (SQLException e) 
		{
			e.printStackTrace();
			return new ObjectMessage("Unexpected Error.","Unsucessfull");		
		}	 
		return answer;
	}
	
	
	public static ObjectMessage checkIfAllBorrowed(ObjectMessage msg, Connection connToSQL)
	{
		PreparedStatement ps;
		ObjectMessage answer;
		Copy askedBook=(Copy)msg.getObjectList().get(0);
		try 
		{
			ps = connToSQL.prepareStatement("SELECT COUNT(*) FROM obl.copy WHERE bookId=? AND borrowerId IS NULL");
			ps.setInt(1,askedBook.getBookID());
			ResultSet rs = ps.executeQuery();
			rs.next();
			int x =rs.getInt(1);
			if(rs.getInt(1)!= 0)
			{
				return new ObjectMessage("checkIfAllBorrowed","FoundBook");
			}
			else
			{
				return new ObjectMessage("checkIfAllBorrowed","NoFoundBook");
			}
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * This function will return all the borrows of specific reader account
	 * also it will provide whether the reader account can ask for delay for each one of his borrows
	 * @param msg - the object from the client
	 * @param connToSQL - the connection to the MySQL created in the Class OBLServer
	 * @return ObjectMessage with the answer to the client
	 */
	public static ObjectMessage getBorrows(ObjectMessage msg, Connection connToSQL)
	{
		ObjectMessage answer = null; 
		ReaderAccount reader=(ReaderAccount) msg.getObjectList().get(0);
		boolean resultExist = false;
		boolean canDelay = false;
		
		PreparedStatement getCopies = null; 
		PreparedStatement getBook = null;
		PreparedStatement getReaderAccount = null;
		PreparedStatement getReservs = null;
		ResultSet rs1 = null; 
		ResultSet rs2 = null; 
		ResultSet rs3 = null;
		ResultSet rs4 = null; 

		try 
		{
			//get the copies that the reader account borrowed 
			getCopies = connToSQL.prepareStatement("SELECT * FROM Copy WHERE borrowerId = ? ");
			getCopies.setString(1, reader.getId() ); 
			rs1 =getCopies.executeQuery();
			
			ArrayList <IEntity[]> result=new ArrayList<IEntity[]>(); 
			
			//go by all the copies the reader account borrowing and get the book of each one
			while(rs1.next())
			{
				resultExist = true;
				
				IEntity []CopyAndBook = new IEntity[2]; //CopyAndBook[0]->the Copy info , CopyAndBook[1]->the Book info
				
				//set the copy info from the first query
				CopyAndBook[0]=(new Copy(rs1.getInt(1), rs1.getInt(2), rs1.getString(3), rs1.getString(4), rs1.getString(5)));
				
				int bookId = rs1.getInt(2); //the bookID of the current copy
				
				//get the book of that copy
				getBook = connToSQL.prepareStatement("SELECT * FROM Book WHERE bookId = ? ");
				getBook.setInt(1, bookId ); 
				rs2 =getBook.executeQuery();
				if(rs2.next())
				{
					//set the book info from the second query
					CopyAndBook[1]=( new Book(rs2.getString(2), rs2.getString(3), rs2.getString(4), rs2.getString(5), rs2.getString(6), rs2.getInt(7)) );
				}

				
				//////////////////////////////////////////////////////////
				//check if the reader account can ask for delay this book
				//////////////////////////////////////////////////////////
				
				//check if the book is desired
				if(rs2.getBoolean(8))
				{
					canDelay = false;
				}
				else
				{
					
					//check if it reserved by someone
					getReservs = connToSQL.prepareStatement("SELECT * FROM Reservations WHERE bookId = ? ");
					getReservs.setInt(1, rs1.getInt(2) ); 
					rs4 =getReservs.executeQuery();
					
					if(rs4.next())
					{
						canDelay = false;
					}
					else
					{
						//check if he is not already in delay
						Date dateOfReturn =  rs1.getDate(5);
						
						//DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");  
						LocalDateTime now = LocalDateTime.now();
						
						Instant instant = now.toInstant(ZoneOffset.UTC);
					    Date today = Date.from(instant);
							
						if( today.after(dateOfReturn) )
						{
							canDelay = false;
						}
						else
						{
							//check if reader account is active
							getReaderAccount = connToSQL.prepareStatement("SELECT * FROM ReaderAccount WHERE ID = ? ");
							getReaderAccount.setString(1, reader.getId() ); 
							rs3 =getReaderAccount.executeQuery();
							
							if(rs3.next())
							{
								//if the reader account is active
								if(!rs3.getString(8).equals("Active"))
								{
									canDelay = false;	
								}
								else
								{
									canDelay = true;
								}
							}
						}	
					}
	
				}
				
				if(canDelay)
				{
					((Copy)(CopyAndBook[0])).setCanDelay(true);
					//answer.setExtra("canDelay");
				}
				else
				{
					((Copy)(CopyAndBook[0])).setCanDelay(false);
					//answer.setExtra("canNotDelay");
				}
				
				result.add(CopyAndBook);
			}
			
			if(resultExist)
			{
				answer = new ObjectMessage(result,"TheBorrows");
			}
			else
			{
				answer = new ObjectMessage(result,"NoBorrows");
			}
			
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		

		return answer;
	}
	
	
	private static ObjectMessage deleteBook(ObjectMessage msg, Connection connToSQL)
	{
		PreparedStatement ps;
		PreparedStatement getBook = null;
		PreparedStatement getNumOfCopies = null;
		ObjectMessage answer;
		ResultSet rs2 = null; 
		ResultSet rs3 = null;
		Copy askedCopy=(Copy)msg.getObjectList().get(0);
		int countNumOfCopies;
		
		
		try 
		{
			
			//get id of the book of the copy
			getBook = connToSQL.prepareStatement("SELECT * FROM obl.copy WHERE copyId = ? ");
			getBook.setInt(1, askedCopy.getCopyID()); 
			rs2 =getBook.executeQuery();
			if(!rs2.next())
			{
				answer= new ObjectMessage("The copy is not exist in obl,you can not delete it","Unsucessfull");
			}
			
			else{
			int bookOfCopyID=rs2.getInt(2);
			
			//get number of copies of this bookID
			getNumOfCopies = connToSQL.prepareStatement("SELECT COUNT(*) FROM copy WHERE bookID=? ");
			getNumOfCopies.setInt(1, bookOfCopyID); 
			rs3 =getNumOfCopies.executeQuery();
			rs3.next();
			countNumOfCopies=rs3.getInt(1);
			
			//delete the copy
			ps = connToSQL.prepareStatement("DELETE copy FROM obl.copy WHERE copyId=?");
			ps.setInt(1,askedCopy.getCopyID());
			ps.executeUpdate();
			countNumOfCopies--;
		
			
			//there is no copies from the book
			if(countNumOfCopies==0)
			{
				try 
				{
					ps = connToSQL.prepareStatement("DELETE book FROM obl.book WHERE bookId=?");
					ps.setInt(1,bookOfCopyID);
					ps.executeUpdate();
				}
				catch (SQLException e) 
				{
					e.printStackTrace();
					answer= new ObjectMessage("Unexpected Error.","Unsucessfull");
				}
			}
			answer=new ObjectMessage("This Book was successfully deleted ","Successfull");
			}
			
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
			answer=new ObjectMessage("Unexpected Error.","Unsucessfull");
		}
		
		return answer;
	}

	public static String checkIfBookIsAvailable(ObjectMessage msg, Connection connToSQL, String readerId) 
	{
		ReaderAccount reader=(ReaderAccount)msg.getObjectList().get(0);
		Copy copy=(Copy)msg.getObjectList().get(1);
		String borrowDate,returnDateDesire,returnDateNotDesire;
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");  
		LocalDateTime now = LocalDateTime.now();  
		LocalDateTime desireDate = LocalDateTime.now().plusDays(3);  
		LocalDateTime notDesireDate = LocalDateTime.now().plusDays(7); 
		borrowDate= dtf.format(now);
		returnDateNotDesire=dtf.format(now);
		returnDateDesire=dtf.format(desireDate);
		returnDateNotDesire=dtf.format(notDesireDate);
		
		try 
		{
			//get the copies that the reader account want to borrow
			PreparedStatement getCopy = connToSQL.prepareStatement("SELECT * FROM Copy WHERE copyId = ? ");
			System.out.println();
			getCopy.setInt(1, copy.getCopyID()); 
			ResultSet rs1 = getCopy.executeQuery();
			
			if(rs1.next())//if there is copy with this id exist in DB
			{
				if(rs1.getString(3)==null)
				{  
					//get book id
					PreparedStatement getBook = connToSQL.prepareStatement("SELECT * FROM Book WHERE bookId = ? ");
					getBook.setInt(1, rs1.getInt(2));
					ResultSet rs2 = getBook.executeQuery();
					
					//set borrower id to the table copies
					PreparedStatement setBorroweID =connToSQL.prepareStatement("UPDATE `obl`.`copy` SET `borrowerId`=?  WHERE `copyId` = ?");	
					setBorroweID.setString(1, readerId);
					setBorroweID.setInt(2,copy.getCopyID()); 
					
					setBorroweID.executeUpdate();
					
////////////check if desire or not
					if(!rs2.getBoolean(6))
					{
						System.out.println("Here1");
						PreparedStatement setReturnDay =connToSQL.prepareStatement("UPDATE `obl`.`copy` SET `borrowDate` = ? AND `returnDate` = ? WHERE `copyId` = ?");
						System.out.println("Here3");
						setReturnDay.setString(1, borrowDate);
						System.out.println("Here4");
						setReturnDay.setString(2,returnDateNotDesire);
						System.out.println("Here5");
						setReturnDay.setInt(3, copy.getCopyID());
						System.out.println("Here6");
						setReturnDay.executeUpdate();
						return "NotDesired";// Success borrowed not desired book
					}
					else
					{
						System.out.println("Here2");
						PreparedStatement setReturnDay =connToSQL.prepareStatement("UPDATE `obl`.`copy` SET `borrowDate` = ? AND `returnDate` = ? WHERE `copyId` = ?");
						setReturnDay.setString(1, borrowDate);
						setReturnDay.setString(2, returnDateDesire);
						setReturnDay.setInt(3, copy.getCopyID());
						setReturnDay.executeUpdate();
						
						return "Desired";// Success borrowed desired book
					}
				
					
				}
				else //this copy was already borrowed
				{
				 return "CopyAlreadyBorrowed";
				}
			}
			
			
		}
		catch (SQLException e) 
		{
			e.printStackTrace();
			new ObjectMessage("Unexpected Error.","Unsucessfull");
		}
		return "CopyNotExist";
	}

}
