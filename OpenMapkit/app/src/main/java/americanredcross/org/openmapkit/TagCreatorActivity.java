package americanredcross.org.openmapkit;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


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

                Toast toast = Toast.makeText(getApplicationContext(), "tapped save", Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }
}
