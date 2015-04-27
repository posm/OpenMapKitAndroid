package org.redcross.openmapkit.controlpanel;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.astuetz.PagerSlidingTabStrip;

import org.redcross.openmapkit.R;

/**
 * Note that we are using a 3rd party library PagerSlidingTagStrip
 * for the Play Store-like tabs.
 *
 * https://github.com/AmericanRedCross/OpenMapKit/issues/51
 * * *
 * https://guides.codepath.com/android/Sliding-Tabs-with-PagerSlidingTabStrip#install-pagerslidingtabstrip
 * https://github.com/astuetz/PagerSlidingTabStrip/blob/master/sample/src/com/astuetz/viewpager/extensions/sample/MainActivity.java
 * * ** * * * * *
 */
public class ControlPanelActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_panel);

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        ViewPager viewPager = (ViewPager) findViewById(R.id.controlpanelviewpager);
        viewPager.setAdapter(new ControlPanelFragmentPagerAdapter(getSupportFragmentManager()));

        // Give the PagerSlidingTabStrip the ViewPager
        PagerSlidingTabStrip tabsStrip = (PagerSlidingTabStrip) findViewById(R.id.controlpaneltabs);
        // Attach the view pager to the tab strip
        tabsStrip.setViewPager(viewPager);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_control_panel, menu);
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

    public class ControlPanelFragmentPagerAdapter extends FragmentPagerAdapter {
        final int PAGE_COUNT = 3;
        private String tabTitles[] = new String[] { "Basemaps", "OSM Layers", "Download OSM" };

        public ControlPanelFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return BasemapsFragment.newInstance("param1", "param2");
                case 1:
                    return OSMLayersFragment.newInstance("param1", "param2");
                case 2:
                    return OSMLayersFragment.newInstance("param1", "param2");
            }
            return BasemapsFragment.newInstance("param1", "param2");
        }

        @Override
        public CharSequence getPageTitle(int position) {
            // Generate title based on item position
            return tabTitles[position];
        }
    }
}
