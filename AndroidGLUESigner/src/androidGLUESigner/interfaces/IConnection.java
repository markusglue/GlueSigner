/*
* This program is free software; you can redistribute it and/or modify it under the terms of the 
* GNU Affero General Public License version 3 as published by the Free Software Foundation 
* with the addition of the following permission added to Section 15 as permitted in Section 7(a): 
* FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY Glue Software Engineering AG, 
* Glue Software Engineering AG DISCLAIMS THE WARRANTY OF NON INFRINGEMENT OF THIRD PARTY RIGHTS.
* 
* This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
* without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
* See the GNU Affero General Public License for more details. You should have received a copy 
* of the GNU Affero General Public License along with this program; if not, 
* see http://www.gnu.org/licenses or write to the Free Software Foundation, 
* Inc., 51 Franklin Street, Fifth Floor, Boston, MA, 02110-1301 USA.

* The interactive user interfaces in modified source and object code versions of this program 
* must display Appropriate Legal Notices, as required under Section 5 of the GNU Affero General Public License.
* 
* In accordance with Section 7(b) of the GNU Affero General Public License, 
* a covered work must retain the producer line in every PDF that is created or manipulated using GlueSigner.
* 
* For more information, please contact Glue Software Engineering AG at this address: info@glue.ch
*/

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
