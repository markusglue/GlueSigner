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