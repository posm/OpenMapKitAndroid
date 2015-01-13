package com.mapbox.mapboxsdk.offline;

public interface OfflineMapDownloaderListener {

    public void stateChanged(OfflineMapDownloader.MBXOfflineMapDownloaderState newState);
    public void initialCountOfFiles(Integer numberOfFiles);
    public void progressUpdate(Integer numberOfFilesWritten, Integer numberOfFilesExcepted);
    public void networkConnectivityError(Throwable error);
    public void sqlLiteError(Throwable error);
    public void httpStatusError(Throwable error);
    public void completionOfOfflineDatabaseMap(OfflineMapDatabase offlineMapDatabase);

}
