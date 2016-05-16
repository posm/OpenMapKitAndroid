package org.redcross.openmapkit.tagswipe;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.redcross.openmapkit.Constraints;
import org.redcross.openmapkit.R;
import org.redcross.openmapkit.odkcollect.tag.ODKTag;
import org.redcross.openmapkit.odkcollect.tag.ODKTagItem;

import java.util.Collection;

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

    private OnFragmentInteractionListener mListener;

    
    public static SelectOneTagValueFragment newInstance(int idx) {
        SelectOneTagValueFragment fragment = new SelectOneTagValueFragment();
        Bundle args = new Bundle();
        args.putInt(IDX, idx);
        fragment.setArguments(args);
        return fragment;
    }

    private void setupWidgets() {
        TextView tagKeyLabelTextView = (TextView) rootView.findViewById(R.id.tagKeyLabelTextView);
        TextView tagKeyTextView = (TextView) rootView.findViewById(R.id.tagKeyTextView);

        String keyLabel = tagEdit.getTagKeyLabel();
        String key = tagEdit.getTagKey();

        if (keyLabel != null) {
            tagKeyLabelTextView.setText(keyLabel);
            tagKeyTextView.setText(key);
        } else {
            tagKeyLabelTextView.setText(key);
            tagKeyTextView.setText("");
        }
        
        setupRadioButtons();
    }
    
    private void setupRadioButtons() {
        final RadioGroup tagValueRadioGroup = (RadioGroup)rootView.findViewById(R.id.selectOneTagValueRadioGroup);
        tagEdit.setRadioGroup(tagValueRadioGroup);
        final Activity activity = getActivity();
        ODKTag odkTag = tagEdit.getODKTag();
        if (odkTag == null) return;

        /**
         * Custom radio button for custom OSM tag values.
         * It's got a horizontal linear layout with a ToggleableRadioButton
         * and an EditText.
         */
        final ToggleableRadioButton customButton = new ToggleableRadioButton(activity, tagValueRadioGroup, null, null);
        customButton.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        /**
         * Special EditText that is next to the customButton
         */
        final EditText customEditText = new EditText(activity);
        customEditText.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        customEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0) {
                    customButton.setChecked(true);
                    tagValueRadioGroup.clearCheck();
                } else {
                    customButton.setChecked(false);
                }
                tagEdit.updateTagInOSMElement();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        // Numeric Input Constraint
        if (Constraints.singleton().tagIsNumeric(tagEdit.getTagKey())) {
            customEditText.setRawInputType(Configuration.KEYBOARD_QWERTY);
        }

        customButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (customButton.isChecked()) {
                    customEditText.setFocusableInTouchMode(true);
                    customEditText.requestFocus();
                    final InputMethodManager inputMethodManager = (InputMethodManager) activity
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.showSoftInput(customEditText, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        });

        /**
         * Setting up buttons with prescribed choice values.
         */
        String prevTagVal = tagEdit.getTagVal();
        boolean prevTagValInTagItems = false;
        Collection<ODKTagItem> odkTagItems = odkTag.getItems();
        for (ODKTagItem item : odkTagItems) {
            String label = item.getLabel();
            String value = item.getValue();
            if (value.equals(prevTagVal)) {
                prevTagValInTagItems = true;
            }
            ToggleableRadioButton button = new ToggleableRadioButton(activity, tagValueRadioGroup, customButton, customEditText);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tagEdit.updateTagInOSMElement();
                }
            });
            button.setTextSize(18);
            TextView textView = new TextView(activity);
            textView.setPadding(66, 0, 0, 25);
            textView.setOnClickListener(new TextViewOnClickListener(button));
            if (label != null) {
                button.setText(label);
                textView.setText(value);
            } else {
                button.setText(value);
                textView.setText("");
            }
            tagValueRadioGroup.addView(button);
            if (prevTagVal != null && value.equals(prevTagVal)) {
                button.toggle();
            }
            int id = button.getId();
            odkTag.putButtonIdToTagItemHash(id, item);
            tagValueRadioGroup.addView(textView);
        }
        if (!prevTagValInTagItems) {
            customEditText.setText(prevTagVal);
        }


        /**
         * Adding customButton and customEditText to a horizontal linear layout
         * and puts that as the last item in the tag value radio group.
         */
        if (Constraints.singleton().tagAllowsCustomValue(tagEdit.getTagKey())) {
            LinearLayout customLinearLayout = new LinearLayout(activity);
            customLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
            customLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            customLinearLayout.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
            customLinearLayout.setFocusableInTouchMode(true);
            customLinearLayout.addView(customButton);
            customLinearLayout.addView(customEditText);
            tagValueRadioGroup.addView(customLinearLayout);
        }

    }

    /**
     * Allows us to pass a RadioButton as a parameter to onClick
     * * * 
     */
    private class TextViewOnClickListener implements View.OnClickListener {
        RadioButton radioButton;
        
        public TextViewOnClickListener(RadioButton rb) {
            radioButton = rb;
        }
        
        @Override
        public void onClick(View v) {
            radioButton.toggle();
        }
    }
    
    public SelectOneTagValueFragment() {
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

        rootView =  inflater.inflate(R.layout.fragment_select_one_tag_value, container, false);
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

    /**
     * Allows the user to toggle off a previously checked radio button.
     * * * *
     * http://stackoverflow.com/questions/15836789/android-radio-button-uncheck
     * https://github.com/AmericanRedCross/OpenMapKit/issues/9
     * * * *
     */
    public class ToggleableRadioButton extends RadioButton {
        private RadioGroup radioGroup;
        private RadioButton customButton;
        private EditText customEditText;

        public ToggleableRadioButton(Context context, RadioGroup radioGroup, RadioButton customButton, EditText customEditText) {
            super(context);
            this.radioGroup = radioGroup;
            this.customButton = customButton;
            this.customEditText = customEditText;
        }

        @Override
        public void toggle() {
            if(isChecked()) {
                radioGroup.clearCheck();
                if(!(getParent() instanceof RadioGroup)) {
                    setChecked(false);
                }
            } else {
                setChecked(true);
                // if custom button
                if(!(getParent() instanceof RadioGroup)) {
                    radioGroup.clearCheck();
                }
                // not custom button
                else if (customButton != null){
                    customButton.setChecked(false);
                    if (customEditText != null) {
                        customEditText.clearFocus();
                    }
                }
            }
        }
    }
}
