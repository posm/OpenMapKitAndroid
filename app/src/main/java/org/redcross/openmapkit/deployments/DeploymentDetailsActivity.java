package org.redcross.openmapkit.deployments;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;
import org.redcross.openmapkit.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DeploymentDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // =========== SCAFFOLDING ============
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deployment_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }
        // ====================================

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

    }

}
