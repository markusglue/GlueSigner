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
