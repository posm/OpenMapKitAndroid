package org.redcross.openmapkit.deployments;

import android.os.AsyncTask;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.redcross.openmapkit.ExternalStorage;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is a simple container for the JSON response from the
 * deployments endpoint from OpenMapKit Server.
 */
public class Deployments {
    private static Deployments singleton = new Deployments();

    private List<Deployment> deployments = new ArrayList<>();
    private DeploymentsActivity activity;
    private String omkServerUrl;

    /**
     * When we add a deployment to deployments, we keep
     * track of the name to index in this HashMap. This lets
     * us quickly find a deployment by name.
     */
    private Map<String, Integer> nameToIdx = new HashMap<>();


    public static Deployments singleton() {
        return singleton;
    }

    private void putDeployment(JSONObject deploymentJson) {
        Deployment deployment = new Deployment(deploymentJson);
        String name = deployment.name();
        if (name == null) return; // check if deployment has name. bogus if not.
        Integer idx = nameToIdx.get(name);
        // new deployment
        if (idx == null) {
            nameToIdx.put(name, deployments.size());
            deployments.add(deployment);
        } else {
            deployments.set(idx, deployment);
        }
    }

    public void fetch(DeploymentsActivity activity, String url) {
        this.activity = activity;
        omkServerUrl = url;
        if (url == null) {
            activity.deploymentsFetched(false);
            return;
        }
        new DeploymentsListHttpTask().execute(url);
    }

    public Deployment get(int idx) {
        return deployments.get(idx);
    }

    public int getIdxForName(String name) {
        Integer idx = nameToIdx.get(name);
        if (idx == null) {
            return -1;
        }
        return idx;
    }

    public int size() {
        return deployments.size();
    }

    public String omkServerUrl() {
        return omkServerUrl;
    }

    private void parseJsonFromApi(String json) {
        try {
            JSONArray jsonFromAPI = new JSONArray(json);
            int len = jsonFromAPI.length();
            for (int i = 0; i < len; ++i) {
                JSONObject obj = jsonFromAPI.optJSONObject(i);
                if (obj != null) {
                    obj.put("api", true);
                    putDeployment(obj);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Fetches the deployment JSON currently on disk
     */
    private void fetchFromExternalStorage() {
        List<File> files = ExternalStorage.allDeploymentJSONFiles();
        for (File f : files) {
            try {
                String jsonStr = FileUtils.readFileToString(f, "UTF-8");
                JSONObject obj = new JSONObject(jsonStr);
                // We can later look and know the deployment JSON came
                // from ExternalStorage rather than the API.
                obj.put("persisted", true);
                putDeployment(obj);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public class DeploymentsListHttpTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Boolean doInBackground(String... params) {
            Boolean result = false;
            fetchFromExternalStorage();
            HttpURLConnection urlConnection;
            try {
                String urlStr = params[0];
                String endpoint;
                if (urlStr.charAt(urlStr.length()-1) == '/') {
                    endpoint = urlStr + "omk/deployments";
                } else {
                    endpoint = urlStr + "/omk/deployments";
                }
                URL url = new URL(endpoint);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setConnectTimeout(3000);
                urlConnection.setReadTimeout(7000);
                int statusCode = urlConnection.getResponseCode();

                // 200 represents HTTP OK
                if (statusCode == 200) {
                    BufferedReader r = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = r.readLine()) != null) {
                        response.append(line);
                    }
                    parseJsonFromApi(response.toString());
                    result = true; // Successful
                } else {
                    result = false; //"Failed to fetch data!";
                }
            } catch (Exception e) {
                result = false;
            }
            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            activity.deploymentsFetched(result);
        }
    }
}
