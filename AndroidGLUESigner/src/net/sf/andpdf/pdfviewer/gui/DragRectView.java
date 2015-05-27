package net.sf.andpdf.pdfviewer.gui;

import java.io.IOException;

import net.sf.andpdf.pdfviewer.PdfViewerActivity;
import android.R;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import androidGLUESigner.pdf.ui.PDFViewerFragment;


public class DragRectView extends View {

    private static final int RECT_SELECTED = 2;

    public enum ZoomEvent{
	IN,
	OUT, NONE
}
    
	private Paint mRectPaint;

    private int mStartX = 0;
    private int mStartY = 0;
    private int mEndX = 0;
    private int mEndY = 0;
    private float lastZoomFactor = 1.0f;
    private boolean mDrawRect = false;
    public boolean isDragging = false;
    public boolean zoomEventOccured = false;

	private TextPaint mTextPaint = null;
    private Rect mRect;
    private PdfViewerActivity pdfActivity;
    private int rectPage = 1;

	private boolean sigPageVisible;

	private ZoomEvent zoomEvent = ZoomEvent.NONE;

	private boolean usingFragment;

	private PDFViewerFragment pdfViewerFragment;

	private boolean isUsingFragment;

    

	

    public interface OnUpCallback {
        void onRectFinished(Rect rect);
    }

    public DragRectView(final Context context, PdfViewerActivity mainAct) {
        super(context);
        this.pdfActivity = mainAct;
        init();
    }

    public DragRectView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DragRectView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
    
    /**
	 * @param context
	 * @param pdfViewerFragment
	 */
	public DragRectView(Context context, PDFViewerFragment pdfViewerFragment) {
		super(context);
		this.isUsingFragment = true;
		this.pdfViewerFragment = pdfViewerFragment;
	
	}

	public Rect getRect(){
    	return this.mRect;
    }
    
    public void SetZoomEventOccured(ZoomEvent z){
    	this.zoomEventOccured = true;
    	this.zoomEvent = z;
    }
    
    public void setSigPageVisible(boolean sigPageIsVisible){
    	this.sigPageVisible = sigPageIsVisible;
    }

    /**
     * Sets callback for up
     *
     * @param callback {@link OnUpCallback}
     */
    public void setOnUpCallback(OnUpCallback callback) {
       // mCallback = callback;
    }

