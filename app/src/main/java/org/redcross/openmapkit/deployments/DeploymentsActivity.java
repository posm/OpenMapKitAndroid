package org.redcross.openmapkit.deployments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.redcross.openmapkit.R;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DeploymentsActivity extends AppCompatActivity {
    private List<Deployment> deploymentsList = new ArrayList<>();
    private RecyclerView recyclerView;
    private DeploymentsRecyclerAdapter adapter;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // =========== SCAFFOLDING ============
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deployments);
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


        // Initialize recycler view
        recyclerView = (RecyclerView) findViewById(R.id.deploymentsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);

        String url = "http://54.200.124.199:3210/deployments";
        new DeploymentsListHttpTask().execute(url);

    }

    private void parseResult(String result) {
        try {
            JSONArray deploymentsArr = new JSONArray(result);

            for (int i = 0; i < deploymentsArr.length(); ++i) {
                JSONObject deploymentObj = deploymentsArr.optJSONObject(i);
                Deployment deployment = new Deployment();
                deployment.setName(deploymentObj.optString("name"));

                JSONObject manifest = deploymentObj.optJSONObject("manifest");
                if (manifest != null) {
                    deployment.setDescription(manifest.optString("description"));
                }
                deploymentsList.add(deployment);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    private class DeploymentsListHttpTask extends AsyncTask<String, Void, Boolean> {
        private static final String TAG = "DeploymentsListHttpTask";

        @Override
        protected void onPreExecute() {
            setProgressBarIndeterminateVisibility(true);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            Boolean result = false;
            HttpURLConnection urlConnection;
            try {
                URL url = new URL(params[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                int statusCode = urlConnection.getResponseCode();

                // 200 represents HTTP OK
                if (statusCode == 200) {
                    BufferedReader r = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = r.readLine()) != null) {
                        response.append(line);
                    }
                    parseResult(response.toString());
                    result = true; // Successful
                } else {
                    result = false; //"Failed to fetch data!";
                }
            } catch (Exception e) {
                Log.d(TAG, e.getLocalizedMessage());
            }
            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            // Download complete. Let us update UI
            progressBar.setVisibility(View.GONE);

            if (result) {
                adapter = new DeploymentsRecyclerAdapter(DeploymentsActivity.this, deploymentsList);
                recyclerView.setAdapter(adapter);
            } else {
                Toast.makeText(DeploymentsActivity.this, "Failed to connect to OpenMapKit Server!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
