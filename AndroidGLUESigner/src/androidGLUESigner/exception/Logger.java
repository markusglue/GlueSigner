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
