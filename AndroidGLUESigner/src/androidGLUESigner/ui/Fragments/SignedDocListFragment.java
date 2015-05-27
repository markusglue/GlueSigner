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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import androidGLUESigner.helpers.SettingsHelper;
import androidGLUESigner.ui.R;
import androidGLUESigner.ui.SignedDocActivity;

/**
 * fragment of the document list
 * @author roland
 *
 */
public class SignedDocListFragment extends Fragment {

private View view;
private SettingsHelper settingsHelper;
private ImageButton deleteButton;

private SdocListAdapter adapter;
private ArrayList<String> list;

public SignedDocListFragment(){
	
}

/* (non-Javadoc)
 * @see android.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
 */
@Override
public View onCreateView(LayoutInflater inflater, ViewGroup container,
		Bundle savedInstanceState) {
	this.settingsHelper = new SettingsHelper(getActivity());
	this.view = inflater.inflate(R.layout.fragment_sdoclist, container,false);
	 final ListView listview = (ListView) view.findViewById(R.id.sdocListView);
	      list = settingsHelper.getSignedDocList();
	     adapter = new SdocListAdapter(getActivity(),
	        android.R.layout.simple_list_item_1, list);
	    listview.setAdapter(adapter);
	    listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				SignedDocActivity activity = (SignedDocActivity) getActivity();
				activity.launchReader(list.get(position));
				
			}
		});
	       listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, final View view,
					int position, long id) {
				  final String item = (String) parent.getItemAtPosition(position);
			      showDeleteButton(item,list);
			      
				return true;
			}
		});
	    this.deleteButton = (ImageButton) view.findViewById(R.id.sigListDeleteButton);
	return view;
}

/**
 * @param item
 * @param list 
 */
protected void showDeleteButton(final String item, final ArrayList<String> list) {
	if (list.size() == 0){
		deleteButton.setVisibility(View.INVISIBLE);
	}else{
	deleteButton.setVisibility(Button.VISIBLE);
	}
	deleteButton.setOnClickListener(new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			list.remove(item);
			adapter.notifyDataSetChanged();
			settingsHelper.removeSignedDocumentFromList(item);
			File file = new File(item);
			file.delete();
				deleteButton.setVisibility(View.INVISIBLE);
		}
	});
	
}
protected class SdocListAdapter extends ArrayAdapter<String> {

    HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

    public SdocListAdapter(Context context, int textViewResourceId,
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
    
    
    /* (non-Javadoc)
     * @see android.widget.ArrayAdapter#getView(int, android.view.View, android.view.ViewGroup)
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
    	LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
    	view = inflater.inflate(R.layout.sdoc_row, parent,false);
    	TextView nameView = (TextView) view.findViewById(R.id.rowNameTextView);
    	TextView pathView = (TextView) view.findViewById(R.id.rowPathTextView);
    	TextView sizeView = (TextView) view.findViewById(R.id.rowSizeTextView);
    	String filename =list.get(position);
    	nameView.setText (FilenameUtils.getBaseName(filename) + "."+ FilenameUtils.getExtension(filename));
    	pathView.setText(filename);
    	File file = new File(filename);
    	double bytes = file.length();
		double kilobytes = (bytes / 1024);
		double megabytes = (kilobytes / 1024);
    	sizeView.setText("Size: " + String.format("%.2g", megabytes) + " MB");
    	return view;
    }

  }

} 


