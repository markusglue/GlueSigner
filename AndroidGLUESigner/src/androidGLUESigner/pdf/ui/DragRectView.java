package androidGLUESigner.pdf.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import androidGLUESigner.models.SignatureInfo;
import androidGLUESigner.ui.R;

/**
 * Handles positioning of signature placement over a PDF page
 * @author roland
 *
 */
public class DragRectView extends View {

	private static final float MAX_TEXTSIZE = 80;

	/**
	 * describes what type of zoom event has occurred in a viewer
	 * @author roland
	 *
	 */
	public enum ZoomEvent {
		IN, OUT, NONE
	}

	private Paint mRectPaint;
	
	// the signature information for the current rectangle
	private SignatureInfo signatureInfo;

	// dragging/drawing coordinates
	private int mStartX = 0;
	private int mStartY = 0;
	private int mEndX = 0;
	private int mEndY = 0;
	private int imageWidth;
	private int imageHeight;

	// dragging/zooming flags
	private boolean mDrawRect = false;
	public boolean isDragging = false;
	public boolean zoomEventOccured = false;

	private TextPaint mTextPaint = null;
	//selected rectangle
	private Rect mRect;

	// decides wheter view responds to touches
	private boolean touchEnabled = true;

	// field to keep track of the last zoom event 
	private ZoomEvent zoomEvent = ZoomEvent.NONE;

	// reference to parent fragment
	private PDFViewerFragment pdfViewerFragment;

	// used if showing already signed documents
	private boolean previewMode;

	/**
	 * standard constructor for a DragRectView
	 * @param context the application context
	 */
		/**
	 * constructor for preview mode
	 * @param context the application context
	 * @param previewMode if preview mode is enabled or not
	 */
	public DragRectView(Context context, boolean previewMode){
		super(context);
		this.previewMode = previewMode;
		init();
	}

	/**
	 * get the signature rectangle
	 * @return signature rectangle
	 */
	public Rect getRect() {
		return this.mRect;
	}

	/**
	 * notify the DragRectView of a zoom event
	 * @param z the zoom event
	 */
	public void SetZoomEventOccured(ZoomEvent z) {
		this.zoomEventOccured = true;
		this.zoomEvent = z;
	}

	/**
	 * sets whether view responds to touches
	 * @param touchEnabled enables if true
	 */
	public void setTouchEnabled(boolean touchEnabled) {
		this.touchEnabled = touchEnabled;
		this.mDrawRect = touchEnabled;
		this.mStartX=0;
		this.mEndX=0;
		this.mStartY=0;
		this.mEndY =0;
	}

	/**
	 * Inits internal data
	 */
	private void init() {
		// rectangle drawing tool for canvas
		mRectPaint = new Paint();
		mRectPaint.setColor(getContext().getResources().getColor(
				android.R.color.holo_green_light));
		mRectPaint.setStyle(Paint.Style.STROKE);
		mRectPaint.setStrokeWidth(5);
		// text drawing tool for canvas
		mTextPaint = new TextPaint();		
		mTextPaint.setTextSize(20);
		// Set empty Rectangle on construction
		mRect = new Rect(0, 0, 0, 0);
		if (previewMode){
			// draw black if in preview mode
			mTextPaint.setColor(getResources().getColor(R.color.black));
		    mRectPaint.setColor(getResources().getColor(R.color.black));
		}else{
			// draw green if in selection mode
			mTextPaint.setColor(getContext().getResources().getColor(
					android.R.color.holo_green_light));
		}

	}

	@Override
	public boolean onTouchEvent(final MotionEvent event) {
		// Log.d("draw","drawnot " +event.getX() + " " + event.getY());
		// if in rectangle selection mode
		if (touchEnabled) {
			switch (event.getAction()) {
			// starts dragging rect
			case MotionEvent.ACTION_DOWN:
				mDrawRect = false;
				mStartX = (int) event.getX();
				mStartY = (int) event.getY();
				invalidate();
				break;
			// drags rectangle if delta > 5
			case MotionEvent.ACTION_MOVE:
				isDragging = true;
				final int x = (int) event.getX();
				final int y = (int) event.getY();

				if (!mDrawRect || Math.abs(x - mEndX) > 5
						|| Math.abs(y - mEndY) > 5) {
					mEndX = x;
					mEndY = y;
					invalidate();
				}
				mDrawRect = true;
				break;
			// rect dragging finished
			case MotionEvent.ACTION_UP:
				invalidate();
				break;
				
			default:
				break;
			}
			return true;
		}
		return false;
	}


