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

package androidGLUESigner.pdf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.cert.Certificate;

import org.apache.commons.io.FilenameUtils;

import androidGLUESigner.exception.Logger;
import androidGLUESigner.interfaces.IConnection;
import androidGLUESigner.models.SignatureInfo;

/**
 * Initiates the signing process, input- output files, 
 * gets certificates from the IConnection, sets the information
 * of the signature
 * 
 * @author mario
 *
 */
public class PDFSigner {
	
	private static final String SIGNED_SUFFIX = "_signed.pdf";
	private IConnection connection;
	private Certificate[] chain;
	private String sigPath;
	private String tsaUrl;
	
	public PDFSigner(IConnection connection, String sigPath, String tsaUrl) {	
		if(connection.isInitialized()){
			this.connection = connection;
		}else{
			String errorMessage = "Connection is not initialized.";
			Logger.printError(errorMessage);
		}
		this.sigPath = sigPath;
		this.tsaUrl = tsaUrl;
	}
	

	/**
	 * signs a PDF file
	 * @param signInformation holds the reason, location, inputfilepath, ...
	 * @param withTSA flag to decide if online TimeStamp should be received
	 * @return the signed PDF File
	 * @throws Exception
	 */
    public File signPDF(SignatureInfo signInformation, boolean withTSA)
        throws Exception {

    	// input, temp and output PDF-files
        final String ORIGINAL = signInformation.getInputFilePath();
        final String TMP = signInformation.getInputFilePath() + ".tmp";
        final String RESULT;
        if(sigPath.equals("")){
           RESULT = signInformation.getInputFilePath().split("\\.")[0] + SIGNED_SUFFIX;
        }else{
           RESULT = (sigPath + "/" + FilenameUtils.getBaseName(signInformation.getInputFilePath())) + SIGNED_SUFFIX;
        }
        
        // get the certificate-chain from the selected connection
        chain = connection.getCertificateChain();
       
        // initiate the Signer with the certificates and signature info
        PDFSignerEngine signer = new PDFSignerEngine();
        signer.setCertificateChain(chain);
        signer.setSiginfo(signInformation);
        
        if(withTSA){
        	// prepare the signature and get the digest
        	byte[] digest = signer.prepareSign(new FileInputStream(ORIGINAL), new FileOutputStream(TMP));
        	byte[] state = signer.getState();
        
        	// sign the digest via connection
        	byte[] signedHash = connection.sign(digest);

        	// finalize the siganture by adding the signedHash into the signature
        	PDFSignerEngine signer2 = new PDFSignerEngine();
        	signer2.setTsaUrl(this.tsaUrl);
        	signer2.setState(state);
        	signer2.finalizeSign(signedHash, new FileInputStream(TMP), new FileOutputStream(RESULT));
        }else{
        	signer.simpleSign(ORIGINAL, RESULT, connection);
        }
        //return null;
		return new File(RESULT);
    }   
}
