package at.alexkiefer.voltboy.components.ppu.fifo;

import at.alexkiefer.voltboy.VoltBoy;

import java.util.Arrays;

public class ObjectPixelFIFO extends FIFO {


    public ObjectPixelFIFO(VoltBoy gb) {
        super(gb);
    }

    @Override
    public void push(Pixel p) {
        pixels[size++] = p;
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