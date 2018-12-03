================================================================================
SharePointClient class
================================================================================

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.Authenticator;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;

import org.apache.xerces.dom.ElementNSImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import com.microsoft.schemas.sharepoint.soap.copy.Copy;
import com.microsoft.schemas.sharepoint.soap.copy.CopyResultCollection;
import com.microsoft.schemas.sharepoint.soap.copy.CopySoap;
import com.microsoft.schemas.sharepoint.soap.copy.DestinationUrlCollection;
import com.microsoft.schemas.sharepoint.soap.copy.FieldInformation;
import com.microsoft.schemas.sharepoint.soap.copy.FieldInformationCollection;
import com.microsoft.schemas.sharepoint.soap.lists.GetListCollection;
import com.microsoft.schemas.sharepoint.soap.lists.GetListCollectionResponse;
import com.microsoft.schemas.sharepoint.soap.lists.GetListItems;
import com.microsoft.schemas.sharepoint.soap.lists.GetListItemsResponse;
import com.microsoft.schemas.sharepoint.soap.lists.Lists;
import com.microsoft.schemas.sharepoint.soap.lists.ListsSoap;
import com.microsoft.schemas.sharepoint.soap.lists.GetListCollectionResponse.GetListCollectionResult;
import com.microsoft.schemas.sharepoint.soap.versions.GetVersionsResponse;
import com.microsoft.schemas.sharepoint.soap.versions.Versions;
import com.microsoft.schemas.sharepoint.soap.versions.VersionsSoap;

public class SharePointClient {
  
	private static String username = "your sharepoint username";
	private static String password = "your sharepoinnt password";
	private static String BasesharepointUrl = "https://mysharepoint.com/Book Names";
	private static ListsSoap listsoapstub;
	private static VersionsSoap versionssoapstub;
	private static CopySoap copysoapstub;
	
	private static SharePointClient getInstance(){
		return(new SharePointClient());
	}
	public static void main(String[] args) {
		try {
			configureProxySettings();
	
			Authenticator.setDefault(new SimpleAuthenticator("", ""));
			
			
			//Authenticating and Opening the SOAP port of the Copy Web Service
			listsoapstub = getSPListSoapStub(username, password, BasesharepointUrl);

			//Authenticating and opening the SOAP port of the Versions Web Service
			versionssoapstub = getSPVersionsStub(username, password,BasesharepointUrl);
							
			//Authenticating and Opening the SOAP port of the Copy Web Service
			copysoapstub = getSPCopySoapStub(username, password, BasesharepointUrl);
			 
			// Displays the lists items in the console
			SharePointClient.displaySharePointList();
			
			//Checks-out a file
			//String checkoutURL = BasesharepointUrl+"test.log";
			//SharePointClient.checkOutFile(listsoapstub,checkoutURL);
			//SharePointClient.undoCheckOutFile(listsoapstub,checkoutURL);
			//SharePointClient.checkOutFile(listsoapstub,checkoutURL);
			
			
			//Download a document with CopySoap Web Service
			//DownloadDocumentVersionsFromSPDocumentLibrary(BasesharepointUrl+"test.log");
			
			// <!-- Do something on downloaded document -->
			//
			// Upload a file - Remember its not needed to checkout first, you can directly upload a new document.
			//
			//SharePointClient.UploadFileUsingCopyWebService("sharepoint-downloads/test.log");
			
			//Checks-In
			//SharePointClient.checkInFile(listsoapstub, checkoutURL, "Test Checkin");
		
		} catch (Exception ex) {
			ex.printStackTrace();
			System.err.println(ex);
		}
	}
	
	private static Logger LOGGER = Logger.getLogger(SPClient.class.getName());

