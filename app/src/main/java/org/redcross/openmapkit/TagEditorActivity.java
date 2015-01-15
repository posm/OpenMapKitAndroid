package org.redcross.openmapkit;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.spatialdev.osm.model.OSMElement;

import java.util.LinkedList;
import java.util.Map;
import java.util.Set;


public class TagEditorActivity extends ActionBarActivity {
    
    private LinkedList<OSMElement> selectedElements;
    private EditText keyText;
    private EditText valueText;
    private Map<String, String> tags;
    private String[] tagKeys;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        selectedElements = OSMElement.getSelectedElements();
        // only 1 element is selected on the proof of concept
        OSMElement osmElement = selectedElements.getFirst();
        tags = osmElement.getTags();
        Set<String> tagKeysSet = tags.keySet();
        tagKeys = tagKeysSet.toArray(new String[tagKeysSet.size()]);

        setContentView(R.layout.activity_tag_editor);

        Spinner spinner = (Spinner) findViewById(R.id.existingTagsSpinner);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, tagKeys);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        initializeCancelButton();
        
        keyText = (EditText) findViewById(R.id.editTextTagName);
        valueText = (EditText) findViewById(R.id.editTextTagValue);
        
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String key = tagKeys[position];
                String value = tags.get(key);
                keyText.setText(key, TextView.BufferType.EDITABLE);
                valueText.setText(value, TextView.BufferType.EDITABLE);
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

}
