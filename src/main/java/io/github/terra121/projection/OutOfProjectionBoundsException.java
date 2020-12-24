package io.github.terra121.projection;

public final class OutOfProjectionBoundsException extends Exception {
    public static final OutOfProjectionBoundsException INSTANCE = new OutOfProjectionBoundsException();

    private OutOfProjectionBoundsException() {
        super(null, null, false, false);
    }
}
