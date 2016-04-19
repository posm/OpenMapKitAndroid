package org.redcross.openmapkit.deployments;


import android.app.Activity;
import android.app.DownloadManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;
import org.redcross.openmapkit.ExternalStorage;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DeploymentDownloader extends AsyncTask<Void, Void, Void> {
    private Set<DeploymentDownloaderListener> listeners = new HashSet<>();
    private DownloadManager downloadManager;
    private Deployment deployment;
    private long[] downloadIds;

    private boolean downloading = false;
    private boolean canceled = false;
    private long bytesDownloaded = 0;
    private int filesCompleted = 0;

    public DeploymentDownloader(Deployment deployment, Activity activity) {
        downloadManager = (DownloadManager)activity.getSystemService(Activity.DOWNLOAD_SERVICE);
        this.deployment = deployment;
        downloadIds = new long[deployment.fileCount()];
        if (activity instanceof DeploymentDownloaderListener) {
            addListener((DeploymentDownloaderListener)activity);
        }
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

    public boolean isDownloading() {
        return downloading;
    }

    @Override
    protected void onPreExecute() {
        canceled = false;
        downloading = true;
        String msg = progressMsg();
        notifyDeploymentDownloadProgressUpdate(msg, 0);
    }

    @Override
    protected Void doInBackground(Void... nothing) {
        deployment.writeJSONToDisk();
        String deploymentDir = ExternalStorage.deploymentDirRelativeToExternalDir(deployment.name());

        int idx = 0;
        List<JSONObject> files = deployment.filesToDownload();
        for (JSONObject f : files) {
            String url = f.optString("url");
            if (url == null) continue;
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setDestinationInExternalPublicDir(deploymentDir, Deployment.fileNameFromUrl(url));
            long downloadId = downloadManager.enqueue(request);
            downloadIds[idx++] = downloadId;
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

    private void notifyDeploymentDownloadProgressUpdate(String msg, long bytesDownloaded) {
        for (DeploymentDownloaderListener listener : listeners) {
            if (listener != null) {
                listener.onDeploymentDownloadProgressUpdate(msg, bytesDownloaded);
            }
        }
    }

    private void notifyDeploymentDownloadComplete() {
        for (DeploymentDownloaderListener listener : listeners) {
            if (listener != null) {
                listener.onDeploymentDownloadComplete();
            }
        }
    }

    private void notifyDeploymentDownloadCanceled() {
        for (DeploymentDownloaderListener listener : listeners) {
            if (listener != null) {
                listener.onDeploymentDownloadCancel();
            }
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
