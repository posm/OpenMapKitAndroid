package org.redcross.openmapkit.deployments;

import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;

import org.json.JSONObject;
import org.redcross.openmapkit.R;


public class DeploymentDetailsActivity extends AppCompatActivity {

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
         * Getting deployment fields we want from the JSON and stuffing it
         * where it needs to go!
         */
        int position = getIntent().getIntExtra("POSITION", 0);
        JSONObject deployment = Deployments.singleton().get(position);

        String name = deployment.optString("name");
        TextView nameTextView = (TextView)findViewById(R.id.nameTextView);
        nameTextView.setText(name);

        JSONObject manifest = deployment.optJSONObject("manifest");
        if (manifest != null) {
            String description = manifest.optString("description");
            TextView descriptionTextView = (TextView)findViewById(R.id.descriptionTextView);
            descriptionTextView.setText(description);
        }

        /**
         * SETUP FOR EXPANDABLE LIST VIEW FOR MBTILES AND OSM FILES
         */
        ExpandableListView expandableListView = (ExpandableListView) findViewById(R.id.expandableListView);
        ExpandableListAdapter expandableListAdapter = new ExpandableListAdapter(this, position);
        expandableListView.setAdapter(expandableListAdapter);

        /**
         * FAB to initiate downloads.
         */
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setCancelFab(fab);

            }
        });
    }

    private void setCancelFab(FloatingActionButton fab) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_clear_white_36dp, getApplicationContext().getTheme()));
        } else {
            fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_clear_white_36dp));
        }
    }

    private void setDeleteFab(FloatingActionButton fab) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_delete_white_36dp, getApplicationContext().getTheme()));
        } else {
            fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_delete_white_36dp));
        }
    }

    private void setDownloadFab(FloatingActionButton fab) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_file_download_white_36dp, getApplicationContext().getTheme()));
        } else {
            fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_file_download_white_36dp));
        }
    }
}
