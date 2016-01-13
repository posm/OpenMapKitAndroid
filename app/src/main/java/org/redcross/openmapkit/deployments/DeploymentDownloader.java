package org.redcross.openmapkit.deployments;


import android.app.Activity;
import android.app.DownloadManager;
import android.os.AsyncTask;

import java.util.List;
import java.util.Vector;

public class DeploymentDownloader extends AsyncTask<Void, String, Long> {

    private List<DeploymentDownloaderListener> listeners = new Vector<>();
    private DownloadManager downloadManager;
    int fileCount = 0;

    public DeploymentDownloader(Deployment deployment, Activity activity) {
        downloadManager = (DownloadManager)activity.getSystemService(Activity.DOWNLOAD_SERVICE);
        fileCount = deployment.fileCount();
    }

    public void addListener(DeploymentDownloaderListener listener) {
        listeners.add(listener);
    }

    public void cancel() {

    }

    @Override
    protected void onPreExecute() {

    }

    @Override
    protected Long doInBackground(Void... nothing) {
        return null;
    }

    @Override
    protected void onProgressUpdate(String... msgs) {
        String msg = msgs[0];

    }

    @Override
    protected void onPostExecute(Long downloadId) {

    }


}
