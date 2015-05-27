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

package androidGLUESigner.ui.Fragments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import androidGLUESigner.helpers.SettingsHelper;
import androidGLUESigner.interfaces.IWizardEventListener;
import androidGLUESigner.models.SignatureInfo;
import androidGLUESigner.ui.R;

/**
 * the user data fragment
 * @author mario
 *
 */
@SuppressLint("ValidFragment")
public class WizardUserDataFragment extends WizardFragment {
	
	public WizardUserDataFragment(){
	
	}
	 public WizardUserDataFragment(SignatureInfo sigInfo) {
		super(sigInfo);
	}

	protected static final int SELECT_FILE = 100;
	private View view;
	private Button pictureButton;
	private String selectedImageUri;
	private TextView pictureTextView;
	private ArrayList<String> messagesList;
	private ListView messagesView;
	private EditText nameEditText;
	private EditText reasonEditText;
	private EditText locationEditText;
	private boolean attachToRoot;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// Inflate the layout for this fragment
		view = inflater.inflate(R.layout.fragment_wizard_ud, container,
				attachToRoot);
		messagesView = (ListView) view.findViewById(R.id.messagesListView);
		nameEditText = (EditText) view.findViewById(R.id.name_editText);
		reasonEditText = (EditText) view.findViewById(R.id.reason_editText);
		locationEditText = (EditText) view.findViewById(R.id.location_editText);

		pictureButton = (Button) view.findViewById(R.id.selectPicture_button);
		pictureTextView = (TextView) view.findViewById(R.id.picture_textView);
		pictureButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				selectPicture();
			}

			/**
			 * picture select dialog
			 */
			private void selectPicture() {
				Intent intent = new Intent(
						Intent.ACTION_PICK,
						android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				intent.setType("image/*");
				startActivityForResult(Intent.createChooser(
						intent,
						getActivity().getApplicationContext().getString(
								R.string.selected_file)), SELECT_FILE);
			}
		});
		return view;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == SELECT_FILE && data != null) {
            selectedImageUri = getRealPathFromURI(data.getData());
            pictureTextView.setText(getActivity().getApplicationContext().getString(R.string.change_picture));            
		}else{
			IWizardEventListener listener = (IWizardEventListener) getActivity();
			listener.setStepCancelled();
		}
	}
	 
    /**
     * validates the input given in the form for this fragment
     * @return if validated
     */
	public boolean validateAndSave(){
		boolean flag = true;
		messagesList = new ArrayList<String>();
		 
		String sigName = nameEditText.getText().toString();
		String sigReason = reasonEditText.getText().toString();
		String sigLocation = locationEditText.getText().toString();
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(InputMethodManager.HIDE_NOT_ALWAYS,0);

		if (sigName.length()==0){
		   flag = flag & false;
		   messagesList.add(getActivity().getString(R.string.siginfo_name_error));
		}else{
			this.getSignatureInfo().setSignatureName(sigName);
		}
		if (sigReason.length()==0){
			   flag = flag & false;
			   messagesList.add(getActivity().getString(R.string.siginfo_reason_error));
			}else{
				this.getSignatureInfo().setSignatureReason(sigReason);
			}
		if (sigLocation.length()==0){
			   flag = flag & false;
			   messagesList.add(getActivity().getString(R.string.siginfo_location_error));
			}else{
				this.getSignatureInfo().setSignatureLocation(sigLocation);
			}
		if(selectedImageUri!= null && this.getSignatureInfo().getImagePath() == null){
			this.getSignatureInfo().setImagePath(selectedImageUri);
			//flag = flag & false;
		}
		
		SettingsHelper helper = new SettingsHelper(getActivity());
		if (!flag){
			messagesView.setAdapter(new UserDataListAdapter(getActivity(),android.R.layout.simple_list_item_2,messagesList));
			messagesView.setVisibility(View.VISIBLE);
		}else{		
			helper.setFirstTime();
			messagesView.setVisibility(View.GONE);
		}
        if (getActivity() instanceof IWizardEventListener){		
		IWizardEventListener listener = (IWizardEventListener) getActivity();
		listener.updateDefaultSettings();
        }else{
        	helper.setSignatureInfo(getSignatureInfo());
        }	
		return flag;
	}
	
	/**
	 * translates URI into String path
	 * @param contentUri the URI
	 * @return the path as String
	 */
	private String getRealPathFromURI(Uri contentUri) {
	    String[] proj = { MediaStore.Images.Media.DATA };
	    CursorLoader loader = new CursorLoader(this.getActivity().getApplicationContext(), contentUri, proj, null, null, null);
	    Cursor cursor = loader.loadInBackground();
	    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
	    cursor.moveToFirst();
	    return cursor.getString(column_index);
	}
	protected class UserDataListAdapter extends ArrayAdapter<String> {

	    HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

	    public UserDataListAdapter(Context context, int textViewResourceId,
	        List<String> objects) {
	      super(context, textViewResourceId, objects);
	      for (int i = 0; i < objects.size(); ++i) {
	        mIdMap.put(objects.get(i), i);
	      }
	    }

	    @Override
	    public long getItemId(int position) {
	      String item = getItem(position);
	      return mIdMap.get(item);
	    }

	    @Override
	    public boolean hasStableIds() {
	      return true;
	    }
	
	   @Override
	   public View getView(int position, View convertView, ViewGroup parent) {
		   LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
	    	view = inflater.inflate(R.layout.validation_row, parent,false);
	    	TextView validationTextView = (TextView) view.findViewById(R.id.validation_text_row);
	    	validationTextView.setText(messagesList.get(position));
	    	return view;
	   }
	}
	
	/**
	 * Allows the fragment to be attatched to root layout upon creation
	 */
	public void setAttachToRoot() {
		this.attachToRoot = true;		
	}
}

