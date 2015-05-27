package androidGLUESigner.pdf;


import androidGLUESigner.exception.Logger;
import androidGLUESigner.helpers.FilePatchHelper;
import androidGLUESigner.helpers.UtilityHelper;
import androidGLUESigner.interfaces.IConnection;
import androidGLUESigner.models.SignatureInfo;
import androidGLUESigner.models.SignatureInfo.SignatureType;

import com.acs.smartcard.ReaderException;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.OcspClientBouncyCastle;
import com.lowagie.text.pdf.PdfDate;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfPKCS7;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfSignature;
import com.lowagie.text.pdf.PdfSignatureAppearance;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.PdfString;
import com.lowagie.text.pdf.TSAClient;
import com.lowagie.text.pdf.TSAClientBouncyCastle;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.HashMap;

import org.apache.commons.io.IOUtils;

/**
 * Class that handling the signing operations (ocsp, timestamp, obtaining hash value,
 * placeholders, signature appearance, siganture image)
 * @author mario
 *
 */
public class PDFSignerEngine {

    // These need to be saved between runs to continue signing later.
    // Use getState() and setState() to save / restore these values.
    private byte[] hash = null;
    private byte[] ocsp = null;
    private Calendar calendar = null;

    // Size of the raw signature in PDF; in hex encoding it's 2x bigger
    final static int SIGNATURE_MAX_SIZE = 15000;

    // the certificate chain and certs
    Certificate[] certificateChain = null;
    private X509Certificate cert = null;
    private X509Certificate issuerCert = null;
    
    // the timestamp url and authentication credentials
    private String tsa_url    = "";
    private String tsa_login  = null;
    private String tsa_passw  = null;
    
    // the signature info used for the current document
    private SignatureInfo siginfo = null;
    
    /**
     * get the signature info object
     * @return signature info
     */
    public SignatureInfo getSiginfo() {
		return siginfo;
	}

    /**
     * set the signature info object
     */
	public void setSiginfo(SignatureInfo siginfo) {
		this.siginfo = siginfo;
	}

	private String hashAlgo = "SHA256";
    private static final String tempPlaceholder = "-- PDFSigner temporary placeholder -- 4A84CBFB 41289900 0320CDF3 2C38D3DF --";

    /**
     * set the timestamp url 
     * @param url the timestamp url
     */
    void setTsaUrl(String url) {
        this.tsa_url = url;
    }
    
    /**
     * set the timestamp service login 
     * @param url the service timestamp login
     */
    void setTsaLogin(String login) {
        this.tsa_login = login;
    }

    /**
     * set the timestamp service password 
     * @param url the timestamp service password
     */
    void setTsaPassword(String passw) {
        this.tsa_passw = passw;
    }

    /**
     * set the certificate chain
     * @param chain the certificate chain
     */
    public void setCertificateChain(Certificate[] chain) {
        this.certificateChain = chain;
        cert = (X509Certificate)certificateChain[0];
        issuerCert = (X509Certificate)certificateChain[1];
    }

    /**
     * set the signer certificate
     * @param cert the signer certificate
     */
    public void setCertificate(X509Certificate cert) {
        this.cert = cert;
    }
    
    /**
     * set the issuer certificate
     * @param issuerCert the issuer certificate
     */
    public void setIssuerCertificate(X509Certificate issuerCert) {
        this.issuerCert = issuerCert;
    }

    /**
     * get the certificate chain
     * @return the certificate chain
     */
    private Certificate[] getCertificateChain() throws CertificateException {
        if (certificateChain == null) {
            if (cert == null || issuerCert == null)
                throw new CertificateException("Certificates missing");

            certificateChain = new Certificate[2];
            certificateChain[0] = cert;
            certificateChain[1] = issuerCert;
        }

        return certificateChain;
    }

    /**
     * Send a ocsp request to the ocsp url stored in the certificate information
     * @param cert the signer cert
     * @param root the corresponding root cert
     * @return ocsp information
     * @throws CertificateException
     * @throws FileNotFoundException
     */
    private static byte[] ocspRequest(X509Certificate cert, X509Certificate root) throws CertificateException, FileNotFoundException {
        String url = PdfPKCS7.getOCSPURL(cert);
        return new OcspClientBouncyCastle(cert, root, url).getEncoded();
    }

