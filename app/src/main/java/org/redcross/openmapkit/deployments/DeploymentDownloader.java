package org.redcross.openmapkit.deployments;


import android.app.DownloadManager;
import android.os.AsyncTask;

public class DeploymentDownloader extends AsyncTask<Void, String, Long> {

    public DeploymentDownloader() {

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
