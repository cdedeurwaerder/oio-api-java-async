package io.openio.sds.models;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 *
 *
 */
public class Position {

    private static final Pattern POSITION_PATTERN = Pattern
            .compile("^([\\d]+)(\\.([\\d]+)(p)?)?$");

    private int meta;
    private boolean parity;
    private int sub;

    private Position(int meta, int sub, boolean parity) {
        this.meta = meta;
        this.sub = sub;
        this.parity = parity;
    }

    public static Position parse(String pos) {
        Matcher m = POSITION_PATTERN.matcher(pos);
        checkArgument(m.matches(), String.format("Invalid position %s", pos));
        if (null == m.group(2))
            return simple(Integer.valueOf(m.group(1)));
        return composed(Integer.valueOf(m.group(1)),
                Integer.valueOf(m.group(3)), null != m.group(4));
    }

    public static Position simple(int meta) {
        checkArgument(0 <= meta, "Invalid position");
        return new Position(meta, -1, false);
    }

    public static Position composed(int meta, int sub, boolean parity) {
        checkArgument(0 <= meta, "Invalid meta position");
        checkArgument(0 <= sub, "Invalid sub position");
        return new Position(meta, sub, parity);
    }

    public int meta() {
        return meta;
    }

    public int sub() {
        return sub;
    }

    public boolean parity() {
        return parity;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder().append(meta);
        if (-1 != sub)
            sb.append(".").append(sub).append(parity ? "p" : "");
        return sb.toString();
    }

    /**
     * Returns negative, 0 or positive int if the position is lower, equals or
     * higher than the specified one .
     * <p>
     * This method compares the meta position, if equals, it compares parity
     * boolean, if equals, it compares the sub position.
     * 
     * @param pos
     *            the position to compare to
     * @return
     */
    public int compare(Position pos) {
        return (meta == pos.meta()) ? ((parity == pos.parity()) ? sub - pos.sub() : parity ? 1 : -1)
                : meta - pos.meta();
    }
}