	@Override
	protected void onDraw(final Canvas canvas) {
		super.onDraw(canvas);
		if (mDrawRect) {
			// Log.d("draw", "drawrect =" + Boolean.toString(mDrawRect));
			// Log.d("draw", "dragging =" + Boolean.toString(isDragging));
			if (isDragging) {
				drawForSignatureType(canvas);
				//Log.d("draw", "fall 2 dragging and not isZooming");
				isDragging = false;
			} else if (!isDragging) {
				// recalculate size of rect when zooming
				switch (zoomEvent) {
				case IN:
					this.mStartX *= 1.5;
					this.mStartY *= 1.5;
					this.mEndX *= 1.5;
					this.mEndY *= 1.5;
					break;
				case OUT:
					this.mStartX /= 1.5;
					this.mStartY /= 1.5;
					this.mEndX /= 1.5;
					this.mEndY /= 1.5;
					break;
				default:
					break;
				}
				drawForSignatureType(canvas);
			}
			setResultRect();
		}
	}

	/**
	 * sets the dragged rectangle after all onDraw events
	 */
	private void setResultRect() {
		if (touchEnabled){
			// set rectangle if in selection mode
			mRect = new Rect(Math.min(mStartX, mEndX), Math.min(mStartY, mEndY),
				Math.max(mEndX, mStartX), Math.max(mEndY, mStartX));
			pdfViewerFragment.setNextEnabled(true);
			//Log.d("rectangle", "Rect is (" + mRect.left + ", " + mRect.top + ", "+ mRect.right + ", " + mRect.bottom + ")");
			this.getSignatureInfo().setPageNumber(pdfViewerFragment.getmPage());
		}
	}
	
	/**
	 * sets the coordinates of an existing rect if in preview mode
	 */
	public void setMRect(){
		mStartX = signatureInfo.getGraphicRect().left;
		mEndX = signatureInfo.getGraphicRect().right;
		mStartY = signatureInfo.getGraphicRect().top;
		mEndY = signatureInfo.getGraphicRect().bottom;
		mDrawRect = true;
		this.touchEnabled = false;
	}

	/**
	 * calls different methods depending on the way the signature should be displayed
	 * @param canvas the current drawing canvas
	 */
	private void drawForSignatureType(final Canvas canvas) {
		
			switch (getSignatureInfo().getSignatureType()) {
			// visible signture
			case NORMAL:
				drawBasicRectangle(canvas);
					// draw signature info inside rectangel (name, reason, location)
					drawPreviewText(canvas,
							(int) (Math.min(mStartX, mEndX) + mRectPaint
									.getStrokeWidth()),
							(int) (Math.min(mStartY, mEndY) + mRectPaint
									.getStrokeWidth()),
							(int) (Math.abs(mStartX - mEndX) - (mRectPaint
									.getStrokeWidth() * 2)),
							(int) (Math.abs(mStartY - mEndY) - (mRectPaint
									.getStrokeWidth() * 2)));
				zoomEvent = zoomEvent.NONE;
				break;
			// signature with picture
			case PICTURE:
				drawBasicRectangle(canvas);
					// draws bitmap directly onto canvas
					drawBitmapFromFile(canvas, pdfViewerFragment
							.getSignatureInfo().getImagePath());
					// draw signature info inside rectangel (name, reason, location)
					drawPreviewText(canvas,
							(int) (Math.min(mStartX, mEndX) + mRectPaint
									.getStrokeWidth()),
							(int) (Math.min(mStartY, mEndY) + mRectPaint
									.getStrokeWidth()),
							(int) (Math.abs(mStartX - mEndX) - (mRectPaint
									.getStrokeWidth() * 2)),
							(int) (Math.abs(mStartY - mEndY) - (mRectPaint
									.getStrokeWidth() * 2)));			
				break;
			default:
				break;
			}
	}

	/**
	 * draws a bitmap on a canvas
	 * @param canvas drawing canvas
	 * @param imagePath path to image file
	 */
	private void drawBitmapFromFile(Canvas canvas, String imagePath) {
		// get bitmap from file
		Bitmap image = BitmapFactory.decodeFile(imagePath);
		canvas.drawBitmap(image, null,
				new Rect(Math.min(mStartX, mEndX), Math.min(mStartY, mEndY),
						Math.max(mEndX, mStartX), Math.max(mEndY, mStartX)),
				mRectPaint);		
	}

