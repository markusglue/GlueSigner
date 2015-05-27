/**
 * 
 */
package androidGLUESigner.ui.DialogFragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import androidGLUESigner.pdf.ui.PDFViewerFragment;
import androidGLUESigner.ui.R;

/**
 * handles password protected pdf files, input for password
 * @author roland
 *
 */
public class PDFPasswordDialog extends DialogFragment {

	private View view;
	private PDFViewerFragment parentFragment;
	
	

	/* (non-Javadoc)
	 * @see android.app.DialogFragment#onCreateDialog(android.os.Bundle)
	 */
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		LayoutInflater inflater = getActivity().getLayoutInflater();
		view = inflater.inflate(R.layout.dialog_pdf_file_password,null);
		 AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		 builder.setView(view);
		final EditText etPW= (EditText) view.findViewById(R.id.etPassword);
		builder.setPositiveButton("OK",new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String password = etPW.getText().toString();
				if (password != null)
				parentFragment.setContent(password);
				
			}
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				
			}
		});
		return builder.create();
      
    }


	/**
	 * links the password dialog to the PDFViewer
	 * @param parentFragment the parentFragment to set
	 */
	public void setParentFragment(PDFViewerFragment parentFragment) {
		this.parentFragment = parentFragment;
	}

}
