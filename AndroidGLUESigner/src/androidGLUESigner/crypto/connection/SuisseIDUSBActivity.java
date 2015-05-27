package androidGLUESigner.crypto.connection;

/*
 * ReaderTestActivity.java 1.0, 12/08/11
 * 
 * Copyright (C) 2011 Advanced Card Systems Ltd. All Rights Reserved.
 * 
 * This software is the confidential and proprietary information of Advanced
 * Card Systems Ltd. ("Confidential Information").  You shall not disclose such
 * Confidential Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with ACS.
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidGLUESigner.crypto.specification.APDUCommands;
import androidGLUESigner.crypto.specification.SHA256Hasher;
import androidGLUESigner.exception.Logger;
import androidGLUESigner.helpers.NetworkHelper;
import androidGLUESigner.helpers.SettingsHelper;
import androidGLUESigner.helpers.UtilityHelper;
import androidGLUESigner.interfaces.IAPDUConnection;
import androidGLUESigner.models.SignatureInfo;
import androidGLUESigner.pdf.PDFSigner;
import androidGLUESigner.ui.DialogFragments.ICredentialListener;
import androidGLUESigner.ui.DialogFragments.SIDPassPhraseDialog;
import androidGLUESigner.ui.R;

import com.acs.smartcard.Features;
import com.acs.smartcard.Reader;
import com.acs.smartcard.Reader.OnStateChangeListener;
import com.acs.smartcard.ReaderException;

/**
 * Activity to connect to SuisseID USB reader, getting usb permissions
 * extraction of certificates and signing of hashvalue
 * 
 * @author Godfrey Chung
 * @editor Mario Bischof
 * @version 1.1, 6/05/14
 */
