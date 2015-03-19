package org.redcross.openmapkit.tagswipe;

import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import org.redcross.openmapkit.R;
import org.redcross.openmapkit.odkcollect.ODKCollectHandler;

public class TagSwipeActivity extends ActionBarActivity {

    private List<TagEdit> tagEdits;

    
    private void setupModel() {
        tagEdits = TagEdit.buildTagEdits();
    }

    
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_swipe);

        setupModel();
        
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
    
        pageToCorrectTag();
    }

    private void pageToCorrectTag() {
        String tagKey = getIntent().getStringExtra("TAG_KEY");
        if (tagKey == null) return;
        int idx = TagEdit.getIndexForTagKey(tagKey);
        mViewPager.setCurrentItem(idx);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tag_swipe, menu);
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
            TagEdit.saveToODKCollect();
            setResult(Activity.RESULT_OK);
            finish();
        }
        
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        
        private Fragment fragment;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        private void hideKeyboard() {
            if (fragment != null && fragment instanceof StringTagValueFragment) {
                StringTagValueFragment stvf = (StringTagValueFragment) fragment;
                EditText et = stvf.getEditText();
                if (et != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
                }
            }
        }
        
        @Override
        public Fragment getItem(int position) {
            
            // hide keyboard if last fragment had a user edit text
            hideKeyboard();
            
            if (position < tagEdits.size()) {
                TagEdit tagEdit = tagEdits.get(position);
                if (tagEdit != null) {
                    if (tagEdit.isReadOnly()) {
                        fragment = ReadOnlyTagFragment.newInstance(position);
                        return fragment;
                    } else if (tagEdit.isSelectOne()) {
                        fragment = SelectOneTagValueFragment.newInstance(position);
                        return fragment;
                    } else {
                        fragment = StringTagValueFragment.newInstance(position);
                        return fragment;
                    }
                }
            }
            
            if (ODKCollectHandler.isODKCollectMode()) {
                return ODKCollectFragment.newInstance("one", "tow");    
            } else {
                return StandaloneFragment.newInstance("one", "two");
            }
        }

        @Override
        public int getCount() {
            return tagEdits.size() + 1;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position < tagEdits.size()) {
                TagEdit tagEdit = tagEdits.get(position);
                if (tagEdit != null) {
                    return tagEdit.getTitle();
                }
            }
            if (ODKCollectHandler.isODKCollectMode()) {
                return "SAVE";
            } else {
                return "ADD OR SAVE";
            }
        }
    }

}
