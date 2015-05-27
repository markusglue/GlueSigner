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

/**

 * Globally provides APDUs as byte[] constants

 *

 * @author Mario Bischof

 * @version 1.0

 */
public final class APDUCommands {
	//APDU: 00 A4 00 0C 02 3F 00
	public static final byte[] COMMAND1 = new byte[]{(byte)0x00, (byte)0xA4, (byte)0x00, (byte)0x0C,(byte) 0x02, (byte)0x3F, (byte)0x00};
	public static final String COMMAND1_STRING = "00 A4 00 0C 02 3F 00";
	
	//APDU 00 A4 01 00 02 1F FF 00
	public static final byte[] COMMAND2 = new byte[]{(byte)0x00, (byte)0xA4, (byte)0x01, (byte)0x00,(byte) 0x02, (byte)0x1F, (byte)0xFF, (byte)0x00};
	public static final String COMMAND2_STRING = "00 A4 01 00 02 1F FF 00";

	// APDU: 00 20 00 81 (06) ASCII_PIN (apdu to login to suisseid)
	public static final byte[] COMMANDHEADER_LOGIN = new byte[]{0x00, 0x20, 0x00, (byte) 0x81};
	public static final String COMMANDHEADER_LOGIN_STRING = "00 20 00 81";

    // APDU: 00 22 01 B6 03 83 01 02
	public static final byte[] COMMAND4 = new byte[]{(byte)0x00, (byte)0x22, (byte)0x01, (byte)0xB6,(byte) 0x03, (byte)0x83, (byte)0x01, (byte)0x02};
	public static final String COMMAND4_STRING = "00 22 01 B6 03 83 01 02";
	
	// APDU: 00 2A 9E 9A (33) OID HASHVALUE (apdu to sign a hashvalue)  
	public static final byte[] COMMANDHEADER_SIGNATURE = new byte[]{0x00, 0x2A, (byte) 0x9E, (byte) 0x9A, 0x33};
	public static final String COMMANDHEADER_SIGNATURE_STRING = "00 2A 9E 9A 33";
	
	// Object-ID of SHA256
	public static final byte[] SHA256_ALGORITHM_OBJECT_ID = 
			new byte[] {(byte) 0x30, (byte) 0x31,
	        (byte) 0x30, (byte) 0x0D, (byte) 0x06, (byte) 0x09,
	        (byte) 0x60, (byte) 0x86, (byte) 0x48, (byte) 0x01,
	        (byte) 0x65, (byte) 0x03, (byte) 0x04, (byte) 0x02,
	        (byte) 0x01, (byte) 0x05, (byte) 0x00, (byte) 0x04,
	        (byte) 0x20};
	public static final String SHA256_ALGORITHM_OBJECT_ID_STRING = "30 31 30 0D 06 09 60 86 48 01 65 03 04 02 01 05 00 04 20";
	
	// successful return code for an apdu
	public static final int APDU_STATE_SUCCESSFUL = 0x9000;
	
	// Location where the certificates can be found on the suisseid
	public static final byte[] APDU_SELECT_PATH_CERTIFICATES = { (byte) 0x3F,
		(byte) 0x00, (byte) 0x50, (byte) 0x15, (byte) 0x43, (byte) 0x04,
		(byte) 0x43, (byte) 0x01 };
	public static final String APDU_SELECT_PATH_CERTIFICATES_STRING = "3F 00 50 15 43 04 43 01";

	public static final byte[] APDU_CERTIFICATE_PATH_SELECTION = {
		(byte) 0x00, (byte) 0xA4, (byte) 0x08, (byte) 0x00,
		(byte) 0x3F, (byte) 0x00, (byte) 0x50, (byte) 0x15, 
		(byte) 0x43, (byte) 0x04, (byte) 0x43, (byte) 0x01, 
		(byte)0xFF};
	public static final String APDU_CERTIFICATE_PATH_SELECTION_STRING = "00 A4 08 00 3F 00 50 15 43 04 43 01 FF";
}
