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

package androidGLUESigner.ui.Fragments;

import android.app.Fragment;
import androidGLUESigner.models.SignatureInfo;

/**
 * fragment of the wizard
 * @author roland
 *
 */
public abstract class WizardFragment  extends Fragment{

	private SignatureInfo signatureInfo;
  
	public  WizardFragment(){
		
	}

	/**
	 * Class for conformity in each Wizard Step
	 */
	public WizardFragment(SignatureInfo sigInfo) {
		this.setSignatureInfo(sigInfo);
	}
	
	/**
	 * sets the signature information 
	 * @param model the model holding the signature info
	 */
	public void setSignatureInfo(SignatureInfo model) {
		if (model != null) {
			this.signatureInfo = model;		
		}
	}
	public abstract boolean validateAndSave();

	/**
	 * @return the signatureInfo
	 */
	public SignatureInfo getSignatureInfo() {
		return signatureInfo;
	}
}
