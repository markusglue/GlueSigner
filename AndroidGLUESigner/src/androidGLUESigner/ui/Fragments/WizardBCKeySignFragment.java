package androidGLUESigner.ui.Fragments;

import java.io.File;
import java.security.KeyStore;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidGLUESigner.crypto.connection.BouncyCastleExternal;
import androidGLUESigner.exception.Logger;
import androidGLUESigner.helpers.KeyStoreHelper;
import androidGLUESigner.helpers.SettingsHelper;
import androidGLUESigner.interfaces.IWizardEventListener;
import androidGLUESigner.models.SignatureInfo;
import androidGLUESigner.pdf.PDFSigner;
import androidGLUESigner.ui.FileDialog;
import androidGLUESigner.ui.R;
import androidGLUESigner.ui.WizardActivity;
import androidGLUESigner.ui.DialogFragments.BCPassPhraseDialog;
import androidGLUESigner.ui.DialogFragments.ICredentialListener;

/**
 * the bouncy castle wizard fragment
 * @author roland
 *
 */
@SuppressLint("ValidFragment")
public class WizardBCKeySignFragment extends WizardFragment implements ICredentialListener{
	private View view;
	private String certificateAlias;
	private File importedCertificateFile;
	private String importedPassPhrase;
	private Button chooseCertificateButton;
	private Button signButton;
	private WizardActivity activity; 
	private KeyStore ks = null;

	public WizardBCKeySignFragment() {
	}

	public WizardBCKeySignFragment(SignatureInfo sigInfo) {
		super(sigInfo);		
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
	 
	
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_wizard_bc_cert, container, false);
        activity = (WizardActivity) getActivity();
		ks = KeyStoreHelper.getApplicationKeyStore(activity.settingsHelper.getBasePath());
        chooseCertificateButton = (Button) view.findViewById(R.id.certChooseButton);
        chooseCertificateButton.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
			buildAndShowImportDialog();				
			}
		});
        signButton = (Button) view.findViewById(R.id.certSignButton); 
        signButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {				
				signPDF();				
			}
		});
        return view;
	
	}

	/**
	 * signs a PDF file
	 */
	protected void signPDF() {
		SettingsHelper settingsHelper = new SettingsHelper(getActivity().getApplicationContext());
	BouncyCastleExternal be = new BouncyCastleExternal(settingsHelper.getBasePath(), getSignatureInfo().getCredential());
	try {
		be.init();
		PDFSigner signer = new PDFSigner(be, settingsHelper.getSigPath(), settingsHelper.getTSAUrl());
		File outputFile = signer.signPDF(getSignatureInfo(), false);
		IWizardEventListener listener = (IWizardEventListener) getActivity();
		listener.SignatureComplete(outputFile.getAbsolutePath());
		be.cleanup();
	} catch (Exception e) {
		Logger.toConsole(e);
	}	
	}

	/**
	 * show the import dialog for a pkcs12 file
	 */
	protected void buildAndShowImportDialog() {
		ArrayList<String> items = KeyStoreHelper.getAliases(ks);
		items.add(getActivity().getApplicationContext().getString(R.string.bc_import));
		final String[] itemsarr = items.toArray(new String[items.size()]);
		final int itemlength = itemsarr.length;
	
		  AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		    builder.setTitle(getActivity().getApplicationContext().getString(R.string.bc_choose_cert));
		    builder.setItems(itemsarr, new DialogInterface.OnClickListener() {
		               public void onClick(DialogInterface dialog, int which) {
		              if (which != itemlength-1){
		            	  certificateAlias = itemsarr[which];
		            	  getSignatureInfo().setCredential(certificateAlias);
		            		signButton.setEnabled(true);
		                	chooseCertificateButton.setEnabled(false);
		              }else{
		            	  importCertificate();
		              }	            
		              }	           
		    });
		    AlertDialog dialog = builder.create();
		    dialog.show();		
	}

	/**
	 * imports a pkcs12 cert from a file
	 */
	protected void importCertificate() {
		File mPath = new File(Environment.getExternalStorageDirectory() + "//");
        FileDialog fileDialog = new FileDialog(getActivity(), mPath, getString(R.string.bc_choose_cert_file));
        fileDialog.setFileEndsWith(".p12");
        fileDialog.addFileListener(new FileDialog.FileSelectedListener() {
            public void fileSelected(File selectedFile) {
            	importedCertificateFile = selectedFile;
            	showPassPhraseDialog();
            }
        });
        fileDialog.showDialog();	
	}

	/**
	 * shows the password dialog
	 * @param passphrase
	 */
	protected void showPassPhraseDialog() {	
		BCPassPhraseDialog dialog = new BCPassPhraseDialog(this,getActivity().getApplicationContext().getString(R.string.bc_cert_pass));
		dialog.show(getFragmentManager(), getTag());
	}

	/* (non-Javadoc)
	 * @see android_GLUESigner.ui.WizardFragments.WizardFragment#validateAndSave()
	 */
	@Override
	public boolean validateAndSave() {
	  //todo openPDF;
		return true;
	}

	/* (non-Javadoc)
	 * @see android_GLUESigner.ui.DialogFragments.ICredentialListener#setCredential(java.lang.String)
	 */
	@Override
	public void setCredential(String credential) {
		importedPassPhrase = credential;
		certificateAlias = KeyStoreHelper.importCertificate(importedCertificateFile, ks, importedPassPhrase).get(0);
		KeyStoreHelper.saveApplicationKeyStore(ks,activity.settingsHelper.getBasePath());
		getSignatureInfo().setCredential(certificateAlias);
    	signButton.setEnabled(true);
    	chooseCertificateButton.setEnabled(false);
		
	}

}
