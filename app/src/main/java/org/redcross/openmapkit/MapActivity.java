package org.redcross.openmapkit;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.TouchDelegate;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.tileprovider.tilesource.MBTilesLayer;
import com.mapbox.mapboxsdk.tileprovider.tilesource.WebSourceTileLayer;
import com.mapbox.mapboxsdk.views.MapView;
import com.spatialdev.osm.events.OSMSelectionListener;
import com.spatialdev.osm.model.OSMElement;

import org.redcross.openmapkit.odkcollect.ODKCollectHandler;
import org.redcross.openmapkit.odkcollect.ODKCollectTagActivity;
import org.redcross.openmapkit.tagswipe.TagSwipeActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;

public class MapActivity extends ActionBarActivity implements OSMSelectionListener {

    private MapView mapView;
    private Button tagsButton;
    private String mSelectedMBTilesFile;
    private ListView mTagListView;
    private ImageButton mCloseListViewButton;
    private LinearLayout mTopLinearLayout;
    private LinearLayout mBottomLinearLayout;
    private TextView mTagTextView;

    /**
     * intent request codes
     */
    private static final int ODK_COLLECT_TAG_ACTIVITY_CODE = 2015;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        //add map data based on connectivity status
        boolean deviceIsConnected = Connectivity.isConnected(getApplicationContext());

        if (deviceIsConnected) {

            addOnlineDataSources();

        } else {

            presentMBTilesOptions();
        }

        initializeOsmXml();

        //add user location toggle button
        initializeLocationButton();

        //add tags button
        tagsButton = (Button) findViewById(R.id.tagsButton);
        initializeTagsButton();

        //set default map extent and zoom
        LatLng initialCoordinate = new LatLng(23.707873, 90.409774);
        mapView.setCenter(initialCoordinate);
        mapView.setZoom(19);
        mapView.setMaxZoomLevel(21);

