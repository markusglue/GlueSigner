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

package androidGLUESigner.crypto.connection;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;

import androidGLUESigner.helpers.KeyStoreHelper;
import androidGLUESigner.interfaces.IConnection;

import com.acs.smartcard.ReaderException;
import com.lowagie.text.DocumentException;



/**

 * This is the BouncyCastle Connection class, to sign a HashValue

 * with a BC Keystore pk and cert with an external signature in droidText

 *

 * @author Mario Bischof + Roland Hediger

 * @version 1.0

 */
public class BouncyCastleExternal implements IConnection {
	
	private String basePath;
	private KeyStore keyStore;
	private String alias;
	private boolean initialized;

	/**
	 * Constructor
	 * @param basePath default directory for GLUESigner
	 * @param alias the name of the certificate within the keystore
	 */
	public BouncyCastleExternal(String basePath,String alias){
		this.basePath = basePath;
		this.alias = alias;
	}
	
	@Override
	public boolean isInitialized() {
		return initialized;
	}

	@Override
	public void init() throws InterruptedException {
		this.keyStore = KeyStoreHelper.getApplicationKeyStore(basePath);	
		this.initialized = true;
	}

	@Override
	public Certificate[] getCertificateChain() throws ReaderException {
		return KeyStoreHelper.getCertificateChain(keyStore, alias);
	}

	@Override
	public byte[] sign(byte[] hashValue) throws IOException, DocumentException,
			ReaderException, NoSuchAlgorithmException, SignatureException, InvalidKeyException {
		if(isInitialized()){
			PrivateKey key = KeyStoreHelper.getPrivateKey(keyStore, alias);
			Signature signature =  Signature.getInstance("SHA1withRSA");
			signature.initSign(key);
			signature.update(hashValue);
			return signature.sign();
		}
		return null;
	}

	@Override
	public void cleanup() {
		this.initialized = false;
		keyStore = null;
	}

	/* (non-Javadoc)
	 * @see android_GLUESigner.interfaces.IConnection#logMsg(java.lang.String)
	 */
	@Override
	public void logMsg(String msg) {
		// no LogMsgs necessary for this implementation	
	}
}
