package androidGLUESigner.helpers;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import androidGLUESigner.exception.Logger;

/**
 * Checks for internet availability by pinging www.google.com
 * used to decide if a signature should be created with a timestamp
 * or not.
 * 
 * @author mario
 *
 */
public final class NetworkHelper {

	/**
	 * pings google and waits for a reply
	 * @param context the application context
	 * @return true, if internet connection is active
	 */
	public static boolean hasActiveInternetConnection(Context context) {
	    if (isNetworkAvailable(context)) {
	        try {
	            HttpURLConnection urlc = (HttpURLConnection) (new URL("http://www.google.com").openConnection());
	            urlc.setRequestProperty("User-Agent", "Test");
	            urlc.setRequestProperty("Connection", "close");
	            urlc.setConnectTimeout(1500); 
	            urlc.connect();
	            return (urlc.getResponseCode() == 200);
	        } catch (IOException e) {
				Logger.toConsole(e);
	        }
	    } else {
			Logger.printError("Network not available");	    
		}
	    return false;
	}
	
	/**
	 * checks if the network of the device is up
	 * @param context the application context
	 * @return true if network is available
	 */
	public static boolean isNetworkAvailable(Context context) {
	    ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
}
