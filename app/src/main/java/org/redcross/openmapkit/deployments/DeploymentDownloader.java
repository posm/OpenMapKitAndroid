package org.redcross.openmapkit.deployments;


import android.app.DownloadManager;
import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Vector;

public class DeploymentDownloader extends AsyncTask<Void, String, Long> {

    private List<DeploymentDownloaderListener> listeners = new Vector<>();

    public DeploymentDownloader(JSONObject deployment) {
        try {
            deployment.getJSONArray("files");
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
