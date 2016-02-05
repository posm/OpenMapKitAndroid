package org.redcross.openmapkit.deployments;


public interface DeploymentDownloaderListener {

    /**
     * Messages used to update the download progress UI.
     *
     * @param msg A string message to be shown to user.
     * @param bytesDownloaded How many bytes downloaded.
     */
    void onDeploymentDownloadProgressUpdate(String msg, int bytesDownloaded);

    /**
     * Download canceled.
     */
    void onDeploymentDownloadCancel();

    /**
     * Error occurred.
     * @param msg String explaining the error.
     */
    void onDeploymentDownloadError(String msg);

    /**
     * Download completed.
     */
    void onDeploymentDownloadComplete();

}
