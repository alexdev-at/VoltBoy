package at.alexkiefer.voltboy.components.ppu.fifo;

import at.alexkiefer.voltboy.VoltBoy;

import java.util.Arrays;

public class ObjectPixelFIFO extends FIFO {


    public ObjectPixelFIFO(VoltBoy gb) {
        super(gb);
    }

    @Override
    public void fill(Pixel[] p) {
        for(int i = 0; i < 8; i++) {
            Pixel oldPixel = pixels[i];
            Pixel newPixel = p[i];
            if(oldPixel == null || oldPixel.getColor() == 0b00) {
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
        // Shift all pixels left once
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