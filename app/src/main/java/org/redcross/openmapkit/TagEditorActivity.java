package org.redcross.openmapkit;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.spatialdev.osm.model.OSMElement;

import java.util.LinkedList;


public class TagEditorActivity extends ActionBarActivity {
    
    private LinkedList<OSMElement> selectedElements;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        selectedElements = OSMElement.getSelectedElements();

        setContentView(R.layout.activity_tag_editor);

        Spinner spinner = (Spinner) findViewById(R.id.existingTagsSpinner);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.testTags, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        initializeCancelButton();
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
