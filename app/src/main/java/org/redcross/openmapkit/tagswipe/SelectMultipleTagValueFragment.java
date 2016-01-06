package org.redcross.openmapkit.tagswipe;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.redcross.openmapkit.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SelectMultipleTagValueFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SelectMultipleTagValueFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SelectMultipleTagValueFragment extends Fragment {

    private static final String IDX = "IDX";

    private TagEdit tagEdit;
    private View rootView;

    private OnFragmentInteractionListener mListener;

    public SelectMultipleTagValueFragment() {
        // Required empty public constructor
    }


    public static SelectMultipleTagValueFragment newInstance(int idx) {
        SelectMultipleTagValueFragment fragment = new SelectMultipleTagValueFragment();
        Bundle args = new Bundle();
        args.putInt(IDX, idx);
        fragment.setArguments(args);
        return fragment;
    }

    private void setupWidgets() {

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
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_select_multiple_tag_value, container, false);
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
        void onFragmentInteraction(Uri uri);
    }
}
