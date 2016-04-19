package org.redcross.openmapkit.deployments;


import android.app.Activity;
import android.app.DownloadManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;
import org.redcross.openmapkit.ExternalStorage;

import java.util.List;
import java.util.Vector;

public class DeploymentDownloader extends AsyncTask<Void, Void, Void> {
    private List<DeploymentDownloaderListener> listeners = new Vector<>();
    private DownloadManager downloadManager;
    private Deployment deployment;
    private long[] downloadIds;

    private boolean downloading = false;
    private boolean canceled = false;
    private long bytesDownloaded = 0;
    private int filesCompleted = 0;

    public DeploymentDownloader(Deployment deployment, DeploymentDetailsActivity activity) {
        downloadManager = (DownloadManager)activity.getSystemService(Activity.DOWNLOAD_SERVICE);
        this.deployment = deployment;
        downloadIds = new long[deployment.fileCount()];
        addListener(activity);
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
        JSONArray osms = deployment.osm();
        int osmsLen = osms.length();
        int idx = 0;
        for (int i = 0; i < osmsLen; ++i) {
            JSONObject osm = osms.optJSONObject(i);
            if (osm == null) continue;
            String osmUrl = osm.optString("url");
            if (osmUrl == null) continue;
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(osmUrl));
            request.setDestinationInExternalPublicDir(deploymentDir, Deployment.fileNameFromUrl(osmUrl));
            long downloadId = downloadManager.enqueue(request);
            downloadIds[idx++] = downloadId;
        }
        JSONArray mbtiles = deployment.mbtiles();
        int mbtilesLen = mbtiles.length();
        for (int j = 0; j < mbtilesLen; ++j) {
            JSONObject mbtile = mbtiles.optJSONObject(j);
            if (mbtile == null) continue;
            String mbtileUrl = mbtile.optString("url");
            if (mbtileUrl == null) continue;
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(mbtileUrl));
            request.setDestinationInExternalPublicDir(deploymentDir, Deployment.fileNameFromUrl(mbtileUrl));
            long downloadId = downloadManager.enqueue(request);
            downloadIds[idx++] = downloadId;
        }
        JSONArray geojsons = deployment.geojson();
        int geojsonsLen = geojsons.length();
        for (int k = 0; k < geojsonsLen; ++k) {
            JSONObject geojson = geojsons.optJSONObject(k);
            if (geojson == null) continue;
            String geojsonUrl = geojson.optString("url");
            if (geojsonUrl == null) continue;
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(geojsonUrl));
            request.setDestinationInExternalPublicDir(deploymentDir, Deployment.fileNameFromUrl(geojsonUrl));
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
