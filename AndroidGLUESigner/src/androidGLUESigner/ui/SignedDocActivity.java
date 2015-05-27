/**
 * 
 */
package androidGLUESigner.ui;

import android.app.Activity;
import android.os.Bundle;
import androidGLUESigner.pdf.ui.PDFViewerFragment;
import androidGLUESigner.ui.Fragments.SignedDocListFragment;

/**
 * the activity for the signed document list.
 * Activity was made to accomodate later implementation of landscape feature.
 * @author roland
 
 */
public class SignedDocActivity extends Activity{

	/**
	 * 
	 */
	public SignedDocActivity() {
		// TODO Auto-generated constructor stub
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		if (savedInstanceState == null)
		setContentView(R.layout.activity_signeddoc);
		getFragmentManager().beginTransaction().add(R.id.sd_list_container,new SignedDocListFragment()).commit();
		setTitle(R.string.sdoc_list);
	}

	/**
	 * @param string 
	 * 
	 */
	public void launchReader(String path) {
		PDFViewerFragment fragment = new PDFViewerFragment();
		fragment.setPathForSignedPDF(path);
		setTitle(getString(R.string.preview_pdf));
		getFragmentManager().beginTransaction().replace(R.id.sd_list_container,fragment).addToBackStack(null).commit();
		
	}

}
