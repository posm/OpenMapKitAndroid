package org.redcross.openmapkit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class TagCreatorActivity extends ActionBarActivity {

    EditText tagKeyEditText;
    EditText tagValueEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tag_creator);

        initializeCancelButton();

        initializeSaveButton();
    }

    /**
     * For handling when user taps on the cancel button
     */
    private void initializeCancelButton() {

        Button cancelButton = (Button)findViewById(R.id.buttonCancel);

        cancelButton.setOnClickListener(new View.OnClickListener(){

            public void onClick(View v) {

                //finish activity
                finish();
            }
        });
    }

    /**
     * For handling when user taps on the save button
     */
    private void initializeSaveButton() {

        Button saveButton = (Button)findViewById(R.id.buttonSave);

        saveButton.setOnClickListener(new View.OnClickListener(){

            public void onClick(View v) {

                tagKeyEditText = (EditText)findViewById(R.id.editTextTagName);
                tagValueEditText = (EditText)findViewById(R.id.editTextTagValue);

                Intent resultIntent = new Intent();
                resultIntent.putExtra("TAG_KEY", tagKeyEditText.getText().toString());
                resultIntent.putExtra("TAG_VALUE", tagValueEditText.getText().toString());
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });
    }
}
