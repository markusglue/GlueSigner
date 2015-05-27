/**
 * 
 */
package androidGLUESigner.ui.Fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidGLUESigner.ui.MainActivity;
import androidGLUESigner.ui.R;

/**
 * fragment of the start screen
 * @author roland
 *
 */
public class MainFragment  extends Fragment {

	/* (non-Javadoc)
	 * @see android.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_main, container, false);
		MainActivity activity = (MainActivity) getActivity();
		activity.setTitle("GLUESigner");
		
		
		return view;
	}
	
/* (non-Javadoc)
 * @see android.app.Fragment#onStart()
 */

}
