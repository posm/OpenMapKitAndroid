package org.redcross.openmapkit.controlpanel;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import org.redcross.openmapkit.R;

import java.util.ArrayList;
import java.util.TreeSet;

/**
 * Created by Nicholas Hallahan on 4/27/15.
 * nhallahan@spatialdev.com
 */
public class BasemapsAdapter extends BaseAdapter {

    private static final int TYPE_SECTION = 0;
    private static final int TYPE_BASEMAP = 1;
    private static final int TYPE_COUNT = TYPE_BASEMAP + 1;

    private ArrayList<String> mData = new ArrayList<>();
    private LayoutInflater mInflater;

    private TreeSet<Integer> mSeparatorsSet = new TreeSet<Integer>();

    public BasemapsAdapter(Activity activity) {
        mInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void addBasemap(final String item) {
        mData.add(item);
        notifyDataSetChanged();
    }

    public void addSection(final String item) {
        mData.add(item);
        // save separator position
        mSeparatorsSet.add(mData.size() - 1);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return mSeparatorsSet.contains(position) ? TYPE_SECTION : TYPE_BASEMAP;
    }

    @Override
    public int getViewTypeCount() {
        return TYPE_COUNT;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int type = getItemViewType(position);
        switch (type) {
            case TYPE_BASEMAP:
                BasemapViewHolder holder;
                if (convertView == null) {
                    holder = new BasemapViewHolder();
                    convertView = mInflater.inflate(R.layout.item_basemap, null);
                    holder.name = (TextView) convertView.findViewById(R.id.name);
                    holder.desc1 = (TextView) convertView.findViewById(R.id.desc1);
                    holder.desc2 = (TextView) convertView.findViewById(R.id.desc2);
                    holder.radioButton = (RadioButton) convertView.findViewById(R.id.radioButton);
                    convertView.setTag(holder);
                } else {
                    holder = (BasemapViewHolder) convertView.getTag();
                }
                holder.name.setText(mData.get(position));
                holder.desc1.setText("desc1");
                holder.desc2.setText("desc2");
                holder.radioButton.setChecked(false);
                break;
            case TYPE_SECTION:
                SectionViewHolder sectionViewHolder;
                if (convertView == null) {
                    sectionViewHolder = new SectionViewHolder();
                    convertView = mInflater.inflate(R.layout.item_basemap_section, null);
                    sectionViewHolder.sectionTitle = (TextView) convertView.findViewById(R.id.sectionTitle);
                    sectionViewHolder.onlineStatus = (TextView) convertView.findViewById(R.id.onlineStatus);
                    convertView.setTag(sectionViewHolder);
                } else {
                    sectionViewHolder = (SectionViewHolder) convertView.getTag();
                }
                sectionViewHolder.sectionTitle.setText(mData.get(position));
                sectionViewHolder.onlineStatus.setText("onlineStatus");
                break;
        }
        return convertView;
    }

    public static class SectionViewHolder {
        public TextView sectionTitle;
        public TextView onlineStatus;
    }

    public static class BasemapViewHolder {
        public TextView name;
        public TextView desc1;
        public TextView desc2;
        public RadioButton radioButton;
    }

}
