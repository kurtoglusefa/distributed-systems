package it.polito.dsp.lab3.common;

public final class Protocol {
    private Protocol() {}

    // Supported types
    public static final String PNG = "PNG";
    public static final String JPG = "JPG";
    public static final String GIF = "GIF";

    public static boolean isValidType(String t) {
        return PNG.equals(t) || JPG.equals(t) || GIF.equals(t);
    }

    // Status codes in server response
    public static final byte STATUS_OK = '0';
    public static final byte STATUS_BAD_REQUEST = '1';
    public static final byte STATUS_INTERNAL_ERROR = '2';

    public static final int PORT = 2001;
}
