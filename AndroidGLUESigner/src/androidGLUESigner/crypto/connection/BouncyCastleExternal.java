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
