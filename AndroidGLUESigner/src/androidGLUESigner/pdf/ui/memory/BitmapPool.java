package androidGLUESigner.pdf.ui.memory;

import java.util.ArrayList;

import android.graphics.Bitmap;
/**
 * http://ruchitsharma.blogspot.ch/2013/04/android-bitmap-memory-management.html
 * @author roland
 *
 */
public class BitmapPool {
    private final ArrayList<Bitmap> bPool;
    private final int bPoolLimit;

    /**
     * Construct a BitmapPool which creates bitmap with the specified size.
     * @param width bitmap width
     * @param height bitmap height
     * @param poolLimit maximum size of the pool
     */
    public BitmapPool(int width, int height, int poolLimit) {
        bPoolLimit = poolLimit;
        bPool = new ArrayList<Bitmap>(poolLimit);
    }

    /**
     * Construct a BitmapPool which caches bitmap with any size;
     * @param poolLimit the maximum size of the pool
     */
    public BitmapPool(int poolLimit) {
        bPoolLimit = poolLimit;
        bPool = new ArrayList<Bitmap>(poolLimit);
    }

    /**
     * Get a Bitmap from the pool.
     * @return a bitmap
     */
    public synchronized Bitmap getBitmap() {
        int size = bPool.size();
        return size > 0 ? bPool.remove(size - 1) : null;
    }

    /**
     * Get a Bitmap from the pool with the specified size.
     * @param width the bitmap width
     * @param height the bitmap height
     * @return the bitmap that matches the parameters
     */
    public synchronized Bitmap getBitmap(int width, int height) {
        for (int i = 0; i < bPool.size(); i++) {
            Bitmap b = bPool.get(i);
            if (b.getWidth() == width && b.getHeight() == height) {
                return bPool.remove(i);
            }
        }
        return null;
    }

    /**
     * Put a Bitmap into the pool
     * @param bitmap the bitamp
     */
    public void addBitmap(Bitmap bitmap) {
        if (bitmap == null) return;
       /* if ((bitmap.getWidth() != bWidth) ||
                (bitmap.getHeight() != bHeight)) {
            bitmap.recycle();
            return;
        }*/
        synchronized (this) {
            if (bPool.size() >= bPoolLimit) bPool.remove(0);
            bPool.add(bitmap);
        }
    }

    public synchronized void clear() {
        bPool.clear();
    }

}