	public static URL convertToURLEscapingIllegalCharacters(String string){
	    try {
	        String decodedURL = URLDecoder.decode(string, "UTF-8");
	        URL url = new URL(decodedURL);
	        URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef()); 
	        return uri.toURL(); 
	    } catch (Exception ex) {
	        ex.printStackTrace();
	        return null;
	    }
	}

	public static void configureProxySettings()
	{
		  System.out.println("Configuring Proxy settings");
	      System.getProperties().put("http.proxyHost",proxyHost);
	      System.getProperties().put("http.proxyPort",proxyPort);
	      System.getProperties().put("https.proxyHost",proxyHost);
	      System.getProperties().put("https.proxyPort",proxyPort);
	      
	}

	public static ListsSoap getSPListSoapStub(String username, String password, String url) throws Exception {
	    ListsSoap port = null;
	    if (username != null && password != null) {
	        try {
	        	URL wsdlURL = new URL(getInstance().getClass().getClassLoader().getResource("wsdl/lists.wsdl").toExternalForm());
	        	Lists service = new Lists(wsdlURL);
	            port = service.getListsSoap();
	    		if (LOGGER.isLoggable(Level.INFO)) {
	    			LOGGER.info("LISTS Web Service Auth Username: " + username);
	    		}
	            ((BindingProvider) port).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, username);
	            ((BindingProvider) port).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, password);
	            
	            URL convertedurl = convertToURLEscapingIllegalCharacters(url+"/_vti_bin/Lists.asmx");
	            ((BindingProvider) port).getRequestContext().put(
						BindingProvider.ENDPOINT_ADDRESS_PROPERTY, convertedurl.toString());
			 
	        } catch (Exception e) {
	        	e.printStackTrace();
	            throw new Exception("Error: " + e.toString());
	        }
	    } else {
	        throw new Exception("Couldn't authenticate: Invalid connection details given.");
	    }
	    return port;
	}
	

	public static VersionsSoap getSPVersionsStub(String userName, String password, String url) throws Exception {
		VersionsSoap port = null;

		 if (userName != null && password != null) {
		 try {
	     URL wsdlURL = new URL(getInstance().getClass().getClassLoader().getResource("wsdl/versions.wsdl").toExternalForm());
		 Versions service = new Versions(wsdlURL);
		 port = service.getVersionsSoap();
		 System.out.println("Web Service Auth Username: " + userName);
			((BindingProvider) port).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, userName);
			((BindingProvider) port).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, password);
			 URL convertedurl = convertToURLEscapingIllegalCharacters(url+"/_vti_bin/Versions.asmx");
	        ((BindingProvider) port).getRequestContext().put(
						BindingProvider.ENDPOINT_ADDRESS_PROPERTY, convertedurl.toString());
		 } catch (Exception e) {
			 throw new Exception("Error: " + e.toString());
		 }
		 } else {
			 throw new Exception("Couldn’t authenticate: Invalid connection details given.");
		 }
		 return port;
	}

	 public static CopySoap getSPCopySoapStub(String userName, String password, String url) throws Exception {
	 CopySoap port = null;

		 if (userName != null && password != null) {
			 try {
			     URL wsdlURL = new URL(getInstance().getClass().getClassLoader().getResource("wsdl/copy.wsdl").toExternalForm());
				 Copy service = new Copy(wsdlURL);
				 port = service.getCopySoap();
				 System.out.println("Web Service Auth Username: " + userName);
				 ((BindingProvider) port).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, userName);
				 ((BindingProvider) port).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, password);
				 URL convertedurl = convertToURLEscapingIllegalCharacters(url+"/_vti_bin/Copy.asmx");
		            ((BindingProvider) port).getRequestContext().put(
							BindingProvider.ENDPOINT_ADDRESS_PROPERTY, convertedurl.toString());
				 } 
			 catch (Exception e) {
				 throw new Exception("Error: " + e.toString());
			 }
		 } else {
			 throw new Exception("Couldn’t authenticate: Invalid connection details given.");
		 }
	 return port;
	 }
	 
	/**
	 * Creates a string from an XML file with start and end indicators
	 * @param docToString document to convert
	 * @return string of the xml document
	 */
	public static String xmlToString(Document docToString) {
	    String returnString = "";
	    try {
	        //create string from xml tree
	        //Output the XML
	        //set up a transformer
	        TransformerFactory transfac = TransformerFactory.newInstance();
	        Transformer trans;
	        trans = transfac.newTransformer();
	        trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
	        trans.setOutputProperty(OutputKeys.INDENT, "yes");
	        StringWriter sw = new StringWriter();
	        StreamResult streamResult = new StreamResult(sw);
	        DOMSource source = new DOMSource(docToString);
	        trans.transform(source, streamResult);
	        String xmlString = sw.toString();
	        //print the XML
	        returnString = returnString + xmlString;
	    } catch (TransformerException ex) {
	    	LOGGER.severe(ex.toString());
	    }
	    return returnString;
	}

	/**
	 * Connects to a SharePoint Lists Web Service through the given open port,
	 * and reads all the elements of the given list. Only the given column names
	 * are displayed.
	 */ 
	public static void displaySharePointList() throws Exception {
	        try {



 		// you can also give id of "Documents" node
		//{44131435-EAFB-4244-AA39-F431F55ADA9B}
		//
		String listName = "Documents";
		String rowLimit = "150";
		ArrayList<String> listColumnNames = new ArrayList<String>();
		listColumnNames.add("LinkFilename");
		listColumnNames.add("FileRef");

				
	         //Here are additional parameters that may be set
	         String viewName = "";
	         GetListItems.ViewFields viewFields = null;
	         GetListItems.Query query = null;
	         GetListItems.QueryOptions queryOptions = null;
	         String webID = "";
	             
	         //Calling the List Web Service
	         GetListItemsResponse.GetListItemsResult result = listsoapstub.getListItems(listName, viewName, query, viewFields, rowLimit, queryOptions, webID);
	         Object listResult = result.getContent().get(0);
	            if ((listResult != null) && (listResult instanceof Element)) {
	                Element node = (Element) listResult;

	                //Dumps the retrieved info in the console
	                Document document = node.getOwnerDocument();
	                LOGGER.info("SharePoint Online Lists Web Service Response:" + SPClient.xmlToString(document));

	                //selects a list of nodes which have z:row elements
	                NodeList list = node.getElementsByTagName("z:row");
	                LOGGER.info("=> " + list.getLength() + " results from SharePoint Online");

	                //Displaying every result received from SharePoint, with its ID
	                for (int i = 0; i < list.getLength(); i++) {

	                    //Gets the attributes of the current row/element
	                    NamedNodeMap attributes = list.item(i).getAttributes();
	                    LOGGER.info("******** Item ID: " + attributes.getNamedItem("ows_ID").getNodeValue()+" ********");

	                    //Displays all the attributes of the list item that correspond to the column names given
	                    for (String columnName : listColumnNames) {
	                        String internalColumnName = "ows_" + columnName;
	                        if (attributes.getNamedItem(internalColumnName) != null) {
	                        	LOGGER.info(columnName + ": " + attributes.getNamedItem(internalColumnName).getNodeValue());
	                        } else {
	                            throw new Exception("Couldn't find the '" + columnName + "' column in the '" + listName + "' list in SharePoint.\n");
	                        }
	                    }
	                }
	            } else {
	                throw new Exception(listName + " list response from SharePoint is either null or corrupt\n");
	            }
	        } catch (Exception ex) {
	        	ex.printStackTrace();
	            throw new Exception("Exception. See stacktrace.\n" + ex.toString() + "\n");
	        }
}
	
	/**
	 * Checks-out the specified file
	 * @param port Lists web service port
	 * @param pageUrl
	 * @return true if the operation succeeded; otherwise, false. 
	 */
	public static boolean checkOutFile(ListsSoap port, String pageUrl) {
		if (LOGGER.isLoggable(Level.INFO)) {
			LOGGER.info("Checking-out pageUrl=" + pageUrl);
		}
		String checkoutToLocal = "true";
		String lastModified    = "";
		boolean result = port.checkOutFile(pageUrl, checkoutToLocal, lastModified);
		if (LOGGER.isLoggable(Level.INFO)) {
			LOGGER.info("Check-out result = " + result);
		}
		return result;
	}
	
	/**
	 * Undo checked-out file
	 * @param port Lists web service port
	 * @param pageUrl
	 * @return true if the operation succeeded; otherwise, false. 
	 */
	public static boolean undoCheckOutFile(ListsSoap port, String pageUrl) {
		if (LOGGER.isLoggable(Level.INFO)) {
			LOGGER.info("Undo checkout pageUrl=" + pageUrl);
		}
		boolean result = port.undoCheckOut(pageUrl);
		if (LOGGER.isLoggable(Level.INFO)) {
			LOGGER.info("Undo checkout result = " + result);
		}
		return result;
	}
	
	/**
	 * Checks-in the specified file
	 * @param port Lists web service port
	 * @param pageUrl
	 * @param comment
	 * @return true if the operation succeeded; otherwise, false. 
	 */
	public static boolean checkInFile(ListsSoap port, String pageUrl, String comment) {
		if (LOGGER.isLoggable(Level.INFO)) {
			LOGGER.info("Checking-in pageUrl=" + pageUrl + " comment=" + comment);
		}
		// checkinType = values 0, 1 or 2, where 0 = MinorCheckIn, 1 = MajorCheckIn, and 2 = OverwriteCheckIn.
		String checkinType = "0";
		boolean result = port.checkInFile(pageUrl, comment, checkinType);
		if (LOGGER.isLoggable(Level.INFO)) {
			LOGGER.info("Check-in result = " + result);
		}
		return result;
	}
	
	
	 /**
	 *
	 */
	 public static void DownloadDocumentVersionsFromSPDocumentLibrary(String FileUrl) throws Exception {
	 try {
		 URL convertedurl = convertToURLEscapingIllegalCharacters(FileUrl);
		 System.out.println("convertedurl.toString()=" + convertedurl.toString());
		 GetVersionsResponse.GetVersionsResult result = DocumentLibrary.VersionsGetVersions(versionssoapstub,convertedurl.toString());
		 Object listResult = result.getContent().get(0);
		 System.out.println("GetVersions Result=" + result);
		 if ((listResult != null) && (listResult instanceof ElementNSImpl)) {
		 ElementNSImpl node = (ElementNSImpl) listResult;
	
		 //Dumps the retrieved info in the console
		 Document document = node.getOwnerDocument();
		 System.out.println("SharePoint Online Lists Web Service Response:" + xmlToString(document));
	
		 //selects a list of nodes which have z:row elements
		 NodeList list = node.getElementsByTagName("result");//("z:row");
		 System.out.println("=> " + list.getLength() + " results from SharePoint Online");
	
		 //Displaying every result received from SharePoint
		 for (int i = 0; i < list.getLength(); i++) {
	
			 //Gets the attributes of the current row/element
			 NamedNodeMap attributes = list.item(i).getAttributes();
			 String ver = attributes.getNamedItem("version").getNodeValue();
			 //Download Latest Version only
			 if (ver.indexOf("@") != -1) {
				 System.out.println("******** Url: " + attributes.getNamedItem("url").getNodeValue() + " ********");
				 //Download File on Local Hard Disk using Copy Web Service
				 DownloadFileUsingCopyWebService(attributes.getNamedItem("url").getNodeValue(), "sharepoint-downloads", attributes.getNamedItem("version").getNodeValue());
			 }
		 }
		 } else {
		 	throw new Exception("List response from SharePoint is either null or corrupt\n");
		 }
	
		 } catch (Exception e) {
			 e.printStackTrace();
			 throw new Exception("Error: " + e.toString());
		 }
}

	
	 /**
	 *
	 * @param sourceUrl
	 * @param destination
	 * @param versionNumber
	 * @throws Exception
	 */
	 public static void DownloadFileUsingCopyWebService(String sourceUrl, String destination, String versionNumber) throws Exception {
	 try {
	
		 //Extract the filename from the source URL
		 String fileName = sourceUrl.substring(sourceUrl.lastIndexOf("/") + 1);

//		 if (versionNumber != null) {
//			 fileName = versionNumber + "-" + fileName;
//		 }
		 destination = destination + "\\" + fileName;
	
		 //Prepare the Parameters required for the method
		 javax.xml.ws.Holder fieldInfoArray = new javax.xml.ws.Holder();
		 javax.xml.ws.Holder cResultArray = new javax.xml.ws.Holder();
		 javax.xml.ws.Holder fileContents = new javax.xml.ws.Holder(); // no need to initialize the GetItem takes care of that.
	
		 //Cal Web Service Method
		 copysoapstub.getItem(sourceUrl, cResultArray, fieldInfoArray, fileContents);
	
		 //Write the byte[] to the output file
		 //Integer val = fileContents.value;
		 FileOutputStream fos = new FileOutputStream(destination);
		 fos.write((byte[])fileContents.value);
		 fos.close();
	 } catch (FileNotFoundException ex) {
		 System.out.println("FileNotFoundException : " + ex);
	 } catch (IOException ioe) {
		 System.out.println("IOException : " + ioe);
	 } catch (Exception ex) {
		 ex.printStackTrace();
		 throw new Exception("Error: " + ex.toString());
	 }
	 }


