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

package androidGLUESigner.models;

import java.io.Serializable;

import android.graphics.Rect;

/**

 * SignatureInfo provides information

 * for each individual Signature

 *

 * @author Mario Bischof

 * @version 1.0

 */
public class SignatureInfo implements Serializable {
	
	private static final long serialVersionUID = 8421381324244789591L;
	
	/**
	 * the way the signature should be displayed in the viewer
	 * @author mario
	 *
	 */
	public enum SignatureType{
		INVISIBLE,
		NORMAL,
		PICTURE
	}
	// coordinates for the signature appearance and graph rect
	private int top,left,right,bottom;
	private int gtop,gleft,gright,gbottom;
	// page on which the siganture is set
	private int pageNumber = 1;
	// signature information
	private String signatureReason;
	private String signatureLocation;
	private String signatureName;
	// path to the file to be signed
	private String inputFilePath;
	// the PIN oder password for the IConnection object
	private String credential;
	// path to an image if set
	private String imagePath;
	// path to the url of the timestamp service
	private String tsaUrl;
	// type of signature (invisible, visible (normal), with image)
	private SignatureType signatureType = SignatureType.NORMAL;
	
	/**
	 * gets the page number of the signature
	 * @return page number
	 */
	public int getPageNumber() {
		return pageNumber;
	}
	/**
	 * set the page number of the signature
	 * @param the page number
	 */
	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}
	/**
	 * gets the dimension rectangle (bounding box) of the signature 
	 * @return Rect of signature
	 */
	public Rect getSignatureRect() {		
		return new Rect(left,top,right,bottom);
	}
	/**
	 * sets the dimension (bounding box) rectangle of the signature
	 * @param signatureRect the Rect to be set
	 */
	public void setSignatureRect(Rect signatureRect) {
		this.top = signatureRect.top;
		this.bottom = signatureRect.bottom;
		this.right = signatureRect.right;
		this.left = signatureRect.left;
	}
	
	/**
	 * get the reason for signing
	 * @return signature reason
	 */
	public String getSignatureReason() {
		return signatureReason;
	}
	
	/**
	 * set the reason for signing
	 */
	public void setSignatureReason(String signatureReason) {
		this.signatureReason = signatureReason;
	}
	
	/**
	 * get the location for signing
	 * @return signature location
	 */
	public String getSignatureLocation() {
		return signatureLocation;
	}
	
	/**
	 * set the location for signing
	 */
	public void setSignatureLocation(String signatureLocation) {
		this.signatureLocation = signatureLocation;
	}
	
	/**
	 * get the name for signing
	 * @return signature name
	 */
	public String getSignatureName() {
		return signatureName;
	}
	
	/**
	 * set the name for signing
	 */
	public void setSignatureName(String signatureName) {
		this.signatureName = signatureName;
	}
	
	/**
	 * get the pin/password for signing
	 * @return the connection pin/password
	 */
	public String getCredential() {
		return credential;
	}
	
	/**
	 * set the pin/password for signing
	 */
	public void setCredential(String credential) {
		this.credential = credential;
	}
	
	/**
	 * get the path to the signature image
	 * @return signature image path
	 */
	public String getImagePath() {
		return imagePath;
	}
	
	/**
	 * gest the path to the signature image
	 */
	public void setImagePath(String selectedImageUri) {
		this.imagePath = selectedImageUri;
	}
	/**
	 * @return the inputFilePath
	 */
	public String getInputFilePath() {
		return inputFilePath;
	}
	/**
	 * @param inputFilePath the inputFilePath to set
	 */
	public void setInputFilePath(String inputFilePath) {
		this.inputFilePath = inputFilePath;
	}
	
	/**
	 * get the url for the timestamp service
	 * @return string of TS url
	 */
	public String getTsaUrl() {
		return tsaUrl;
	}
	
	/**
	 * set the url for the timestamp service
	 * @param  tsaUrl the timestamp url
	 */
	public void setTsaUrl(String tsaUrl) {
		this.tsaUrl = tsaUrl;
	}
	
	/**
	 * set the signature type 
	 * @param signatureType the signature type
	 */
	public void setSignatureType(SignatureType signatureType) {
		this.signatureType = signatureType;
		
	}
	
	/**
	 * gets the signature type
	 * @return the signature type
	 */
	public SignatureType getSignatureType(){
		return signatureType;
		
	}
	
	/**
	 * sets the signature rectangle for the Android coordinate system
	 * @param rect the rectangle
	 * @param mZoom the zoom factor
	 */
	public void setGraphicRect(Rect rect, float mZoom) {
		gtop = (int) (rect.top/mZoom);
		gbottom = (int) (rect.bottom/mZoom);
		gright = (int) (rect.right/mZoom);
		gleft = (int) (rect.left/mZoom);
	}
	
	/**
	 * gets the signature rectangle for the Android coordinate system
	 * @return the signature rectangle
	 */
	public Rect getGraphicRect(){
		return new Rect(gleft, gtop, gright, gbottom);
	}
}
