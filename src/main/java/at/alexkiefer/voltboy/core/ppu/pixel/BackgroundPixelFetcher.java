package at.alexkiefer.voltboy.core.ppu.pixel;

import at.alexkiefer.voltboy.core.VoltBoy;
import at.alexkiefer.voltboy.util.BitMasks;

public class BackgroundPixelFetcher extends PixelFetcher {

    private final BackgroundPixelFIFO fifo;

    private boolean firstFetch;
    private boolean windowMode;
    private int fetcherWindowY;

    public BackgroundPixelFetcher(VoltBoy gb) {

        super(gb);

        fifo = gb.getPpu().getBackgroundPixelFifo();

        reset();

    }

    @Override
    public void tick() {

        switch (step) {
            case 1 -> fetchTileId();
            case 3 -> fetchTileDataLow();
            case 5 -> fetchTileDataHigh();
            case 7 -> pushPixels();
        }

        step++;

        if (step == 9) {
            step = 1;
        }

    }

    @Override
    public void reset() {

        step = 1;
        fetcherX = 0;
        fetcherY = 0;
        firstFetch = true;
        windowMode = false;
        fetcherWindowY = 0;

    }

    public void startWindowMode() {

        step = 1;
        fetcherX = 0;
        fetcherY = 0;
        windowMode = true;
        fetcherWindowY = 0;

    }

    public void resetFetcherWindowY() {
        fetcherWindowY = 0;
    }

    public boolean isWindowMode() {
        return windowMode;
    }

    public void incFetcherWindowY() {
        fetcherWindowY++;
    }

    @Override
    protected void fetchTileId() {

        if (windowMode) {

            int tileMapArea = (memoryBus.readUnrestricted(0xFF40) & BitMasks.SIX) == 0 ? 0x9800 : 0x9C00;
            int y = 32 * (fetcherWindowY / 8);
            tileId = memoryBus.read(tileMapArea + ((fetcherX + y )& 0x03FF));

        } else {

            int ly = memoryBus.readUnrestricted(0xFF44);
            int tileMapArea = (memoryBus.readUnrestricted(0xFF40) & BitMasks.THREE) == 0 ? 0x9800 : 0x9C00;
            int scy = memoryBus.readUnrestricted(0xFF42);
            int scx = memoryBus.readUnrestricted(0xFF43);
            int x = (fetcherX + (scx / 8)) & 0x1F;
            int y = 32 * (((ly + scy) & 0xFF) / 8);
            tileId = memoryBus.read(tileMapArea + ((x + y) & 0x03FF));

        }

    }

    @Override
    protected void fetchTileDataLow() {

        int tileDataArea = (memoryBus.readUnrestricted(0xFF40) & BitMasks.FOUR) == 0 ? 0x9000 : 0x8000;
        boolean signed = tileDataArea == 0x9000;

        if (windowMode) {

            int offset = 2 * (fetcherWindowY % 8);
            tileDataAddress = tileDataArea + offset + ((signed ? (byte) tileId : tileId) * 16);
            tileData = memoryBus.readUnrestricted(tileDataAddress++);

        } else {

            int ly = memoryBus.readUnrestricted(0xFF44);
            int scy = memoryBus.readUnrestricted(0xFF42);
            int offset = 2 * ((ly + scy) % 8);
            tileDataAddress = tileDataArea + offset + ((signed ? (byte) tileId : tileId) * 16);
            tileData = memoryBus.readUnrestricted(tileDataAddress++);

        }

    }

    @Override
    protected void fetchTileDataHigh() {

        tileData |= memoryBus.readUnrestricted(tileDataAddress) << 8;

        if (firstFetch) {
            firstFetch = false;
            step = 0;
        }

    }

    @Override
    protected void pushPixels() {

        if(fifo.getSize() > 0) {
            step--;
            return;
        }

        int lo = tileData & 0xFF;
        int hi = tileData >> 8;

        Pixel[] pixels = new Pixel[8];
        int palette = memoryBus.readUnrestricted(0xFF47);

        for(int i = 0; i < 8; i++) {
            int loBit = (lo & (1 << (7 - i))) >> (7 - i);
            int hiBit = (hi & (1 << (7 - i))) >> (7 - i);
            int color = (loBit | (hiBit << 1));

            switch(color) {
                case 0b00 -> color = palette & 0b11;
                case 0b01 -> color = (palette & 0b1100) >> 2;
                case 0b10 -> color = (palette & 0b110000) >> 4;
                case 0b11 -> color = (palette & 0b11000000) >> 6;
            }

            pixels[i] = new Pixel(color, 0, 0);
        }

        fifo.fill(pixels);

        fetcherX = (fetcherX + 1) & 0x1F;

    }

}
