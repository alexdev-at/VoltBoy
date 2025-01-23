package at.alexkiefer.voltboy.core.ppu.pixel;

import at.alexkiefer.voltboy.core.ConnectedInternal;
import at.alexkiefer.voltboy.core.Tickable;
import at.alexkiefer.voltboy.core.VoltBoy;
import at.alexkiefer.voltboy.core.memory.MemoryBus;

public abstract class PixelFetcher extends ConnectedInternal implements Tickable {

    protected int fetcherX;
    protected int fetcherY;

    protected int step;

    protected int tileId;
    protected int tileData;
    protected int tileDataAddress;

    protected MemoryBus memoryBus;

    public PixelFetcher(VoltBoy gb) {
        super(gb);
        memoryBus = gb.getMemoryBus();
    }

    public int getStep() {
        return step;
    }

    @Override
    public abstract void tick();

    public abstract void reset();

    protected abstract void fetchTileId();

    protected abstract void fetchTileDataLow();

    protected abstract void fetchTileDataHigh();

    protected abstract void pushPixels();

}