    /**
     * Inits internal data
     */
    private void init() {
        mRectPaint = new Paint();
        mRectPaint.setColor(getContext().getResources().getColor(R.color.holo_green_light));
        mRectPaint.setStyle(Paint.Style.STROKE);
        mRectPaint.setStrokeWidth(5); // TODO: should take from resources

        mTextPaint = new TextPaint();
        mTextPaint.setColor(getContext().getResources().getColor(R.color.holo_green_light));
        mTextPaint.setTextSize(20);      
        // Set empty Rectangle on construction
        mRect = new Rect(0,0,0,0);
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {

       if(pdfActivity.isSignatureSelectionMode){
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
             
                mDrawRect = false;
                mStartX = (int) event.getX();
                mStartY = (int) event.getY();
                invalidate();
                break;

            case MotionEvent.ACTION_MOVE:
            	isDragging = true;
                final int x = (int) event.getX();
                final int y = (int) event.getY();
              ;
                if (!mDrawRect || Math.abs(x - mEndX) > 5 || Math.abs(y - mEndY) > 5) {
                    mEndX = x;
                    mEndY = y;
                    invalidate();
                }
                
                
                mDrawRect = true;
                break;

            case MotionEvent.ACTION_UP:
            /*    if (mCallback != null) {
                	Rect rect = new Rect(Math.min(mStartX, mEndX), Math.min(mStartY, mEndY),
                            Math.max(mEndX, mStartX), Math.max(mEndY, mStartX));
                    mCallback.onRectFinished(new Rect(Math.min(mStartX, mEndX), Math.min(mStartY, mEndY),
                            Math.max(mEndX, mStartX), Math.max(mEndY, mStartX)));
                    Log.d("rectangle", "Rect is (" + rect.left + ", " + rect.top + ", " + rect.right + ", " + rect.bottom + ")");
                }*/
               
                // as soon as rectangle is selected, update Activity Result 
                mRect =  new Rect(Math.min(mStartX, mEndX), Math.min(mStartY, mEndY),
                        Math.max(mEndX, mStartX), Math.max(mEndY, mStartX));
                
                Log.d("rectangle", "Rect is (" + mRect.left + ", " + mRect.top + ", " + mRect.right + ", " + mRect.bottom + ")");

                if (!usingFragment){
				pdfActivity.setActivityResult(RECT_SELECTED);
                }else{
                	//todo fragment
                }
			
		        isDragging = false;
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
      float zoomVector = 0;
      float zoomFactor = 1;
      switch (zoomEvent){
      case IN:
    	  zoomFactor = pdfActivity.getZoomFactor(false);
    	  break;
      case OUT:
    	  zoomFactor= pdfActivity.getZoomFactor(false);
    	  break;
      }
       
       boolean isZooming;
	if (Math.abs(zoomFactor-lastZoomFactor) > 0){
    	   isZooming = true;
    	   lastZoomFactor = zoomFactor;
       }else{
    	 isZooming = false;
       }
    
        if (mDrawRect && sigPageVisible) {
        	   Log.d("draw", "drawrect =" + Boolean.toString(mDrawRect));
               Log.d("draw", "dragging =" + Boolean.toString(isDragging));
        	 if (isDragging){
        		canvas.drawRect(Math.min(mStartX, mEndX), Math.min(mStartY, mEndY),
        				Math.max(mEndX, mStartX), Math.max(mEndY, mStartY), mRectPaint);
        		canvas.drawText("  (" + Math.abs(mStartX - mEndX) + ", " + Math.abs(mStartY - mEndY) + ")",
        				Math.max(mEndX, mStartX), Math.max(mEndY, mStartY), mTextPaint);
        		  Log.d("draw", "fall 2 dragging");		
        	}else  	if(!isDragging && (isZooming || zoomEventOccured)){
        		canvas.drawRect((int)Math.floor(Math.min(mStartX, mEndX)*zoomFactor), (int)Math.floor(Math.min(mStartY, mEndY)*zoomFactor),
                		(int)Math.floor(Math.max(mEndX, mStartX)*zoomFactor), (int)Math.floor(Math.max(mEndY, mStartY)*zoomFactor), mRectPaint);
                canvas.drawText("  (" + Math.abs(mStartX - mEndX)*zoomFactor + ", " + Math.abs(mStartY - mEndY)*zoomFactor + ")",
                        Math.max(mEndX, mStartX)*zoomFactor, Math.max(mEndY, mStartY)*zoomFactor, mTextPaint);
                Log.d("draw", "fall zoomed + not drag +zoomFactor = " + Float.toString(zoomFactor));
             	zoomEventOccured = false;
             	zoomEvent = zoomEvent.NONE;
        	}else if (!zoomEventOccured && !isDragging && !isZooming){
        		canvas.drawRect(Math.min(mStartX, mEndX), Math.min(mStartY, mEndY),
        				Math.max(mEndX, mStartX), Math.max(mEndY, mStartY), mRectPaint);
        		canvas.drawText("  (" + Math.abs(mStartX - mEndX) + ", " + Math.abs(mStartY - mEndY) + ")",
        				Math.max(mEndX, mStartX), Math.max(mEndY, mStartY), mTextPaint);
        		  Log.d("draw", "fall 3 standard +zoomFactor = " + Float.toString(zoomFactor) + " " + "iszooming " + Boolean.toString(isZooming) + " " + "isdragging " + Boolean.toString(isDragging));
        		
        	}

       
       
        	
        	 
        }
    }
}