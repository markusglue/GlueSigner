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
