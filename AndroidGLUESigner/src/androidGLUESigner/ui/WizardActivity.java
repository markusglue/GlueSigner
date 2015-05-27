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

import java.io.File;

import java.util.ArrayList;
import java.util.Arrays;

import net.sf.andpdf.pdfviewer.PdfViewerActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import androidGLUESigner.crypto.connection.SuisseIDUSBActivity;
import androidGLUESigner.helpers.SettingsHelper;
import androidGLUESigner.helpers.SettingsHelper.Signer;
import androidGLUESigner.interfaces.IWizardEventListener;
import androidGLUESigner.models.SignatureInfo;
import androidGLUESigner.models.SignatureInfo.SignatureType;
import androidGLUESigner.pdf.ui.PDFViewerFragment;
import androidGLUESigner.ui.DialogFragments.SigFieldEditDialog;
import androidGLUESigner.ui.Fragments.WizardBCKeySignFragment;
import androidGLUESigner.ui.Fragments.WizardFragment;
import androidGLUESigner.ui.Fragments.WizardUserDataFragment;
import androidGLUESigner.ui.R;

/**
 * Activity to carry out the signing process
 * @author roland
 *
 */
public class WizardActivity extends Activity implements IWizardEventListener {

	/**
	 * Enum to describe each integer used in the currentStep variable
	 * @author roland
	 *
	 */
	public enum WizardStep {
		START, USERDATA, SIGPLACEMENT, CONVARIANT
	}

	
	private static final String TAGNAME = "lastWizardFragment";
	private static final int SID_FINISHED = 4;
	WizardFragment currentFragment;
	WizardStep currentStep;
	boolean isExternalStep = false;
	boolean pathselectedexternally = false;

