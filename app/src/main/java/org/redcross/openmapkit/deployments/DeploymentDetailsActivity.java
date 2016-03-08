package org.redcross.openmapkit.deployments;

import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONObject;
import org.redcross.openmapkit.R;


public class DeploymentDetailsActivity extends AppCompatActivity implements View.OnClickListener, DeploymentDownloaderListener {

    private Deployment deployment;
    private DeploymentDownloader downloader;

    private FloatingActionButton fab;
    private TextView progressTextView;
    private ProgressBar progressBar;

    private enum DownloadState {
        FRESH, DOWNLOADING, CANCELED, ERROR, COMPLETE
    }
    private DownloadState downloadState = DownloadState.FRESH;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deployment_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        /**
         * Getting deployment object from JSON
         */
        int position = getIntent().getIntExtra("POSITION", 0);
        deployment = Deployments.singleton().get(position);

        String name = deployment.json().optString("name");
        TextView nameTextView = (TextView)findViewById(R.id.nameTextView);
        nameTextView.setText(name);

        JSONObject manifest = deployment.json().optJSONObject("manifest");
        if (manifest != null) {
            String description = manifest.optString("description");
            TextView descriptionTextView = (TextView)findViewById(R.id.descriptionTextView);
            descriptionTextView.setText(description);
        }

        progressTextView = (TextView)findViewById(R.id.progressTextView);
        progressTextView.setText(deployment.fileCount() + " files. Total Size: " + deployment.totalSizeMB());

//        progressBar = (ProgressBar)findViewById(R.id.progressBar);
//        progressBar.setMax(100);

        /**
         * SETUP FOR EXPANDABLE LIST VIEW FOR MBTILES AND OSM FILES
         */
        ExpandableListView expandableListView = (ExpandableListView) findViewById(R.id.expandableListView);
        FileExpandableListAdapter fileExpandableListAdapter = new FileExpandableListAdapter(this, position);
        expandableListView.setAdapter(fileExpandableListAdapter);

        /**
         * FAB to initiate downloads.
         */
        fab = (FloatingActionButton)findViewById(R.id.fab);
        fab.setOnClickListener(this);
    }

    private void setCancelFab() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_clear_white_36dp, getApplicationContext().getTheme()));
        } else {
            fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_clear_white_36dp));
        }
    }

    private void setDeleteFab() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_delete_white_36dp, getApplicationContext().getTheme()));
        } else {
            fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_delete_white_36dp));
        }
    }

    private void setDownloadFab() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_file_download_white_36dp, getApplicationContext().getTheme()));
        } else {
            fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_file_download_white_36dp));
        }
    }

    /**
     * FAB OnClickListener method
     * @param v
     */
    @Override
    public void onClick(View v) {
        if (downloadState == DownloadState.FRESH) {
            startDownload();
            return;
        }
        if (downloadState == DownloadState.DOWNLOADING) {
            cancelDownload();
            return;
        }
        if (downloadState == DownloadState.COMPLETE) {
            deleteDownload();
            return;
        }
        if (downloadState == DownloadState.CANCELED) {
            startDownload();
            return;
        }
        if (downloadState == DownloadState.ERROR) {
            startDownload();
        }
    }

    private void startDownload() {
        /**
         * Instantiate downloader.
         */
        downloader = new DeploymentDownloader(deployment, this);
        downloader.addListener(this);
        downloader.execute();
        setCancelFab();
    }

    private void cancelDownload() {
        if (downloader != null) {
            downloader.cancel();
        }
    }

    private void deleteDownload() {
        // TODO Do something!
    }


    /**
     * DeploymentDownloader Listener Methods
     */

    @Override
    public void onDeploymentDownloadProgressUpdate(String msg, long bytesDownloaded) {
        downloadState = DownloadState.DOWNLOADING;
        progressTextView.setText(msg);
        progressTextView.setTextColor(getResources().getColor(R.color.black));
        progressTextView.setTypeface(null, Typeface.NORMAL);
//        progressBar.setProgress((int)bytesDownloaded);
    }

    @Override
    public void onDeploymentDownloadCancel() {
        downloadState = DownloadState.CANCELED;
        setDownloadFab();
        progressTextView.setText(R.string.deploymentDownloadCanceled);
        progressTextView.setTextColor(getResources().getColor(R.color.holo_red_light));
        progressTextView.setTypeface(null, Typeface.BOLD);
    }

    @Override
    public void onDeploymentDownloadError(String msg) {
        downloadState = DownloadState.ERROR;
        setDownloadFab();
        progressTextView.setText(msg);
        progressTextView.setTextColor(getResources().getColor(R.color.holo_red_light));
        progressTextView.setTypeface(null, Typeface.BOLD);
    }

    @Override
    public void onDeploymentDownloadComplete() {
        downloadState = DownloadState.COMPLETE;
        setDeleteFab();
        progressTextView.setText(R.string.deploymentDownloadComplete);
        progressTextView.setTextColor(getResources().getColor(R.color.osm_dark_green));
        progressTextView.setTypeface(null, Typeface.BOLD);
    }


}
