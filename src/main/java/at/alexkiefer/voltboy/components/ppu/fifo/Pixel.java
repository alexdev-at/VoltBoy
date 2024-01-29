package at.alexkiefer.voltboy.components.ppu.fifo;

public class Pixel {

    private final int color;
    private final int palette;
    private final int backgroundPriority;

    public Pixel(int color, int palette, int backgroundPriority) {
        this.color = color;
        this.palette = palette;
        this.backgroundPriority = backgroundPriority;
    }

    public int getColor() {
        return color;
    }

}