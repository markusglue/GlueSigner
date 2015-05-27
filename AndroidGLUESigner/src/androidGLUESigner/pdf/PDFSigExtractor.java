/**
 * 
 */
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

package androidGLUESigner.pdf;

import java.io.IOException;
import java.util.ArrayList;

import android.graphics.Rect;
import androidGLUESigner.models.SignatureInfo;
import androidGLUESigner.models.SignatureInfo.SignatureType;

import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfReader;

/**
 * Extracts the signature field from a PDF file
 * @author roland
 *
 */
public class PDFSigExtractor {

	/**
	 * extracts the signature field for previewing.
	 * @throws IOException 
	 */
	public static ArrayList<SignatureInfo> getSignatureInfo (String inputPath){
            PdfReader reader;
			try {
				reader = new PdfReader(inputPath);
			} catch (IOException e) {
				return new ArrayList<SignatureInfo>();
			}
            AcroFields af = reader.getAcroFields();
        	ArrayList names = af.getSignatureNames();
        	ArrayList<SignatureInfo> signatures = new ArrayList<SignatureInfo>();
    		// For every signature :
    		for (int k = 0; k < names.size(); ++k) {
    		   String name = (String)names.get(k);
    		   SignatureInfo sigInfo = new SignatureInfo();
    		   // get coordinates
    		   float[] position = af.getFieldPositions(name);
    		   // page number
    		   float page = position[0];
    		   // left
    		   float llx = position[1];
    		   // bottom
    		   float lly = position[2];
    		   // right
    		   float urx = position[3];
    		   // top
    		   float ury = position[4];
    		   
    		   // get size of pdf page
    		   Rectangle size = reader.getPageSize((int) page);
    		   float height = size.getHeight();
    		   // subtract height to translate to Android canvas coordinate system
    		   lly = height-lly;
    		   ury = height-ury;
    		   float ulx = llx;
 
    		   // create a Rectangle from obtained signature field coordinates
    		   Rect sigRect = new Rect((int)ulx, (int)ury, (int)urx, (int)lly);
    		   sigInfo.setGraphicRect(sigRect,1.0f);
    		   // obtain additional information like reason, location, ...
    		   PdfDictionary sig = af.getSignatureDictionary(name);
    		   sigInfo.setSignatureName(sig.getAsString(PdfName.NAME).toString());
    		   sigInfo.setSignatureLocation(sig.getAsString(PdfName.LOCATION).toString());
    		   sigInfo.setSignatureReason(sig.getAsString(PdfName.REASON).toString());
    		   sigInfo.setSignatureType(SignatureType.NORMAL);
    		   sigInfo.setPageNumber((int) page);
    		   // add new signature information to signatures
    		   signatures.add(sigInfo);
    		}
    		return signatures;
	    }	    
}
