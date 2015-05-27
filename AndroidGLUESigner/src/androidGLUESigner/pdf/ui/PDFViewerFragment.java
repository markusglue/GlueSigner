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

package androidGLUESigner.pdf.ui;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import net.sf.andpdf.nio.ByteBuffer;
import net.sf.andpdf.refs.HardReference;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidGLUESigner.exception.Logger;
import androidGLUESigner.interfaces.IWizardEventListener;
import androidGLUESigner.models.SignatureInfo;
import androidGLUESigner.pdf.PDFSigExtractor;
import androidGLUESigner.pdf.ui.DragRectView.ZoomEvent;
import androidGLUESigner.pdf.ui.memory.BitmapPool;
import androidGLUESigner.ui.DialogFragments.PDFPageDialog;
import androidGLUESigner.ui.DialogFragments.PDFPasswordDialog;
import androidGLUESigner.ui.DialogFragments.SigFieldEditDialog;
import androidGLUESigner.ui.Fragments.WizardFragment;
import androidGLUESigner.ui.R;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFImage;
import com.sun.pdfview.PDFPage;
import com.sun.pdfview.PDFPaint;
import com.sun.pdfview.decrypt.PDFAuthenticationFailureException;
import com.sun.pdfview.decrypt.PDFPassword;
import com.sun.pdfview.font.PDFFont;

/**
 * A fragment for viewing a PDF document
 * @author roland
 * 
 */
public class PDFViewerFragment extends WizardFragment {


	private static final int STARTPAGE = 1;
	private static final float MIN_ZOOM = 1.0f;
	private static final float MAX_ZOOM = 3.375f;
	private static final float ZOOM_INCREMENT = 1.5f;

	private static final String TAG = "PDFVIEWER";

	public static final String EXTRA_PDFFILENAME = "net.sf.andpdf.extra.PDFFILENAME";
	public static final String EXTRA_SHOWIMAGES = "net.sf.andpdf.extra.SHOWIMAGES";
	public static final String EXTRA_ANTIALIAS = "net.sf.andpdf.extra.ANTIALIAS";
	public static final String EXTRA_USEFONTSUBSTITUTION = "net.sf.andpdf.extra.USEFONTSUBSTITUTION";
	public static final String EXTRA_KEEPCACHES = "net.sf.andpdf.extra.KEEPCACHES";

	public static final boolean DEFAULTSHOWIMAGES = true;
	public static final boolean DEFAULTANTIALIAS = true;
	public static final boolean DEFAULTUSEFONTSUBSTITUTION = false;
	public static final boolean DEFAULTKEEPCACHES = false;

	private GraphView mOldGraphView;
	private GraphView mGraphView;
	private String pdffilename;
	private PDFFile mPdfFile;
	private int mPage = STARTPAGE;
	private int sigPage = 1;
	private BitmapPool mBitmapPool;
	private float mZoom;
	private ProgressDialog progress;
	
	private PDFPage mPdfPage;

	private Thread backgroundThread;
	private Handler uiHandler;

	private ImageButton bZoomIn;
	private ImageButton bZoomOut;
	private RelativeLayout relativelayout;
	private Context context;
	private ImageButton bLeft;
	private ImageButton bRight;
	private TextView mPageTextView;
	private ImageButton bSig;
	private TextView tvPageNum;
	private View view;
	private boolean previewMode;
	private ImageButton bShare;
	
	// toggle signature selection button
	private boolean sigButton = false;
	private ImageButton bEdit;

	public PDFViewerFragment() {
		mZoom = MIN_ZOOM;
		this.mBitmapPool = new BitmapPool(4);
	}

	/**
	 * sets file name of pdf
	 * @param pdfFileName the file name of the pdf
	 */
	public void setPDFFileName(String pdfFileName) {
		this.pdffilename = pdfFileName;
	}

	/**
	 * gets the current page
	 * @return current page number
	 */
	public int getmPage() {
		return mPage;
	}

