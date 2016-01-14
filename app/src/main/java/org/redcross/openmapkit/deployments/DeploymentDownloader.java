package org.redcross.openmapkit.deployments;


import android.app.Activity;
import android.app.DownloadManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import org.redcross.openmapkit.ExternalStorage;

import java.util.List;
import java.util.Vector;

public class DeploymentDownloader extends AsyncTask<Void, Void, Void> {
    private List<DeploymentDownloaderListener> listeners = new Vector<>();
    private DownloadManager downloadManager;
    private Deployment deployment;
    private long[] downloadIds;

    private boolean downloading = true;
    private boolean canceled = false;
    private int bytesDownloaded = 0;
    private int filesCompleted = 0;

    public DeploymentDownloader(Deployment deployment, Activity activity) {
        downloadManager = (DownloadManager)activity.getSystemService(Activity.DOWNLOAD_SERVICE);
        this.deployment = deployment;
        downloadIds = new long[deployment.fileCount()];
    }

    public void addListener(DeploymentDownloaderListener listener) {
        listeners.add(listener);
    }

    public void cancel() {
        canceled = true;
        downloading = false;
        for (long id : downloadIds) {
            if (id > -1) {
                downloadManager.remove(id);
            }
        }
        notifyDeploymentDownloadCanceled();
    }

    @Override
    protected void onPreExecute() {
        canceled = false;
        String msg = progressMsg();
        notifyDeploymentDownloadProgressUpdate(msg, 0);
    }

    @Override
    protected Void doInBackground(Void... nothing) {
        List<String> osmUrls = deployment.osmUrls();
        int i = 0;
        for (String osmUrl : osmUrls) {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(osmUrl));
            request.setDestinationInExternalPublicDir(ExternalStorage.getOSMDirRelativeToExternalDir(), Deployment.fileNameFromUrl(osmUrl));
            long downloadId = downloadManager.enqueue(request);
            downloadIds[i++] = downloadId;
        }
        List<String> mbtilesUrls = deployment.mbtilesUrls();
        for (String mbtilesUrl : mbtilesUrls) {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(mbtilesUrl));
            request.setDestinationInExternalPublicDir(ExternalStorage.getMBTilesDirRelativeToExternalDir(), Deployment.fileNameFromUrl(mbtilesUrl));
            long downloadId = downloadManager.enqueue(request);
            downloadIds[i++] = downloadId;
        }
        pollDownloadManager();
        return null;
    }

    @Override
    protected void onProgressUpdate(Void... nothing) {
        String msg = progressMsg();
        notifyDeploymentDownloadProgressUpdate(msg, bytesDownloaded);
    }

    @Override
    protected void onPostExecute(Void nothing) {
        if (canceled) return;
        notifyDeploymentDownloadComplete();
    }

    private String progressMsg() {
        return "Downloading deployment. "
                + filesCompleted + "/"
                + deployment.fileCount()
                + " files. "
                + ((double)bytesDownloaded) / 1000000.0 + " MB.";
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

    private void notifyDeploymentDownloadCanceled() {
        for (DeploymentDownloaderListener listener : listeners) {
            listener.onDeploymentDownloadCancel();
        }
    }

    private void pollDownloadManager() {
        while (downloading) {
            DownloadManager.Query q = new DownloadManager.Query();
            q.setFilterById(downloadIds);
            Cursor cursor = downloadManager.query(q);
            bytesDownloaded = 0;
            filesCompleted = 0;
            while(cursor.moveToNext()) {
                bytesDownloaded += cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                if (status != DownloadManager.STATUS_PENDING && status != DownloadManager.STATUS_RUNNING) {
                    ++filesCompleted;
                }
            }
            if (!canceled) {
                publishProgress();
            }
            if (deployment.fileCount() == filesCompleted) {
                downloading = false;
            }
            // throttle the thread
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
