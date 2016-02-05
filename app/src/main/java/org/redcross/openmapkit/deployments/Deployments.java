package org.redcross.openmapkit.deployments;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * This is a simple container for the JSON response from the
 * deployments endpoint from OpenMapKit Server.
 */
public class Deployments {
    private static Deployments singleton = new Deployments();

    private JSONArray deploymentsArray = new JSONArray();
    private DeploymentsActivity activity;


    public static Deployments singleton() {
        return singleton;
    }

    public void fetch(DeploymentsActivity activity, String url) {
        this.activity = activity;
        if (url == null) {
            activity.deploymentsFetched(false);
            return;
        }
        new DeploymentsListHttpTask().execute(url);
    }

    public Deployment get(int idx) {
        return new Deployment(deploymentsArray.optJSONObject(idx));
    }

    public int size() {
        return deploymentsArray.length();
    }

    private void parseJSON(String json) {
        try {
            deploymentsArray = new JSONArray(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public class DeploymentsListHttpTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Boolean doInBackground(String... params) {
            Boolean result = false;
            HttpURLConnection urlConnection;
            try {
                URL url = new URL(params[0]);
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
                    parseJSON(response.toString());
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
