package io.github.terra121.projection;

public final class OutOfProjectionBoundsException extends Exception {
    private static final OutOfProjectionBoundsException INSTANCE = new OutOfProjectionBoundsException(false);

    private static final boolean FAST = Boolean.parseBoolean(System.getProperty("terraplusplus.fastExcept", "true"));

    public static OutOfProjectionBoundsException get() {
        if (FAST) {
            return INSTANCE;
        } else {
            return new OutOfProjectionBoundsException(true);
        }
    }

    private OutOfProjectionBoundsException(boolean flag) {
        super(null, null, flag, flag);
    }
}
