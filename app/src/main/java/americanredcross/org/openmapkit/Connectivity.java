package americanredcross.org.openmapkit;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;


public class Connectivity {

    /**
     * For determining if the device is connected or disconnected
     * @param context
     * @return connectivity status
     */
    public static boolean isConnected(Context context){

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if ((wifiInfo != null && wifiInfo.isConnected()) || (mobileInfo != null && mobileInfo.isConnected())) {

            return true;

        } else {

            return false;
        }
    }
}
