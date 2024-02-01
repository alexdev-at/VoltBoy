package at.alexkiefer.voltboy.components.ppu.fifo;

import at.alexkiefer.voltboy.ConnectedInternal;
import at.alexkiefer.voltboy.VoltBoy;

import java.util.Arrays;

public class FIFO extends ConnectedInternal {

    private final Pixel[] pixels;
    private int size;
    private int discard;

    public FIFO(VoltBoy gb) {
        super(gb);
        pixels = new Pixel[8];
        size = 0;
        discard = 0;
    }

    public int getSize() {
        return size;
    }

    public void discardPixels(int count) {
        discard = count;
    }

    public void push(Pixel p) {
        pixels[size++] = p;
    }

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

    public void clear() {
        Arrays.fill(pixels, null);
        size = 0;
    }

    public void print() {
        for(int i = 0; i < 8; i++) {
            System.out.print("<- [" + (pixels[i] == null ? null : Integer.toBinaryString(pixels[i].getColor())) + "] ");
        }
        System.out.println();
    }

}