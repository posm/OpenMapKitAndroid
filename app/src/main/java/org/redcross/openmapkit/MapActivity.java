package org.redcross.openmapkit;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.TouchDelegate;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.views.MapView;
import com.spatialdev.osm.events.OSMSelectionListener;
import com.spatialdev.osm.model.OSMElement;

import org.redcross.openmapkit.odkcollect.ODKCollectHandler;
import org.redcross.openmapkit.tagswipe.TagSwipeActivity;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class MapActivity extends ActionBarActivity implements OSMSelectionListener {

    protected static final String PREVIOUS_LAT = "org.redcross.openmapkit.PREVIOUS_LAT";
    protected static final String PREVIOUS_LNG = "org.redcross.openmapkit.PREVIOUS_LNG";
    protected static final String PREVIOUS_ZOOM = "org.redcross.openmapkit.PREVIOUS_ZOOM";
    
    private static String version = "";

    protected MapView mapView;
    protected ListView mTagListView;
    protected ImageButton mCloseListViewButton;
    protected LinearLayout mTopLinearLayout;
    protected LinearLayout mBottomLinearLayout;
    protected TextView mTagTextView;
    protected Basemap basemap;
    protected TagListAdapter tagListAdapter;

    /**
     * intent request codes
     */
    private static final int ODK_COLLECT_TAG_ACTIVITY_CODE = 2015;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        determineVersion();
        
        if(android.os.Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(getResources().getColor(R.color.osm_light_green));
        }

        // create directory structure for app if needed
        ExternalStorage.checkOrCreateAppDirs();
        
        // Register the intent to the ODKCollect handler
        // This will determine if we are in ODK Collect Mode or not.
        ODKCollectHandler.registerIntent(getIntent());

        //set layout
        setContentView(R.layout.activity_map);

        //get the layout the ListView is nested in
        mBottomLinearLayout = (LinearLayout)findViewById(R.id.bottomLinearLayout);

        //the ListView from layout
        mTagListView = (ListView)findViewById(R.id.tagListView);

        //the ListView close image button
        mCloseListViewButton = (ImageButton)findViewById(R.id.imageViewCloseList);

        //get the layout the Map is nested in
        mTopLinearLayout = (LinearLayout)findViewById(R.id.topLinearLayout);

        //get map from layout
        mapView = (MapView)findViewById(R.id.mapView);
        
        // initialize basemap object
        basemap = new Basemap(this);

        initializeOsmXml();

        //add user location toggle button
        initializeLocationButton();

        positionMap();

        initializeListView();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        saveMapPosition();        
    }

    protected void saveMapPosition() {
        LatLng c = mapView.getCenter();
        float lat = (float) c.getLatitude();
        float lng = (float) c.getLongitude();
        float z = mapView.getZoomLevel();

        SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
        editor.putFloat(PREVIOUS_LAT, lat);
        editor.putFloat(PREVIOUS_LNG, lng);
        editor.putFloat(PREVIOUS_ZOOM, z);
        editor.apply();
    }

    protected void positionMap() {
        SharedPreferences pref = getPreferences(Context.MODE_PRIVATE);
        double lat = (double) pref.getFloat(PREVIOUS_LAT, -999);
        double lng = (double) pref.getFloat(PREVIOUS_LNG, -999);
        float z = pref.getFloat(PREVIOUS_ZOOM, -999);
        
        // no shared pref
        if (lat == -999 || lng == -999 || z == -999) {
            mapView.setUserLocationEnabled(true);
            mapView.goToUserLocation(true);
        } 
        // there is a shared pref
        else {
            LatLng c = new LatLng(lat, lng);
            mapView.setCenter(c);
            mapView.setZoom(z);
        }
    }
    
    /**
     * For initializing the ListView of tags
     */
    protected void initializeListView() {

        //the ListView title
        mTagTextView = (TextView)findViewById(R.id.tagTextView);
        mTagTextView.setText(R.string.tagListViewTitle);

        //hide the ListView by default
        proportionMapAndList(100, 0);

        //handle when user taps on the close button in the list view
        mCloseListViewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                proportionMapAndList(100, 0);
            }
        });

        //increase the 'hit area' of the down arrow
        View parent = findViewById(R.id.bottomLinearLayout);
        parent.post(new Runnable() {
            public void run() {

                Rect delegateArea = new Rect();
                ImageButton delegate = mCloseListViewButton;
                delegate.getHitRect(delegateArea);
                delegateArea.top -= 100;
                delegateArea.bottom += 100;
                delegateArea.left -= 100;
                delegateArea.right += 100;

                TouchDelegate expandedArea = new TouchDelegate(delegateArea, delegate);

                if (View.class.isInstance(delegate.getParent())) {
                    ((View) delegate.getParent()).setTouchDelegate(expandedArea);
                }
            }
        });

        //handle list view item taps
        mTagListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String tappedKey = tagListAdapter.getTagKeyForIndex(position);

                //launch the TagSwipeActivity and pass the key
                Intent tagSwipe = new Intent(getApplicationContext(), TagSwipeActivity.class);
                tagSwipe.putExtra("TAG_KEY", tappedKey);
                startActivityForResult(tagSwipe, ODK_COLLECT_TAG_ACTIVITY_CODE);
            }
        });
    }

    /**
     * For identifying an OSM element and presenting it's tags in the ListView
     * @param osmElement The target OSMElement.
     */
    protected void identifyOSMFeature(OSMElement osmElement) {
        //pass the tags to the list adapter
        tagListAdapter = new TagListAdapter(this, osmElement);
        
        if(!tagListAdapter.isEmpty()) {
            //set the ListView's adapter
            mTagListView.setAdapter(tagListAdapter);

            //show the ListView under the map
            proportionMapAndList(60, 40);
        }
    }

    /**
     * For setting the proportions of the Map weight and the ListView weight for dual display
     * @param topWeight Refers to the layout weight.  Note, topWeight + bottomWeight must equal the weight sum of 100
     * @param bottomWeight Referes to the layotu height.  Note, bottomWeight + topWeight must equal the weight sum of 100
     */
    protected void proportionMapAndList(int topWeight, int bottomWeight) {

        LinearLayout.LayoutParams topLayoutParams = (LinearLayout.LayoutParams)mTopLinearLayout.getLayoutParams();
        LinearLayout.LayoutParams bottomLayoutParams = (LinearLayout.LayoutParams)mBottomLinearLayout.getLayoutParams();

        //update weight of top and bottom linear layouts
        mTopLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(topLayoutParams.width, topLayoutParams.height, topWeight));
        mBottomLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(bottomLayoutParams.width, bottomLayoutParams.height, bottomWeight));
    }

    /**
     * Loads OSM XML stored on the device.
     */
    protected void initializeOsmXml() {
        try {
            OSMMapBuilder.buildMapFromExternalStorage(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * For instantiating the location button and setting up its tap event handler
     */
    protected void initializeLocationButton() {

        //instantiate location button
        final ImageButton locationButton = (ImageButton)findViewById(R.id.locationButton);

        //set tap event
        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean userLocationIsEnabled = mapView.getUserLocationEnabled();
                if (userLocationIsEnabled) {
                    mapView.setUserLocationEnabled(false);
                } else {
                    mapView.setUserLocationEnabled(true);
                    mapView.goToUserLocation(true);
                }
            }
        });
    }
    


    /**
     * For presenting a dialog to allow the user to choose which OSM XML files to use that have been uploaded to their device's openmapkit/osm folder
     */
    private void presentOSMOptions() {
        final File[] osmFiles = ExternalStorage.fetchOSMXmlFiles();
        String[] osmFileNames = ExternalStorage.fetchOSMXmlFileNames();
        final boolean[] checkedOsmFiles = OSMMapBuilder.isFileArraySelected(osmFiles);
        final Set<File> filesToAdd = new HashSet<>();
        final Set<File> filesToRemove = new HashSet<>();

        if (osmFileNames.length > 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.osmChooserDialogTitle));
            builder.setMultiChoiceItems(osmFileNames, checkedOsmFiles, new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i, boolean isChecked) {
                    // load the file
                    if (isChecked) {
                        File fileToAdd = osmFiles[i];
                        filesToAdd.add(fileToAdd);
                    }
                    // remove the file
                    else {
                        File fileToRemove = osmFiles[i];
                        filesToRemove.add(fileToRemove);
                    }
                }
            });
            //handle OK tap event of dialog
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    OSMMapBuilder.removeOSMFilesFromModel(filesToRemove);
                    OSMMapBuilder.addOSMFilesToModel(filesToAdd);
                }
            });

            //handle cancel button tap event of dialog
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    //user clicked cancel
                }
            });
            builder.show();
        } else {
            Toast prompt = Toast.makeText(getApplicationContext(), "Please add .osm files to " + ExternalStorage.getOSMDir(), Toast.LENGTH_LONG);
            prompt.show();
        }
    }
    
    private void downloadOSM() {
        BoundingBox bbox = mapView.getBoundingBox();
        OSMDownloader downloader = new OSMDownloader(bbox);
        downloader.start();
    }

    /**
     * For adding action items to the action bar
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }

    /**
     * For handling when a user taps on a menu item (top right)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        super.onOptionsItemSelected(item);
                
        int id = item.getItemId();

        if (id == R.id.osmdownloader) {
            downloadOSM();
            return true;
        } else if (id == R.id.mbtilessettings) {
            basemap.presentMBTilesOptions();
            return true;
        } else if (id == R.id.osmsettings) {
            presentOSMOptions();
            return true;
        }
        return false;
    }

    @Override
    public void selectedElementsChanged(LinkedList<OSMElement> selectedElements) {
        if (selectedElements != null && selectedElements.size() > 0) {
//            tagsButton.setVisibility(View.VISIBLE);

            //fetch the tapped feature
            OSMElement tappedOSMElement = selectedElements.get(0);

            //present OSM Feature tags in bottom ListView
            identifyOSMFeature(tappedOSMElement);

        }
    }

    /**
     * For sending results from the 'create tag' or 'edit tag' activities back to a third party app (e.g. ODK Collect)
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ( requestCode == ODK_COLLECT_TAG_ACTIVITY_CODE ) {
            if(resultCode == RESULT_OK) {
                String osmXmlFileFullPath = ODKCollectHandler.getODKCollectData().getOSMFileFullPath();
                String osmXmlFileName = ODKCollectHandler.getODKCollectData().getOSMFileName();
                Intent resultIntent = new Intent();
                resultIntent.putExtra("OSM_PATH", osmXmlFileFullPath);
                resultIntent.putExtra("OSM_FILE_NAME", osmXmlFileName);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        }
    }
    
    public MapView getMapView() {
        return mapView;
    }
    
    private void determineVersion() {
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    public static String getVersion() {
        return version;
    }
}
