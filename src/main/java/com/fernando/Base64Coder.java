package com.fernando;


/**
 *
 * A Base64 encoder/decoder.
 * <p>
 described
 * in RFC 1521.
 *
 * This class is used to encode and decode data in Base64 format as
 * <p>
 * Project home page: <a href="http://www.source-
 code.biz/base64coder/java/">
 * www.source-code.biz/base64coder/java</a><br>
 * Author: Christian d'Heureuse, Inventec Informatik AG, Zurich,
 Switzerland<br>
 * Multi-licensed: EPL / LGPL / AL / BSD.
 */
public class Base64Coder {

    // Mapping table from 6-bit nibbles to Base64 characters
    private static final char[] map1 = new char[64];
    static {
        int i = 0;
        for (char c = 'A'; c <= 'Z'; c++) map1[i++] = c;
        for (char c = 'a'; c <= 'z'; c++) map1[i++] = c;
        for (char c = '0'; c <= '9'; c++) map1[i++] = c;
        map1[i++] = '+';
        map1[i++] = '/';
    }

    // Mapping table from Base64 characters to 6-bit nibbles
    private static final byte[] map2 = new byte[128];
    static {
        for (int i = 0; i < map2.length; i++) map2[i] = -1;
        for (int i = 0; i < 64; i++) map2[map1[i]] = (byte) i;
    }

    /**
     * Decodes a string from Base64 format.
     * @param s A Base64 encoded string.
     * @return The decoded string.
     */
    public static String decodeString(String s) {
        return new String(decode(s));
    }

    /**
     * Decodes a byte array from Base64 format and ignores line separators, tabs, and blanks.
     * @param s A Base64 encoded string.
     * @return An array of decoded bytes.
     */
    public static byte[] decodeLines(String s) {
        char[] buf = new char[s.length()];
        int p = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c != ' ' && c != '\r' && c != '\n' && c != '\t') {
                buf[p++] = c;
            }
        }
        return decode(buf, 0, p);
    }

    /**
     * Decodes a byte array from Base64 format.
     * @param s A Base64 encoded string.
     * @return An array of decoded bytes.
     */
    public static byte[] decode(String s) {
        return decode(s.toCharArray());
    }

    /**
     * Decodes a byte array from Base64 format.
     * @param in A character array containing Base64 encoded data.
     * @return An array of decoded bytes.
     */
    public static byte[] decode(char[] in) {
        return decode(in, 0, in.length);
    }

    /**
     * Decodes a byte array from Base64 format.
     * @param in A character array containing Base64 encoded data.
     * @param iOff Offset of the first character in `in` to be processed.
     * @param iLen Number of characters to process in `in`, starting at `iOff`.
     * @return An array of decoded bytes.
     */
    public static byte[] decode(char[] in, int iOff, int iLen) {
        if (iLen % 4 != 0) {
            throw new IllegalArgumentException("Length of Base64 encoded input string is not a multiple of 4.");
        }
        while (iLen > 0 && in[iOff + iLen - 1] == '=') iLen--;

        int oLen = (iLen * 3) / 4;
        byte[] out = new byte[oLen];
        int ip = iOff;
        int iEnd = iOff + iLen;
        int op = 0;

        while (ip < iEnd) {
            int i0 = in[ip++];
            int i1 = in[ip++];
            int i2 = ip < iEnd ? in[ip++] : 'A';
            int i3 = ip < iEnd ? in[ip++] : 'A';

            if (i0 > 127 || i1 > 127 || i2 > 127 || i3 > 127) {
                throw new IllegalArgumentException("Illegal character in Base64 encoded data.");
            }

            int b0 = map2[i0];
            int b1 = map2[i1];
            int b2 = map2[i2];
            int b3 = map2[i3];

            if (b0 < 0 || b1 < 0 || b2 < 0 || b3 < 0) {
                throw new IllegalArgumentException("Illegal character in Base64 encoded data.");
            }

            int o0 = (b0 << 2) | (b1 >>> 4);
            int o1 = ((b1 & 0xf) << 4) | (b2 >>> 2);
            int o2 = ((b2 & 3) << 6) | b3;

            out[op++] = (byte) o0;
            if (op < oLen) out[op++] = (byte) o1;
            if (op < oLen) out[op++] = (byte) o2;
        }

        return out;
    }
}
