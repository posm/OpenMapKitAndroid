package org.redcross.openmapkit.deployments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_deployments, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        super.onOptionsItemSelected(item);

        inputOMKServer();
        return true;
    }

    public void deploymentsFetched(boolean success) {
        // Download complete. Let us update UI
        progressBar.setVisibility(View.GONE);
        if (success) {
            DeploymentsRecyclerAdapter adapter = new DeploymentsRecyclerAdapter(DeploymentsActivity.this);
            recyclerView.setAdapter(adapter);
        } else {
            Snackbar.make(findViewById(R.id.mapActivity),
                    "Failed to connect to OpenMapKit Server!",
                    Snackbar.LENGTH_LONG)
                    .setAction("Setup", new View.OnClickListener() {
                        // undo action
                        @Override
                        public void onClick(View v) {
                            inputOMKServer();
                        }
                    })
                    .setActionTextColor(Color.rgb(126, 188, 111))
                    .show();
        }
    }

    private void inputOMKServer() {
        final SharedPreferences omkServerUrlPref = getSharedPreferences("org.redcross.openmapkit.OMK_SERVER_URL", Context.MODE_PRIVATE);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("OpenMapKit Server");
        builder.setMessage("Please enter the URL of the OpenMapKit Server Deployments REST end point.");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        String omkServerUrl = omkServerUrlPref.getString("omkServerUrl", null);
        if (omkServerUrl != null) {
            input.setText(omkServerUrl);
        }
        builder.setView(input);
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                // just dismiss
            }
        });
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String omkServerUrl = input.getText().toString();
                SharedPreferences.Editor editor = omkServerUrlPref.edit();
                editor.putString("omkServerUrl", omkServerUrl);
                editor.apply();
            }
        });
        builder.show();
    }

}
