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
