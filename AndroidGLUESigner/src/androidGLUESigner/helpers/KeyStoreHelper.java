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

package androidGLUESigner.helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Enumeration;

import androidGLUESigner.exception.Logger;

/**
 * static class for manipulating bouncy castle keystore instances
 * @author roland
 *
 */
public class KeyStoreHelper {

	// keystore pass, strong password
	public static final String KEYSTOREPASS = "E,4t<g(TvPr4Btt";
	/**
	 * 
	 * @param basePath default directory for the GLUESigner application
	 * @return the keystore object
	 */
	public static KeyStore getApplicationKeyStore(String basePath){
		KeyStore ks = null;		
		try {
			 ks = KeyStore.getInstance(KeyStore.getDefaultType());
			 if(!loadExistingKeyStore(ks, basePath)){
				 ks.load(null,null);
				 ks.store(new FileOutputStream(basePath + "/" + "appKeyStore.jks"), KEYSTOREPASS.toCharArray());
			}
			return ks;
		} catch (KeyStoreException e) {
			Logger.toConsole(e);
		} catch (NoSuchAlgorithmException e) {
			Logger.toConsole(e);
		} catch (CertificateException e) {
			Logger.toConsole(e);
		} catch (IOException e) {
			Logger.toConsole(e);
		}	
		return ks;
	}
	
	/**
	 * imports a certificate into the keystore from a .p12 file
	 * @param certificateFile the file to be imported into the keystore
	 * @param ks the keystore
	 * @param passphrase certificate passphrase
	 * @return a list of aliases for the certificates in the file
	 */
	public static ArrayList<String> importCertificate(File certificateFile, KeyStore ks,String passphrase){
		ArrayList<String> aliases = new ArrayList<String>();
		try {
			KeyStore kspkcs12 = KeyStore.getInstance("pkcs12");
			kspkcs12.load(new FileInputStream(certificateFile),passphrase.toCharArray());
			Enumeration<String> eAliases = kspkcs12.aliases();
			while (eAliases.hasMoreElements()){				
				String keyalias = (String) eAliases.nextElement();				
				if (kspkcs12.isKeyEntry(keyalias)){
					aliases.add(keyalias);
					Key key = kspkcs12.getKey(keyalias,passphrase.toCharArray());
					java.security.cert.Certificate[] chain =kspkcs12.getCertificateChain(keyalias);
					ks.setKeyEntry(keyalias, key,KEYSTOREPASS.toCharArray(),chain);					
				}
			}
			
		} catch (KeyStoreException e) {
			Logger.toConsole(e);
		} catch (NoSuchAlgorithmException e) {
			Logger.toConsole(e);
		} catch (CertificateException e) {
			Logger.toConsole(e);
		} catch (FileNotFoundException e) {
			Logger.toConsole(e);
		} catch (IOException e) {
			Logger.toConsole(e);
		} catch (UnrecoverableKeyException e) {
			Logger.toConsole(e);
		}
		return aliases;
	}
	
	/**
	 * saves the in memory keystore into a file
	 * @param ks the keystore
	 * @param basePath default directory for the GLUESigner application
	 */
	public static void saveApplicationKeyStore(KeyStore ks,String basePath){
		try {
			ks.store(new FileOutputStream(basePath+ "/" + "appKeyStore.jks"), KEYSTOREPASS.toCharArray());
		} catch (KeyStoreException e) {
			Logger.toConsole(e);
		} catch (NoSuchAlgorithmException e) {
			Logger.toConsole(e);
		} catch (CertificateException e) {
			Logger.toConsole(e);
		} catch (FileNotFoundException e) {
			Logger.toConsole(e);
		} catch (IOException e) {
			Logger.toConsole(e);
		}
	}

	/**
	 * loads keystore if already exists in a file
	 * @param ks the keystore
	 * @return if successfully executed
	 */
	private static boolean loadExistingKeyStore(KeyStore ks,String basePath) {
	    File keystoreFile = new File(basePath + "/"+ "appKeyStore.jks");
	    if (keystoreFile.exists()){
	    	try {
				ks.load(new FileInputStream(keystoreFile),KEYSTOREPASS.toCharArray());
			} catch (NoSuchAlgorithmException e) {
				Logger.toConsole(e);
			} catch (CertificateException e) {
				Logger.toConsole(e);
			} catch (FileNotFoundException e) {
				Logger.toConsole(e);
			} catch (IOException e) {
				Logger.toConsole(e);
			}
	    	return true;
	    }else{
	    	return false;
	    }	
	}

	/**
	 * fetches the private key for a specific alias in the keystore
	 * @param ks the keystore
	 * @param alias certificate alias
	 * @return
	 */
	public static PrivateKey getPrivateKey(KeyStore ks, String alias) {
		try {
			Enumeration<String> eAliases = ks.aliases();
			ArrayList<String> aliases = new ArrayList<String>();
			while(eAliases.hasMoreElements()){
				aliases.add(eAliases.nextElement());
			}
			return (PrivateKey)ks.getKey(alias, KEYSTOREPASS.toCharArray());
		} catch (UnrecoverableKeyException e) {
			Logger.toConsole(e);
		} catch (KeyStoreException e) {
			Logger.toConsole(e);
		} catch (NoSuchAlgorithmException e) {
			Logger.toConsole(e);
		}
		return null;
		
	}

	/**
	 * @param ks the keystore
	 * @param alias the certificate alias
	 * @return the certificate chain for the certificate
	 */
	public static java.security.cert.Certificate[] getCertificateChain(
			KeyStore ks, String alias) {
		try {
			return ks.getCertificateChain(alias);
		} catch (KeyStoreException e) {
			Logger.toConsole(e);
		}
		return null;
	}
	
	/**
	 * get all aliases from keystore to display in UI chooser dialog
	 * @param ks the keystore
	 * @return list of all aliases within the keystore
	 */
	public static ArrayList<String> getAliases(KeyStore ks){
		ArrayList<String> aliases = new ArrayList<String>();
		try {
			Enumeration<String> aEnumerator = ks.aliases();
			while (aEnumerator.hasMoreElements()){
				String alias = aEnumerator.nextElement();
				aliases.add(alias);			}
			
		} catch (KeyStoreException e) {
			Logger.toConsole(e);
		}
		return aliases;	
	}
}
