package org.redcross.openmapkit;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Environment;
import android.widget.Toast;

import com.mapbox.mapboxsdk.tileprovider.tilesource.MBTilesLayer;
import com.mapbox.mapboxsdk.tileprovider.tilesource.WebSourceTileLayer;
import com.mapbox.mapboxsdk.views.MapView;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Clint Cabanero & Nicholas Hallahan on 3/17/15.
 *
 * nhallahan@spatialdev.com
 * ccabanero@spatialdev.com
 * * * * * * 
 */
public class Basemap {
    
    private static final String PREVIOUS_BASEMAP = "org.redcross.openmapkit.PREVIOUS_BASEMAP";
    
    private MapActivity mapActivity;
    private MapView mapView;
    private Context context;
    
    private String selectedMBTilesFile;
    
    public Basemap(MapActivity mapActivity) {
        this.mapActivity = mapActivity;
        this.mapView = mapActivity.getMapView();
        this.context = mapActivity.getApplicationContext();

        if (Connectivity.isConnected(context)) {
            addOnlineDataSources();
        } else {
            presentMBTilesOptions();
        }
    }
    
    private void addOnlineDataSources() {
        //create OSM tile layer
        String defaultTilePID = mapActivity.getString(R.string.defaultTileLayerPID);
        String defaultTileURL = mapActivity.getString(R.string.defaultTileLayerURL);
        String defaultTileName = mapActivity.getString(R.string.defaultTileLayerName);
        String defaultTileAttribution = mapActivity.getString(R.string.defaultTileLayerAttribution);

        WebSourceTileLayer ws = new WebSourceTileLayer(defaultTilePID, defaultTileURL);
        ws.setName(defaultTileName).setAttribution(defaultTileAttribution);

        //add OSM tile layer to map
        mapView.setTileSource(ws);
    }
    
    public void presentMBTilesOptions() {
        //shared preferences private to mapActivity
        final SharedPreferences sharedPreferences = mapActivity.getPreferences(Context.MODE_PRIVATE);
        String previousMBTilesChoice = sharedPreferences.getString(PREVIOUS_BASEMAP, null);

        //create an array of all mbtile options
        final ArrayList<String> mbtilesFileNames = new ArrayList<>();

        //when device is connected, HOT OSM Basemap is the first option
        if(Connectivity.isConnected(context)) {
            mbtilesFileNames.add(mapActivity.getString(R.string.hotOSMOptionTitle));
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
            AlertDialog.Builder builder = new AlertDialog.Builder(mapActivity);
            builder.setTitle(mapActivity.getString(R.string.mbtilesChooserDialogTitle));
            CharSequence[] charSeq = mbtilesFileNames.toArray(new CharSequence[mbtilesFileNames.size()]);

            //default mbtiles option is based on previous selections (persisted in shared preferences) or connectivity state of device
            int defaultRadioButtonIndex = 0;
            if(previousMBTilesChoice == null) {
                //if user DID NOT previously choose an mbtiles option...
                if(Connectivity.isConnected(context)) {
                    //the first radio button (for HOT OSM) will be selected by default
                    defaultRadioButtonIndex = 0;
                    //the default selected option is HOT OSM
                    selectedMBTilesFile = mbtilesFileNames.get(0); //default choice
                } else {
                    defaultRadioButtonIndex = -1; //no selected radio button by default
                }
            } else {
                //if user previously chose an mbtiles option ...
                for(int i = 0; i < mbtilesFileNames.size(); ++i) {
                    String fileName = mbtilesFileNames.get(i);
                    if(fileName.equals(previousMBTilesChoice)) {
                        defaultRadioButtonIndex = i;
                        selectedMBTilesFile = fileName;
                    } 
                }
                if (selectedMBTilesFile == null) {
                    selectedMBTilesFile = mbtilesFileNames.get(0);
                }
            }

            //add choices to dialog
            builder.setSingleChoiceItems(charSeq, defaultRadioButtonIndex, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //user tapped on radio button and changed previous choice or default
                    selectedMBTilesFile = mbtilesFileNames.get(which);

                    //add user's choice to shared preferences key
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(PREVIOUS_BASEMAP, selectedMBTilesFile);
                    editor.apply();
                }
            });

            //handle OK tap event of dialog
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    //user clicked OK
                    if(selectedMBTilesFile.equals(mapActivity.getString(R.string.hotOSMOptionTitle))) {

                        addOnlineDataSources();

                    } else {

                        addOfflineDataSources(selectedMBTilesFile);
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
            Toast prompt = Toast.makeText(context, "Please add .mbtiles file to " + ExternalStorage.getMBTilesDir(), Toast.LENGTH_LONG);
            prompt.show();
        }
    }

    /**
     * For instantiating a map (when the device is offline) and initializing the default mbtiles layer, extent, and zoom level
     */
    private void addOfflineDataSources(String fileName) {
        String filePath = ExternalStorage.getMBTilesDir();
        if(ExternalStorage.isReadable()) {
            //fetch mbtiles from application folder (e.g. openmapkit/mbtiles)
            File targetMBTiles = ExternalStorage.fetchFileFromExternalStorage(filePath + fileName);

            if(!targetMBTiles.exists()) {
                //inform user if no mbtiles was found
                AlertDialog.Builder builder = new AlertDialog.Builder(mapActivity);
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
}