    /**
     * Prepare the signing of the pdf (siganture appearance, placeholders, sigimage, ..) 
     * @param inputStream the stream to the input pdf file
     * @param outputStream the stream to the output pdf file
     * @return hash value with ocsp included
     * @throws IOException
     * @throws DocumentException
     * @throws GeneralSecurityException
     */
    public byte[] prepareSign(InputStream inputStream, OutputStream outputStream)
        throws IOException, DocumentException, GeneralSecurityException {

        PdfReader reader = new PdfReader(inputStream);

        PdfStamper stp = PdfStamper.createSignature(reader, outputStream, '\0', null, true);
        	
        PdfSignatureAppearance sap = stp.getSignatureAppearance();
        sap.setCrypto(null, getCertificateChain(), null, PdfSignatureAppearance.WINCER_SIGNED);
        PdfSignature dic = new PdfSignature(PdfName.ADOBE_PPKLITE, PdfName.ADBE_PKCS7_DETACHED);
        dic.setReason(siginfo.getSignatureReason());
        dic.setLocation(siginfo.getSignatureLocation());
        dic.setName(siginfo.getSignatureName());
        dic.setDate(new PdfDate(sap.getSignDate()));
        sap.setCryptoDictionary(dic);
        
        // get the selected rectangle and pagenumber for visible signature
		Rectangle signatureRect = new Rectangle(siginfo.getSignatureRect().left, siginfo.getSignatureRect().bottom, siginfo.getSignatureRect().right, siginfo.getSignatureRect().top);
	    int pageNumber = siginfo.getPageNumber();
    	sap.setVisibleSignature(signatureRect, pageNumber , null);

	    // set signature picture, if there is one
		if(siginfo.getSignatureType() == SignatureType.PICTURE){
	        Image obj_pic = Image.getInstance(siginfo.getImagePath());
	        sap.setImage(obj_pic);
	    }


        // preserve some space for the contents
        HashMap<PdfName,Integer> exc = new HashMap<PdfName,Integer>();
        exc.put(PdfName.CONTENTS, new Integer(SIGNATURE_MAX_SIZE * 2 + 2));
        sap.preClose(exc);

        // Save placeholder which will be replaced with actual signature later
        byte[] placeHolder = getPlaceHolderArr(SIGNATURE_MAX_SIZE * 2);
        // Replace the contents
        PdfDictionary dic2 = new PdfDictionary();
        dic2.put(PdfName.CONTENTS, new PdfString(placeHolder).setHexWriting(true));
        sap.close(dic2);

        // Calculate the digest
        InputStream data = sap.getRangeStream();
        
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        byte buf[] = new byte[8192];
        int n;
        while ((n = data.read(buf)) > 0) {
            messageDigest.update(buf, 0, n);
        }

        hash = messageDigest.digest();
        
        calendar = Calendar.getInstance();
        ocsp = ocspRequest(cert, issuerCert);
        System.out.println("Got OCSP response, length = " + ocsp.length);

        // Calculate another digest over authenticatedAttributes
        PdfPKCS7 sgn = new PdfPKCS7(null, getCertificateChain(), null, hashAlgo, null, true);
        byte[] sh = sgn.getAuthenticatedAttributeBytes(hash, calendar, ocsp);
        
        return sh;
    }

    /**
     * Finalize signature with timestamp and store into result file
     * @param signedHash the signed hash value
     * @param inputStream the stream to the input pdf file
     * @param outputStream the stream to the output pdf file
     * @throws Exception
     */
    public void finalizeSign(byte[] signedHash, InputStream inputStream, OutputStream outputStream)
        throws Exception {

        // Create a temporary file
        File tmpFile = File.createTempFile("PDFSigner-", ".pdf");
        //tmpFile.deleteOnExit();

        // Save the PDF in the temporary file
        UtilityHelper.copyStream(inputStream, new FileOutputStream(tmpFile));

        // Add a timestamp:
        TSAClient tsc = new TSAClientBouncyCastle(tsa_url, tsa_login, tsa_passw);

        // Create the signature
        PdfPKCS7 sgn = new PdfPKCS7(null, getCertificateChain(), null, hashAlgo, null, true);
        sgn.setExternalDigest(signedHash, hash, "RSA");
        byte[] encodedSig = sgn.getEncodedPKCS7(hash, calendar, tsc, ocsp);
        System.out.println("finelizeSign: signedHash.length    = " + signedHash.length);
        System.out.println("finelizeSign: encodedSig.length    = " + encodedSig.length);

        if (SIGNATURE_MAX_SIZE + 2 < encodedSig.length)
            throw new DocumentException("Not enough space");

        String encodedSigHex = UtilityHelper.byteArrayToHexString(encodedSig);

        byte[] placeHolder = getPlaceHolder(SIGNATURE_MAX_SIZE * 2).getBytes();

        byte[] paddedSig = new byte[placeHolder.length];
        // fill with harmless data
        for (int i = 0; i < paddedSig.length; i++)
            paddedSig[i] = 0x30;

        System.out.println("finelizeSign: placeHolder.length   = " + placeHolder.length);
        System.out.println("finelizeSign: encodedSigHex.length = " + encodedSigHex.length());
        assert(placeHolder.length == paddedSig.length);

        System.arraycopy(encodedSigHex.getBytes(), 0, paddedSig, 0, encodedSigHex.getBytes().length);

        // Replace the contents
        FilePatchHelper.replace(tmpFile.getPath(), placeHolder, paddedSig);

        // Save the PDF in the outputStream
        UtilityHelper.copyStream(new FileInputStream(tmpFile), outputStream);
        tmpFile.delete();
    }

