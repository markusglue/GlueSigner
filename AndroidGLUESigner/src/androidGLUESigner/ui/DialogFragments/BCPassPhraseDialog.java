/**
 * 
 */
package androidGLUESigner.ui.DialogFragments;

import com.sun.pdfview.Identity8BitCharsetEncoder;

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
 * enter the pin for bouncy castle connection variant
 * @author roland
 *
 */
@SuppressLint("ValidFragment")
public class BCPassPhraseDialog  extends DialogFragment{

	private ICredentialListener listener;
	private View view;
	private String text;


	public BCPassPhraseDialog(ICredentialListener listener,String text){
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
		    view = inflater.inflate(R.layout.dialog_passphrase_normal, null);
		    builder.setView(view);
		    TextView passphraseTextView = (TextView) view.findViewById(R.id.textViewPassPhrase);
		    passphraseTextView.setText(this.text);
		    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				EditText passphraseEditText = (EditText) view.findViewById(R.id.bc_cert_passphrase);
			    listener.setCredential(passphraseEditText.getText().toString());
			    
			}
		});

		    return builder.create();
	}

}
