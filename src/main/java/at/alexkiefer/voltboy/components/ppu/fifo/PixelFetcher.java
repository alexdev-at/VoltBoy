package at.alexkiefer.voltboy.components.ppu.fifo;

import at.alexkiefer.voltboy.ConnectedInternal;
import at.alexkiefer.voltboy.Tickable;
import at.alexkiefer.voltboy.VoltBoy;

public abstract class PixelFetcher extends ConnectedInternal implements Tickable {

    protected int fetcherX;
    protected int fetcherY;

    protected int step;

    protected int tileNumber;
    protected int tileDataAddr;
    protected int tileData;

    protected boolean firstFetch;

    public PixelFetcher(VoltBoy gb) {

        super(gb);

        reset();

    }

    abstract void reset();

    public int getStep() {
        return step;
    }

    @Override
    public abstract void tick();

    abstract void fetchTileNumber();

    abstract void fetchTileDataLow();

    abstract void fetchTileDataHigh();

    abstract void convertAndPush();

}