package org.redcross.openmapkit.deployments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.redcross.openmapkit.R;
import org.redcross.openmapkit.ZXingActivity;

import java.net.MalformedURLException;
import java.net.URL;


public class DeploymentsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private URL pendingQrUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // =========== SCAFFOLDING ============
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deployments);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(android.os.Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(getResources().getColor(R.color.osm_light_green));
        }

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

        SharedPreferences omkServerUrlPref = getSharedPreferences("org.redcross.openmapkit.OMK_SERVER_URL", Context.MODE_PRIVATE);
        String omkServerUrl = omkServerUrlPref.getString("omkServerUrl", null);
        if (omkServerUrl != null) {
            Deployments.singleton().fetch(this, omkServerUrl);
        } else {
            inputOMKServer();
        }
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
        if (item.getItemId() == R.id.serverUrlButton) {
            inputOMKServer();
            return true;
        }
        return false;
    }

    public void scanFieldPaper(View view) {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setCaptureActivity(ZXingActivity.class);
        integrator.setOrientationLocked(false);
        integrator.setPrompt("Place a field paper QR code inside the viewfinder to scan.");
        integrator.setBeepEnabled(true);
        integrator.initiateScan();
    }

    public void deploymentsFetched(Deployments.Status status) {
        if (status == Deployments.Status.SERVER_NOT_FOUND) {
            Snackbar.make(findViewById(R.id.deploymentsActivity),
                    "OpenMapKit Server not found at: " + Deployments.singleton().omkServerUrl(),
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
            return;
        }
        if (status == Deployments.Status.OFFLINE) {
            Snackbar.make(findViewById(R.id.deploymentsActivity),
                    "Showing downloaded deployments only. Connect to OpenMapKit Server to fetch more deployments.",
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
            // Continue on, we want to see the offline deployments...
        }
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        DeploymentsRecyclerAdapter adapter = new DeploymentsRecyclerAdapter(DeploymentsActivity.this);
        recyclerView.setAdapter(adapter);
        if (pendingQrUrl != null) {
            findDeployment(pendingQrUrl);
            pendingQrUrl = null;
        }

    }

    private void inputOMKServer() {
        final SharedPreferences omkServerUrlPref = getSharedPreferences("org.redcross.openmapkit.OMK_SERVER_URL", Context.MODE_PRIVATE);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("OpenMapKit Server");
        builder.setMessage("Please enter the URL of OpenMapKit Server.");
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
                if (omkServerUrl.indexOf("http") != 0) {
                    omkServerUrl = "http://" + omkServerUrl;
                }
                SharedPreferences.Editor editor = omkServerUrlPref.edit();
                editor.putString("omkServerUrl", omkServerUrl);
                editor.apply();
                progressBar.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                Deployments.singleton().fetch(DeploymentsActivity.this, omkServerUrl);
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Log.d("DeploymentsActivity", "Cancelled scan");
                Toast.makeText(this, "Cancelled QR Code Scan", Toast.LENGTH_LONG).show();
            } else {
                processQR(result.getContents());
            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void processQR(String qr) {
        try {
            URL url = new URL(qr);
            if (isDeploymentServerQRServer(url)) {
                findDeployment(url);
            } else {
                setServerAndFetchForQR(url);
            }
        }
        // not a valid url
        catch (MalformedURLException e) {
            Snackbar.make(findViewById(R.id.deploymentsActivity),
                    "The QR code you scanned does not give us a valid URL!",
                    Snackbar.LENGTH_LONG)
                    .setAction("Retry", new View.OnClickListener() {
                        // undo action
                        @Override
                        public void onClick(View v) {
                            scanFieldPaper(null);
                        }
                    })
                    .setActionTextColor(Color.rgb(126, 188, 111))
                    .show();
        }

    }

    private boolean isDeploymentServerQRServer(URL url) {
        String protocol = url.getProtocol();
        String authority = url.getAuthority();
        String urlStr = protocol + "://" + authority;

        SharedPreferences omkServerUrlPref = getSharedPreferences("org.redcross.openmapkit.OMK_SERVER_URL", Context.MODE_PRIVATE);
        String omkServerUrl = omkServerUrlPref.getString("omkServerUrl", "");

        return omkServerUrl.equals(urlStr) || omkServerUrl.equals(urlStr + "/");
    }

    private void setServerAndFetchForQR(URL url) {
        final SharedPreferences omkServerUrlPref = getSharedPreferences("org.redcross.openmapkit.OMK_SERVER_URL", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = omkServerUrlPref.edit();
        String protocol = url.getProtocol();
        String authority = url.getAuthority();
        String omkServerUrl = protocol + "://" + authority;
        editor.putString("omkServerUrl", omkServerUrl);
        editor.apply();

        pendingQrUrl = url;
        Deployments.singleton().fetch(this, omkServerUrl);
    }

    private void findDeployment(URL url) {
        String slug = findSlug(url.getPath());
        int idx = Deployments.singleton().getIdxForName(slug);
        if (idx > -1) {
            Intent deploymentDetailsActivity = new Intent(this, DeploymentDetailsActivity.class);
            deploymentDetailsActivity.putExtra("POSITION", idx);
            startActivity(deploymentDetailsActivity);
        } else {
            Snackbar.make(findViewById(R.id.deploymentsActivity),
                    "There is no deployment for the field paper: " + slug,
                    Snackbar.LENGTH_LONG)
                    .setAction("Retry", new View.OnClickListener() {
                        // undo action
                        @Override
                        public void onClick(View v) {
                            scanFieldPaper(null);
                        }
                    })
                    .setActionTextColor(Color.rgb(126, 188, 111))
                    .show();
        }
    }

    private String findSlug(String path) {
        String[] urlTokens = path.split("/");
        int len = urlTokens.length;
        for (int i = 0; i < len; i++) {
            String t = urlTokens[i];
            if (t.equals("atlases")) {
                // check to see if there is a token after atlases
                if (i + 1 >= len) {
                    return null;
                }
                return urlTokens[i+1];
            }
        }
        return null;
    }

}
