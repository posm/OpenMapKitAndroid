package org.redcross.openmapkit.deployments;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.redcross.openmapkit.R;


public class DeploymentsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // =========== SCAFFOLDING ============
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deployments);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // FAB code just in case we want it.
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }
        // ====================================


        // Initialize recycler view
        recyclerView = (RecyclerView) findViewById(R.id.deploymentsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);

        setProgressBarIndeterminateVisibility(true);
        Deployments.singleton().fetch(this);
    }

    public void deploymentsFetched(boolean success) {
        // Download complete. Let us update UI
        progressBar.setVisibility(View.GONE);
        if (success) {
            DeploymentsRecyclerAdapter adapter = new DeploymentsRecyclerAdapter(DeploymentsActivity.this);
            recyclerView.setAdapter(adapter);
        } else {
            Toast.makeText(DeploymentsActivity.this, "Failed to connect to OpenMapKit Server!", Toast.LENGTH_SHORT).show();
        }
    }


}
