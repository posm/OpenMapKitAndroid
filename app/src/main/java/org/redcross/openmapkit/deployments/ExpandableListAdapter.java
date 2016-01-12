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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExpandableListAdapter extends BaseExpandableListAdapter {
    private Context context;
    private List<String> mbTilesList = new ArrayList<>();
    private List<String> osmXmlList = new ArrayList<>();

    public ExpandableListAdapter(Context context, int deploymentPosition) {
        this.context = context;
        extractMBTilesAndOsmXml(deploymentPosition);
    }

    private void extractMBTilesAndOsmXml(int deploymentPosition) {
        JSONArray files = Deployments.singleton().get(deploymentPosition).optJSONArray("files");
        for (int i = 0, len = files.length(); i < len; ++i) {
            String fileName = files.optString(i);
            int mbtilesIdx = fileName.indexOf(".mbtiles");
            if (mbtilesIdx > -1) {
                mbTilesList.add(fileName);
                continue;
            }
            int osmIdx = fileName.indexOf(".osm");
            if (osmIdx > -1) {
                osmXmlList.add(fileName);
            }
        }
    }

    @Override
    public int getGroupCount() {
        return 2;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        switch (groupPosition) {
            case 0:
                return mbTilesList.size();
            case 1:
                return osmXmlList.size();
            default:
                return 0;
        }
    }

    @Override
    public Object getGroup(int groupPosition) {
        switch (groupPosition) {
            case 0:
                return "MBTilesd";
            case 1:
                return "OSM XML";
            default:
                return "Other";
        }
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        switch (groupPosition) {
            case 0:
                return mbTilesList.get(childPosition);
            case 1:
                return osmXmlList.get(childPosition);
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
        String listTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_group_deployment_details, null);
        }
        TextView listTitleTextView = (TextView) convertView.findViewById(R.id.listTitle);
        listTitleTextView.setTypeface(null, Typeface.BOLD);
        listTitleTextView.setText(listTitle);
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
