package uk.co.senab.bitmapcache;


import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.os.Build;

@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
public class SDK12 {
    static void setHasAlpha(Bitmap bitmap, final boolean hasAlpha) {
        bitmap.setHasAlpha(hasAlpha);
    }
}
