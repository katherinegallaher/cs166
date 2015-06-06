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


   static private int messagenum = 27812;

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
				//ClearScreen();
                System.out.println("MAIN MENU");
                System.out.println("---------");
                System.out.println("1. Goto Friend List");
                System.out.println("2. Update Profile");
                System.out.println("3. Write a new message");
                System.out.println("4. Send Friend Request");
				System.out.println("5. Display Profile");
				System.out.println("6. Search for People");
				System.out.println("7. View/Edit Messages");
                System.out.println(".........................");
                System.out.println("9. Log out");
                switch (readChoice()){
                   case 1: 
				   	FriendList(esql,authorisedUser); 
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
				   case 6:
				    SearchPeople(esql, authorisedUser);
					break;
				   case 7:
				    Messages(esql, authorisedUser);
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
   *
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
		   //ClearScreen();
		   System.out.println("UPDATE PROFILE MENU");
           System.out.println("---------");
           System.out.println("1. Change password");
           System.out.println("2. Change email");
           System.out.println("3. Change full name");
           System.out.println("4. Add work experience");
		   System.out.println("5. Add education details");
           System.out.println(".........................");
           System.out.println("6. End update");
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
               case 6: 
				update = false; 
				break; default : System.out.println("Unrecognized choice!"); 
				break;
           }//end switch
	   }
   }//end

   /*
   * Allows the user to change their login password.
   *
   * */
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

   /*
   * Allows the user to change their login email.
   *
   * */
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

   /*
   * Allows the user to change the name associated with their profile.
   *
   * */
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

   /*
   * Allows the user to add additional work experience.
   *
   * */
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

   /*
   * Allows the user to add additional Education experience.
   *
   * */
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
		
		 String query = String.format("INSERT INTO EDUCATIONAL_DETAILS (userId, instituitionName, major, degree, startDate, endDate) VALUES ('%s','%s','%s', '%s', '%s', '%s')", authorisedUser, inst, maj, deg, start, end);
         esql.executeUpdate(query);
         System.out.println ("Education experience added!!");

	   }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }//end

   /*
   * Allows the user to search for people on the network. 
   *
   * */
   public static void SearchPeople(ProfNetwork esql, String authorisedUser){
       try{
		   System.out.println("Please enter the name of the person you would like to search for: ");
		   String search = in.readLine();

		   if(authorisedUser.equals(search) ){
			   System.out.println("You cannot search for yourself!");
		   }

		   else{
	           String query = String.format("SELECT * FROM USR WHERE name='" +search + "'");
			   int exists = esql.executeQuery(query);
			   if(exists >0){
				   System.out.println("The user exists....add more later");
	   //do connection/message option <------future functions
			   }
			   else{
				   System.out.println("The user does not exist!");
			   }
		   }
	   }catch(Exception e){
		   System.err.println(e.getMessage());
	   }
   }//end

   /* 
   * View friends list with option to go to their profile
   *
   * */
   public static void FriendList(ProfNetwork esql, String authorisedUser){
	   try{
		   List<String> FriendsList = new ArrayList<String>();
		   FriendsList = getFriendsList(esql, authorisedUser);

		   boolean viewfriends = true;
		   while(viewfriends){
			   System.out.println("\nYour Friends List: ");
		       int i=0;
		       for(; i<FriendsList.size(); i++){
				   System.out.println(( i+1) + ". " + FriendsList.get(i));
		       }
		       System.out.println( (i+1) + ". Go back");
		       System.out.println("\n");
			   
			   int choice = readChoice();
			   if(choice == (i+1)){
				   viewfriends = false;
			   }
			   else{//they have chosen to view a profile, display it
				   DisplayProfile(esql,FriendsList.get(choice-1));
				   OptionMenu(esql,authorisedUser, FriendsList.get(choice-1));
			   }
		   }//end while
	   }catch(Exception e){
		   System.err.println(e.getMessage() );
	   }
   }//end

   /*
   * Option menu when you are on someone's profile
   *
   * */
   public static void OptionMenu(ProfNetwork esql, String authorisedUser, String friendUser){
	   try{
		   //switch statement, 1. send connection 2. send message?
		   boolean optionmenu = true;
		   while(optionmenu){
			   System.out.println("1. Send Connection");
			   System.out.println("2. Send Message");
			   System.out.println("3. Go Back");

			   switch(readChoice() ){
				   case 1:
				    //SendConnection();
				    break;
				   case 2:
				    SendMessage(esql, authorisedUser, friendUser);
				    break;
				   case 3:
				    optionmenu=false;
				    break;
				   default : System.out.println("Unrecognized choice!"); 
				    break;
			   }
		   }
	   }catch(Exception e){
		   System.err.println(e.getMessage() );
	   }
   }//end

   /*
   * 
   *
   * */
   public static void SendMessage(ProfNetwork esql, String authorisedUser, String friendUser){
	   try{
		   System.out.println("\n");
		   System.out.println("Please enter your message contents: ");
		   String msgcontent = in.readLine();
		   if(msgcontent.length() > 500){
			   System.out.println("Message is too long.");
		   }
		   else{
			   String query = String.format("INSERT INTO MESSAGE (msgId, senderId, receiverId, contents, sendTime,deleteStatus, status) VALUES ('%d', '%s', '%s', '%s', '%s', '%d', '%s')", messagenum, authorisedUser, friendUser, msgcontent, "1992-06-02 10:30:12 -0700", 0, "Delivered" );
			   messagenum++;
			   esql.executeUpdate(query);
			   System.out.println("Message Sent!");
		   }
	   }catch(Exception e){
		   System.err.println(e.getMessage() );
	   }

   }//end

   /*
   * Returns the friends list for authorisedUser
   *
   * */
   public static List<String> getFriendsList(ProfNetwork esql, String authorisedUser){
	   try{
		   List<String> FriendsList = new ArrayList<String>();
	       String query = String.format("SELECT connectionId FROM CONNECTION_USR WHERE userId='" + authorisedUser +"' AND status = 'Accept'");
	       List<List<String> > friends = new ArrayList<List<String> >();
	       friends = esql.executeQueryAndReturnResult(query);
	       for(int i=0; i<friends.size();i++){
		      FriendsList.add(friends.get(i).get(0));
	       }
		   
	       query = String.format("SELECT userId FROM CONNECTION_USR WHERE connectionId='" + authorisedUser +"' AND status = 'Accept'");
	       friends.clear();
	       friends = esql.executeQueryAndReturnResult(query);
	       for(int i=0; i<friends.size();i++){
		       FriendsList.add(friends.get(i).get(0));
	       }
	       return FriendsList;
	   }catch(Exception e){
		   System.err.println(e.getMessage());
		   return null;
	   }
   }

   /*
   * Function that allows the user to view and delete their messages
   *
   * Messages delete key: neither sender nor receiver has deleted the message: 0
   *                      sender has deleted the message but not the receiver: 1
   *					  receiver has deleted the message but not the sender: 2
   *					  both the sender and the receiver have deleted the msg: 3
   * */
   public static void Messages(ProfNetwork esql, String authorisedUser){
	   try{
		   boolean messageMenu = true;
		   while(messageMenu){
			   System.out.println("\nMessage Menu: \n");
			   
			   System.out.println("1. View/read messages");
			   System.out.println("2. Delete messages");
			   System.out.println("3. Go back");
			   
			   switch (readChoice()){
				   case 1:
				    ShowAllMessages(esql, authorisedUser);
				    break;
				   case 2:
				    DeleteMessages(esql, authorisedUser);
				    break;
				   case 3:
				    messageMenu = false; 
				    break;
				   default : System.out.println("Unrecognized choice!"); 
				    break;
               }//end switch
		   }//end while
	   }catch(Exception e){
		   System.err.println(e.getMessage() );
	   }
   }//end


   /* 
   * Displays all received messages in a menu where you have the option to view the contents
   *
   * */
   public static void ShowAllMessages(ProfNetwork esql, String authorisedUser){
	   try{
		   boolean viewmessages = true;
     	   String query = String.format("SELECT msgId, senderId, sendTime, status FROM MESSAGE WHERE receiverId='" +authorisedUser + "' AND (deleteStatus <> 2 AND deleteStatus <>3) AND (status <> 'Failed to Deliver' AND status <> 'Draft')");
	       List<List<String> > allMessages = new ArrayList<List<String> >();

		   while(viewmessages){
	           allMessages = esql.executeQueryAndReturnResult(query);
			   int i=0;
		       for(; i<allMessages.size(); i++){

				   System.out.print(i+1 + ". " + allMessages.get(i).get(1) + " " + allMessages.get(i).get(2) + " ");
			       if(allMessages.get(i).get(3).equals("Delivered"))
					   System.out.print("Unread");
			       else
					   System.out.print("Read");
				   System.out.print("\n");
		   	   }
			   System.out.println( (i+1) + ". Go back");
		       System.out.println("\n");
			   
			   int choice = readChoice();
			   if(choice == (i+1)){
				   viewmessages = false;
			   }
			   else{//they have chosen to view a message, display it
				   DisplayMessage(esql,allMessages.get(choice-1).get(0));
			   }
		   }
	   }catch(Exception e){
		   System.err.println(e.getMessage() );
		   System.err.println("error in showallmessages");
	   }
   }//end

   /*
   * Allows the user to delete a message 
   *
   * */
   public static void DeleteMessages(ProfNetwork esql, String authorisedUser){
	   try{
		   boolean deletemessages = true;
		   String query = String.format("SELECT msgId, senderId, sendTime, status, deleteStatus FROM MESSAGE WHERE receiverId='" +authorisedUser + "' AND (deleteStatus <> 2 AND deleteStatus <>3) AND (status <> 'Failed to Deliver' AND status <> 'Draft')");
	       List<List<String> > delMessages = new ArrayList<List<String> >();

           System.out.print("\n");
		   System.out.println("DELETE MESSAGE MENU:");
		   while(deletemessages){
	           delMessages = esql.executeQueryAndReturnResult(query);
			   int i=0;
		       for(; i<delMessages.size(); i++){
				   System.out.print(i+1 + ". " + delMessages.get(i).get(0) + " " + delMessages.get(i).get(1) + " " + delMessages.get(i).get(2) + " ");
				   
			       if(delMessages.get(i).get(3).equals("Delivered"))
					   System.out.print("Unread");
			       else
					   System.out.print("Read");
				   System.out.print("\n");
		   	   }
			   System.out.println( (i+1) + ". Go back");
		       System.out.println("\n");

			   int choice = readChoice();
			   if(choice == (i+1)){
				   deletemessages = false;
			   }
			   else{//delete the message
				   if(delMessages.get(choice-1).get(4).equals("1") ){
					   String nquery = String.format("UPDATE MESSAGE SET deleteStatus = '3'");
					   esql.executeUpdate(nquery);
				   }
				   else{
					   String nquery = String.format("UPDATE MESSAGE SET deleteStatus = '2'");
					   esql.executeUpdate(nquery);
				   }
			   }//end delete message

		  }//end while
	   }catch(Exception e){
		   System.err.println(e.getMessage() );
	   }
   }//end

   /* 
   * Displays the contents of a message 
   *
   * */
   public static void DisplayMessage(ProfNetwork esql, String msId){
	   try{
		   while(true){
			   String query = String.format("SELECT senderId, sendTime, contents FROM MESSAGE WHERE msgId='" +msId +"'");
	       	   List<List<String> > display = new ArrayList<List<String> >();
	           display = esql.executeQueryAndReturnResult(query);

		       System.out.println(display.get(0).get(0) + " " + display.get(0).get(1));
		       System.out.println(display.get(0).get(2) );
		       System.out.print("\n");
		   
		       query = String.format("UPDATE MESSAGE SET status = 'Read' WHERE msgId = '" + msId + "'");
		       esql.executeUpdate(query);

		       System.out.println("1. Return");
		       if(readChoice() == 1){
			      return;
		       }
			  
		   }
	   }catch(Exception e){
		   System.err.println(e.getMessage() );
		   System.err.println("displaymessage error");
	   }
   }//end

   /*
   * Displays the profile of the user.
   * Displays user info, work and education experience. 
   * */
   public static void DisplayProfile(ProfNetwork esql, String authorisedUser){

	   String query = String.format("SELECT email, name, dateOfBirth FROM USR WHERE userId='" +authorisedUser + "'");
	   List<List<String> > usrInfo = new ArrayList<List<String> >();
	   try{
		   System.out.println("\n");
		   usrInfo = esql.executeQueryAndReturnResult(query);
	   	   System.out.println("Name: " + usrInfo.get(0).get(1) + "");
	   	   System.out.println("Email: " + usrInfo.get(0).get(0) + "");
	       System.out.println("Date of Birth: " + usrInfo.get(0).get(2) + "");
	       System.out.println("\n");
	   }catch(Exception e){
		   System.err.println(e.getMessage());
	   }

	   query = String.format("SELECT company, role, location, startdate, enddate FROM WORK_EXPR WHERE userId='" + authorisedUser + "'");
	   List<List<String> > workInfo = new ArrayList<List<String> >();
	   try{
		   workInfo = esql.executeQueryAndReturnResult(query);
	       if(!workInfo.isEmpty()){
			   System.out.println("Work Experience: ");
		       for(int i=0; i<workInfo.size(); i++){
				   System.out.println("Company: " + workInfo.get(i).get(0) + "");
			       System.out.println("Role: " + workInfo.get(i).get(1) + "");
			       System.out.println("Location: " + workInfo.get(i).get(2) +"");
			       System.out.println("Start Date: " + workInfo.get(i).get(3) +"");
			       System.out.println("End Date: " + workInfo.get(i).get(4) +"");
			       System.out.println("\n");
			   }
	      }
	   }catch(Exception e){
		   System.err.println(e.getMessage());
	   }

	   query = String.format("SELECT instituitionName, major, degree, startdate, enddate FROM EDUCATIONAL_DETAILS WHERE userId='" + authorisedUser + "'");
	   List<List<String> > eduInfo = new ArrayList<List<String> >();
	   try{
		   eduInfo = esql.executeQueryAndReturnResult(query);
	       if(!eduInfo.isEmpty()){
			   System.out.println("Education Experience: ");
			   for(int i=0; i<eduInfo.size(); i++){
				   System.out.println("institutionName: " + eduInfo.get(i).get(0) + "");
				   System.out.println("Major: " + eduInfo.get(i).get(1) + "");
				   System.out.println("Degree: " + eduInfo.get(i).get(2) + "");
				   System.out.println("Start Date: " + eduInfo.get(i).get(3) + "");
				   System.out.println("End Date: " + eduInfo.get(i).get(4) + "");
				   System.out.println("\n");
			   }
		   }
	   }catch(Exception e){
		   System.err.println(e.getMessage());
	   }
   }//end


}//end ProfNetwork