	SignatureInfo model;
	public SettingsHelper settingsHelper = null;
	private MenuItem nextButton;
	//chosen method for signing
	protected Signer signMethod = Signer.SUISSEID_USB;
	//flag to keep track of cancelled dialogs
	private boolean stepWasCancelled;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		settingsHelper = new SettingsHelper(getApplicationContext());
		model = settingsHelper.getDefaultSignatureInfo();
		handleIntent();
		if (savedInstanceState == null) {
			
			setContentView(R.layout.activity_wizard);
			if (settingsHelper.getIsFirstTime()) {
				//first time set up
				currentStep = WizardStep.START;
			} 	else {
			currentStep = WizardStep.USERDATA;
			}
			LoadNextStep();
		} else {
			model = (SignatureInfo) savedInstanceState
					.getSerializable("wizardmodel");
			currentStep = WizardStep.values()[savedInstanceState
					.getInt("fragmentstep")];
			currentFragment = (WizardFragment) getFragmentManager().findFragmentById(R.id.container);
		}
	
		

	}
	
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		outState.putSerializable("wizardmodel", model);
		outState.putInt("fragmentstep", currentStep.ordinal());
	}

	/**
	 * 
	 */
	private void handleIntent() {
		Intent intent = getIntent();
		if (intent.getAction() =="android.intent.action.SEND") {
			this.pathselectedexternally = true;
			Uri uri = (Uri) intent.getExtras().getParcelable(Intent.EXTRA_STREAM);
			String filename = uri.getPath();
		    model.setInputFilePath(filename);
		}else if (intent.getAction()=="android.intent.action.VIEW"){
			this.pathselectedexternally = true;
			String path = intent.getData().getPath();
			model.setInputFilePath(path);
		}

	}

	/**
	 * sets the necessary coditions before loading every step
	 */
	private void setupStep(WizardStep step) {
		switch (step) {
		case SIGPLACEMENT:
			isExternalStep = true;
			// nextButton.setEnabled(false);
			setTitle(getApplicationContext().getString(R.string.signature_placement));
			sigRectMethodDialog();
			break;
		case USERDATA:
			currentFragment = new WizardUserDataFragment(model);
			setTitle(getApplicationContext().getString(R.string.first_time_setup));
			break;
		case CONVARIANT:
			isExternalStep = true;
			nextButton.setVisible(false);
			sigMethodDialog();
			break;
		default:
			break;

		}
	}

	
	/**
	 * dialog for choosing signature method
	 */
	private void sigMethodDialog() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		String[] methods = { getApplicationContext().getString(R.string.sig_p12),
				getApplicationContext().getString(R.string.sig_sid) };
		builder.setTitle(getApplicationContext().getString(R.string.sigmethodtitle));
		builder.setSingleChoiceItems(methods, 1,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						signMethod = Signer.values()[which];

					}
				});

		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

				switch (signMethod) {
				case BOUNCYCASTLE_EXT:
					currentFragment = new WizardBCKeySignFragment(model);
					getFragmentManager().beginTransaction()
							.replace(R.id.container, currentFragment).commit();
					break;
				case SUISSEID_USB:
					Intent intent = new Intent(WizardActivity.this,
							SuisseIDUSBActivity.class);
					intent.putExtra("model", model);
					startActivityForResult(intent, 0);
					break;
				default:
					break;

				}
			}
		});
		builder.setNegativeButton(getApplicationContext().getString(R.string.cancel_button),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						stepWasCancelled = true;
						nextButton.setVisible(true);
						}
				});
		if (model.getSignatureType()==SignatureType.INVISIBLE){
			builder.setNeutralButton(getString(R.string.sig_field_edit), new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					SigFieldEditDialog sigdialog = new SigFieldEditDialog();
					sigdialog.setSignatureInfo(model);
					sigdialog.setOldDialog(builder);
					sigdialog.show(getFragmentManager(), null);
					
				}
			});
		}
		builder.show();

	}

	private void sigRectMethodDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		ArrayList<String> methodlist = new ArrayList<String>();
		methodlist.add(getApplicationContext().getString(R.string.invisible));
		methodlist.add(getApplicationContext().getString(R.string.visible));
		if (model.getImagePath() !=  null){
			methodlist.add(getApplicationContext().getString(R.string.withpicture));
		}
		 	
		builder.setTitle(getApplicationContext().getString(R.string.saptitle));
		builder.setSingleChoiceItems(methodlist.toArray(new String[methodlist.size()]), 1,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						model.setSignatureType(SignatureType.values()[which]);

					}
				});

		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
					choosePDFFile();
			}
		});
		builder.setNegativeButton(getApplicationContext().getString(R.string.cancel_button),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						if (currentStep == WizardStep.USERDATA){
						setStepCancelled();
						}else{
							finish();
						}

						
					}
				});
		builder.show();
	}
	
	/**
	 * Depending on whether the signature type is visible, either the viewer is launched or the step after the viewer.
	 */

	private void LoadStepforSignatureType() {
		if (model.getSignatureType() != SignatureType.INVISIBLE){
		launchPDFReader();
		}else{
			model.setSignatureRect(new Rect());
			currentStep = WizardStep.SIGPLACEMENT;
			LoadNextStep(); 
		}
	}
	
	/**
	 * File dialog for choosing which pdf to open
	 */
	private void choosePDFFile() {
		if (!pathselectedexternally || model.getInputFilePath() == null){
		FileDialog fileDialog = new FileDialog(this, new File(
				Environment.getExternalStorageDirectory().toString()), getApplicationContext().getString(R.string.select_pdf_file));
		fileDialog.setFileEndsWith(".pdf");
		fileDialog.addFileListener(new FileDialog.FileSelectedListener() {

			@Override
			public void fileSelected(File file) {
				model.setInputFilePath(file.getAbsolutePath());
				LoadStepforSignatureType();
			}
		
		});
		fileDialog.showDialog();
	}else{
		pathselectedexternally = false;
		LoadStepforSignatureType();
	}
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.wizard, menu);
		nextButton = menu.getItem(0);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_next && currentFragment.validateAndSave()) {
		
			LoadNextStep();

		}

		return super.onOptionsItemSelected(item);
	}

	/**
	 * Loads next Step in the Wizard
	 */
	private void LoadNextStep() {
		if (!stepWasCancelled) {
			currentStep = WizardStep.values()[currentStep.ordinal() + 1];
		}else{
			stepWasCancelled = false;
		}
			setupStep(currentStep);
			//if step is not external (requires increment of current step
			if (!isExternalStep) {
				getFragmentManager().beginTransaction()
						.replace(R.id.container, currentFragment,TAGNAME).commit();
			}
		

	}

	/**
	 * callback from IWizardEventListener
	 */
	@Override
	public void SignatureComplete(String outputPath) {
		Log.d("deb", outputPath);
		settingsHelper.addSignedDocumentToList(outputPath);
		PDFViewerFragment fragment = new PDFViewerFragment();
		fragment.setPathForSignedPDF(outputPath);
		setTitle(getString(R.string.preview_pdf));
		getFragmentManager().beginTransaction().replace(R.id.container, fragment,TAGNAME).commit();

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		switch(resultCode){
		//suisse id activity  finished, calls the callback
		case SID_FINISHED: 
		 String path = data.getStringExtra("outputpath");
		 this.SignatureComplete(path);
		break;
		default:
			this.setNextButtonEnabled(true);
			break;
		}
	}



	/*
	 * (non-Javadoc)
	 * 
	 * @see android_GLUESigner.ui.WizardFragments.IWizardEventListener#
	 * updateDefaultSettings()
	 */
	@Override
	public void updateDefaultSettings() {
		settingsHelper.setSignatureInfo(model);

	}

	/**
 * lauches the pdf viewer fragment
 */
	private void launchPDFReader() {
		currentFragment = new PDFViewerFragment();
		currentFragment.setSignatureInfo(model);
		getFragmentManager().beginTransaction()
				.replace(R.id.container, currentFragment,TAGNAME).commit();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android_GLUESigner.ui.WizardFragments.IWizardEventListener#
	 * setNextButtonEnabled(boolean)
	 */
	@Override
	public void setNextButtonEnabled(boolean enabled) {
		if (nextButton != null)
		nextButton.setEnabled(enabled);

	}



	/* (non-Javadoc)
	 * @see android_GLUESigner.ui.Fragments.IWizardEventListener#setStepCancelled()
	 */
	@Override
	public void setStepCancelled() {
	  this.stepWasCancelled = true;
		
	}
}