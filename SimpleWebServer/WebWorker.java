//Changes made by Benjamin Nutter
/**
* Web worker: an object of this class executes in its own new thread
* to receive and respond to a single HTTP request. After the constructor
* the object executes on its "run" method, and leaves when it is done.
*
* One WebWorker object is only responsible for one client connection. 
* This code uses Java threads to parallelize the handling of clients:
* each WebWorker runs in its own thread. This means that you can essentially
* just think about what is happening on one client at a time, ignoring 
* the fact that the entirety of the webserver execution might be handling
* other clients, too. 
*
* This WebWorker class (i.e., an object of this class) is where all the
* client interaction is done. The "run()" method is the beginning -- think
* of it as the "main()" for a client interaction. It does three things in
* a row, invoking three methods in this class: it reads the incoming HTTP
* request; it writes out an HTTP header to begin its response, and then it
* writes out some HTML content for the response content. HTTP requests and
* responses are just lines of text (in a very particular format). 
*
**/

import java.net.Socket;
import java.lang.Runnable;
import java.io.*;
import java.util.Date;
import java.text.DateFormat;
import java.util.TimeZone;

public class WebWorker implements Runnable
	{
	private Socket socket;
	
	/**
	* Constructor: must have a valid open socket
	**/
	public WebWorker(Socket s)
	{
	   socket = s;
	}
	/**
	* Worker thread starting point. Each worker handles just one HTTP 
	* request and then returns, which destroys the thread. This method
	* assumes that whoever created the worker created it with a valid
	* open socket object.
	**/
	public void run()
	{
	   System.err.println("Handling connection...");
	   try {
	      InputStream  is = socket.getInputStream();
	      OutputStream os = socket.getOutputStream();
	      String pathName = readHTTPRequest(is);
	      writeHTTPHeader(os,"text/html", pathName);
	      writeContent(os, pathName);
	      os.flush();
	      socket.close();
	   } catch (Exception e) {
	      System.err.println("Output error: "+e);
	   }
	   System.err.println("Done handling connection.");
	   return;
	}
	/**
	* Read the HTTP request header.
	**/
	// returns the filepath requested from the user
	private String readHTTPRequest(InputStream is)
	{
	   String line;
	   String pathName = "";
	
	   BufferedReader r = new BufferedReader(new InputStreamReader(is));
	   while (true) {
	      try {
	         while (!r.ready()) Thread.sleep(1);
	         line = r.readLine();
	         System.err.println("Request line: ("+line+")");
	         // get name of file path
	         String[] words = line.split(" ");
	         for (String word : words) {
	            if (word.startsWith("/")) {
	               pathName = word;
	            }
	         }
	         if (line.length()==0) break;
	      } catch (Exception e) {
	         System.err.println("Request error: "+e);
	         break;
	      }
	   }
	   pathName = "." + pathName;
	   // if pathName is blank
	   if (pathName.equals("./"))
	      pathName = pathName + "home.html";
	   // if the file at filepath doesn't exist
	   try {
	      BufferedReader br = new BufferedReader(new FileReader(pathName));
	   }
	   catch (Exception e) {
	      return "";
	   }
	   return pathName;
	}
	/**
	* Write the HTTP header lines to the client network connection.
	* @param os is the OutputStream object to write to
	* @param contentType is the string MIME content type (e.g. "text/html")
	**/
	private void writeHTTPHeader(OutputStream os, String contentType, String pathName) throws Exception
	{
	   Date d = new Date();
	   DateFormat df = DateFormat.getDateTimeInstance();
	   df.setTimeZone(TimeZone.getTimeZone("GMT"));
	   if (pathName == "") {
	      os.write("HTTP/1.1 404 Not Found\n".getBytes());
	   }
	   else {
	      os.write("HTTP/1.1 200 OK\n".getBytes());
	   }
	   os.write("Date: ".getBytes());
	   os.write((df.format(d)).getBytes());
	   os.write("\n".getBytes());
	   os.write("Server: Jon's very own server\n".getBytes());
	   //os.write("Last-Modified: Wed, 08 Jan 2003 23:11:55 GMT\n".getBytes());
	   //os.write("Content-Length: 438\n".getBytes()); 
	   os.write("Connection: close\n".getBytes());
	   os.write("Content-Type: ".getBytes());
	   os.write(contentType.getBytes());
	   os.write("\n\n".getBytes()); // HTTP header ends with 2 newlines
	   return;
	}
	/**
	* Write the data content to the client network connection. This MUST
	* be done after the HTTP header has been written out.
	* @param os is the OutputStream object to write to
	**/
	private void writeContent(OutputStream os, String pathName) throws Exception
	{
	   String sname = "Ben's Server";
	   // if file at pathName wasn't found
	   if (pathName == "") {
	      pathName = "./404.html";
	   }
	   // reading the html file at pathName
	   BufferedReader br = new BufferedReader(new FileReader(pathName));
	   String line;
	   while (true) {
	      line = br.readLine();
	      if (line == null)
	         break;
	      // custom html tag processing
	      if (line.startsWith("<cs371date>")) {
	         Date d = new Date();
	         DateFormat df = DateFormat.getDateTimeInstance();
	         df.setTimeZone(TimeZone.getTimeZone("GMT"));
	         os.write("Date: ".getBytes());
	         os.write((df.format(d)).getBytes());
	         os.write("\n".getBytes());
	      }
	      else if (line.startsWith("<cs371server>")) {
	         os.write(("Server: " + sname + "\n").getBytes());
	      }
	      else {
	         os.write((line + "\n").getBytes());
	    }
	  }
	}
} // end class
