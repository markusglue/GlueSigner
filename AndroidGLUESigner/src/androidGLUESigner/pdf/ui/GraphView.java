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

import java.util.ArrayList;

import net.sf.andpdf.pdfviewer.gui.FullScrollView;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Handler;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidGLUESigner.models.SignatureInfo;
import androidGLUESigner.pdf.ui.DragRectView.ZoomEvent;

/**
 * Extends the full scrollview from net.sf.andpdf
 * 
 * @author mario
 * 
 */
public class GraphView extends FullScrollView {
	Bitmap mBi;
	// the bitmap representation of a pdf page
	ImageView mImageView;

	private DragRectView mDragRectView;
	// inserts runables into UI thread queue for modifying GUI elements inside other threads
	private Handler uiHandler;
	private Handler invalidateHandler;
	private Context context;
	private RelativeLayout relativeLayout;
	// all signature rectangles
	private ArrayList<DragRectView> rects;
	
	public GraphView(Context context) {
		super(context);
		this.context = context;
		uiHandler = new Handler();
		invalidateHandler = new Handler();

		RelativeLayout.LayoutParams rpWrap1 = new RelativeLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	
		

		// vertical layout
		relativeLayout = new RelativeLayout(context);
		relativeLayout.setLayoutParams(rpWrap1);;

		mImageView = new ImageView(context);
		mDragRectView = new DragRectView(context,false);
		setPageBitmap(null);
		updateImage();
		mImageView.setLayoutParams(rpWrap1);
		mDragRectView.setLayoutParams(rpWrap1);
		relativeLayout.addView(mImageView);
		//relativeLayout.addView(mDragRectView);

		setBackgroundColor(Color.LTGRAY);
		setHorizontalScrollBarEnabled(true);
		setHorizontalFadingEdgeEnabled(true);
		setVerticalScrollBarEnabled(true);
		setVerticalFadingEdgeEnabled(true);
		this.addView(relativeLayout);
	}

	/**
	 * toggles between scrolling and selection mode
	 */
	public void toggleScrollViewMode() {
		this.setTouchesRedireactable(!this.touchesRedirectable());
		mDragRectView.setTouchEnabled(this.touchesRedirectable());
		mDragRectView.invalidate();
	}

	/**
	 * 
	 * set rect for viewing page with existing signature
	 */
	public void setMRect() {
		this.setTouchesRedireactable(false);
		mDragRectView.setTouchEnabled(false);
		mDragRectView.setMRect();
		mDragRectView.invalidate();

	}

	/**
	 * notifies each drag rect after rendering is finished
	 * @param width width of the newly rendered page
	 * @param height height of the newly rendered page
	 * @param page page number
	 */
	public void updateDragRect(final float width, final float height,
			final int page) {
		invalidateHandler.post(new Runnable() {
			@Override
			public void run() {
				setRectMeasures(width, height);

			}
		});
	}
	
	/**
	 * gives the image view the bitmap of newly rendered page
	 */
	void updateImage() {
		uiHandler.post(new Runnable() {
			public void run() {
				mImageView.setImageBitmap(mBi);

			}
		});
	}

	/**
	 * sets a bitmap
	 * @param bi the bitmap
	 */
	void setPageBitmap(Bitmap bi) {
		if (bi != null) {
			mBi = bi;
		}
	}

	/**
	 * notifies drag rect views of zoom events from the viewer
	 * @param event the zoom event
	 * @param currentZoom the current zoom factor
	 */
	public void setZoomEventOccured(ZoomEvent event, float currentZoom) {
		for (DragRectView rect : rects) {
			rect.SetZoomEventOccured(event);
		}
		
	}

	/**
	 * get the rectangle from the draw rect in selection mode
	 * @return the rectangle
	 */
	public Rect getRect() {
		return mDragRectView.getRect();
	}

	/**
	 * Creates all necessary drag rect views for the PDF document
	 * @param sigInfos the signature info models for existing signatures
	 * @param fragment the viewer
	 * @param previewMode if in preview mode
	 */
	public void createDragRectViews(ArrayList<SignatureInfo> sigInfos,
			PDFViewerFragment fragment, boolean previewMode) {
		
		rects = new ArrayList<DragRectView>();
		for (SignatureInfo sig : sigInfos) {
			DragRectView d = new DragRectView(context, true);
			d.setLayoutParams(new RelativeLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			d.setSignatureInfo(sig);
			d.setMRect();
			d.setPdfViewerFragment(fragment);
			relativeLayout.addView(d);
			rects.add(d);
		}
		if (!previewMode) {
			mDragRectView.setPdfViewerFragment(fragment);
			mDragRectView.setSignatureInfo(fragment.getSignatureInfo());
			relativeLayout.addView(mDragRectView);
			rects.add(mDragRectView);
		} else {
			mDragRectView = null;
		}
	}

	/**sets the size of the draw rectangles to the same size as the rendered page.
	 * 
	 * @param width page width
	 * @param height page height
	 */
	private void setRectMeasures(float width, float height) {
		synchronized (rects) {
			for (DragRectView d : rects) {
				d.setMeasures(width, height);
			}
		}
	}

	/**
	 * Sets the visibility of all DragRectViews if the page number is correct.
	 * @param mPage
	 */
	public void setDragRectVisibility(int currentPage) {
	 for (DragRectView d : rects){
		 
		 d.setRectVisibleOnPage(currentPage);
	 }
		
	}
}
