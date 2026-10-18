package com.framework.core;

public class MapFile {

    private final String filename;
    private final String contentType;
    private final byte[] content;

    public MapFile(
            String filename,
            String contentType,
            byte[] content
    ) {
        this.filename = filename;
        this.contentType = contentType;
        this.content = content;
    }

    public String getFilename() {
        return filename;
    }

    public String getContentType() {
        return contentType;
    }

    public byte[] getContent() {
        return content;
    }

    public int getSize() {
        return content.length;
    }
}