package org.redcross.openmapkit.deployments;


import android.app.Activity;
import android.app.DownloadManager;
import android.net.Uri;
import android.os.AsyncTask;

import org.redcross.openmapkit.ExternalStorage;

import java.util.List;
import java.util.Vector;

public class DeploymentDownloader extends AsyncTask<Void, String, Long> {
    private static final String PROGRESS_MSG = "Downloading deployment. ";

    private List<DeploymentDownloaderListener> listeners = new Vector<>();
    private DownloadManager downloadManager;
    private Deployment deployment;
    private List<Long> downloadIds = new Vector<>();

    private boolean downloading = true;
    private int filesCompleted = 0;

    public DeploymentDownloader(Deployment deployment, Activity activity) {
        downloadManager = (DownloadManager)activity.getSystemService(Activity.DOWNLOAD_SERVICE);
        this.deployment = deployment;
    }

    public void addListener(DeploymentDownloaderListener listener) {
        listeners.add(listener);
    }

    public void cancel() {

    }

    @Override
    protected void onPreExecute() {
        String msg = PROGRESS_MSG + "0/"
                + deployment.fileCount()
                + " files. "
                + "0 MB.";
        notifyDeploymentDownloadProgressUpdate(msg, 0);
    }

    @Override
    protected Long doInBackground(Void... nothing) {
        List<String> osmUrls = deployment.osmUrls();
        for (String osmUrl : osmUrls) {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(osmUrl));
            request.setDestinationInExternalPublicDir(ExternalStorage.getOSMDirRelativeToExternalDir(), Deployment.fileNameFromUrl(osmUrl));
            long downloadId = downloadManager.enqueue(request);
            downloadIds.add(downloadId);
        }
        List<String> mbtilesUrls = deployment.mbtilesUrls();
        for (String mbtilesUrl : mbtilesUrls) {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(mbtilesUrl));
            request.setDestinationInExternalPublicDir(ExternalStorage.getMBTilesDirRelativeToExternalDir(), Deployment.fileNameFromUrl(mbtilesUrl));
            long downloadId = downloadManager.enqueue(request);
            downloadIds.add(downloadId);
        }
        pollDownloadManager();
        return null;
    }

    @Override
    protected void onProgressUpdate(String... msgs) {
        String msg = msgs[0];
        notifyDeploymentDownloadProgressUpdate(msg, 1000);
    }

    @Override
    protected void onPostExecute(Long nothing) {
        notifyDeploymentDownloadComplete();
    }

    private void notifyDeploymentDownloadProgressUpdate(String msg, int bytesDownloaded) {
        for (DeploymentDownloaderListener listener : listeners) {
            listener.onDeploymentDownloadProgressUpdate(msg, bytesDownloaded);
        }
    }

    private void notifyDeploymentDownloadComplete() {
        for (DeploymentDownloaderListener listener : listeners) {
            listener.onDeploymentDownloadComplete();
        }
    }

    private void pollDownloadManager() {

    }

}
