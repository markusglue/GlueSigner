package androidGLUESigner.helpers;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;

public class FilePatchHelper {

  private static boolean matches(MappedByteBuffer bb, byte[] sought, int pos) {
    for (int j = 0; j < sought.length; ++j) {
      if (sought[j] != bb.get(pos + j)) {
        return false;
      }
    }
    return true;
  }

  private static void replace(MappedByteBuffer bb, byte[] sought, byte[] replacement, int pos) {
    for (int j = 0; j < sought.length; ++j) {
      byte b = (j < replacement.length) ? replacement[j] : (byte) ' ';
      bb.put(pos + j, b);
    }
  }

  private static void searchAndReplace(MappedByteBuffer bb, byte[] sought, byte[] replacement, int sz) {
    int replacementsCount = 0;
    for (int pos = 0; pos <= sz - sought.length; ++pos) {
      if (matches(bb, sought, pos)) {
        replace(bb, sought, replacement, pos);
        pos += sought.length - 1;
        ++replacementsCount;
      }
    }
    System.out.println("" + replacementsCount + " replacements done.");
  }

  // Search for occurrences of the input pattern in the given file
  private static void patch(File f, byte[] sought, byte[] replacement) throws
      IOException {
    // Open the file and then get a channel from the stream
    RandomAccessFile raf = new RandomAccessFile(f, "rw"); // "rws", "rwd"
    
    try{
        FileChannel fc = raf.getChannel();
        // Get the file's size and then map it into memory
        int sz = (int) fc.size();
        MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_WRITE, 0, sz);
        searchAndReplace(bb, sought, replacement, sz);
        bb.force(); // Write back to file, like "flush()"
    }finally{
        // Close the channel and the stream
        raf.close();
    }
  }

  public static void replace(String filename, byte[] sought, byte[] replacement)
      throws Exception {
    if (sought.length != replacement.length) {
      // Better build-in some support for padding with blanks.
      throw new Exception("sought length must match replacement length");
    }
    
    System.out.println( "looking for " + sought.length + " bytes in '" + filename + "'" );
    // log.debug( "looking for " + sought.length + " bytes in '" + filename + "'" );
    File f = new File(filename);
    patch(f, sought, replacement);
  }
}
