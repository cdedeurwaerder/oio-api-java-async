package io.openio.sds.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.MoreObjects;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;

public class ObjectInfo {

    private OioUrl url;
    private String oid;
    private long ctime;
    private boolean deleted;
    private String policy;
    private String hash;
    private String hashMethod;
    private String chunkMethod;
    private long size;
    private long version;
    private String mtype;
    private List<ChunkInfo> chunks;
    private transient Map<Integer, List<ChunkInfo>> sortedChunks;

    private static final Comparator<ChunkInfo> comparator = new Comparator<ChunkInfo>() {

        @Override
        public int compare(ChunkInfo c1, ChunkInfo c2) {
            return c1.pos()
                    .compare(c2.pos());
        }
    };

    public ObjectInfo() {
        // TODO: content hash
        this.hash = BaseEncoding.base16()
                .encode(Hashing.md5().hashBytes("".getBytes()).asBytes());
    }

    public List<ChunkInfo> chunks() {
        return chunks;
    }

    public Map<Integer, List<ChunkInfo>> sortedChunks() {
        return sortedChunks;
    }

    public ObjectInfo chunks(List<ChunkInfo> chunks) {
        this.sortedChunks = sortChunks(chunks);
        this.chunks = chunks;
        return this;
    }

    public ObjectInfo size(long size) {
        this.size = size;
        return this;
    }

    public ObjectInfo hash(String hash) {
        this.hash = hash;
        return this;
    }

    public ObjectInfo url(OioUrl url) {
        this.url = url;
        return this;
    }

    public OioUrl url() {
        return url;
    }

    public String oid() {
        return oid;
    }

    public ObjectInfo oid(String oid) {
        this.oid = oid;
        return this;
    }

    public String hash() {
        return hash;
    }

    public long size() {
        return size;
    }

    public String policy() {
        return policy;
    }

    public ObjectInfo policy(String policy) {
        this.policy = policy;
        return this;
    }

    public boolean deleted() {
        return deleted;
    }

    public long ctime() {
        return ctime;
    }

    public ObjectInfo ctime(long ctime) {
        this.ctime = ctime;
        return this;
    }

    public String hashMethod() {
        return hashMethod;
    }

    public ObjectInfo hashMethod(String hashMethod) {
        this.hashMethod = hashMethod;
        return this;
    }

    public String chunkMethod() {
        return chunkMethod;
    }

    public ObjectInfo chunkMethod(String chunkMethod) {
        this.chunkMethod = chunkMethod;
        return this;
    }

    public long version() {
        return version;
    }

    public ObjectInfo version(long version) {
        this.version = version;
        return this;
    }

    public String mtype() {
        return mtype;
    }

    public ObjectInfo mtype(String mtype) {
        this.mtype = mtype;
        return this;
    }

    public int nbchunks() {
        return sortedChunks.size();
    }

    // FIXME Not good for RAIN
    public long chunksize(int pos) {
        return sortedChunks.get(pos).get(0).size();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .omitNullValues()
                .addValue(url)
                .add("ctime", ctime)
                .add("mime-type", mtype)
                .add("deleted", deleted)
                .add("policy", policy)
                .add("hash", hash)
                .add("hash-method", hashMethod)
                .add("chunk-method", chunkMethod)
                .add("size", size)
                .add("version", version)
                .add("chunks", chunks)
                .toString();
    }

    /* -- INTERNAL -- */

    private Map<Integer, List<ChunkInfo>> sortChunks(
            List<ChunkInfo> chunks) {
//        Map<Integer, List<ChunkInfo>> res = chunks.stream()
//                .collect(groupingBy((c) -> c.pos().meta()));
//        res.values().forEach(l -> l.stream().sorted(comparator));
        
        Map<Integer, List<ChunkInfo>> res = new HashMap<Integer, List<ChunkInfo>>();
        for(ChunkInfo ci : chunks){
            List<ChunkInfo> l = res.get(ci.pos().meta());
            if(null == l){
                l = new ArrayList<ChunkInfo>();
                res.put(ci.pos().meta(), l);
            }
            l.add(ci);    
        }
        for(List<ChunkInfo> l : res.values())
            Collections.sort(l, comparator);
        
        return res;
    }
}
