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

import java.util.List;

/**
 * Created by Nicholas Hallahan on 4/27/15.
 * nhallahan@spatialdev.com
 */
public class BasemapsAdapter extends BaseAdapter {

    private List<BasemapsModel> mBasemapItems;
    private LayoutInflater mInflater;

    public BasemapsAdapter(Activity activity) {
        mInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mBasemapItems = BasemapsModel.getItems();
    }

    @Override
    public int getItemViewType(int position) {
        return mBasemapItems.get(position).getType();
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getCount() {
        return mBasemapItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mBasemapItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int type = getItemViewType(position);
        switch (type) {
            case BasemapsModel.TYPE_ONLINE:
            case BasemapsModel.TYPE_MBTILES:
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
                holder.name.setText(mBasemapItems.get(position).getName());
                holder.desc1.setText(mBasemapItems.get(position).getDesc1());
                String desc2 = mBasemapItems.get(position).getDesc2();
                if (desc2 == null) {
                    holder.desc2.setVisibility(View.GONE);
                } else {
                    holder.desc2.setVisibility(View.VISIBLE);
                    holder.desc2.setText(desc2);
                }
                holder.radioButton.setChecked(false);
                break;
            case BasemapsModel.TYPE_SECTION:
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
                sectionViewHolder.sectionTitle.setText(mBasemapItems.get(position).getName());
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
