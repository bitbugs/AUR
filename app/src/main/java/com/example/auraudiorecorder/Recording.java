package com.example.auraudiorecorder;

public class Recording {
    String Uri, fileName;
    boolean isPlaying = false;
    int lastProgress = 0;
    long fileSize;

    public Recording(String uri, String fileName, boolean isPlaying, long fileSize) {
        Uri = uri;
        this.fileName = fileName;
        this.isPlaying = isPlaying;
        this.fileSize = fileSize;
    }

    public String getUri() {
        return Uri;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileSize() {
        return ( String.format("%.1f", ((float)fileSize) /1000 ) ) + " kB";
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing){
        this.isPlaying = playing;
    }
}