public class SuisseIDUSBActivity extends Activity implements IAPDUConnection,
		ICredentialListener {

	private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
	private static final int MAX_LINES = 25;
	private static final int SID_FINISHED = 4;

	private UsbManager mManager;
	private Reader mReader;
	private PendingIntent mPermissionIntent;
	private Features mFeatures = new Features();
	private boolean initialized = true;
	private String pin = null;
	private SettingsHelper settingsHelper;

	// UI elements
	private TextView mResponseTextView;
	private Button mOpenButton;
	private Button mCloseButton;
	private Button initUSBButton;
	private Menu menu;

	/**
	 * Broadcast Receiver for USB device permission
	 */
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (ACTION_USB_PERMISSION.equals(action)) {
				UsbDevice device = (UsbDevice) intent
						.getParcelableExtra(UsbManager.EXTRA_DEVICE);
				if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED,
						false)) {
					if (device != null) {
						// Open reader
						logMsg(context.getString(R.string.usb_opening_reader)
								+ device.getDeviceName() + "...");
						new OpenTask().execute(device);
					}
				} else {
					logMsg(context.getString(R.string.usb_permission_denied)
							+ device.getDeviceName());
					// Enable open button
					mOpenButton.setEnabled(true);
				}

			} else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
				UsbDevice device = (UsbDevice) intent
						.getParcelableExtra(UsbManager.EXTRA_DEVICE);
				mOpenButton.setEnabled(true);
				if (device != null && device.equals(mReader.getDevice())) {
					// Disable buttons
					mCloseButton.setEnabled(false);

					// Close reader
					logMsg(context.getString(R.string.usb_closing_reader));
					new CloseTask().execute();
				}
			} else if (UsbManager.ACTION_USB_ACCESSORY_ATTACHED.equals(action)) {
				mOpenButton.setEnabled(true);
			}
		}
	};

	/**
	 * Opens a device on the Reader, refreshes UI
	 * 
	 */
	private class OpenTask extends AsyncTask<UsbDevice, Void, Exception> {

		@Override
		protected Exception doInBackground(UsbDevice... params) {

			Exception result = null;
			try {
				if(params[0]!= null){
					mReader.open(params[0]);
				}
			} catch (Exception e) {
				result = e;
			}
			return result;
		}

		@Override
		protected void onPostExecute(Exception result) {
			// power up with a cold reset
			if (result != null) {
				logMsg(result.toString());
			} else {
				// Remove all control codes
				mFeatures.clear();

				// Enable buttons
				mCloseButton.setEnabled(true);
				initUSBButton.setEnabled(true);
			}
		}
	}

	/**
	 * Closes a device on the reader, refreshes UI
	 * 
	 */
	private class CloseTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			mReader.close();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			mOpenButton.setEnabled(true);
			initUSBButton.setEnabled(false);
		}
	}

	private class PowerParams {
		public int slotNum;
		public int action;
	}

	private class PowerResult {
		public byte[] atr;
		public Exception e;
	}

	private class SetProtocolParams {
		public int slotNum;
		public int preferredProtocols;
	}

	private class SetProtocolResult {
		public int activeProtocol;
		public Exception e;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_suisseid_usb);

		settingsHelper = new SettingsHelper(getApplicationContext());
		setTitle(getString(R.string.sid_act_title));

		// Get USB manager
		mManager = (UsbManager) getSystemService(Context.USB_SERVICE);

		// Initialize reader
		mReader = new Reader(mManager);
		mReader.setOnStateChangeListener(new OnStateChangeListener() {

			@Override
			public void onStateChange(int slotNum, int prevState, int currState) {
				if (prevState < Reader.CARD_UNKNOWN
						|| prevState > Reader.CARD_SPECIFIC) {
					prevState = Reader.CARD_UNKNOWN;
				}
				if (currState < Reader.CARD_UNKNOWN
						|| currState > Reader.CARD_SPECIFIC) {
					currState = Reader.CARD_UNKNOWN;
				}

				// Show output
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
					}
				});
			}
		});

		// Register receiver for USB permission
		mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(
				ACTION_USB_PERMISSION), 0);
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_USB_PERMISSION);
		filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
		registerReceiver(mReceiver, filter);

		// Initialize response text view
		mResponseTextView = (TextView) findViewById(R.id.main_text_view_response);
		mResponseTextView.setMovementMethod(new ScrollingMovementMethod());
		mResponseTextView.setMaxLines(MAX_LINES);
		mResponseTextView.setText("");

		// Initialize open button
		mOpenButton = (Button) findViewById(R.id.main_button_open);
		mOpenButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				boolean requested = false;

				// Disable open button
				mOpenButton.setEnabled(false);

				// attached device via OTG is always the last one
				int listSize = mManager.getDeviceList().values().toArray().length;
				UsbDevice device = (UsbDevice) mManager.getDeviceList()
						.values().toArray()[listSize-1];

				if (device != null) {
					// Request permission
					mManager.requestPermission(device, mPermissionIntent);
					requested = true;
				}
				if (!requested) {
					// Enable open button
					mOpenButton.setEnabled(true);
				}
			}
		});

		// Initialize close button
		mCloseButton = (Button) findViewById(R.id.main_button_close);
		mCloseButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Disable buttons
				mCloseButton.setEnabled(false);

				// Close reader
				logMsg(getApplicationContext().getString(
						R.string.usb_closing_reader));
				new CloseTask().execute();
			}
		});

		// init the init button
		initUSBButton = (Button) findViewById(R.id.initusbButton);
		initUSBButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					init();
					SIDPassPhraseDialog dialog = new SIDPassPhraseDialog(
							SuisseIDUSBActivity.this, getApplicationContext()
									.getString(R.string.usb_suisseid_pin));
					dialog.show(getFragmentManager(), "passphraseDialog");
				} catch (InterruptedException e) {
					Logger.toConsole(e);
				}
			}
		});

		// Disable buttons
		mCloseButton.setEnabled(false);
		initUSBButton.setEnabled(false);

		// Hide input window
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	}

	@Override
	protected void onDestroy() {

		// Close reader
		mReader.close();

		// Unregister receiver
		unregisterReceiver(mReceiver);
		initialized = false;
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.usbsid, menu);
		menu.findItem(R.id.action_next_sid).setEnabled(false);
		this.menu = menu;
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		/** Handle action bar item clicks here. The action bar will
		* automatically handle clicks on the Home/Up button, so long
		* as you specify a parent activity in AndroidManifest.xml.**/
		int id = item.getItemId();

		switch (id) {
		case R.id.action_next_sid:
			finish();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Logs the message to the UI textView
	 * 
	 * @param msg the message.
	 * 
	 */
	@Override
	public void logMsg(String msg) {
		DateFormat dateFormat = new SimpleDateFormat("[dd-MM-yyyy HH:mm:ss]: ");
		Date date = new Date();
		String oldMsg = mResponseTextView.getText().toString();
		mResponseTextView
				.setText(oldMsg + "\n" + dateFormat.format(date) + msg);

		if (mResponseTextView.getLineCount() > MAX_LINES) {
			mResponseTextView.scrollTo(0,
					(mResponseTextView.getLineCount() - MAX_LINES)
							* mResponseTextView.getLineHeight());
		}
	}

	/**
	 * Takes an input byte[] to be signed on the SID
	 * 
	 * @params toSign data to get signed on SuisseID
	 * @return byte[] with signed data
	 */
	public byte[] sign(byte[] toSign) {
		if (initialized) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			byte[] trimmedResponse = null;
			byte[] response = new byte[300];
			byte[] command5 = SHA256Hasher.calculateMessageDigest(toSign, true);

			// Transmit sign setup APDUs
			try {
				mReader.transmit(0, APDUCommands.COMMAND1,
						APDUCommands.COMMAND1.length, response, response.length);
				trimmedResponse = trim(response);
				
				if (trimmedResponse[trimmedResponse.length - 1] == -112) {

					mReader.transmit(0, APDUCommands.COMMAND2,
							APDUCommands.COMMAND2.length, response,
							response.length);

					// Check login
					if (authenticate(pin)) {

						// Transmit APDUs
						mReader.transmit(0, APDUCommands.COMMAND4,
								APDUCommands.COMMAND4.length, response,
								response.length);
						trimmedResponse = trim(response);
						if (trimmedResponse[trimmedResponse.length - 1] == -112) {

							// trasmit signing APDU
							mReader.transmit(0, command5, command5.length,
									response, response.length);
							// get response data and return it
							baos.write(response, 0, 256);
							
							logMsg(getApplicationContext().getString(
									R.string.usb_data_signed));
							Toast.makeText(this,getApplicationContext().getString(R.string.usb_data_signed),
							Toast.LENGTH_SHORT).show();
							return baos.toByteArray();
						}
					}
				}
			} catch (ReaderException e) {
				Logger.toConsole(e);
			} finally {
				try {
					if (baos != null) {
						baos.close();
					}
				} catch (IOException e) {
					Logger.toConsole(e);
				}
			}
		}
		return null;
	}

	/**
	 * Extracts the Qualified Signature from the SuisseID using USB Connection
	 * 
	 * @return X509Certificate of Qualified Signature on SuisseID
	 * 
	 */
	public Certificate[] getCertificateChain() throws ReaderException {
		if (initialized) {

			X509Certificate certificate = null;
			// prepare certificate chain, size 3 for qualified, CA and root CA
			Certificate[] certs = new X509Certificate[3];
			byte[] response = new byte[258];
			byte[] trimmedResponse = null;
			ByteArrayOutputStream baos = null;

			// check login
			if (authenticate(pin)) {
				try {
					CertificateFactory certificateFactory = CertificateFactory
							.getInstance("X.509");
					// position of qualified signature
					int position = 5;
					
					for (int i = 0; i < 3; i++) {
						position -= i;
						// APDU command for the path selection:
						// CLA = 0x00
						// INS = 0xA4: Select file
						// P1 = 0x08: Read
						// P2 = 0x00
						// DATA = Path
						// LE = 0xFF: Length
						baos = new ByteArrayOutputStream();
						byte[] commandSelectPath = new byte[] { 0x00,
								(byte) 0xa4, 0x08, 0x00, 0x08, (byte) 0x3f,
								(byte) 0x00, (byte) 0x50, (byte) 0x15,
								(byte) 0x43, (byte) 0x04, (byte) 0x43,
								(byte) position, (byte) 0xff };

						mReader.transmit(0, commandSelectPath,
								commandSelectPath.length, response,
								response.length);

						// trim trailing zeros
						trimmedResponse = trim(response);

						// check if return code was 9000 (-112 = 90)
						if (trimmedResponse[trimmedResponse.length - 1] == -112) {
							// offset to step further in reading 256byte blocks
							int offset = 0;
							do {
								// create byte[] for data response, 256 bytes
								// maxdata + 2 bytes return code
								response = new byte[258];

								// APDU command for the file reading:
								// CLA = 0x00
								// INS = 0xB0: Read binary
								// P1 = Offset
								// P2 = 0x00
								// LE = 0x100: Length
								byte[] commandReadFile = new byte[] { 0x00,
										(byte) 0xB0, (byte) offset++, 0x00,
										(byte) 0x100 };
								mReader.transmit(0, commandReadFile,
										commandReadFile.length, response,
										response.length);

								// trim trailing zeros
								trimmedResponse = trim(response);

								// write returned data into outputstream
								baos.write(response, 0,
										trimmedResponse.length - 1);
							} while (trimmedResponse[trimmedResponse.length - 1] == -112); 
							// as long as return code is ok
							
							// create certificate from read data
							certificate = (X509Certificate) certificateFactory
									.generateCertificate(new ByteArrayInputStream(
											baos.toByteArray()));
							certs[i] = certificate;

							logMsg(getApplicationContext().getString(
									R.string.usb_received_certificate)
									+ certificate.getSubjectDN());
						}
					}
				} catch (CertificateException e) {
					String errorMessage = getApplicationContext().getString(
							R.string.usb_error_pub_cert);
					throw new RuntimeException(errorMessage, e);
				} finally {
					try {
						if (baos != null) {
							baos.close();
						}
					} catch (IOException e) {
						Logger.toConsole(e);
					}
				}
				logMsg(getApplicationContext().getString(
						R.string.usb_chain_retrieved));
				return certs;
			}
		}
		return null;
	}

	/**
	 * removes trailing zeros from a byte[] and returns it
	 * 
	 * @param input
	 *            byte[]
	 * @return trimmed byte[]
	 */
	public static byte[] trim(byte[] bytes) {
		int i = bytes.length - 1;
		while (i >= 0 && bytes[i] == 0) {
			--i;
		}
		return Arrays.copyOf(bytes, i + 1);
	}

	/**
	 * @return returns if the connection has already been prepared for usage
	 */
	@Override
	public boolean isInitialized() {
		return initialized;
	}

	public boolean close() {
		mReader.close();
		return true;
	}

	public boolean open() {
		// For each USB-device
		UsbDevice device = (UsbDevice) mManager.getDeviceList().values()
				.toArray()[mManager.getDeviceList().values().toArray().length - 1];

		// If device name is found
		if (mReader.isSupported(device)) {
			// Request permission
			mManager.requestPermission(device, mPermissionIntent);
		}
		return true;
	}

	/**
	 * up of the SuisseID_USB connection
	 * 
	 * closes reader and unregisters the broadcastreceiver, reverts initialized
	 * attribute
	 * 
	 */
	@Override
	public void cleanup() {
		// Close reader
		mReader.close();
		// Unregister receiver
		unregisterReceiver(mReceiver);
		mReader = null;
		mManager = null;
		mPermissionIntent = null;
		initialized = false;
	}


	/**
	 * Authenticates to the SuisseID with a pin
	 * 
	 * @params pin the SuisseID pin
	 * @return true if login was successful
	 */
	@Override
	public boolean authenticate(String pin) throws ReaderException {
		if (initialized) {
			byte[] response = new byte[258];

			byte[] prepared_login = UtilityHelper
					.concateByteArrays(APDUCommands.COMMANDHEADER_LOGIN,
							new byte[] { (byte) UtilityHelper
									.stringToByteArray(pin).length });

			byte[] login = UtilityHelper.concateByteArrays(prepared_login,
					UtilityHelper.stringToByteArray(pin));

			mReader.transmit(0, login, login.length, response, response.length);

			if (trim(response)[trim(response).length - 1] == -112) {
				Toast.makeText(
						this,
						getApplicationContext().getString(
								R.string.usb_login_successful),
						Toast.LENGTH_SHORT).show();
				logMsg(getApplicationContext().getString(
						R.string.usb_login_successful));
				return true;
			} else {
				Toast.makeText(
						this,
						getApplicationContext().getString(
								R.string.usb_login_failed), Toast.LENGTH_SHORT)
						.show();
				logMsg(getApplicationContext().getString(
						R.string.usb_login_failed));
				return false;
			}
		}
		return false;
	}

	@Override
	public void init() throws InterruptedException {
		PowerParams paramsP = new PowerParams();
		// always slot 0 if there's only 1 device attached
		paramsP.slotNum = 0;
		// 2 = warm reset (1 = cold, 0 = power down)
		paramsP.action = 2;
		PowerResult resultP = new PowerResult();
		try {
			resultP.atr = mReader.power(paramsP.slotNum, paramsP.action);
		} catch (ReaderException e) {
			Logger.toConsole(e);

		}

		// Protocol = T1
		SetProtocolParams paramsS = new SetProtocolParams();
		paramsS.slotNum = 0;
		// paramsS.preferredProtocols = Reader.PROTOCOL_T1;
		paramsS.preferredProtocols = 2;
		SetProtocolResult resultS = new SetProtocolResult();
		try {
			resultS.activeProtocol = mReader.setProtocol(paramsS.slotNum,
					paramsS.preferredProtocols);
		} catch (ReaderException e) {
			Logger.toConsole(e);

		}
		initialized = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android_GLUESigner.ui.DialogFragments.ICredentialListener#setCredential
	 * (java.lang.String)
	 */
	@Override
	public void setCredential(String credential) {
		this.pin = credential;
		try {
			Intent intent = getIntent();
			SignatureInfo sigInfo = (SignatureInfo) intent
					.getSerializableExtra("model");
			// authenticate with PIN
			SuisseIDUSBActivity.this.authenticate(SuisseIDUSBActivity.this.pin);
			PDFSigner signer = new PDFSigner(SuisseIDUSBActivity.this,
					settingsHelper.getSigPath(), settingsHelper.getTSAUrl());
			// check if the device has got connection to the internet
			if (NetworkHelper.hasActiveInternetConnection(getApplicationContext())) {
				// if true, sign with timestamp
				File outputFile = signer.signPDF(sigInfo, true);
				Intent data = new Intent();
				data.putExtra("outputpath", outputFile.getAbsolutePath());
				setResult(SID_FINISHED, data);
				menu.findItem(R.id.action_next_sid).setEnabled(true);
			} else {
				// else skip timestamp and just use local time
				logMsg(getApplicationContext().getString(
						R.string.usb_no_internet));
				File outputFile = signer.signPDF(sigInfo, false);
				Intent data = new Intent();
				data.putExtra("outputpath", outputFile.getAbsolutePath());
				setResult(SID_FINISHED, data);
				menu.findItem(R.id.action_next_sid).setEnabled(true);
			}
		}catch (ReaderException e) {
			Logger.toConsole(e);
		} catch (Exception e) {
			Logger.toConsole(e);
		}
	}
}
