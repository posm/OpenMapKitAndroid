package org.redcross.openmapkit.deployments;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;
import org.redcross.openmapkit.R;


public class FileExpandableListAdapter extends BaseExpandableListAdapter {
    private Context context;
    private Deployment deployment;
    private JSONArray mbtiles = new JSONArray();
    private JSONArray osms = new JSONArray();
    private JSONArray geojsons = new JSONArray();

    public FileExpandableListAdapter(Context context, Deployment deployment) {
        this.context = context;
        this.deployment = deployment;
        mbtiles = deployment.mbtiles();
        osms = deployment.osm();
        geojsons = deployment.geojson();
    }

    @Override
    public int getGroupCount() {
        return 3;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        switch (groupPosition) {
            case 0:
                return mbtiles.length();
            case 1:
                return osms.length();
            case 2:
                return geojsons.length();
            default:
                return 0;
        }
    }

    @Override
    public Object getGroup(int groupPosition) {
        switch (groupPosition) {
            case 0:
                return "MBTiles";
            case 1:
                return "OSM XML";
            case 2:
                return "Field Papers GeoJSON";
            default:
                return "Other";
        }
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        switch (groupPosition) {
            case 0:
                JSONObject mbtile = mbtiles.optJSONObject(childPosition);
                if (mbtile == null) return "";
                String mbtileName = mbtile.optString("name");
                if (mbtileName == null) return "";
                return mbtileName;
            case 1:
                JSONObject osm = osms.optJSONObject(childPosition);
                if (osm == null) return "";
                String osmName = osm.optString("name");
                if (osmName == null) return "";
                return osmName;
            case 2:
                JSONObject geojson = geojsons.optJSONObject(childPosition);
                if (geojson == null) return "";
                String geojsonName = geojson.optString("name");
                if (geojsonName == null) return "";
                return geojsonName;
            default:
                return "";
        }
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String listTitle = (String)getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_group_deployment_details, null);
        }
        TextView listTitleTextView = (TextView)convertView.findViewById(R.id.listTitle);
        listTitleTextView.setTypeface(null, Typeface.BOLD);
        listTitleTextView.setText(listTitle);
        int fileCount = 0;
        if (groupPosition == 0) {
            fileCount = deployment.mbtilesCount();
        } else if (groupPosition == 1) {
            fileCount = deployment.osmCount();
        } else {
            fileCount = deployment.geojsonCount();
        }
        TextView listDetails = (TextView)convertView.findViewById(R.id.listDetails);
        listDetails.setText(fileCount + " Files");
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final String expandedListText = (String) getChild(groupPosition, childPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_item_deployment_details, null);
        }
        TextView expandedListTextView = (TextView) convertView.findViewById(R.id.expandedListItem);
        expandedListTextView.setText(expandedListText);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}