	/**
	 * gets the current zoomfactor
	 * @param inverse get the inversed zoom factor
	 * @return the zoom factor
	 */
	public float getZoomFactor(boolean inverse) {
		float adjustedMZoom = mZoom;
		if (mZoom < MIN_ZOOM) {
			adjustedMZoom = MIN_ZOOM;
		} else if (mZoom > MAX_ZOOM) {
			adjustedMZoom = MAX_ZOOM;
		}
		if (inverse) {
			return 1 / adjustedMZoom;
		} else {
			return adjustedMZoom;
		}
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Fragment#onCreateView(android.view.LayoutInflater,
	 * android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		if (savedInstanceState != null) {
			this.pdffilename = savedInstanceState.getString("pdffilename");
			this.setSignatureInfo((SignatureInfo) savedInstanceState
					.getSerializable("sigInfo"));
			this.mZoom = savedInstanceState.getFloat("zoom");
			this.mPage = savedInstanceState.getInt("page");
		}
	
		if (view == null){
		 view = inflater.inflate(R.layout.fragment_pdfviewer, container,
				false);
		}else{
			  ((ViewGroup)view.getParent()).removeView(view);
		}
		relativelayout = (RelativeLayout) view.findViewById(R.id.pdfviewer_mainlayout);
		context = getActivity();
		setupOnClickEvents(view);
		mPageTextView = (TextView) view.findViewById(R.id.pdfpagenum);
		uiHandler = new Handler();
		if (mOldGraphView != null) {
			mGraphView = new GraphView(relativelayout.getContext());
			mGraphView.mBi = mOldGraphView.mBi;
			setupExisitingDragRects();
			mOldGraphView = null;
			mGraphView.mImageView.setImageBitmap(mGraphView.mBi);
			updatePageStatus();
			setupGraphView();
		} else {
			mGraphView = new GraphView(relativelayout.getContext());
			setupExisitingDragRects();
			PDFImage.sShowImages = DEFAULTSHOWIMAGES;
			PDFPaint.s_doAntiAlias = DEFAULTANTIALIAS;
			PDFFont.sUseFontSubstitution = DEFAULTUSEFONTSUBSTITUTION;
			HardReference.sKeepCaches = DEFAULTKEEPCACHES;            
			setContent(null);
		}
		return view;
	}
	
	/**
	 * instantiate drag rect views for existing signatures
	 */
	private void setupExisitingDragRects() {
		ArrayList<SignatureInfo> sigInfos;
		
			sigInfos = PDFSigExtractor.getSignatureInfo(pdffilename);
			
			mGraphView.createDragRectViews(sigInfos,this,previewMode);
	
	}

	/**
	 * defines the sharing intent
	 * @throws URISyntaxException 
	 */	
	private Intent createShareIntent() throws URISyntaxException{
		Intent shareIntent = new Intent();
		shareIntent.setAction(Intent.ACTION_SEND);
		shareIntent.putExtra(Intent.EXTRA_STREAM,Uri.fromFile(new File(pdffilename)));
		shareIntent.setType("application/pdf");
		return shareIntent;
	}
	
	
	
