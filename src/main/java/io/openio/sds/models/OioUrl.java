package io.openio.sds.models;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;

/**
 * 
 *
 *
 */
public class OioUrl {

    private static final byte[] BACK_ZERO = { '\0' };

    private String account;
    private String container;
    private String cid;
    private String object;

    private OioUrl(String account, String container, String cid,
            String object) {
        this.account = account;
        this.container = container;
        this.cid = cid;
        this.object = object;
    }

    public static OioUrl url(String account, String container) {
        return url(account, container, null);
    }

    public static OioUrl url(String account, String container,
            String object) {
        checkArgument(!Strings.isNullOrEmpty(account),
                "account cannot be null or empty");
        checkArgument(!Strings.isNullOrEmpty(container),
                "container cannot be null or empty");
        return new OioUrl(account,
                container,
                cid(account, container),
                object);
    }

    public String account() {
        return account;
    }

    public OioUrl account(String account) {
        this.account = account;
        return this;
    }

    public String container() {
        return container;
    }

    public OioUrl container(String container) {
        this.container = container;
        return this;
    }

    public String object() {
        return object;
    }

    public OioUrl object(String object) {
        this.object = object;
        return this;
    }

    public String cid() {
        return cid;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .omitNullValues()
                .add("account", account)
                .add("container", container)
                .add("object", object)
                .toString();
    }

    /**
     * Generates the container id from the specified account and container name
     * 
     * @param account
     *            the name of the account
     * @param container
     *            the name of the container
     * @return the generated id
     */
    public static String cid(String account, String container) {
        try {
            byte[] b = Hashing.sha256().newHasher()
                    .putBytes(account.getBytes())
                    .putBytes(BACK_ZERO)
                    .putBytes(container.getBytes())
                    .hash().asBytes();
            return BaseEncoding.base16().encode(b);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

}
