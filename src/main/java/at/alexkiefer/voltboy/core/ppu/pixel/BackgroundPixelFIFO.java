package at.alexkiefer.voltboy.core.ppu.pixel;

import at.alexkiefer.voltboy.core.VoltBoy;

import java.util.Arrays;

public class BackgroundPixelFIFO extends PixelFIFO {

    private int discard;

    public BackgroundPixelFIFO(VoltBoy gb) {
        super(gb);
        discard = 0;
    }

    public void discardPixels(int count) {
        discard = count;
    }

    @Override
    public void fill(Pixel[] p) {
        pixels = p;
        size = 8;
    }

    @Override
    public Pixel pop() {
        Pixel ret;
        if (discard > 0) {
            ret = null;
            discard--;
        } else {
            ret = pixels[0];
        }
        for (int i = 0; i < size - 1; i++) {
            pixels[i] = pixels[i + 1];
        }
        pixels[size - 1] = null;
        size--;
        return ret;
    }

    @Override
    public void clear() {
        Arrays.fill(pixels, null);
        size = 0;
        discard = 0;
    }

}
