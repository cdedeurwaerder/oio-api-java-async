package io.openio.sds.models;

import com.google.common.base.MoreObjects;

public class ChunkInfo {

    public ChunkInfo() {
    }

    private String url;
    private long size;
    private String hash;
    private Position pos;

    public String url() {
        return url;
    }

    public String hash() {
        return hash;
    }

    public Position pos() {
        return pos;
    }

    public long size() {
        return size;
    }

    public ChunkInfo url(String url) {
        this.url = url;
        return this;
    }

    public ChunkInfo size(long size) {
        this.size = size;
        return this;
    }

    public ChunkInfo hash(String hash) {
        this.hash = hash;
        return this;
    }

    public ChunkInfo pos(Position pos) {
        this.pos = pos;
        return this;
    }
    
    public String id(){
        return url.substring(url.lastIndexOf("/") + 1);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
        		.omitNullValues()
                .add("url", url)
                .add("size", size)
                .add("hash", hash)
                .add("pos", pos)
                .toString();
    }
}
