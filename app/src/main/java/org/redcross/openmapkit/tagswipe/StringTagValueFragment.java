package org.redcross.openmapkit.tagswipe;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import org.redcross.openmapkit.R;


public class StringTagValueFragment extends Fragment {

    private static final String IDX = "IDX";

    private TagEdit tagEdit;
    private View rootView;
    
    private TextView tagKeyLabelTextView;
    private TextView tagKeyTextView;
    private EditText tagValueEditText;
    
    private OnFragmentInteractionListener mListener;


    public static StringTagValueFragment newInstance(int idx) {
        StringTagValueFragment fragment = new StringTagValueFragment();
        Bundle args = new Bundle();
        args.putInt(IDX, idx);
        fragment.setArguments(args);
        return fragment;
    }
    
    private void setupWidgets() {
        tagKeyLabelTextView = (TextView)rootView.findViewById(R.id.tagKeyLabelTextView);
        tagKeyTextView = (TextView)rootView.findViewById(R.id.tagKeyTextView);
        tagValueEditText = (EditText)rootView.findViewById(R.id.tagValueEditText);
        
        String keyLabel = tagEdit.getTagKeyLabel();
        String key = tagEdit.getTagKey();
        String val = tagEdit.getTagVal();
        
        if (keyLabel != null) {
            tagKeyLabelTextView.setText(keyLabel);
            tagKeyTextView.setText(key);
        } else {
            tagKeyLabelTextView.setText(key);
            tagKeyTextView.setText("");
        }
        
        tagValueEditText.setText(val);
    }

    public StringTagValueFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            int idx = getArguments().getInt(IDX);
            tagEdit = TagEdit.getTag(idx);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_string_tag_value, container, false);
        setupWidgets();
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
