package androidGLUESigner.interfaces;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.cert.*;

import com.acs.smartcard.ReaderException;
import com.lowagie.text.DocumentException;


/**

 * Connectioninterface defining common methods for the various
 
 * ways of connecting the signingprocess

 *

 * @author Mario Bischof

 * @version 1.0

 */
public interface IConnection {
	/**
	 * @return if connection has been initialized
	 */
	public boolean isInitialized();
	
	/**
	 * initializes the connection
	 * @throws InterruptedException
	 */
	public void init() throws InterruptedException;
	
	/**
	 * extracts the certificate chain from the connection object
	 * @return the Certificate[] chain
	 * @throws ReaderException
	 */
	public Certificate[] getCertificateChain() throws ReaderException;
		
	/**
	 * Signs a hash value using the connection
	 * @param hashValue hash value to be signed
	 * @return signed hash value as byte[]
	 * @throws IOException
	 * @throws DocumentException
	 * @throws ReaderException
	 * @throws NoSuchAlgorithmException
	 * @throws SignatureException
	 * @throws InvalidKeyException
	 */
	public byte[] sign(byte[] hashValue) throws IOException, DocumentException, ReaderException, NoSuchAlgorithmException, SignatureException, InvalidKeyException;

	/**
	 * disposal of any resources still in memory
	 * closing of devices and streams
	 */
	public void cleanup();
	
	/**
	 * To show connection information in the UI
	 * @param msg logged message
	 */
	public void logMsg(String msg);
}
