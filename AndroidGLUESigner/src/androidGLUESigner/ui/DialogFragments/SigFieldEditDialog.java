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

package androidGLUESigner.ui.DialogFragments;


import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import androidGLUESigner.models.SignatureInfo;
import androidGLUESigner.models.SignatureInfo.SignatureType;
import androidGLUESigner.ui.R;

/**
 * Modifies the signature field to deviate from defaults for the specific signature
 * @author roland 
 */
public class SigFieldEditDialog extends DialogFragment {

	private View view;
	private SignatureInfo signatureInfo;
	private EditText nameEditText;
	private EditText reasonEditText;
	private EditText locationEditText;
	private Builder oldDialog;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.DialogFragment#onCreateDialog(android.os.Bundle)
	 */
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    LayoutInflater inflater = getActivity().getLayoutInflater();
	    view = inflater.inflate(R.layout.dialog_sig_field_edit, null);
	    nameEditText = (EditText)view.findViewById(R.id.editingname_editText);
	    reasonEditText = (EditText)view.findViewById(R.id.editingreason_editText);
	    locationEditText = (EditText)view.findViewById(R.id.editinglocation_editText);
	    nameEditText.setText(signatureInfo.getSignatureName());
	    reasonEditText.setText(signatureInfo.getSignatureReason());
	    locationEditText.setText(signatureInfo.getSignatureLocation());;
	    builder.setView(view);
	    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String nameString = nameEditText.getText().toString();
				String reasonString = reasonEditText.getText().toString();
				String locationString = locationEditText.getText().toString();
			 if (nameString.length()>0&&reasonString.length()>0&&locationString.length()>0){
				 SigFieldEditDialog.this.signatureInfo.setSignatureName(nameString);
				 SigFieldEditDialog.this.signatureInfo.setSignatureLocation(locationString);
				 SigFieldEditDialog.this.signatureInfo.setSignatureReason(reasonString);
			 }
				if (signatureInfo.getSignatureType()==SignatureType.INVISIBLE)
					oldDialog.show();
			}
		});
	    
	    builder.setNegativeButton(getResources().getString(R.string.cancel_button), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
				
			}
		});
		return builder.create();
	}



	/**
	 * @return the signatureInfo
	 */
	public SignatureInfo getSignatureInfo() {
		return signatureInfo;
	}



	/**
	 * @param signatureInfo the signatureInfo to set
	 */
	public void setSignatureInfo(SignatureInfo signatureInfo) {
		this.signatureInfo = signatureInfo;
	}



	/**
	 * @param builder
	 */
	public void setOldDialog(Builder builder) {
	
		this.oldDialog = builder;
		
	}

}
