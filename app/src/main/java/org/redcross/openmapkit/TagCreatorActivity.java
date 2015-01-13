package org.redcross.openmapkit;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;


public class TagCreatorActivity extends ActionBarActivity {

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

                //get device connectivity status, proceed with online or offline save workflow
                boolean deviceIsConnected = Connectivity.isConnected(getApplicationContext());

                if (deviceIsConnected) {

                    saveTagOnline();

                } else {

                    saveTagOffline();
                }
            }
        });
    }

    /**
     * For saving a tag when device is online/connected
     */
    private void saveTagOnline() {

        //PLACEHOLDER - DEVICE IS ONLINE - CONTINUE WITH ONLINE SAVE WORKFLOW
    }

    /**
     * For saving a tag when device is offline/disconnected
     */
    private void saveTagOffline() {

        //PLACEHOLDER - DEVICE IS OFFLINE - CONTINUE WITH OFFLINE SAVE WORKFLOW
    }
}
