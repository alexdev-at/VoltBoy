package at.alexkiefer.voltboy.util;

public class HexUtils {

    public static String toHex(int value) {
        String ret = Integer.toHexString(value).toUpperCase();
        if(ret.length() == 1 || ret.length() == 3) {
            ret = "0" + ret;
        }
        return ret;
    }

    public static String toBinary(int value) {
        String ret = Integer.toBinaryString(value).toUpperCase();
        for(int i = 0; i < (8 - ret.length()); i++) {
            ret = "0" + ret;
        }
        return ret;
    }

}
