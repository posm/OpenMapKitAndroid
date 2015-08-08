package com.mapbox.mapboxsdk.tileprovider.tilesource;

import android.os.AsyncTask;
import android.util.Log;
import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.util.NetworkUtils;
import com.mapbox.mapboxsdk.util.constants.UtilConstants;
import com.squareup.okhttp.Cache;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

/**
 * A type of tile layer that loads tiles from the internet and metadata about itself
 * with the <a href='https://github.com/mapbox/tilejson-spec'>TileJSON</a> standard.
 */
public class TileJsonTileLayer extends WebSourceTileLayer {

    private static final String TAG = "TileJsonTileLayer";

    private JSONObject tileJSON;
    private Cache cache;

    public TileJsonTileLayer(final String pId, final String url, final boolean enableSSL) {
        super(pId, url, enableSSL);

        File cacheDir =
                new File(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        try {
            cache = NetworkUtils.getCache(cacheDir, 1024);
        } catch (Exception e) {
            Log.e(TAG, "Cache creation failed.", e);
        }

        String jsonURL = this.getBrandedJSONURL();
        if (jsonURL != null) {
            fetchBrandedJSONAndInit(jsonURL);
        }
    }

    private void initWithTileJSON(JSONObject aTileJSON) {
        this.setTileJSON((aTileJSON != null) ? aTileJSON : new JSONObject());
        if (aTileJSON != null) {
            if (this.tileJSON.has("tiles")) {
                try {
                    setURL(this.tileJSON.getJSONArray("tiles").getString(0).replace(".png", "{2x}.png"));
                } catch (JSONException e) {
                    Log.e(TAG, "Couldn't set tile url", e);
                }
            }
            mMinimumZoomLevel = getJSONFloat(this.tileJSON, "minzoom");
            mMaximumZoomLevel = getJSONFloat(this.tileJSON, "maxzoom");
            mName = this.tileJSON.optString("name");
            mDescription = this.tileJSON.optString("description");
            mAttribution = this.tileJSON.optString("attribution");
            mLegend = this.tileJSON.optString("legend");

            double[] center = getJSONDoubleArray(this.tileJSON, "center", 3);
            if (center != null) {
                mCenter = new LatLng(center[0], center[1], center[2]);
            }
            double[] bounds = getJSONDoubleArray(this.tileJSON, "bounds", 4);
            if (bounds != null) {
                mBoundingBox = new BoundingBox(bounds[3], bounds[2], bounds[1], bounds[0]);
            }
        }
        if (UtilConstants.DEBUGMODE) {
            Log.d(TAG, "TileJSON " + this.tileJSON.toString());
        }
    }

    public JSONObject getTileJSON() {
        return tileJSON;
    }

    public void setTileJSON(JSONObject aTileJSON) {
        this.tileJSON = aTileJSON;
    }

    private float getJSONFloat(JSONObject JSON, String key) {
        float defaultValue = 0;
        if (JSON.has(key)) {
            try {
                return (float) JSON.getDouble(key);
            } catch (JSONException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    private double[] getJSONDoubleArray(JSONObject JSON, String key, int length) {
        double[] defaultValue = null;
        if (JSON.has(key)) {
            try {
                boolean valid = false;
                double[] result = new double[length];
                Object value = JSON.get(key);
                if (value instanceof JSONArray) {
                    JSONArray array = ((JSONArray) value);
                    if (array.length() == length) {
                        for (int i = 0; i < array.length(); i++) {
                            result[i] = array.getDouble(i);
                        }
                        valid = true;
                    }
                } else {
                    String[] array = JSON.getString(key).split(",");
                    if (array.length == length) {
                        for (int i = 0; i < array.length; i++) {
                            result[i] = Double.parseDouble(array[i]);
                        }
                        valid = true;
                    }
                }
                if (valid) {
                    return result;
                }
            } catch (JSONException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    byte[] readFully(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        for (int count; (count = in.read(buffer)) != -1;) {
            out.write(buffer, 0, count);
        }
        return out.toByteArray();
    }

    private void fetchBrandedJSONAndInit(String url) {
        new RetrieveJSONTask() {
            @Override
            protected void onPostExecute(JSONObject jsonObject) {
                initWithTileJSON(jsonObject);
            }
        } .execute(url);
    }

    protected String getBrandedJSONURL() {
        return null;
    }

    class RetrieveJSONTask extends AsyncTask<String, Void, JSONObject> {
        protected JSONObject doInBackground(String... urls) {
            InputStream in = null;
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = NetworkUtils.getHttpURLConnection(url, cache);
                in = connection.getInputStream();
                byte[] response = readFully(in);
                String result = new String(response, "UTF-8");
                return new JSONObject(result);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error closing InputStream: " + e.toString());
                }
            }
        }
    }
}
