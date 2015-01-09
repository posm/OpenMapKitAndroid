package americanredcross.org.openmapkit;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.tileprovider.tilesource.WebSourceTileLayer;
import com.mapbox.mapboxsdk.views.MapView;


public class MapActivity extends ActionBarActivity {

    private MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        initializeMap();
    }

    /**
     * For initializing the map with default tile layer, location, extent, and zoom level
     */
    private void initializeMap() {

        // instantiate map
        this.mapView = (MapView)findViewById(R.id.mapView);

        // set the  default map tile layer (OSM)
        String defaultTilePID = getString(R.string.defaultTileLayerPID);
        String defaultTileURL = getString(R.string.defaultTileLayerURL);
        String defaultTileName = getString(R.string.defaultTileLayerName);
        String defaultTileAttribution = getString(R.string.defaultTileLayerAttribution);

        WebSourceTileLayer ws = new WebSourceTileLayer(defaultTilePID, defaultTileURL);
        ws.setName(defaultTileName)
                .setAttribution(defaultTileAttribution)
                .setMinimumZoomLevel(1)
                .setMaximumZoomLevel(18);

        mapView.setTileSource(ws);

        // set default map extent and zoom
        LatLng initialCoordinate = new LatLng(23.728791, 90.409412);
        mapView.setCenter(initialCoordinate);
        mapView.setZoom(12);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
