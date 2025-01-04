package net.buildtheearth.terraminusminus.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Hex {

    public char[] DIGITS_LOWER = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    public String encodeHexString(byte[] bytes) {
        char[] hex = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            hex[i * 2] = DIGITS_LOWER[(bytes[i] >> 4) & 0x0f];
            hex[i * 2 + 1] = DIGITS_LOWER[bytes[i] & 0x0f];
        }
        return new String(hex);
    }

}
