package org.redcross.openmapkit.odkcollect;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.TextView;

import com.spatialdev.osm.model.OSMElement;

import org.redcross.openmapkit.R;
import org.redcross.openmapkit.odkcollect.osmtag.OSMTag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ODKCollectTagActivity extends ActionBarActivity {

    private PlaceholderFragment fragment;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_odkcollect_tag);
        if (savedInstanceState == null) {
            fragment = new PlaceholderFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, fragment)
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_odkcollect_tag, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // save to odk collect action bar button
        if (id == R.id.action_save_to_odk_collect) {
            saveToOdkCollect();
        }
        
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    
    void saveToOdkCollect() {
        OSMElement osmElement = fragment.updateTagsInOSMElement();
        String osmXmlFileFullPath = ODKCollectHandler.saveXmlInODKCollect(osmElement);
        
        Intent resultIntent = new Intent();
        resultIntent.putExtra("OSM_PATH", osmXmlFileFullPath);
        setResult(Activity.RESULT_OK, resultIntent);

        finish();
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        /**
         * MODEL FIELDS
         * * *
         */
        private OSMElement osmElement;
        private List<OSMTag> requiredTags;
        private Map<String, String> tags;

        /**
         * UI FIELDS
         * * * 
         */
        private View rootView;
        private GridLayout gridLayout;

        /**
         * TAG KEY TO TEXT EDIT HASH, 
         * USED TO GET EDITED VALUES
         * * * * 
         */
        private Map<String, TextView> tagEditTextHash;
        
        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.fragment_odkcollect_tag, container, false);
            gridLayout = (GridLayout) rootView.findViewById(R.id.odkCollectTagGridLayout);

            setupModel();
            insertRequiredOSMTags();
            
            return rootView;
        }
        
        private void setupModel() {
            osmElement = OSMElement.getSelectedElements().getFirst();
            tags = osmElement.getTags();
            tagEditTextHash = new HashMap<>();
        }
        
        private void insertRequiredOSMTags() {
            requiredTags = ODKCollectHandler.getRequiredTags();
            int row = 1; // The first row in the GridView is the instructions.
            for (OSMTag reqTag : requiredTags) {
                String initialTagVal = tags.get(reqTag.key);
                insertTagKeyAndValueForRow(row++, reqTag, initialTagVal);
            }
        }
        
        private void insertTagKeyAndValueForRow(int row, OSMTag reqTag, String initialTagVal) {
            Activity activity = getActivity();

            TextView keyTextView = reqTag.createTagKeyTextView(activity);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.rowSpec = GridLayout.spec(row);
            params.columnSpec = GridLayout.spec(0);
            keyTextView.setLayoutParams(params);
            gridLayout.addView(keyTextView);

            TextView tagValueTextView = reqTag.createTagValueTextView(activity, initialTagVal);
            GridLayout.LayoutParams params2 = new GridLayout.LayoutParams();
            params2.rowSpec = GridLayout.spec(row);
            params2.columnSpec = GridLayout.spec(1);
            params2.width = GridLayout.LayoutParams.MATCH_PARENT;
            tagValueTextView.setLayoutParams(params2);
            gridLayout.addView(tagValueTextView);
            tagEditTextHash.put(reqTag.key, tagValueTextView);
        }

        /**
         * Updates the tags in the OSMElement according to the values in the
         * EditText UI
         * * * *
         * @return OSMElement with edited tags
         */
        public OSMElement updateTagsInOSMElement() {
            Set<String> keySet = tagEditTextHash.keySet();
            for( String key : keySet) {
                TextView et = tagEditTextHash.get(key);
                String val = et.getText().toString();
                osmElement.addOrEditTag(key, val);
            }
            return osmElement;
        }
        
    }
}
