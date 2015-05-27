/**
 * 
 */
package androidGLUESigner.ui.DialogFragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import androidGLUESigner.pdf.ui.PDFViewerFragment;
import androidGLUESigner.ui.R;

/**
 * dialog for changing the page
 * @author roland
 *
 */
public class PDFPageDialog extends DialogFragment{

	private PDFViewerFragment parentFragment;
	private View view; 
	
	/* (non-Javadoc)
	 * @see android.app.DialogFragment#onCreateDialog(android.os.Bundle)
	 */
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    // Get the layout inflater
	    LayoutInflater inflater = getActivity().getLayoutInflater();
	   view =  inflater.inflate(R.layout.dialog_pagenumber, null);
		final EditText edPagenum = (EditText)view.findViewById(R.id.pagenum_edit);
		edPagenum.setText(Integer.toString(parentFragment.getmPage()));
		edPagenum.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (event == null || ( event.getAction() == 1)) {
				    // Hide the keyboard
				    InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
				    imm.hideSoftInputFromWindow(edPagenum.getWindowToken(), 0);
				}					    
				return true;
			}
		});
        builder.setTitle(getActivity().getApplicationContext().getString(R.string.jump_page))
            .setView(view)
            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
            		String strPagenum = edPagenum.getText().toString();
            		int pageNum = parentFragment.getmPage();
            		int oldpageNum = pageNum;
            		try {
            			pageNum = Integer.parseInt(strPagenum);
            		}
            		catch (NumberFormatException ignore) {}
            		if ((pageNum!=oldpageNum) && (pageNum>=1) && (pageNum <=parentFragment.getNumberOfPages())) {
            		parentFragment.renderPageNumber(pageNum);
            		}
                }
            })
            .setNegativeButton(getActivity().getApplicationContext().getString(R.string.cancel_button), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            })
            .create();

		return builder.create();
	}



	/**
	 * links PDFViewer with dialog
	 * @param parentFragment the parentFragment to set
	 */
	public void setParentFragment(PDFViewerFragment parentFragment) {
		this.parentFragment = parentFragment;
	}

}
