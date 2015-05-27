package androidGLUESigner.ui;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager.OnBackStackChangedListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import androidGLUESigner.helpers.SettingsHelper;
import androidGLUESigner.models.SignatureInfo;
import androidGLUESigner.ui.R;
import androidGLUESigner.ui.Fragments.MainFragment;
import androidGLUESigner.ui.Fragments.WizardUserDataFragment;
import androidGLUESigner.ui.settings.SettingsFragment;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;

/**

 * MainActivity

 * the main launcher for this application.

 * @author Mario Bischof

 * @version 1.0

 */
public class MainActivity extends Activity {
	
	
	private SignatureInfo model;
   private MenuItem[] items;
   private Fragment fragment;
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		PreferenceManager.setDefaultValues(this, R.layout.settings, true);
		setContentView(R.layout.activity_main);
		getFragmentManager().addOnBackStackChangedListener(new OnBackStackChangedListener() {
			
			@Override
			public void onBackStackChanged() {
				 Fragment currentFragment = getFragmentManager().findFragmentById(R.id.mainContainer);
				 if (currentFragment instanceof SettingsFragment){
					 for (int i = 0; i<4;i++){
						 items[i].setVisible(false);
					 }
					 }else if (currentFragment instanceof WizardUserDataFragment){
						 items[3].setVisible(true);
					 }else{
					 for (int i = 0;i<3;i++) items[i].setVisible(true);
					 items[3].setVisible(false);
				 }
				 
				
			}
		});
		
		SettingsHelper helper = new SettingsHelper(getApplicationContext());
		//helper.setNotFirstTime();
		// for web access
		if (android.os.Build.VERSION.SDK_INT > 9) {
		      StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		      StrictMode.setThreadPolicy(policy);
		    }
		if (savedInstanceState == null){
		getFragmentManager().beginTransaction().add(R.id.mainContainer, new MainFragment()).commit();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		items = new MenuItem[4];
		for (int i = 0;i<4;i++){
			items[i] = menu.getItem(i);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		
		
		switch (id) {
		case R.id.action_wizard:
			Intent intent = new Intent(MainActivity.this,WizardActivity.class);
			MainActivity.this.startActivity(intent);
			break;
		case R.id.action_documents:
			Intent signedDocIntent = new Intent(getApplicationContext(),SignedDocActivity.class);
			startActivity(signedDocIntent);
			break;
		case R.id.action_settings:
			SettingsHelper helper = new SettingsHelper(getApplicationContext());
		  if (helper.getIsFirstTime()){
			  this.model = new SignatureInfo();
		fragment = new WizardUserDataFragment(model);
		 for (int i = 0; i<3;i++){
			 items[i].setVisible(false);
		 }
		 items[3].setVisible(true);
		  }else{
			  fragment = new SettingsFragment();
			 
		  }
		  
		  getFragmentManager().beginTransaction().replace(R.id.mainContainer, fragment).addToBackStack(null).commit();
		break;
		case R.id.action_done:
		WizardUserDataFragment wFragment = (WizardUserDataFragment) fragment;
		if(wFragment.validateAndSave()){
		getFragmentManager().popBackStack();
		fragment = new SettingsFragment();
		getFragmentManager().beginTransaction().replace(R.id.mainContainer, fragment).addToBackStack(null).commit();
		 for (int i=0;i<2;i++) items[i].setVisible(true);
		 items[3].setVisible(false);
		}
		break;
		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}
	
	
}
	

	