	/**
	 * setup buttons
	 * @param view the current view
	 */
	private void setupOnClickEvents(View view) {
		bZoomIn = (ImageButton) view.findViewById(R.id.pdfzoomin);
		bZoomOut = (ImageButton) view.findViewById(R.id.pdfzoomout);
		bLeft = (ImageButton) view.findViewById(R.id.pdfleftarrow);
		bRight = (ImageButton) view.findViewById(R.id.pdfrightarrow);
		bSig = (ImageButton) view.findViewById(R.id.pdfsignature);
		tvPageNum = (TextView) view.findViewById(R.id.pdfpagenum);
		bShare = (ImageButton)view.findViewById(R.id.shareButton);
		bEdit = (ImageButton)view.findViewById(R.id.sigfieldeditbutton);
		bZoomIn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				zoomIn();

			}
		});

		bZoomOut.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				zoomOut();

			}
		});

		bLeft.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				prevPage();

			}
		});

		bRight.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				nextPage();

			}
		});

		bSig.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mGraphView.toggleScrollViewMode();
				// toggle button image
				 if(sigButton){ 
			            bSig.setImageResource(R.drawable.select_rect_button);
			        }else{
			            bSig.setImageResource(R.drawable.clear_rect_button);
			        }
				 sigButton = !sigButton;  
			}
		});
		
		tvPageNum.setOnClickListener(new View.OnClickListener() {		
			@Override
			public void onClick(View v) {
				PDFPageDialog pageNumDialog = new PDFPageDialog();
				pageNumDialog.setParentFragment(PDFViewerFragment.this);
				pageNumDialog.show(getFragmentManager(), getTag());
				
			}
		});
	
		bShare.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {	
				try {
					Intent shareIntent = createShareIntent();
					 List<Intent> targetedShareIntents = new ArrayList<Intent>();
					  List<ResolveInfo> resInfo = getActivity().get/*
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

packageManager().queryIntentActivities(shareIntent, 0);
					  for (ResolveInfo info : resInfo){
						  Intent targettedShare  = createShareIntent();
						  // removes GLUESigner from the share list
						  if (!info.activityInfo./*
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

packageName.contains("GLUESigner")){
							  targettedShare.set/*
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

package(info.activityInfo./*
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

packageName);
							  targetedShareIntents.add(targettedShare);
						  }
					  }
					 Intent chooserIntent = Intent.createChooser(targetedShareIntents.remove(0), getActivity().getApplicationContext().getString(R.string.send_pdf));
					 chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
				                targetedShareIntents.toArray(new Parcelable[] {}));
					startActivity(chooserIntent);
				} catch (URISyntaxException e) {
					Logger.toConsole(e);;				}
				
			}
		});
		bEdit.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
			   SigFieldEditDialog dialog = new SigFieldEditDialog();
			   dialog.setSignatureInfo(getSignatureInfo());
			   dialog.show(getFragmentManager(), null);
			}
		});
		if (previewMode){
			// if in previewMode hide signature button
			bSig.setVisibility(view.GONE);
			bShare.setVisibility(View.VISIBLE);
			bEdit.setVisibility(View.GONE);
		}else{
			bEdit.setVisibility(View.VISIBLE);
			
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (getActivity() instanceof IWizardEventListener){
			IWizardEventListener listener = (IWizardEventListener) getActivity();
			listener.setNextButtonEnabled(false);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Fragment#onSaveInstanceState(android.os.Bundle)
	 */
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString("pdffilename", this.pdffilename);
		if (getSignatureInfo() != null)
			outState.putSerializable("sigInfo", getSignatureInfo());
		outState.putFloat("zoom", mZoom);
		outState.putInt("page", mPage);
	}

	/* (non-Javadoc)
	 * @see android_GLUESigner.ui.WizardFragments.WizardFragment#setSignatureInfo(android_GLUESigner.models.SignatureInfo)
	 */
	@Override
	public void setSignatureInfo(SignatureInfo model) {
		super.setSignatureInfo(model);
		this.pdffilename = model.getInputFilePath();
	}
	
	/**
	 * sets the filename for the viewer and turns on preview mode
	 * @param path the path of the file
	 */
	public void setPathForSignedPDF(String path){
		this.setPDFFileName(path);
		this.previewMode=true;
	}
	
	/**
	 * setup graphview
	 */
	private void setupGraphView() {
		RelativeLayout.LayoutParams rpWrap1 = new RelativeLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		rpWrap1.topMargin = 100;
		mGraphView.setLayoutParams(rpWrap1);
		relativelayout.addView(mGraphView);
	}

	/**
	 * loads the first page at startup
	 * @param password the password required to view the pdf
	 * 
	 */
	public void setContent(String password) {
		try {
			parsePDF(pdffilename, password);
			setupGraphView();
			progress = ProgressDialog.show(context, getActivity().getApplicationContext().getString(R.string.loading),
					getActivity().getApplicationContext().getString(R.string.pdf_loading));
			startRenderThread(mPage, mZoom);
		} catch (PDFAuthenticationFailureException e) {
			PDFPasswordDialog dialog = new PDFPasswordDialog();
			dialog.setParentFragment(this);
			dialog.show(getChildFragmentManager(), getTag());
			Logger.toConsole(e);
		}
	}

	/**
	 * rendering thread for pdf page image
	 * @param page page number
	 * @param zoom zoom factor
	 */
	private synchronized void startRenderThread(final int page, final float zoom) {
		if (backgroundThread != null)
			return;
		backgroundThread = new Thread(new Runnable() {
			public void run() {
				try {
					if (mPdfFile != null) {	
						showPage(page, zoom);
					}
				} catch (Exception e) {
					Logger.toConsole(e);
				}
				backgroundThread = null;
			}
		});
		updateImageStatus();
		backgroundThread.start();
	}

	/**
	 * sets the image view bitmap to newly rendered result
	 * @param page page number
	 * @param zoom zoom factor
	 * @throws Exception
	 */
	private void showPage(int page, float zoom) throws Exception {
		try {
			// free memory from previous page
			mGraphView.setPageBitmap(null);
			mGraphView.updateImage();

			// Only load the page if it's a different page (i.e. not just
			// changing the zoom level)
			if (mPdfPage == null || mPdfPage.getPageNumber() != page) {
				mPdfPage = mPdfFile.getPage(page, true);
			}

			float width = mPdfPage.getWidth();
			float height = mPdfPage.getHeight();

			RectF clip = null;
			
			updateImageView(zoom, width, height, clip);
			mGraphView.updateImage();
			mGraphView.updateDragRect(width * zoom, height * zoom,page);

			if (progress != null)
				progress.dismiss();
			uiHandler.post(new Runnable() {
				
				@Override
				public void run() {
					mGraphView.requestLayout();					
				}
			});
	
		} catch (Throwable e) {
			Logger.toConsole(e);
		}
	}

	/**
	 * sets the bitmap object in the graph view, gets image from cache if already exists
	 * @param zoom zoom factor
	 * @param width page width
	 * @param height page height
	 * @param clip clipping rect
	 */
	private void updateImageView(float zoom, float width, float height,
			RectF clip) {
		Bitmap bi;
		bi = mBitmapPool.getBitmap((int) (width * zoom), (int) (height * zoom));
		if (bi == null) {
			mBitmapPool.addBitmap(bi);
			bi = mPdfPage.getImage((int) (width * zoom), (int) (height * zoom),
					clip, true, true);
		}		mGraphView.setPageBitmap(bi); 
	}

	/**
	 * updates image status
	 */
	private void updateImageStatus() {

		if (backgroundThread == null) {
			updatePageStatus();
			return;
		}
		updatePageStatus();
		mGraphView.postDelayed(new Runnable() {
			public void run() {
				updateImageStatus();
			}
		}, 1000);
	}

	/**
	 * updates page status
	 */
	private void updatePageStatus() {
		if (mPdfFile != null && mPageTextView != null) {
			mPageTextView.setText(mPage + "/" + mPdfFile.getNumPages());
		}
	}

	/**
	 * starts the render thread and notifies drag rect views of zoom event
	 * @param event the zoom event
	 * 
	 */
	private void renderZoomedPage(ZoomEvent event) {
		mGraphView.setZoomEventOccured(event, mZoom);
		startRenderThread(mPage, mZoom);
	}

	/**
	 * zoom in to a page
	 */
	private void zoomIn() {
		if (mPdfFile != null) {
			if (mZoom < MAX_ZOOM) {
				mZoom *= ZOOM_INCREMENT;
				if (mZoom > MAX_ZOOM)
					mZoom = MAX_ZOOM;
				if (mZoom >= MAX_ZOOM) {
					bZoomIn.setEnabled(false);
				} else {
					bZoomIn.setEnabled(true);
					bZoomOut.setEnabled(true);
				}
				renderZoomedPage(ZoomEvent.IN);
			}
		}
	}

	/**
	 * zoom out from a page
	 */
	private void zoomOut() {
		if (mPdfFile != null) {
			if (mZoom > MIN_ZOOM) {
				mZoom /= ZOOM_INCREMENT;
				if (mZoom < MIN_ZOOM)
					mZoom = MIN_ZOOM;
				if (mZoom <= MIN_ZOOM) {
					Log.d(TAG, "Disabling zoom out button");
					bZoomOut.setEnabled(false);
				} else
					bZoomOut.setEnabled(true);
				
				bZoomIn.setEnabled(true);
				renderZoomedPage(ZoomEvent.OUT);
			}
		}
	}

	/**
	 * renders next page
	 */
	private void nextPage() {
		if (mPdfFile != null) {
			if (mPage < mPdfFile.getNumPages()) {
				mPage += 1;
				bZoomOut.setEnabled(true);
				bZoomIn.setEnabled(true);
				progress = ProgressDialog.show(context, getActivity().getApplicationContext().getString(R.string.loading),
						getActivity().getApplicationContext().getString(R.string.pdf_loading) + mPage, true, true);
				mBitmapPool.clear();
				startRenderThread(mPage, mZoom);
				mGraphView.setDragRectVisibility(mPage);
			}
		}
	}

	/**
	 * renders previous page
	 */
	private void prevPage() {
		if (mPdfFile != null) {
			if (mPage > 1) {
				mPage -= 1;
				bZoomOut.setEnabled(true);
				bZoomIn.setEnabled(true);
				progress = ProgressDialog.show(context, getActivity().getApplicationContext().getString(R.string.loading),
						getActivity().getApplicationContext().getString(R.string.pdf_loading) + mPage, true, true);
				mBitmapPool.clear();
				startRenderThread(mPage, mZoom);
				mGraphView.setDragRectVisibility(mPage);
			}
		}
	}

	/**
	 * creates a pdf file object from a file
	 * @param filename the filename
	 * @param password password for the pdf file
	 * @throws PDFAuthenticationFailureException
	 */
	private void parsePDF(String filename, String password)
			throws PDFAuthenticationFailureException {
		try {
			File f = new File(filename);
			long len = f.length();
			if (len == 0) {
			} else {
				openFile(f, password);
			}
		} catch (PDFAuthenticationFailureException e) {
			Logger.toConsole(e);
		} catch (Throwable e) {
			Logger.toConsole(e);
		}

	}

	/**
	 * Open a specific pdf file. Creates a DocumentInfo from the file, and opens
	 * that.
	 * Note: Mapping the file locks the file until the PDFFile is closed.
	 * 
	 * @param file the file to open
	 * @throws IOException
	 */
	public void openFile(File file, String password) throws IOException {
		// first open the file for random access
		RandomAccessFile raf = new RandomAccessFile(file, "r");

		// extract a file channel
		FileChannel channel = raf.getChannel();

		// now memory-map a byte-buffer
		ByteBuffer bb = ByteBuffer.NEW(channel.map(
				FileChannel.MapMode.READ_ONLY, 0, channel.size()));
		// create a PDFFile from the data
		if (password == null)
			mPdfFile = new PDFFile(bb);
		else
			mPdfFile = new PDFFile(bb, new PDFPassword(password));
	}

	/* (non-Javadoc)
	 * @see android_GLUESigner.ui.WizardFragments.WizardFragment#validateAndSave()
	 */
	@Override
	public boolean validateAndSave() {
		if (mGraphView.getRect() != null) {
			sigPage = mPage;
			int canvasHeight = 0;
			if (mGraphView != null) {
				canvasHeight = (int) (mGraphView.mImageView.getHeight());
			}
			this.getSignatureInfo().setGraphicRect(mGraphView.getRect(),mZoom);
			int top = (int) ((canvasHeight-mGraphView.getRect().top)/mZoom);
			int bottom = (int) ((canvasHeight-mGraphView.getRect().bottom)/mZoom);
			int right = (int) (mGraphView.getRect().right /mZoom);
			int left = (int) (mGraphView.getRect().left/mZoom);
			
			this.getSignatureInfo().setSignatureRect(new Rect(left,top,right,bottom));		
			this.getSignatureInfo().setPageNumber(sigPage);
		}
		return true;
	}

	/**
	 * enable the next button
	 */
	public void setNextEnabled(boolean enabled) {
		IWizardEventListener listener = (IWizardEventListener) getActivity();
		listener.setNextButtonEnabled(enabled);
		
	}

	/**
	 * gets the number of pages of a pdf
	 * @return number of page
	 */
	public int getNumberOfPages() {
		return mPdfFile.getNumPages();
	}

	/**
	 * renders a specific page
	 * @param pageNum the page number
	 */
	public void renderPageNumber(int pageNum) {
		mPage = pageNum;
		bZoomOut.setEnabled(true);
		bZoomIn.setEnabled(true);
		progress = ProgressDialog.show(getActivity(), getActivity().getApplicationContext().getString(R.string.loading), getActivity().getApplicationContext().getString(R.string.pdf_loading) + mPage, true, true);
		mBitmapPool.clear();
		startRenderThread(mPage, mZoom);		
	}
}