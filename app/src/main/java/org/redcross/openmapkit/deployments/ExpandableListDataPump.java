package org.redcross.openmapkit.deployments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class ExpandableListDataPump {
    public static HashMap<String, List<String>> getData() {
        HashMap<String, List<String>> expandableListDetail = new HashMap<String, List<String>>();

        List<String> technology = new ArrayList<>();
        technology.add("Beats sued for noise-cancelling tech");
        technology.add("Wikipedia blocks US Congress edits");
        technology.add("Google quizzed over deleted links");
        technology.add("Nasa seeks aid with Earth-Mars links");
        technology.add("The Good, the Bad and the Ugly");

        List<String> entertainment = new ArrayList<>();
        entertainment.add("Goldfinch novel set for big screen");
        entertainment.add("Anderson stellar in Streetcar");
        entertainment.add("Ronstadt receives White House honour");
        entertainment.add("Toronto to open with The Judge");
        entertainment.add("British dancer return from Russia");

        expandableListDetail.put("MBTiles", technology);
        expandableListDetail.put("OSM XML", entertainment);
        return expandableListDetail;
    }
}