/**
*
* @param sourceUrl
* @param destination
* @param versionNumber
* @throws Exception
*/
public static void UploadFileUsingCopyWebService(String filepath) throws Exception {
	try {
	 
		 File file = new File(filepath);
		 BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
		 byte[] stream = new byte[bis.available()];
		 bis.read(stream);
		 FieldInformation fieldInformation = new FieldInformation();
		 fieldInformation.setDisplayName(file.getName());
		 fieldInformation.setInternalName(file.getName());
		 FieldInformationCollection fields = new FieldInformationCollection();
		 fields.getFieldInformation().add(fieldInformation);
		 
		 String sourceUrl = file.getName();
		 DestinationUrlCollection destinationUrls = new DestinationUrlCollection();
		 destinationUrls.getString().add(BasesharepointUrl +file.getName());
		 Holder<Long> copyIntoItemsResult = new Holder<Long>();
		 Holder<CopyResultCollection> results = new Holder<CopyResultCollection>();
	   
		 copysoapstub.copyIntoItems(sourceUrl, destinationUrls, fields, stream, copyIntoItemsResult, results);
		 // error message always exists
		 String errorMessage = results.value.getCopyResult().get(0).getErrorMessage();
		 if (errorMessage != null) {
			 throw new FileAlreadyExistsException("File already exists in directory");
		 } else {
			 System.out.println("file " + file.getName() + " has been successfully uploaded to server" + "\n ");
		 }
	} catch (FileNotFoundException ex) {
		 System.out.println("FileNotFoundException : " + ex);
	} catch (IOException ioe) {
		 System.out.println("IOException : " + ioe);
	} catch (Exception ex) {
		 throw new Exception("Error: " + ex.toString());
	}
	}

}

