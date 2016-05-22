package org.redcross.openmapkit.tagswipe;

import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import org.redcross.openmapkit.R;
import org.redcross.openmapkit.odkcollect.ODKCollectHandler;

public class TagSwipeActivity extends ActionBarActivity {

    private List<TagEdit> tagEdits;
    private SharedPreferences userNamePref;

    
    private void setupModel() {
        tagEdits = TagEdit.buildTagEdits();
        TagEdit.setTagSwipeActivity(this);
        userNamePref = getSharedPreferences("org.redcross.openmapkit.USER_NAME", Context.MODE_PRIVATE);
    }


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
            saveToODKCollect();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * We check to see if there is a saved user name. If there is not,
     * we present a dialog to ask for it. Otherwise, we just use what
     * is saved for writing OSM XML and saving to ODK Collect.
     */
    public void saveToODKCollect() {
        String userName = userNamePref.getString("userName", null);
        if (userName == null) {
            askForOSMUsername();
        } else {
            if (TagEdit.saveToODKCollect(userName)) {
                setResult(Activity.RESULT_OK);
                finish();
            }
        }
    }
    
    public void cancel() {
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    private void askForOSMUsername() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("OpenStreetMap User Name");
        builder.setMessage("Please enter your OpenStreetMap user name.");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        builder.setView(input);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String userName = input.getText().toString();
                SharedPreferences.Editor editor = userNamePref.edit();
                editor.putString("userName", userName);
                editor.apply();
                if (TagEdit.saveToODKCollect(userName)) {
                    setResult(Activity.RESULT_OK);
                    finish();
                }
            }
        });
        builder.show();
    }

    public void updateUI(String activeTagKey) {
        mSectionsPagerAdapter.notifyDataSetChanged();
        int idx = TagEdit.getIndexForTagKey(activeTagKey);
        mViewPager.setCurrentItem(idx);
    }

    /**
     * Only call if you have more than one missing tag.
     *
     * @param missingTags - tags that are required that are missing.
     */
    public void notifyMissingTags(final Set<String> missingTags) {
        Snackbar.make(findViewById(R.id.tagSwipeActivity),
                "There are " + missingTags.size() + " required tags that you need to complete.",
                Snackbar.LENGTH_INDEFINITE)
                .setAction("OK", new View.OnClickListener() {
                    // undo action
                    @Override
                    public void onClick(View v) {
                        try {
                            String missingTag = missingTags.iterator().next();
                            int idx = TagEdit.getIndexForTagKey(missingTag);
                            mViewPager.setCurrentItem(idx);
                        } catch (Exception e) {
                            // do nothing
                        }
                    }
                })
                .setActionTextColor(Color.rgb(126, 188, 111))
                .show();
    }

    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {
        
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
//                        fragment = SelectMultipleTagValueFragment.newInstance(position);
                        return fragment;
                    } else {
                        fragment = StringTagValueFragment.newInstance(position);
                        return fragment;
                    }
                }
            }
            
            if (ODKCollectHandler.isODKCollectMode()) {
                return ODKCollectFragment.newInstance();    
            } else {
                return StandaloneFragment.newInstance("one", "two");
            }
        }

        @Override
        public int getCount() {
            return tagEdits.size() + 1;
        }

        //this is called when notifyDataSetChanged() is called
        @Override
        public int getItemPosition(Object object) {
            // refresh all fragments when data set changed
            return PagerAdapter.POSITION_NONE;
        }


        @Override
        public CharSequence getPageTitle(int position) {
            if (position < tagEdits.size()) {
                TagEdit tagEdit = tagEdits.get(position);
                if (tagEdit != null) {
                    return tagEdit.getTitle();
                }
            }
            Resources res = getResources();
            if (ODKCollectHandler.isODKCollectMode()) {
                return res.getString(R.string.odkcollect_fragment_title);
            } else {
                return res.getString(R.string.standalone_fragment_title);
            }
        }
    }

}
