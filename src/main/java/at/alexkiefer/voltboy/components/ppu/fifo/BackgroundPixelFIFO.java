package at.alexkiefer.voltboy.components.ppu.fifo;

import at.alexkiefer.voltboy.VoltBoy;

import java.util.Arrays;

public class BackgroundPixelFIFO extends FIFO {

    private int discard;

    public BackgroundPixelFIFO(VoltBoy gb) {
        super(gb);
        pixels = new Pixel[8];
        size = 0;
        discard = 0;
    }

    public void discardPixels(int count) {
        discard = count;
    }

    @Override
    public void push(Pixel p) {
        pixels[size++] = p;
    }

    @Override
    public Pixel pop() {
        Pixel ret;
        if(discard > 0) {
            ret = null;
            discard--;
        } else {
            ret = pixels[0];
        }
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