package org.redcross.openmapkit.odkcollect;

import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.TextView;

import com.spatialdev.osm.model.OSMElement;

import org.redcross.openmapkit.R;

import java.util.List;
import java.util.Map;

public class ODKCollectTagActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_odkcollect_tag);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
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
        // TODO
        
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
        private List<String> requiredTags;
        private Map<String, String> tags;

        /**
         * UI FIELDS
         * * * 
         */
        private View rootView;
        private GridLayout gridLayout;
        private Button saveButton;
        
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
        }
        
        private void insertRequiredOSMTags() {
            requiredTags = ODKCollectHandler.getRequiredTags();
            int row = 1; // The first row in the GridView is the instructions.
            for (String tagKey : requiredTags) {
                String tagVal = tags.get(tagKey);
                insertTagKeyAndValueForRow(row++, tagKey, tagVal);
            }
        }
        
        private void insertTagKeyAndValueForRow(int row, String key, String val) {
            Activity activity = getActivity();

            TextView tv = new TextView(activity);
            tv.setText(key);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.rowSpec = GridLayout.spec(row);
            params.columnSpec = GridLayout.spec(0);
            tv.setLayoutParams(params);
            gridLayout.addView(tv);

            EditText et = new EditText(activity);
            if (val != null) {
                et.setText(val);
            }
            GridLayout.LayoutParams params2 = new GridLayout.LayoutParams();
            params2.rowSpec = GridLayout.spec(row);
            params2.columnSpec = GridLayout.spec(1);
            params2.width = GridLayout.LayoutParams.MATCH_PARENT;
            et.setLayoutParams(params2);
            gridLayout.addView(et);
        }
    }
}
