package at.alexkiefer.voltboy.core.ppu.pixel;

import at.alexkiefer.voltboy.core.ConnectedInternal;
import at.alexkiefer.voltboy.core.VoltBoy;

public abstract class PixelFIFO extends ConnectedInternal {

    protected Pixel[] pixels;
    protected int size;

    public PixelFIFO(VoltBoy gb) {
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

}
