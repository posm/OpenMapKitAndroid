package org.redcross.openmapkit.odkcollect;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.spatialdev.osm.model.OSMElement;
import com.spatialdev.osm.model.OSMXmlWriter;

import org.redcross.openmapkit.R;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class ODKCollectTagActivityOld extends ActionBarActivity {

    private LinkedList<OSMElement> selectedElements;
    private OSMElement osmElement;

    private Button saveButton;

    private EditText keyText;
    private EditText valueText;

    private String selectedKey;
    private String selectedVal;

    private Map<String, String> tags;
    private String[] tagKeys;

    private Map<String, String> editedTags = new HashMap<>();
    private Set<String> deletedTags = new HashSet<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        selectedElements = OSMElement.getSelectedElements();
        // only 1 element is selected on the proof of concept
        osmElement = selectedElements.getFirst();
        tags = osmElement.getTags();
        Set<String> tagKeysSet = tags.keySet();
//        tagKeys = tagKeysSet.toArray(new String[tagKeysSet.size()]);
        
        List<String> requiredTags = ODKCollectHandler.getRequiredTags();
        tagKeys = requiredTags.toArray(new String[requiredTags.size()]);
        
        setContentView(R.layout.activity_tag_editor);

        Spinner spinner = (Spinner) findViewById(R.id.existingTagsSpinner);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, tagKeys);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        initializeCancelButton();
        initializeSaveButton();
        initializeEditTextFields();

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedKey = tagKeys[position];
                selectedVal = tags.get(selectedKey);
                keyText.setText(selectedKey, TextView.BufferType.EDITABLE);
                valueText.setText(selectedVal, TextView.BufferType.EDITABLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    /**
     * For handling when user taps on the cancel button
     */
    private void initializeCancelButton() {
        Button cancelButton = (Button)findViewById(R.id.buttonCancel);
        cancelButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initializeSaveButton() {
        saveButton = (Button) findViewById(R.id.buttonSave);
        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                saveToModel();

                Toast tst = Toast.makeText(getApplicationContext(), "SAVE", Toast.LENGTH_LONG);
                tst.show();

                Intent resultIntent = new Intent();
                resultIntent.putExtra("TEST_RESULT", "TestSentFromOpenMapKit");
                setResult(Activity.RESULT_OK, resultIntent);

                finish();
            }
        });

    }

    private void saveToModel() {
        for (String k : deletedTags) {
            osmElement.deleteTag(k);
        }
        Set<String> editedTagKeys = editedTags.keySet();
        for (String k : editedTagKeys) {
            String v = editedTags.get(k);
            osmElement.addOrEditTag(k, v);
        }
        editsToXml();
    }

    private void editsToXml() {
        LinkedList<OSMElement> els = OSMElement.getModifiedElements();
        String xml = null; // TODO: Need to specify OSM User
        try {
            xml = OSMXmlWriter.elementsToString(els, "theoutpost");
//            ODKCollectHandler.saveXmlInODKCollect(xml);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initializeEditTextFields() {
        keyText = (EditText) findViewById(R.id.editTextTagName);
        valueText = (EditText) findViewById(R.id.editTextTagValue);

        keyText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String key = s.toString();
                if ( ! key.equals(selectedKey) ) {
                    deletedTags.add(key);
                    editedTags.put(key, valueText.toString());
                }
                checkToEnableSave();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        valueText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String val = s.toString();
                if ( ! val.equals(selectedVal) ) {
                    editedTags.put(keyText.toString(), val);
                }
                checkToEnableSave();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private boolean isTagEdited() {
        if (editedTags.size() > 0) {
            return true;
        }
        if (deletedTags.size() > 0) {
            return true;
        }
        return false;
    }

    private void checkToEnableSave() {
        if (isTagEdited()) {
            saveButton.setEnabled(true);
        }
    }
}