        initializeListView();
    }

    /**
     * For initializing the ListView of tags
     */
    private void initializeListView() {

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
            };
        });

        //handle list view item taps
        mTagListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //fetch the key associated with the list view cell the user tapped
                TextView tagKeyTextView = (TextView)view.findViewById(R.id.textViewTagKey);
                String tappedKey = String.valueOf(tagKeyTextView.getText());

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
    private void identifyOSMFeature(OSMElement osmElement) {

        //fetch tags associated with tapped OSM element
        Map<String, String> tagMap = osmElement.getTags();

        if(tagMap.size() > 0) {

            //pass the tags to the list adapter
            TagListAdapter adapter = new TagListAdapter(this, tagMap);

            //set the ListView's adapter
            mTagListView.setAdapter(adapter);

            //show the ListView under the map
            proportionMapAndList(60, 40);
        }
    }

    /**
     * For setting the proportions of the Map weight and the ListView weight for dual display
     * @param topWeight Refers to the layout weight.  Note, topWeight + bottomWeight must equal the weight sum of 100
     * @param bottomWeight Referes to the layotu height.  Note, bottomWeight + topWeight must equal the weight sum of 100
     */
    private void proportionMapAndList(int topWeight, int bottomWeight) {

        LinearLayout.LayoutParams topLayoutParams = (LinearLayout.LayoutParams)mTopLinearLayout.getLayoutParams();
        LinearLayout.LayoutParams bottomLayoutParams = (LinearLayout.LayoutParams)mBottomLinearLayout.getLayoutParams();

        //update weight of top and bottom linear layouts
        mTopLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(topLayoutParams.width, topLayoutParams.height, topWeight));
        mBottomLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(bottomLayoutParams.width, bottomLayoutParams.height, bottomWeight));
    }

    /**
     * For adding data to map when online
     */
    private void addOnlineDataSources() {

        //create OSM tile layer
        String defaultTilePID = getString(R.string.defaultTileLayerPID);
        String defaultTileURL = getString(R.string.defaultTileLayerURL);
        String defaultTileName = getString(R.string.defaultTileLayerName);
        String defaultTileAttribution = getString(R.string.defaultTileLayerAttribution);

        WebSourceTileLayer ws = new WebSourceTileLayer(defaultTilePID, defaultTileURL);
        ws.setName(defaultTileName).setAttribution(defaultTileAttribution);

        //add OSM tile layer to map
        mapView.setTileSource(ws);
    }

    /**
     * For instantiating a map (when the device is offline) and initializing the default mbtiles layer, extent, and zoom level
     */
    private void addOfflineDataSources(String fileName) {

        String filePath = Environment.getExternalStorageDirectory() + "/" + ExternalStorage.APP_DIR + "/" + ExternalStorage.MBTILES_DIR + "/";

        if(ExternalStorage.isReadable()) {

            //fetch mbtiles from application folder (e.g. openmapkit/mbtiles)
            File targetMBTiles = ExternalStorage.fetchFileFromExternalStorage(filePath + fileName);

            if(!targetMBTiles.exists()) {

                //inform user if no mbtiles was found
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Device is Offline");
                builder.setMessage("Please add mbtiles to " + filePath);
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //placeholder
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();

            } else {

                //add mbtiles to map
                mapView.setTileSource(new MBTilesLayer(targetMBTiles));
            }

        }
    }

    /**
     * Loads OSM XML stored on the device.
     */
    private void initializeOsmXml() {
        try {
            OSMMapBuilder.buildMapFromExternalStorage(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * For instantiating the location button and setting up its tap event handler
     */
    private void initializeLocationButton() {

        //instantiate location button
        ImageButton locationButton = (ImageButton)findViewById(R.id.locationButton);

        //set tap event
        locationButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                boolean userLocationIsEnabled = mapView.getUserLocationEnabled();

                if (userLocationIsEnabled) {
                    mapView.setUserLocationEnabled(false);

                } else {
                    mapView.setUserLocationEnabled(true);
                    mapView.goToUserLocation(true);
                    mapView.setUserLocationRequiredZoom(15);
                }
            }
        });
    }
    
    private void initializeTagsButton() {
        tagsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent tagSwipe = new Intent(getApplicationContext(), TagSwipeActivity.class);
                tagSwipe.putExtra("TAG_KEY", "name");
                startActivity(tagSwipe);
            }
        });
        
    }


    /**
     * For presenting a dialog to allow the user to choose which MBTILES file to use that has been uploaded to their device/s openmapkit/mbtiles folder
     */
    private void presentMBTilesOptions() {

        //shared preferences private to this activity
        final SharedPreferences sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
        String previousMBTilesChoice = sharedPreferences.getString(getString(R.string.preferenceMBTileChoice), "nopreviouschoice");

        //create an array of all mbtile options
        final ArrayList<String> mbtilesFileNames = new ArrayList<>();

        //when device is connected, HOT OSM Basemap is the first option
        if(Connectivity.isConnected(getApplicationContext())) {
            mbtilesFileNames.add(getString(R.string.hotOSMOptionTitle));
        }

        //add mbtiles names from external storage
        File[] mbtiles = ExternalStorage.fetchMBTilesFiles();
        if (mbtiles != null && mbtiles.length > 0) {
            for (File file : mbtiles) {
                String fileName = file.getName();
                mbtilesFileNames.add(fileName);
            }
        }

        //present a dialog of mbtiles options
        if(mbtilesFileNames.size() > 0) {

            //create dialog of mbtiles choices
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.mbtilesChooserDialogTitle));
            CharSequence[] charSeq = (CharSequence[]) mbtilesFileNames.toArray(new CharSequence[mbtilesFileNames.size()]);

            //default mbtiles option is based on previous selections (persisted in shared preferences) or connectivity state of device
            int defaultRadioButtonIndex = -1; //none radio button selected
            if(previousMBTilesChoice.equals("nopreviouschoice")) {
                //if user DID NOT previously choose an mbtiles option...
                if(Connectivity.isConnected(getApplicationContext())) {
                    //the first radio button (for HOT OSM) will be selected by default
                    defaultRadioButtonIndex = 0;
                    //the default selected option is HOT OSM
                    mSelectedMBTilesFile = mbtilesFileNames.get(0); //default choice
                } else {
                    defaultRadioButtonIndex = -1; //no selected radio button by default
                }
            } else {
                //if user previously chose an mbtiles option ...
                for(int i = 0; i < mbtilesFileNames.size(); i++) {
                    if(mbtilesFileNames.get(i).equals(previousMBTilesChoice)) {
                        defaultRadioButtonIndex = i;
                    }
                }
            }

            //add choices to dialog
            builder.setSingleChoiceItems(charSeq, defaultRadioButtonIndex, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //user tapped on radio button and changed previous choice or default
                    String userMBTilesChoice = mbtilesFileNames.get(which);

                    //add user's choice to shared preferences key
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(getString(R.string.preferenceMBTileChoice), userMBTilesChoice);
                    editor.commit();

                    mSelectedMBTilesFile = userMBTilesChoice;
                }
            });

            //handle OK tap event of dialog
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    //user clicked OK
                    if(mSelectedMBTilesFile.equals(getString(R.string.hotOSMOptionTitle))) {
                        
                        addOnlineDataSources();

                    } else {

                        addOfflineDataSources(mSelectedMBTilesFile);
                    }
                }
            });

            //handle cancel button tap event of dialog
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    //user clicked cancel
                }
            });

            //present dialog to user
            builder.show();

        } else {

            Toast prompt = Toast.makeText(getApplicationContext(), "Please add .mbtiles file to " + getString(R.string.mbtilesAppPath), Toast.LENGTH_LONG);
            prompt.show();
        }
    }

    /**
     * For presenting a dialog to allow the user to choose which OSM XML files to use that have been uploaded to their device's openmapkit/osm folder
     */
    private void presentOSMOptions() {

        //fetch names of all files in osm folder
        final ArrayList osmFileNames = new ArrayList();
        File primaryExternalStorageDirectory = Environment.getExternalStorageDirectory();
        if (primaryExternalStorageDirectory != null) {
            File mbTilesFolder = new File(primaryExternalStorageDirectory, getString(R.string.osmAppPath));
            for (File file : mbTilesFolder.listFiles()) {
                if (file.isFile()) {
                    String fileName = file.getName();
                    osmFileNames.add(fileName);
                }
            }
        }

        if(osmFileNames.size() > 0) {

            final ArrayList selectedOSMFiles = new ArrayList();

            //present dialog to user with the ability to choose one or more osm xml files
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.osmChooserDialogTitle));
            CharSequence[] charSeq = (CharSequence[]) osmFileNames.toArray(new CharSequence[osmFileNames.size()]);
            builder.setMultiChoiceItems(charSeq, null, new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                    if (isChecked) {
                        //user made a choice
                        selectedOSMFiles.add(osmFileNames.get(which).toString());
                    }
                }
            });

            //handle OK button
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    //user clicked OK
                    Log.i("test", "Adding " + selectedOSMFiles);
                    //TODO potentially pass to OSMMapBuilder by passing a collection of osm xml file names?
                }
            });

            //handle cancel button
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    //user clicked cancel
                }
            });

            //present to user
            builder.show();

        } else {

            Toast prompt = Toast.makeText(getApplicationContext(), "Please add .osm files to " + getString(R.string.osmAppPath), Toast.LENGTH_LONG);
            prompt.show();
        }
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
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        if (id == R.id.mbtilessettings) {

            presentMBTilesOptions();

            return true;
        }
        /*
         else if (id == R.id.osmsettings) {

            presentOSMOptions();

            return true;
         }
        */

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void selectedElementsChanged(LinkedList<OSMElement> selectedElements) {
        if (selectedElements != null && selectedElements.size() > 0) {
            tagsButton.setVisibility(View.VISIBLE);

            //fetch the tapped feature
            OSMElement tappedOSMElement = selectedElements.get(0);

            //present OSM Feature tags in bottom ListView
            identifyOSMFeature(tappedOSMElement);

        } else {
            tagsButton.setVisibility(View.INVISIBLE);
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
    
}
