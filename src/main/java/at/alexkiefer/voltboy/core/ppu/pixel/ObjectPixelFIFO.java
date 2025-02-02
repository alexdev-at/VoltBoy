package at.alexkiefer.voltboy.core.ppu.pixel;

import at.alexkiefer.voltboy.core.VoltBoy;

import java.util.Arrays;

public class ObjectPixelFIFO extends PixelFIFO {

    public ObjectPixelFIFO(VoltBoy gb) {
        super(gb);
    }

    public void fillSize(Pixel[] p, int effectiveSize) {
        int startIndex = 8 - effectiveSize;

        for(int i = 0; i < effectiveSize; i++) {
            Pixel oldPixel = pixels[i];
            Pixel newPixel = p[startIndex + i];
            if(oldPixel == null || oldPixel.getColor() == -1) {
                pixels[i] = newPixel;
            } else {
                pixels[i] = oldPixel;
            }
        }

        size = effectiveSize;
    }

    @Override
    public void fill(Pixel[] p) {
        for(int i = 0; i < 8; i++) {
            Pixel oldPixel = pixels[i];
            Pixel newPixel = p[i];
            if(oldPixel == null || oldPixel.getColor() == -1) {
                pixels[i] = newPixel;
            } else {
                pixels[i] = oldPixel;
            }
        }
        size = 8;
    }

    @Override
    public Pixel pop() {
        Pixel ret = pixels[0];
        for(int i = 0; i < size - 1; i++) {
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
    }

}
