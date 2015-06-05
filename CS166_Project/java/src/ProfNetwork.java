/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class ProfNetwork {

   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   /**
    * Creates a new instance of ProfNetwork
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public ProfNetwork (String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end ProfNetwork

   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate (String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the update instruction
      stmt.executeUpdate (sql);

      // close the instruction
      stmt.close ();
   }//end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()){
	 if(outputHeader){
	    for(int i = 1; i <= numCol; i++){
		System.out.print(rsmd.getColumnName(i) + "\t");
	    }
	    System.out.println();
	    outputHeader = false;
	 }
         for (int i=1; i<=numCol; ++i)
            System.out.print (rs.getString (i) + "\t");
         System.out.println ();
         ++rowCount;
      }//end while
      stmt.close ();
      return rowCount;
   }//end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and saves the data returned by the query.
      boolean outputHeader = false;
      List<List<String>> result  = new ArrayList<List<String>>();
      while (rs.next()){
          List<String> record = new ArrayList<String>();
         for (int i=1; i<=numCol; ++i)
            record.add(rs.getString (i));
         result.add(record);
      }//end while
      stmt.close ();
      return result;
   }//end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery (String query) throws SQLException {
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count nuber of results.
       if(rs.next()){
          rowCount++;
       }//end while
       stmt.close ();
       return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
	Statement stmt = this._connection.createStatement ();

	ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
	if (rs.next())
		return rs.getInt(1);
	return -1;
   }

   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup(){
      try{
         if (this._connection != null){
            this._connection.close ();
         }//end if
      }catch (SQLException e){
         // ignored.
      }//end try
   }//end cleanup

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
   public static void main (String[] args) {
      if (args.length != 3) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            ProfNetwork.class.getName () +
            " <dbname> <port> <user>");
         return;
      }//end if

      Greeting();
      ProfNetwork esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the ProfNetwork object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new ProfNetwork (dbname, dbport, user, "");

         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            String authorisedUser = null;
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
            if (authorisedUser != null) {
              boolean usermenu = true;
              while(usermenu) {
				ClearScreen();
                System.out.println("MAIN MENU");
                System.out.println("---------");
                System.out.println("1. Goto Friend List");
                System.out.println("2. Update Profile");
                System.out.println("3. Write a new message");
                System.out.println("4. Send Friend Request");
				System.out.println("5. Display Profile");
                System.out.println(".........................");
                System.out.println("9. Log out");
                switch (readChoice()){
                   case 1: 
				   	//FriendList(esql); 
				   	break;
                   case 2: 
				   	UpdateProfile(esql,authorisedUser); 
				   	break;
                   case 3: 
				   	//NewMessage(esql); 
				   	break;
                   case 4: 
				   	//SendRequest(esql); 
				   	break;
				   case 5:
				    DisplayProfile(esql, authorisedUser);
					break;
                   case 9: 
				   	usermenu = false; 
				   	break;
                   default : 
				   	System.out.println("Unrecognized choice!"); 
					break;
                }
              }
            }
         }//end while
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }finally{
         // make sure to cleanup the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Done\n\nBye !");
            }//end if
         }catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }//end main

   public static void Greeting(){
      System.out.println(
         "\n\n*******************************************************\n" +
         "              User Interface      	               \n" +
         "*******************************************************\n");
   }//end Greeting

   /*
    * Reads the users choice given from the keyboard
    * @int
    **/
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("Please make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         }catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         }//end try
      }while (true);
      return input;
   }//end readChoice

   /*
    * Creates a new user with privided login, passowrd and phoneNum
    * An empty block and contact list would be generated and associated with a user
    **/
   public static void CreateUser(ProfNetwork esql){
      try{
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         String password = in.readLine();
         System.out.print("\tEnter user email: ");
         String email = in.readLine();

	 //Creating empty contact\block lists for a user
	 String query = String.format("INSERT INTO USR (userId, password, email) VALUES ('%s','%s','%s')", login, password, email);

         esql.executeUpdate(query);
         System.out.println ("User successfully created!");
      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }//end
   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
   public static String LogIn(ProfNetwork esql){
      try{
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         String password = in.readLine();

         String query = String.format("SELECT * FROM USR WHERE userId = '%s' AND password = '%s'", login, password);
         int userNum = esql.executeQuery(query);
	 if (userNum > 0)
		return login;
         return null;
      }catch(Exception e){
         System.err.println (e.getMessage ());
         return null;
      }
   }//end

   /* 
   * Clears the screen 
   * */
   public static void ClearScreen(){
	   try{
		   final String ANSI_CLS = "\u001b[2J";
		   final String ANSI_HOME = "\u001b[H";
		   System.out.println(ANSI_CLS + ANSI_HOME);
		   System.out.flush();
	   }catch(Exception e){
		   System.err.println(e.getMessage() );
	   }
   }//end
   
   /*
   * Allows user to update their profile, change their password etc
   *
   * */
   public static void UpdateProfile(ProfNetwork esql, String authorisedUser){
	   boolean update=true;
	   while(update) {
		   ClearScreen();
		   System.out.println("UPDATE PROFILE MENU");
           System.out.println("---------");
           System.out.println("1. Change password");
           System.out.println("2. Change email");
           System.out.println("3. Change full name");
           System.out.println("4. Add work experience");
		   System.out.println("5. Add education details");
           System.out.println(".........................");
           System.out.println("9. End update");
           switch (readChoice()){
			   case 1: 
			    ChangePassword(esql,authorisedUser);
			   	break;
               case 2: 
			    ChangeEmail(esql,authorisedUser);
				break;
               case 3: 
			    ChangeName(esql,authorisedUser);
				break;
               case 4:
			    AddWork(esql,authorisedUser); 
				break;
			   case 5:
			    AddEducation(esql, authorisedUser);
			    break;
               case 9: 
				update = false; 
				break; default : System.out.println("Unrecognized choice!"); 
				break;
           }
	   }
   }//end

   public static void ChangePassword(ProfNetwork esql, String authorisedUser){
	   try{
	   	System.out.println("Please input your new password:");
       	String pw = in.readLine();
	   	String query = String.format("UPDATE USR SET password = '%s' WHERE userId='%s'", pw, authorisedUser); 
	   	esql.executeUpdate(query);
	   	System.out.println("Password updated!");
	   }catch (Exception e){
			System.err.println (e.getMessage ());
         	return;
	   }

   }//end

   public static void ChangeEmail(ProfNetwork esql, String authorisedUser){
	   try{
	   	System.out.println("Please input your new email:");
       	String email = in.readLine();
	   	String query = String.format("UPDATE USR SET email = '%s' WHERE userId='%s'", email, authorisedUser); 
	   	esql.executeUpdate(query);
	   	System.out.println("Email updated!");
	   }catch (Exception e){
			System.err.println (e.getMessage ());
         	return;
	   }
   }//end

   public static void ChangeName(ProfNetwork esql, String authorisedUser){
	   try{
	   	System.out.println("Please input your new name:");
       	String name = in.readLine();
	   	String query = String.format("UPDATE USR SET name = '%s' WHERE userId='%s'", name, authorisedUser); 
	   	esql.executeUpdate(query);
	   	System.out.println("Name updated!");
	   }catch (Exception e){
			System.err.println (e.getMessage ());
         	return;
	   }
   }//end

   public static void AddWork(ProfNetwork esql, String authorisedUser){
	   try{
	     System.out.println("Please enter the company: ");
		 String comp = in.readLine();
		 System.out.println("Please enter your role at that company: ");
		 String rol = in.readLine();
		 System.out.println("Please enter the location of the company: ");
		 String loc = in.readLine();
		 System.out.println("Please enter the startdate in the form of YYYY/MM/DD: ");
		 String start = in.readLine();
		 System.out.println("Please enter the end date in the form of YYYY/MM/DD: ");
		 String end = in.readLine();
		
		 String query = String.format("INSERT INTO WORK_EXPR (userId, company, role, location, startDate, endDate) VALUES ('%s','%s','%s', '%s', '%s', '%s')", authorisedUser, comp, rol, loc, start, end);
         esql.executeUpdate(query);
         System.out.println ("Work experience added!!");

	   }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }//end

   public static void AddEducation(ProfNetwork esql, String authorisedUser){
	   try{
	     System.out.println("Please enter the institution name: ");
		 String inst = in.readLine();
		 System.out.println("Please enter your major: ");
		 String maj = in.readLine();
		 System.out.println("Please enter your degree: ");
		 String deg = in.readLine();
		 System.out.println("Please enter the startdate in the form of YYYY/MM/DD: ");
		 String start = in.readLine();
		 System.out.println("Please enter the end date in the form of YYYY/MM/DD: ");
		 String end = in.readLine();
		
		 String query = String.format("INSERT INTO EDUCATION_DETAILS (userId, institutionName, major, degree, startDate, endDate) VALUES ('%s','%s','%s', '%s', '%s', '%s')", authorisedUser, inst, maj, deg, start, end);
         esql.executeUpdate(query);
         System.out.println ("Education experience added!!");

	   }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }//end

   public static void DisplayUser(ProfNetwork esql, String authorisedUser){
	   //get all info from USR table for authorised user
	   //get all info from work_expr table for authorisedUser
	   //get all info from education_details table for authorisedUser
	   //Connections???	

   }


}//end ProfNetwork
