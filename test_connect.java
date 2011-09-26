import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.io.ByteArrayOutputStream;

try {
  //Access to Zimbra with SSL protocol
  System.setProperty("java.protocol.handler.pkgs", "com.sun.net.ssl.internal.www.protocol");
  System.setProperty("javax.net.ssl.trustStore", "C:/keystore.jks");
  System.setProperty("javax.net.ssl.trustStorePassword", "password");
						
  //First create the connection
  SOAPConnectionFactory soapConnFactory = SOAPConnectionFactory.newInstance();
  SOAPConnection connection = soapConnFactory.createConnection();
  
  //Next, create the actual message
  MessageFactory messageFactory = MessageFactory.newInstance();
  SOAPMessage message = messageFactory.createMessage();
  
  //Create objects for the message parts            
  SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
  SOAPHeader header =     envelope.getHeader();
  SOAPBody body =         envelope.getBody();
  SOAPFactory soapFactory = SOAPFactory.newInstance();
  
  //<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\">
  //  <soap:Header>
  //    <context xmlns=\"urn:zimbra\"/>
  //  </soap:Header>
  //  <soap:Body>
  //    <AuthRequest xmlns=\"urn:zimbraAccount\">
  //      <account by=\"name\">admin</account>
  //      <password>*netixia*</password>
  //    </AuthRequest>
  //  </soap:Body>
  //</soap:Envelope>
  //Populate envelope
  envelope.setPrefix("soap");
  envelope.addNamespaceDeclaration("soap", "http://www.w3.org/2003/05/soap-envelope");
  //Populate header
  header.setPrefix("soap");
  SOAPHeaderElement context = header.addHeaderElement(soapFactory.createName("context", "", "urn:zimbra"));
  context.setPrefix("");
  //Populate body
  body.setPrefix("soap");
  SOAPBodyElement authRequest = body.addBodyElement(soapFactory.createName("AuthRequest","","urn:zimbraAccount"));
  SOAPElement account = authRequest.addChildElement("account");
  account.addAttribute(soapFactory.createName("by"), "name");
  account.addTextNode("admin");
  authRequest.addChildElement("password").addTextNode("******");
    
  //Save the message
  message.saveChanges();
  
  //Check the input
  System.out.println("\nREQUEST:\n");
  message.writeTo(System.out);
  System.out.println();
  
  //Send the message and get a reply
  //Set the destination
  String destination = "https://zimbrax.ville-chateauroux.fr:7071/service/admin/soap";
  //Send the message
  SOAPMessage reply = connection.call(message, destination);

  //Check the output
  System.out.println("\nRESPONSE:\n");
  //Create the transformer
  TransformerFactory transformerFactory = TransformerFactory.newInstance();
  Transformer transformer = transformerFactory.newTransformer();
  //Extract the content of the reply
  Source sourceContent = reply.getSOAPPart().getContent();
  //Set the output for the transformation
  StreamResult result = new StreamResult(System.out);
  transformer.transform(sourceContent, result);
  System.out.println();
  
  //Handles the received SOAP message
  System.out.println("\nELEMENTS:\n");
  SOAPBody replyBody = reply.getSOAPPart().getEnvelope().getBody();
  if (replyBody.hasFault()) {
    SOAPFault fault = replyBody.getFault();
    throw new SOAPException("Erreur en récupérant la réponse:" + fault.getFaultString());
    }
  Iterator iter = replyBody.getChildElements();
  if (iter.hasNext()) {
    SOAPElement element1 = (SOAPElement)iter.next();
    System.out.println(element1.getElementName().getLocalName());
    System.out.println(element1.getValue());
    iter = element1.getChildElements();
    while (iter.hasNext()) {
          SOAPElement element2 = (SOAPElement)iter.next();
          System.out.print(element2.getElementName().getLocalName() + ": ");
          System.out.println(element2.getValue());
      }
    }
  else System.out.println("pas de correspondance");
  
  //Close the connection            
  connection.close();
      
  } catch(Exception e) {
      System.out.println(e.getMessage());
  }

