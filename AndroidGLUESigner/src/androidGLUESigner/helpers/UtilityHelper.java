package androidGLUESigner.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

import androidGLUESigner.exception.Logger;


/**
 * Utility class for conversions
 * 
 * @author Mario Bischof
 *
 */
public final class UtilityHelper {
	
	/**
     * Concatenates two byte arrays
     * 
     * @param a first byte[]
     * @param b second byte[] 
     * @return the concatenated byte[]
     */
	public static byte[] concateByteArrays(byte[] a, byte[] b){
		byte[] c = new byte[a.length + b.length];
		System.arraycopy(a, 0, c, 0, a.length);
		System.arraycopy(b, 0, c, a.length, b.length);
		return c;
	}
	
	/**
     * Converts a string to a byte array.
     * 
     * @param string the String           
     * @return the byte array.
     */
	public static byte[] stringToByteArray(String string){
		return(Charset.forName("ASCII").encode(CharBuffer.wrap(string)).array());
	}
	
    /**
     * Converts the byte array to HEX string.
     * 
     * @param buffer
     *            the buffer.
     * @return the HEX string.
     */
    public static String toHexString(byte[] buffer) {

        String bufferString = "";

        for (int i = 0; i < buffer.length; i++) {

            String hexChar = Integer.toHexString(buffer[i] & 0xFF);
            if (hexChar.length() == 1) {
                hexChar = "0" + hexChar;
            }

            bufferString += hexChar.toUpperCase() + " ";
        }

        return bufferString;
    }
    

    /**
     * Converts a HEX string to byte array
     * 
     * @param s the string.
     * @return the HEX byte[]
     */
    public static byte[] hexStringToByteArray(String s) {
	    int len = s.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
	                             + Character.digit(s.charAt(i+1), 16));
	    }
	    return data;
	}

    /**
     * Converts a byte array to HEX string
     * @param a the byte arry
     * @return stringbuilder object in HEX
     */
   public static String byteArrayToHexString(byte[] a){
		   StringBuilder sb = new StringBuilder();
		   for(byte b: a)
		      sb.append(String.format("%02x", b&0xff));
		   return sb.toString();
		}

   /**
    * Copies an InputStream to a OutputStream
    * @param in the input stream
    * @param out the output stream
    */
   public static void copyStream(InputStream in, OutputStream out) {
       byte[] buf = new byte[1024*1024];
       int len;
       try {
		while ((len = in.read(buf)) != -1) {
		       out.write(buf, 0, len);
		   }
	} catch (IOException e) {
		Logger.toConsole(e);
	}
   }
}
