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

package androidGLUESigner.ui.settings;

import java.io.File;

import android.content.CursorLoader;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.provider.MediaStore;
import androidGLUESigner.ui.FileDialog;
import androidGLUESigner.ui.R;

/**
 * subclass of preference fragment with file chooser and picture funtionality
 * @author roland
 *
 */
public class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {
	
	private Preference imgPref;
	private Preference dirPref;
	protected static final int SELECT_FILE = 100;
	private String selectedImageUri;
    
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.settings);
        getActivity().setTitle(getString(R.string.settings_desc));
    }
    
    @Override
    public void onResume() {
      super.onResume();
      getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
      for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); ++i) {
        final Preference preference = getPreferenceScreen().getPreference(i);
        if (preference instanceof PreferenceGroup) {
          PreferenceGroup preferenceGroup = (PreferenceGroup) preference;
          for (int j = 0; j < preferenceGroup.getPreferenceCount(); ++j) {
            updatePreference(preferenceGroup.getPreference(j));
          }
        } else {
          updatePreference(preference);
        }
      }
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
     if (sharedPreferences != null && key != null)
      updatePreference(findPreference(key));
    }

    /**
     * updates the summary preview in the preferences panel
     * @param preference the preference
     */
    private void updatePreference( final Preference preference) {
      if (preference instanceof ListPreference) {
        ListPreference listPreference = (ListPreference) preference;
        listPreference.setSummary(listPreference.getEntry());
      }
      if (preference instanceof EditTextPreference) {
    	  EditTextPreference editTextPreference = (EditTextPreference) preference;
    	  editTextPreference.setSummary(editTextPreference.getText());
        }
      if(preference.getKey().equals("sig_out_dir")){
    	  preference.setSummary(getPreferenceScreen().getSharedPreferences().getString("sig_out_dir", ""));
      }
      if(preference.getKey().equals("sig_imgpath")){
    	  preference.setSummary(getPreferenceScreen().getSharedPreferences().getString("sig_imgpath", ""));
      }
    }
    @Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == SELECT_FILE && data != null) {
            selectedImageUri = getRealPathFromURI(data.getData());
			getPreferenceScreen().getSharedPreferences().edit().putString("sig_imgpath", selectedImageUri).commit();

            imgPref.setSummary(selectedImageUri);
            
		}
    }
    /**
     * translates a URI into a filesystem path
     * @param contentUri the uri
     * @return path as String
     */
    private String getRealPathFromURI(Uri contentUri) {
		    String[] proj = { MediaStore.Images.Media.DATA };
		    CursorLoader loader = new CursorLoader(this.getActivity().getApplicationContext(), contentUri, proj, null, null, null);
		    Cursor cursor = loader.loadInBackground();
		    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		    cursor.moveToFirst();
		    return cursor.getString(column_index);
		}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		 if (preference.getKey().equals("sig_imgpath")) {
	    	  
	    	  imgPref = preference;
	    	  Intent intent = new Intent( Intent.ACTION_PICK,
	                  android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
	          intent.setType("image/*");
	          startActivityForResult(
	                  Intent.createChooser(intent, getActivity().getApplicationContext().getString(R.string.selected_file)),
	                  SELECT_FILE);
	      }
		 if (preference.getKey().equals("sig_out_dir")) {
	    	  	dirPref = preference;
			 	File mPath = new File(Environment.getExternalStorageDirectory() + "//");
		        FileDialog fileDialog = new FileDialog(getActivity(), mPath, getActivity().getApplicationContext().getString(R.string.select_directory));
		        fileDialog.addDirectoryListener(new FileDialog.DirectorySelectedListener() {					
					@Override
					public void directorySelected(File directory) {
					  dirPref.setSummary(directory.getAbsolutePath());
					  getPreferenceScreen().getSharedPreferences().edit().putString("sig_out_dir", directory.getAbsolutePath()).commit();
					}
				});
		        fileDialog.setSelectDirectoryOption(true);
		        fileDialog.showDialog();
	        }
		 return super.onPreferenceTreeClick(preferenceScreen, preference);
	}
	
	/* (non-Javadoc)
	 * @see android.app.Fragment#onPause()
	 */
	@Override
	public void onPause() {
	super.onPause();
	 getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		
	}
}