	/**
	 * draw rectangle on canvas
	 * @param canvas drawing canvas
	 */
	private void drawBasicRectangle(final Canvas canvas) {
		//Log.d("sgr","drawrect called");
		canvas.drawRect(Math.min(mStartX, mEndX), Math.min(mStartY, mEndY),
				Math.max(mEndX, mStartX), Math.max(mEndY, mStartY), mRectPaint);
		if (!previewMode){
			// only draw when in selection mode
			canvas.drawText(
				"  (" + Math.abs(mStartX - mEndX) + ", "
						+ Math.abs(mStartY - mEndY) + ")",
				Math.max(mEndX, mStartX), Math.max(mEndY, mStartY), mTextPaint);		
		}		
	}

	/**
	 * draw name, location and reason inside rectangle
	 * @param canvas drawing canvs
	 */
	private void drawPreviewText(Canvas canvas, int x, int y, int width,
			int height) {
		//Log.d("sigr",String.format("isinsetmode + %b", this.previewMode));
		SignatureInfo info = getSignatureInfo();
		int currentx = x;
		int currenty = y;
		// the information to be drawn
		String[] texts = { pdfViewerFragment.getActivity().getApplicationContext().getString(R.string.rect_text_name) + " " + info.getSignatureName(),
				pdfViewerFragment.getActivity().getApplicationContext().getString(R.string.rect_text_reason) + " " + info.getSignatureReason(),
				pdfViewerFragment.getActivity().getApplicationContext().getString(R.string.rect_text_location) + " " + info.getSignatureLocation() };
		float currentTextSize = MAX_TEXTSIZE;
		String longestText = "";
		float lastMeasuredWidth = width + 1;
		// get most characters = most width
		for (String text : texts) {
			if (text.length() > longestText.length())
				longestText = text;
		}
		float xindent = 0;
		float ylineheight = 0;
		while (lastMeasuredWidth > width - (2 * xindent) && currentTextSize > 0) {
			currentTextSize = currentTextSize - 2;
			mTextPaint.setTextSize(currentTextSize);
			lastMeasuredWidth = mTextPaint.measureText(longestText);
			xindent = (mTextPaint.measureText("a"));
			ylineheight = Math
					.abs((mTextPaint.ascent() + mTextPaint.descent()));
			currentx = (int) (x + xindent);// set same text size for all texts
											// including ones with less
											// characters then longestText

		}

		// draw each text
		if (width < 0)
			width = 0;
		for (String text : texts) {
			StaticLayout sl = new StaticLayout(text, mTextPaint, (int) (width),
					Layout.Alignment.ALIGN_NORMAL, 1, 1, false);
			canvas.save();
			canvas.translate(currentx, currenty);
			sl.draw(canvas);
			canvas.restore();
			currenty = (int) (currenty + 1.5 * ylineheight);
		}

	}
	
	
	
	/**
	 * set the signature info
	 * Note: Not every drag rect view will be using the signatureInformation
	 * associated with the pdfviewerfragment. Useful in case of existing signatures.
	 * @param signatureInfo the signature info
	 */
	public void setSignatureInfo(SignatureInfo signatureInfo){
		this.signatureInfo = signatureInfo;
	}

	/**
	 * get the signature info
	 * @return the signature information
	 */
	public SignatureInfo getSignatureInfo() {
		return this.signatureInfo;
	}

	/**
	 * adjusts the size of the canvas to the new page size in the viewer
	 * when zooming
	 * @param width new width
	 * @param height new height
	 */
	public void setMeasures(float width, float height) {
		this.imageHeight = (int) height;
		this.imageWidth = (int) width;
		this.setLayoutParams(new RelativeLayout.LayoutParams(imageWidth,
				imageHeight));
		requestLayout();
		invalidate();
	}

	/**
	 * set the reference to the viewer
	 * @param pdfViewerFragment the pdfViewerFragment to set
	 */
	public void setPdfViewerFragment(PDFViewerFragment pdfViewerFragment) {
		this.pdfViewerFragment = pdfViewerFragment;
	}

	/**
	 * set the rectangle to visible if the viewer is on the correct page
	 * @param currentPage the currently visible page
	 */
	public void setRectVisibleOnPage(int currentPage) {
		int sigPage = getSignatureInfo().getPageNumber();
		boolean visible = currentPage == sigPage;
		if (!previewMode){
			if (touchEnabled){
			this.mDrawRect = visible;
			}else{
				this.mDrawRect = false;
			}
		}else{
			this.mDrawRect = visible;
		}
	
		if (visible || touchEnabled){
			this.setVisibility(View.VISIBLE);
			
		}else{
			this.setVisibility(View.INVISIBLE);
		}		
	}
}