package com.tokenbrowser.model.local;

public class Attachment {
    private String filename;
    private long size;

    public String getFilename() {
        return filename;
    }

    public Attachment setFilename(String filename) {
        this.filename = filename;
        return this;
    }

    public long getSize() {
        return size;
    }

    public Attachment setSize(long size) {
        this.size = size;
        return this;
    }
}
