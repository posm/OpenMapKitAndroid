package org.redcross.openmapkit.deployments;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONObject;
import org.redcross.openmapkit.ExternalStorage;
import org.redcross.openmapkit.MapActivity;
import org.redcross.openmapkit.R;


public class DeploymentDetailsActivity extends AppCompatActivity implements View.OnClickListener, DeploymentDownloaderListener {

    private Deployment deployment;
    private DeploymentDownloader downloader;

    private FloatingActionButton fab;
    private TextView progressTextView;
    private ProgressBar progressBar;

    private enum DownloadState {
        FRESH, DOWNLOADING, CANCELED, ERROR, COMPLETE, DELETED
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

        if(android.os.Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(getResources().getColor(R.color.osm_light_green));
        }

        Intent intent = getIntent();
        int position = intent.getIntExtra("POSITION", 0);
        deployment = Deployments.singleton().get(position);

        String title = deployment.title();
        TextView nameTextView = (TextView)findViewById(R.id.nameTextView);
        nameTextView.setText(title);

        JSONObject manifest = deployment.json().optJSONObject("manifest");
        if (manifest != null) {
            String description = manifest.optString("description");
            TextView descriptionTextView = (TextView)findViewById(R.id.descriptionTextView);
            descriptionTextView.setText(description);
        }

        progressTextView = (TextView)findViewById(R.id.progressTextView);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        progressBar.setMax((int)deployment.totalSize());

        /**
         * SETUP FOR EXPANDABLE LIST VIEW FOR MBTILES AND OSM FILES
         */
        ExpandableListView expandableListView = (ExpandableListView) findViewById(R.id.expandableListView);
        FileExpandableListAdapter fileExpandableListAdapter = new FileExpandableListAdapter(this, deployment);
        expandableListView.setAdapter(fileExpandableListAdapter);

        /**
         * FAB to initiate downloads.
         */
        fab = (FloatingActionButton)findViewById(R.id.fab);
        fab.setOnClickListener(this);

        setFreshUIState();
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

    private void setFreshUIState() {
        progressTextView.setText(deployment.fileCount() + " files. Total Size: " + deployment.totalSizeMB());
        progressTextView.setTextColor(getResources().getColor(R.color.black));
        progressTextView.setTypeface(null, Typeface.NORMAL);
        progressBar.setProgress(0);
        setDownloadFab();
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
            return;
        }
        if (downloadState == DownloadState.DELETED) {
            startDownload();
        }
    }

    private void startDownload() {
        if (deployment.fileCount() > 0) {
            /**
             * Instantiate downloader.
             */
            downloader = new DeploymentDownloader(deployment, this);
            downloader.addListener(this);
            downloader.execute();
            setCancelFab();
        } else {
            Snackbar.make(findViewById(R.id.deploymentDetailsActivity),
                    "Does not contain any files. Please check that your server deployment is complete.",
                    Snackbar.LENGTH_LONG)
                    .setAction("Retry", new View.OnClickListener() {
                        // undo action
                        @Override
                        public void onClick(View v) {
                            startDownload();
                        }
                    })
                    .setActionTextColor(Color.rgb(126, 188, 111))
                    .show();
        }

    }

    private void cancelDownload() {
        if (downloader != null) {
            downloader.cancel();
        }
    }

    private void deleteDownload() {
        Snackbar.make(findViewById(R.id.deploymentDetailsActivity),
                "Are you sure you want to delete this deployment?",
                Snackbar.LENGTH_LONG)
                .setAction("Delete", new View.OnClickListener() {
                    // undo action
                    @Override
                    public void onClick(View v) {
                        ExternalStorage.deleteDeployment(deployment.name());
                        downloadState = DownloadState.DELETED;
                        setFreshUIState();
                    }
                })
                .setActionTextColor(Color.RED)
                .show();
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
        progressBar.setProgress((int) bytesDownloaded);
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

    public void fabCheckoutDeploymentClick(View v) {
        deployment.addToMap();
        Intent mapActivity = new Intent(getApplicationContext(), MapActivity.class);
        startActivity(mapActivity);
    }

}
