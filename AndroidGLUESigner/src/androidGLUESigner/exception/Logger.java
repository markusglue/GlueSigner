package androidGLUESigner.exception;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
	static DateFormat dateFormat;
	static Date date;
	static String message;
	
	public static void toConsole(Throwable e){
		dateFormat = new SimpleDateFormat("[dd-MM-yyyy HH:mm:ss]: ");
		date = new Date();
		System.out.println(dateFormat.format(date) + e.getMessage());
		e.printStackTrace(System.out);
	}

	/**
	 * For custom error messages that dont come from exceptions
	 * @param errorMessage
	 */
	public static void printError(String errorMessage) {
		dateFormat = new SimpleDateFormat("[dd-MM-yyyy HH:mm:ss]: ");
		date = new Date();
		System.out.println(dateFormat.format(date) +errorMessage);

		
	}
}
