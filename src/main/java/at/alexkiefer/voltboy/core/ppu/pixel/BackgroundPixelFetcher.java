package at.alexkiefer.voltboy.core.ppu.pixel;

import at.alexkiefer.voltboy.core.VoltBoy;
import at.alexkiefer.voltboy.util.BitMasks;

public class BackgroundPixelFetcher extends PixelFetcher {

    private boolean firstFetch;
    private boolean windowMode;
    private int fetcherWindowY;

    public BackgroundPixelFetcher(VoltBoy gb) {

        super(gb);

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

    }

    public void startWindowMode() {

        step = 1;
        fetcherX = 0;
        fetcherY = 0;
        windowMode = true;

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

            int tileMapArea = (gb.getPpu().getLcdc() & BitMasks.SIX) == 0 ? 0x9800 : 0x9C00;
            int y = 32 * (fetcherWindowY / 8);
            tileId = gb.getMemoryBus().read(tileMapArea + ((fetcherX + y ) & 0x03FF));

        } else {

            int ly = gb.getPpu().getLy();
            int tileMapArea = (gb.getPpu().getLcdc() & BitMasks.THREE) == 0 ? 0x9800 : 0x9C00;
            int scy = gb.getPpu().getScy();
            int scx = gb.getPpu().getScx();
            int x = (fetcherX + (scx / 8)) & 0x1F;
            int y = 32 * (((ly + scy) & 0xFF) / 8);
            tileId = gb.getMemoryBus().readUnrestricted(tileMapArea + ((x + y) & 0x03FF));

        }

    }

    @Override
    protected void fetchTileDataLow() {

        int tileDataArea = (gb.getPpu().getLcdc() & BitMasks.FOUR) == 0 ? 0x9000 : 0x8000;
        boolean signed = tileDataArea == 0x9000;

        if (windowMode) {

            int offset = 2 * (fetcherWindowY % 8);
            tileDataAddress = tileDataArea + offset + ((signed ? (byte) tileId : tileId) * 16);
            tileData = gb.getMemoryBus().readUnrestricted(tileDataAddress++);

        } else {

            int ly = gb.getPpu().getLy();
            int scy = gb.getPpu().getScy();
            int offset = 2 * ((ly + scy) % 8);
            tileDataAddress = tileDataArea + offset + ((signed ? (byte) tileId : tileId) * 16);
            tileData = gb.getMemoryBus().readUnrestricted(tileDataAddress++);

        }

    }

    @Override
    protected void fetchTileDataHigh() {

        tileData |= gb.getMemoryBus().readUnrestricted(tileDataAddress) << 8;

        if (firstFetch) {
            firstFetch = false;
            step = 1;
        }

    }

    @Override
    protected void pushPixels() {

        BackgroundPixelFIFO fifo = gb.getPpu().getBackgroundPixelFifo();

        if(fifo.getSize() > 0) {
            step--;
            return;
        }

        int lo = tileData & 0xFF;
        int hi = tileData >> 8;

        Pixel[] pixels = new Pixel[8];
        int palette = gb.getMemoryBus().readUnrestricted(0xFF47);

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
