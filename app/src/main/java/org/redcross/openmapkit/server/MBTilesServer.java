package org.redcross.openmapkit.server;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public class MBTilesServer extends NanoHTTPD {

    public MBTilesServer() {
        super(8080);
    }

    @Override
    public Response serve(String uri, Method method,
                          Map<String, String> header,
                          Map<String, String> parameters,
                          Map<String, String> files) {
        String answer = "";
        try {
            // Open file from SD Card
            File root = Environment.getExternalStorageDirectory();
            FileReader index = new FileReader(root.getAbsolutePath() +
                    "/www/index.html");
            BufferedReader reader = new BufferedReader(index);
            String line = "";
            while ((line = reader.readLine()) != null) {
                answer += line;
            }
            reader.close();

        } catch(IOException ioe) {
            Log.w("Httpd", ioe.toString());
        }


        return new NanoHTTPD.Response(answer);
    }
}