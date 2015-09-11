/*******************************************************************************
 * Copyright (c) 2013 Chris Banes.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package uk.co.senab.bitmapcache;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v4.util.LruCache;
import android.util.Log;

import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

final class BitmapMemoryLruCache extends LruCache<String, CacheableBitmapDrawable> {

    public static final String TAG = "BitmapMemoryLruCache";
    private final Set<SoftReference<CacheableBitmapDrawable>> mRemovedEntries;
    private final BitmapLruCache.RecyclePolicy mRecyclePolicy;
    private int largestValueSeenBytes;

    BitmapMemoryLruCache(int maxSize, BitmapLruCache.RecyclePolicy policy) {
        super(maxSize);

        mRecyclePolicy = policy;
        mRemovedEntries = policy.canInBitmap()
                ? Collections.synchronizedSet(new HashSet<SoftReference<CacheableBitmapDrawable>>())
                : null;
        largestValueSeenBytes = 0;
    }

    CacheableBitmapDrawable put(CacheableBitmapDrawable value) {
        if (null != value) {
            value.setCached(true);
            largestValueSeenBytes = Math.max(value.getMemorySize(), largestValueSeenBytes);
            return put(value.getUrl(), value);
        }

        return null;
    }

    BitmapLruCache.RecyclePolicy getRecyclePolicy() {
        return mRecyclePolicy;
    }

    public void resizeMemoryForTiles(int numberOfTiles) {
        if (largestValueSeenBytes > 0 && numberOfTiles > 0) {
            float factor = numberOfTiles * 1.05f; //increase by 5%
            int newSize = (int) (factor * largestValueSeenBytes);
            if (newSize > maxSize()) {
                Log.d(TAG, "resizing for " + numberOfTiles + " tiles - to " + newSize / (1024 * 1024) + "MB");
                resize(newSize);
            }
        }
    }

    @Override
    protected int sizeOf(String key, CacheableBitmapDrawable value) {
        return value.getMemorySize();
    }

    @Override
    protected void entryRemoved(boolean evicted, String key, CacheableBitmapDrawable oldValue,
            CacheableBitmapDrawable newValue) {
        // Notify the wrapper that it's no longer being cached
        oldValue.setCached(false);

        if (mRemovedEntries != null && canUseForInBitmap(oldValue)) {
            synchronized (mRemovedEntries) {
                mRemovedEntries.add(new SoftReference<CacheableBitmapDrawable>(oldValue));
            }
        }
    }

    public Bitmap getBitmapFromRemoved(final int width, final int height) {
        if (mRemovedEntries == null) {
            return null;
        }

        Bitmap result = null;

        synchronized (mRemovedEntries) {
            final Iterator<SoftReference<CacheableBitmapDrawable>> it = mRemovedEntries.iterator();

            while (it.hasNext()) {
                CacheableBitmapDrawable value = it.next().get();

                if (canUseForInBitmap(value)) {
                    if (canUseForInBitmapForSize(value, width, height) && !value.isBeingDisplayed() && !value.isReferencedByCache()) {
                        result = value.getBitmap();
                        SDK12.setHasAlpha(result, true);
                        result.eraseColor(Color.TRANSPARENT);
                        value.setReused();
                        it.remove();
                        break;
                    }
                } else {
                    it.remove();
                }
            }
        }

        return result;
    }

    private static boolean canUseForInBitmap(
            CacheableBitmapDrawable candidate) {
        return candidate != null &&
                candidate.isBitmapValid() &&
                candidate.isBitmapMutable();
    }

    private static boolean canUseForInBitmapForSize(
            CacheableBitmapDrawable candidate, int width, int height) {
        return
                candidate.getIntrinsicWidth() == width &&
                candidate.getIntrinsicHeight() == height;
    }

    void trimMemory() {
        final Set<Entry<String, CacheableBitmapDrawable>> values = snapshot().entrySet();

        for (Entry<String, CacheableBitmapDrawable> entry : values) {
            CacheableBitmapDrawable value = entry.getValue();
            if (null == value || !value.isBeingDisplayed()) {
                remove(entry.getKey());
            }
        }
        if (mRemovedEntries != null) {
            synchronized (mRemovedEntries) {
                mRemovedEntries.clear();
            }
        }
    }
}
