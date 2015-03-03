package org.redcross.openmapkit;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
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

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MapActivity extends ActionBarActivity implements OSMSelectionListener {

    private MapView mapView;
    private Button tagsButton;
    private String mSelectedMBTilesFile;
    private ListView mTagListView;
    private LinearLayout mTagLinearLayout;
    private TextView mTagTextView;

    /**
     * intent request codes
     */
    private static final int ODK_COLLECT_TAG_ACTIVITY_CODE = 2015;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // create directory structure for app if needed
        ExternalStorage.checkOrCreateAppDirs();
        
        // Register the intent to the ODKCollect handler
        // This will determine if we are in ODK Collect Mode or not.
        ODKCollectHandler.registerIntent(getIntent());

        //set layout
        setContentView(R.layout.activity_map);

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

        //
        initializeListView();
    }

    /**
     * For initializing the ListView of tags
     */
    private void initializeListView() {

        //the layout the ListView is nested in
        mTagLinearLayout = (LinearLayout)findViewById(R.id.tagListViewLayout);

        //the ListView
        mTagListView = (ListView)findViewById(R.id.tagListView);

        //the ListView title
        mTagTextView = (TextView)findViewById(R.id.tagTextView);
        mTagTextView.setText("Tags");

        List<String> list = new ArrayList<String>();
        list.add("Building:");
        list.add("Shelter:");
        list.add("Source:");

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                list);

        mTagListView.setAdapter(arrayAdapter);
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
                if ( ODKCollectHandler.isStandaloneMode() ) {
                    showAlertDialog();
                } else {
                    Intent odkCollectTagIntent = new Intent(getApplicationContext(), ODKCollectTagActivity.class);
                    startActivityForResult(odkCollectTagIntent, ODK_COLLECT_TAG_ACTIVITY_CODE);
                }
            }
        });
        
    }

    public void showAlertDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.tagPromptTitle);

        builder.setItems(R.array.editoptions, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                switch(which) {
                    case 0:

                        Intent editTagIntent = new Intent(getApplicationContext(), TagEditorActivity.class);
                        startActivity(editTagIntent);
                        break;

                    case 1:

                        Intent createTagIntent = new Intent(getApplicationContext(), TagCreatorActivity.class);
                        startActivity(createTagIntent);
                        break;

                    default:

                        break;
                }
            }
        });

        AlertDialog dialog = builder.create();

        dialog.show();
    }

    /**
     * For presenting a dialog to allow the user to choose which MBTILES file to use that has been uploaded to their device/s openmapkit/mbtiles folder
     */
    private void presentMBTilesOptions() {

        //fetch names of all files in mbtiles folder
        final ArrayList<String> mbtilesFileNames = new ArrayList<>();
        File[] mbtiles = ExternalStorage.fetchMBTilesFiles();
        for (File file : mbtiles) {
            String fileName = file.getName();
            mbtilesFileNames.add(fileName);
        }

        if(mbtilesFileNames.size() > 0) {

            //present dialog to user with the ability to choose one mbtiles file
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.mbtilesChooserDialogTitle));
            CharSequence[] charSeq = (CharSequence[]) mbtilesFileNames.toArray(new CharSequence[mbtilesFileNames.size()]);
            builder.setSingleChoiceItems(charSeq, -1, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //user made a choice
                    mSelectedMBTilesFile = mbtilesFileNames.get(which).toString();
                }
            });

            //handle OK button
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    //user clicked OK
                    addOfflineDataSources(mSelectedMBTilesFile);
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
        if (id == R.id.osmsettings) {

            presentOSMOptions();

            return true;
        }
        else if (id == R.id.mbtilessettings) {

            presentMBTilesOptions();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void selectedElementsChanged(LinkedList<OSMElement> selectedElements) {
        if (selectedElements != null && selectedElements.size() > 0) {
            tagsButton.setVisibility(View.VISIBLE);
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
                Bundle extras = data.getExtras();
                String osmXmlFileFullPath = extras.getString("OSM_PATH");
                String osmXmlFileName = ODKCollectHandler.getOSMFileName();
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