    /**
     * get a placeholder in hex to prepare signature
     * @param estimatedSignatureLength approximate size of placeholder
     * @return the placeholder in Hex string
     */
    String getPlaceHolder(int estimatedSignatureLength) {
        return UtilityHelper.byteArrayToHexString(getPlaceHolderArr(estimatedSignatureLength));
    }

    /**
     * get a placeholder in byte[] to prepare signature
     * @param estimatedSignatureLength approximate size of placeholder
     * @return the placeholder as byte[]
     */
    byte[] getPlaceHolderArr(int estimatedSignatureLength) {
        byte[] bArrPlaceHolder = new byte[estimatedSignatureLength / 2];
        System.arraycopy(tempPlaceholder.getBytes(), 0, bArrPlaceHolder, 0, tempPlaceholder.getBytes().length);
        return bArrPlaceHolder;
    }

    /**
     * obtain the state of the SignerEngine (hash, ocsp, calendar from first run)
     * @return the obtained state as a byte[]
     */
    public byte[] getState() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos;
        try {
            oos = new ObjectOutputStream(bos);
            oos.writeObject(this.hash);
            oos.writeObject(this.ocsp);
            oos.writeObject(this.calendar);
            oos.writeObject(getCertificateChain());
            oos.close();
        } catch (Exception e) {
            Logger.toConsole(e);
        }
        return bos.toByteArray();
    }
    
    /**
     * set the state of the SignerEngine (hash, ocsp, calendar from current run)
     * @param in input byte[] holding the information to be stored
     */
    public void setState(byte[] in) {
        ByteArrayInputStream bis = new ByteArrayInputStream(in);
        ObjectInputStream ois;
        try {
            ois = new ObjectInputStream(bis);
            this.hash = (byte[]) ois.readObject();
            this.ocsp = (byte[]) ois.readObject();
            this.calendar = (Calendar) ois.readObject();
            setCertificateChain((Certificate[]) ois.readObject());
            ois.close();
        } catch (Exception e) {
            Logger.toConsole(e);
        }
    }
    
    /**
     * Simple Sign method to create a signature without Online Timestamp (needed if device
     * has no internet connection)
     * 
     * @param inputfile the inputfile
     * @param outputfile the outpuftile
     * @param connection the IConnection Object
     */   
    public void simpleSign(String inputfile, String outputfile, IConnection connection) throws IOException, DocumentException, CertificateException, InvalidKeyException, NoSuchAlgorithmException, SignatureException, ReaderException{
	    try{
    	SignatureInfo sigInfo = getSiginfo();	  
		PdfReader reader = new PdfReader(inputfile);
		FileOutputStream fout = new FileOutputStream(outputfile);
		PdfStamper stp = PdfStamper.createSignature(reader, fout, '\0', null, true);
		PdfSignatureAppearance sap = stp.getSignatureAppearance();
		sap.setCrypto(null, new Certificate[]{getCertificateChain()[0]}, null, PdfSignatureAppearance.SELF_SIGNED);
		sap.setReason(sigInfo.getSignatureReason());
		sap.setLocation(sigInfo.getSignatureLocation());
		
        // get the selected rectangle and pagenumber for visible signature
		Rectangle signatureRect = new Rectangle(siginfo.getSignatureRect().left, siginfo.getSignatureRect().bottom, siginfo.getSignatureRect().right, siginfo.getSignatureRect().top);
	    int pageNumber = siginfo.getPageNumber();
	    sap.setVisibleSignature(signatureRect, pageNumber , null);
	    // set signature picture, if there is one
		if(siginfo.getSignatureType() == SignatureType.PICTURE){
	        Image obj_pic = Image.getInstance(siginfo.getImagePath());
	        sap.setImage(obj_pic);
	    }
				
		sap.setExternalDigest(new byte[256], new byte[20], null);
		sap.preClose();

		java.io.InputStream inp = sap.getRangeStream();
		byte bytesToHash[] = IOUtils.toByteArray(inp);

		// sign the hash value
		byte[] signed = connection.sign(bytesToHash);
		
		PdfPKCS7 pdfSignature = sap.getSigStandard().getSigner();
		pdfSignature.setExternalDigest(signed, null, "RSA"); 
		
		PdfDictionary dic = new PdfDictionary();
		dic.put(PdfName.CONTENTS, new PdfString(pdfSignature.getEncodedPKCS1()).setHexWriting(true));
		sap.close(dic); 
	    }catch(Exception e){
	    	Logger.toConsole(e);
	    }
	}
}
