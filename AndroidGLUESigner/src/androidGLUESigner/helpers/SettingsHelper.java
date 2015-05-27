package androidGLUESigner.helpers;

import java.util.ArrayList;
import java.util.HashSet;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.preference.PreferenceManager;
import androidGLUESigner.exception.Logger;
import androidGLUESigner.models.SignatureInfo;
import androidGLUESigner.ui.R;

/**
 * Manipulates Android shared preferences
 * @author roland
 *
 */
public class SettingsHelper {

	public enum Signer {
	    BOUNCYCASTLE_EXT,SUISSEID_USB ,SUISSEID_WEB,SUISSE_ID_BT
	}
	private SharedPreferences sharedPref;
	private Context context;

	public SettingsHelper(Context context) {
		sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		this.context = context;
	}
	
	/** 
	 * @return the default application directory
	 */
	public String getBasePath(){
		String basepath = sharedPref.getString("pref_out_dir", "/PDF");
		basepath = Environment.getExternalStorageDirectory() + basepath;
		return basepath;
		
	}
	
	/**
	 * @return path where signed documents are stored
	 */
	public String getSigPath(){
		String sigpath = sharedPref.getString("sig_out_dir", "");
		return sigpath;		
	}
	
	/**
	 * @return url of the timestamp service
	 */
	public String getTSAUrl(){
		String tsaUrl = sharedPref.getString("sig_tsaurl", "http://tsa.swisssign.net/");
		return tsaUrl;
	}
	
	/**
	 * @return variant to use for signing
	 */
	public Signer getConnectionVariant(){
		int variant = sharedPref.getInt("pref_variant",Signer.SUISSEID_USB.ordinal());
		return Signer.values()[variant];
	}
	
	/**
	 * @return if first time setup
	 */
	public boolean getIsFirstTime(){
		boolean value = sharedPref.getBoolean("pref_first_time", true);
		return value; 
		
	}
	/**
	 * called when first time setup is done
	 */
	public void setFirstTime(){
		 sharedPref.edit().remove("pref_first_time").apply();
		 sharedPref.edit().putBoolean("pref_first_time", false).apply();		
	}
	
	/**
	 * Stores the default settings for a signature
	 * @param siginfo the SingatureInfo object holding to be stored
	 */
	public void setSignatureInfo(SignatureInfo siginfo){
		Editor edit = sharedPref.edit();
		edit.putString("sig_name", siginfo.getSignatureName());
		edit.putString("sig_reason", siginfo.getSignatureReason());
		edit.putString("sig_location", siginfo.getSignatureLocation());
		edit.putString("sig_imgpath", siginfo.getImagePath());
		edit.apply();
	}
	
	/**
	 * contructs a SignatureInfo object from shared preferences
	 * @return siganture info from the shared preferences 
	 */
	public SignatureInfo getDefaultSignatureInfo(){
		SignatureInfo sigInfo = new SignatureInfo();
		sigInfo.setSignatureName(sharedPref.getString("sig_name", ""));
		sigInfo.setSignatureReason(sharedPref.getString("sig_reason",""));
		sigInfo.setSignatureLocation(sharedPref.getString("sig_location", ""));
		sigInfo.setImagePath(sharedPref.getString("sig_imgpath", null ));
		// set default timestamp server, can be changed in settings later
		sigInfo.setTsaUrl(sharedPref.getString("sig_tsaurl", context.getString(R.string.suissesign_tsa)));
		return sigInfo;
	}
	
	/**
	 * Gets the list of signed documents
	 * @return a list of signed documents
	 */
	public ArrayList<String> getSignedDocList(){
		String docString =sharedPref.getString("signedDocs",null);
		ArrayList<String> urls = new ArrayList<String>();
		if (docString != null) {
	        try {
	            JSONArray a = new JSONArray(docString);
	            for (int i = 0; i < a.length(); i++) {
	                String url = a.optString(i);
	                urls.add(url);
	            }
	        } catch (JSONException e) {
	            Logger.toConsole(e);
	        }
	    }
	    return urls;
	}
	
	/**
	 * adds a new file to the signed document list
	 * @param path to the file
	 */
	public void addSignedDocumentToList (String path){
		
		ArrayList<String> values = getSignedDocList();
		if (!values.contains(path)){
		values.add(path);
		}
		saveNewDocList(values, sharedPref.edit());
		
	}

	/**
	 * Saves the changed document list into sharedprefereces
	 * @param values the new values to be saved
	 * @param editor default shared preferences editor.
	 */
	private void saveNewDocList(ArrayList<String> values, Editor editor) {
		JSONArray a = new JSONArray();
	    for (int i = 0; i < values.size(); i++) {
	        a.put(values.get(i));
	    }
	    if (!values.isEmpty()) {
	        editor.putString("signedDocs", a.toString());
	    } else {
	        editor.putString("signedDocs", null);
	    }
	    editor.apply();
	}
	
	/**
	 * removes a file from the signed document list
	 * @param path to the file
	 */
	public void removeSignedDocumentFromList (String path){
		 ArrayList<String> values = getSignedDocList();
		values.remove(path);
		saveNewDocList(values, sharedPref.edit());
		
	}

	/**
	 * sets first time to false(for debugging purposes)
	 */
	public void setNotFirstTime() {
		sharedPref.edit().putBoolean("pref_first_time",true).apply();
		
	}
}
