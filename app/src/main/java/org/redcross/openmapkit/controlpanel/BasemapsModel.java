package org.redcross.openmapkit.controlpanel;

import org.redcross.openmapkit.ExternalStorage;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Nicholas Hallahan on 4/28/15.
 * nhallahan@spatialdev.com
 */
public class BasemapsModel {
    public static final int TYPE_ONLINE = 0;
    public static final int TYPE_MBTILES = 1;
    public static final int TYPE_SECTION = 2;

    private static List<BasemapsModel> basemaps;

    private int type = TYPE_ONLINE;
    private String name;
    private String url;
    private File file;

    /**
     * We get a list of all of the available basemaps.
     *
     * This list is a fresh construction upon each call.
     *
     * @return list of basemaps
     */
    public static List<BasemapsModel> getItems() {
        basemaps = new ArrayList<>();
        addSectionTitle("Online Basemaps");
        addHardCodedOnlineBasemaps();
        addSectionTitle("MBTiles Offline Basemaps");
        addMBTilesBasemaps();
        return basemaps;
    }

    private static void addHardCodedOnlineBasemaps() {
        String name1 = "Humanitarian OpenStreetMap";
        String url1  = "http://a.tile.openstreetmap.fr/hot/{z}/{x}/{y}.png";
        String name2 = "Stamen Toner Lite";
        String url2  = "http://a.tile.stamen.com/toner-lite/{z}/{x}/{y}.png";
        basemaps.add(new BasemapsModel(name1, url1));
        basemaps.add(new BasemapsModel(name2, url2));
    }

    private static void addMBTilesBasemaps() {
        File[] mbtiles = ExternalStorage.fetchMBTilesFiles();
        for (File f : mbtiles) {
            basemaps.add(new BasemapsModel(f));
        }
    }

    private static void addSectionTitle(String sectionTitle) {
        basemaps.add(new BasemapsModel(sectionTitle));
    }

    /**
     * Construct an Online Basemap
     * @param name
     * @param url
     */
    private BasemapsModel(String name, String url) {
        type = TYPE_ONLINE;
        this.name = name;
        this.url = url;
    }

    private BasemapsModel(File mbtilesFile) {
        type = TYPE_MBTILES;
        file = mbtilesFile;
    }

    /**
     * Private constructor for an item that is a Section Header
     */
    private BasemapsModel(String sectionTitle) {
        type = TYPE_SECTION;
        this.name = sectionTitle;
    }

    public int getType() {
        return type;
    }

    public String getName() {
        if (type == TYPE_ONLINE || type == TYPE_SECTION) {
            return name;
        }
        return file.getName();
    }

    public String getDesc1() {
        if (type == TYPE_ONLINE) {
            return url;
        }
        long mod = file.lastModified();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM d yyyy");
        String modDate = sdf.format(new Date(mod));
        return modDate;
    }

    public String getDesc2() {
        if (type == TYPE_ONLINE) {
            return null;
        }
        long bytes = file.length();
        String fileSize = formatFileSize(bytes);
        return fileSize;
    }

    /**
     * Shows the size of a file nicely formatted.
     *
     * http://stackoverflow.com/questions/13539871/converting-kb-to-mb-gb-tb-dynamicaly
     *
     * @param size
     * @return
     */
    public static String formatFileSize(long size) {
        String hrSize = null;

        double b = size;
        double k = size/1024.0;
        double m = ((size/1024.0)/1024.0);
        double g = (((size/1024.0)/1024.0)/1024.0);
        double t = ((((size/1024.0)/1024.0)/1024.0)/1024.0);

        DecimalFormat dec = new DecimalFormat("0.00");

        if ( t>1 ) {
            hrSize = dec.format(t).concat(" TB");
        } else if ( g>1 ) {
            hrSize = dec.format(g).concat(" GB");
        } else if ( m>1 ) {
            hrSize = dec.format(m).concat(" MB");
        } else if ( k>1 ) {
            hrSize = dec.format(k).concat(" KB");
        } else {
            hrSize = dec.format(b).concat(" Bytes");
        }

        return hrSize;
    }
}
