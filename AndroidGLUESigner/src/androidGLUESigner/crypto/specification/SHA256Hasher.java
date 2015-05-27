package androidGLUESigner.crypto.specification;

import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;

import androidGLUESigner.exception.Logger;

/**

 * Globally accessible Hashingclass. Digests an

 * input byte[] with SHA256. If required, writes  
 
 * AlgorithmOID and digest into the retval
 
 * to be used for signing on the SID. 

 *

 * @author Mario Bischof

 * @version 1.0

 */
public final class SHA256Hasher {
	/**
	 * Returns a SHA256 digested byte[], if withAPDUs is set to true, it will
	 * also store the AODU COMMAND HEADER for signing and the OID of SHA256 into
	 * the returned byte[]
	 *
	 * @param  message the byte[] containing the message to be digested
	 * @param  asAPDUs the boolean flag to enable preparations for APDU
	 * @return SHA256 digester message, with COMMANDHEADER and OID if asAPDU was set
	 *
	 */
	public static byte[] calculateMessageDigest(byte[] message, boolean asAPDU) {
		try {
			// get the digester and do SHA256 with message
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
			messageDigest.update(message);
			byte[] digest = messageDigest.digest();
			// do APDU specific additions
			if(asAPDU){
				ByteArrayOutputStream hash = new ByteArrayOutputStream();
				hash.write(APDUCommands.COMMANDHEADER_SIGNATURE);
				hash.write(APDUCommands.SHA256_ALGORITHM_OBJECT_ID);
				hash.write(digest);
				hash.write(256);
				return hash.toByteArray();
			}
			// if not used as APDU, just return the SHA256 digest
			else{
				return digest;
			}
		} catch (Exception e) {
			String errorMessage = "The calculation of the message digest failed.";
			Logger.printError(errorMessage);
		}
		return null;
	}
}
