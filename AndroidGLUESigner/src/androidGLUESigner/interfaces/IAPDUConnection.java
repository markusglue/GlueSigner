package androidGLUESigner.interfaces;

import com.acs.smartcard.ReaderException;

/**

 * Extended Connectioninterface for SuisseID-Connection classes

 * 

 * @author Mario Bischof

 * @version 1.0

 */
public interface IAPDUConnection extends IConnection {
	/**
	 * authenticate to the SuisseID with personal identification number
	 * @param pin the SuisseID PIN
	 * @return if authentication was successful
	 * @throws ReaderException
	 */
	public boolean authenticate(String pin) throws ReaderException;
	
}
