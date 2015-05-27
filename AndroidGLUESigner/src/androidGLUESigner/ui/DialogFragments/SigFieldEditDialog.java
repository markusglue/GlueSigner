/**
 * 
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
