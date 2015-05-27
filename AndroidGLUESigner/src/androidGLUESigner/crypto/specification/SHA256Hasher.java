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
