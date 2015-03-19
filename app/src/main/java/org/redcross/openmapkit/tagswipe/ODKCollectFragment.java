package org.redcross.openmapkit.tagswipe;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.redcross.openmapkit.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ODKCollectFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ODKCollectFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ODKCollectFragment extends Fragment {

    private View rootView;
    private Button cancelButton;
    private Button saveButton;
    
    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ODKCollectFragment.
     */
    public static ODKCollectFragment newInstance() {
        ODKCollectFragment fragment = new ODKCollectFragment();
        return fragment;
    }

    public ODKCollectFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
    }

    private void setupButtons() {
        cancelButton = (Button)rootView.findViewById(R.id.cancelButton);
        saveButton = (Button)rootView.findViewById(R.id.saveButton);
        
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((TagSwipeActivity)getActivity()).cancel();
            }
        });
        
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((TagSwipeActivity)getActivity()).saveToODKCollect();
            }
        });
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_odkcollect, container, false);
        setupButtons();
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
