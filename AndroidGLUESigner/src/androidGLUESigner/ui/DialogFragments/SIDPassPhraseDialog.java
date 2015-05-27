/**
 * 
 */
package androidGLUESigner.ui.DialogFragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidGLUESigner.ui.R;

/**
 * specialised passphrase dialog for the suisse id connection variant
 * @author roland
 *
 */
@SuppressLint("ValidFragment")
public class SIDPassPhraseDialog  extends DialogFragment{

	private ICredentialListener listener;
	private View view;
	private String text;

	public SIDPassPhraseDialog(ICredentialListener listener,String text){
		this.listener =listener;
		this.text = text;
	}
		

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		    // Get the layout inflater
		    LayoutInflater inflater = getActivity().getLayoutInflater();

		    // Inflate and set the layout for the dialog
		    // Pass null as the parent view because its going in the dialog layout
		    view = inflater.inflate(R.layout.dialog_sid_passphrase, null);
		    builder.setView(view);
		    TextView passphraseTextView = (TextView) view.findViewById(R.id.textViewPassPhrase);
		    passphraseTextView.setText(this.text);
		    builder.setPositiveButton(getActivity().getApplicationContext().getString(R.string.sign_button), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				EditText passphraseEditText = (EditText) view.findViewById(R.id.sid_cert_passphrase);
			    listener.setCredential(passphraseEditText.getText().toString());
			    
			}
		    });
		    builder.setNegativeButton(getActivity().getApplicationContext().getString(R.string.cancel_button), new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) {
		            dialog.cancel();
		      } });
		    return builder.create();
	}
}