================================================================================
DocumentLibrary class
================================================================================
import com.microsoft.schemas.sharepoint.soap.versions.GetVersionsResponse;
import com.microsoft.schemas.sharepoint.soap.versions.VersionsSoap;

public class DocumentLibrary {

	public DocumentLibrary() {}
	
	public static GetVersionsResponse.GetVersionsResult VersionsGetVersions(VersionsSoap port, String FileName) throws Exception
	{
			try {
				GetVersionsResponse.GetVersionsResult Result = port.getVersions(FileName);
				return Result;
			} catch (Exception e) {
				throw new Exception("Error: " + e.toString());
			}
	}
}

================================================================================
SimpleAuthenticator  class
================================================================================
public class SimpleAuthenticator  extends Authenticator
{
	private final static String proxyHost = "proxy.co.uk";
	private final static String proxyPort = "8080";
	private final static String internetproxyusername = "internetproxyusername";
	private final static String internetproxypassword = "internetproxypassword";
	private final static String sharepointusername = "sharepointusername";
	private final static String sharepointpassword = "sharepointpassword";
	private String username;
	private final char[] password;
   
   public SimpleAuthenticator(String username,String password)
   {
	   super();
	   this.username = new String(username);
	   this.password = password.toCharArray(); 
   }
   
   protected PasswordAuthentication getPasswordAuthentication()
   {
	  String requestingHost = getRequestingHost();
	  if (requestingHost == proxyHost){
		  System.out.println("getPasswordAuthentication() request recieved from->" + requestingHost );
		  return new PasswordAuthentication(internetproxyusername,internetproxypassword.toCharArray());
	  }
	  else{
		  System.out.println("getPasswordAuthentication() request recieved from->" + requestingHost );
		  return new PasswordAuthentication(sharepointusername,sharepointpassword.toCharArray());
	  }
		  
   }
}