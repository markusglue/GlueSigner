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
