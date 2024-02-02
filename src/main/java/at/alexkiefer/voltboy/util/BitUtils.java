package at.alexkiefer.voltboy.util;

public class BitUtils {

    public static final int M_ZERO = 0b00000001;
    public static final int M_ONE = 0b00000010;
    public static final int M_TWO = 0b00000100;
    public static final int M_THREE = 0b00001000;
    public static final int M_FOUR = 0b00010000;
    public static final int M_FIVE = 0b00100000;
    public static final int M_SIX = 0b01000000;
    public static final int M_SEVEN = 0b10000000;

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