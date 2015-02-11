package org.redcross.openmapkit.odkcollect;

import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.Button;
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        private OSMElement osmElement;
        private List<String> requiredTags;
        private Map<String, String> tags;
        
        private View rootView;
        private Button saveButton;
        
        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.fragment_odkcollect_tag, container, false);
            
            // Just have cancel button break us back into the MapActivity.
            Button cancelButton = (Button)rootView.findViewById(R.id.cancelButton);
            cancelButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    getActivity().finish();
                }
            });
            
            initializeModel();
            initializeTextAndEditTextViews();
            
            return rootView;
        }
        
        private void initializeModel() {
            osmElement = OSMElement.getSelectedElements().getFirst();
            tags = osmElement.getTags();
        }
        
            private void initializeTextAndEditTextViews() {
                GridLayout gridLayout = (GridLayout) rootView.findViewById(R.id.odkCollectTagGridLayout);
                Activity activity = getActivity();

                TextView tv = new TextView(activity);
                tv.setText("Test");
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.rowSpec = GridLayout.spec(9);
                params.columnSpec = GridLayout.spec(0);
                tv.setLayoutParams(params);
                gridLayout.addView(tv);

                TextView tv2 = new TextView(activity);
                tv2.setText("Test and some more more text");
                GridLayout.LayoutParams params2 = new GridLayout.LayoutParams();
                params2.rowSpec = GridLayout.spec(9);
                params2.columnSpec = GridLayout.spec(1);
                tv2.setLayoutParams(params2);
                gridLayout.addView(tv2);
            }
    }
}
