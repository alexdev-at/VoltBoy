package at.alexkiefer.voltboy.components.ppu.fifo;

import at.alexkiefer.voltboy.ConnectedInternal;
import at.alexkiefer.voltboy.VoltBoy;

import java.util.Arrays;

public abstract class FIFO extends ConnectedInternal {

    protected Pixel[] pixels;
    protected int size;

    public FIFO(VoltBoy gb) {
        super(gb);
        pixels = new Pixel[8];
        size = 0;
    }

    public int getSize() {
        return size;
    }

    abstract void fill(Pixel[] p);

    abstract Pixel pop();

    abstract void clear();

    public void print() {
        for(int i = 0; i < 8; i++) {
            System.out.print("<- [" + (pixels[i] == null ? null : Integer.toBinaryString(pixels[i].getColor())) + "] ");
        }
        System.out.println();
    }

}