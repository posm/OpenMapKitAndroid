package org.redcross.openmapkit.tagswipe;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.redcross.openmapkit.R;
import org.redcross.openmapkit.odkcollect.ODKCollectData;
import org.redcross.openmapkit.odkcollect.ODKCollectHandler;
import org.redcross.openmapkit.odkcollect.tag.ODKTag;
import org.redcross.openmapkit.odkcollect.tag.ODKTagItem;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SelectOneTagValueFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SelectOneTagValueFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SelectOneTagValueFragment extends Fragment {

    private static final String IDX = "IDX";

    private TagEdit tagEdit;
    private View rootView;
    private List<Map<String,String>> mTagValueOptions;

    private OnFragmentInteractionListener mListener;

    
    public static SelectOneTagValueFragment newInstance(int idx) {
        SelectOneTagValueFragment fragment = new SelectOneTagValueFragment();
        Bundle args = new Bundle();
        args.putInt(IDX, idx);
        fragment.setArguments(args);
        return fragment;
    }

    public SelectOneTagValueFragment() {
        // Required empty public constructor
    }

    /**
     * For programmatically creating the widgets in the layout
     */
    private void setupWidgets() {

        //tag key label
        TextView tagKeyLabelTextView = (TextView)rootView.findViewById(R.id.tagKeyLabel);
        tagKeyLabelTextView.setText("What type of establishment is this?");

        //tag key
        TextView tagKeyTextView = (TextView)rootView.findViewById(R.id.tagKey);
        tagKeyTextView.setText("amenity");

        //create a radio button and text view for tag value label and tag value options
        RadioGroup targetRadioGroup = (RadioGroup)rootView.findViewById(R.id.selectOneTagValueRadioGroup);
        for(int i = 0; i < mTagValueOptions.size(); i++) {

            HashMap<String,String> currentOption = (HashMap<String,String>)mTagValueOptions.get(i);
            String tagValueLabel = currentOption.get("tagValueLabel");
            String tagValue = currentOption.get("tagValue");

            final RadioButton radioButton = new RadioButton(getActivity().getApplicationContext());
            radioButton.setText(tagValueLabel);
            radioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //set choice
                }
            });
            targetRadioGroup.addView(radioButton);
            //TODO layout params

            TextView textView = new TextView(getActivity().getApplicationContext());
            textView.setText(tagValue);
            targetRadioGroup.addView(textView);
            //TODO layout params
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

            int idx = getArguments().getInt(IDX);
            tagEdit = TagEdit.getTag(idx);

            ODKCollectData odkData = ODKCollectHandler.getODKCollectData();
            Collection<ODKTag> odkTags = odkData.getRequiredTags();

            Iterator iterator = odkTags.iterator();
            while(iterator.hasNext()) {
                ODKTag currentTag = (ODKTag)iterator.next();
                String currentTagKey = currentTag.getKey();
                String currentTagLabel = currentTag.getLabel();

                //
                Log.e("test", "key: " + currentTagKey + " label: " + currentTagLabel);

                Collection<ODKTagItem> tagItems = currentTag.getItems();
                if(tagItems.size() > 0) {
                    Iterator tagItemsIterator = tagItems.iterator();
                    while(tagItemsIterator.hasNext()) {
                        ODKTagItem currentTagItem = (ODKTagItem)tagItemsIterator.next();
                        String currentTagItemLabel = currentTagItem.getLabel();

                        //
                        Log.e("test", currentTagItemLabel);
                    }
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_select_one_tag_value, container, false);
        //setupWidgets();
        return rootView;

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
