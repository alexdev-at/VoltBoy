package at.alexkiefer.voltboy.components.ppu.fifo;

import at.alexkiefer.voltboy.VoltBoy;
import at.alexkiefer.voltboy.util.BitUtils;

public class BackgroundPixelFetcher extends PixelFetcher {

    private boolean windowMode;
    private int fetcherWindowY;

    public BackgroundPixelFetcher(VoltBoy gb) {

        super(gb);

        reset();

    }

    @Override
    public void reset() {
        fetcherX = 0;
        step = 1;
        tileNumber = 0;
        tileDataAddr = 0;
        tileData = 0;
        firstFetch = true;
        windowMode = false;
    }

    public void softReset() {
        step = 1;
        tileNumber = 0;
        tileDataAddr = 0;
        tileData = 0;
    }

    @Override
    public void tick() {

        switch(step) {
            case 1 -> fetchTileNumber();
            case 3 -> fetchTileDataLow();
            case 5 -> fetchTileDataHigh();
            case 7 -> convertAndPush();
        }

        step++;

        if(step == 9) {
            step = 1;
        }

    }

    public void startWindowMode() {
        fetcherX = 0;
        windowMode = true;
        step = 1;
        tileNumber = 0;
        tileDataAddr = 0;
        tileData = 0;
    }

    public boolean isWindowMode() {
        return windowMode;
    }

    public void incWindowCounter() {
        fetcherWindowY++;
    }

    public int getWindowCounter() {
        return fetcherWindowY;
    }

    public void resetWindowCounter() {
        fetcherWindowY = 0;
    }

    @Override
    protected void fetchTileNumber() {

        int ly = gb.getDataBus().readUnrestricted(0xFF44);

        if(windowMode) {

            int tileMapArea = (gb.getDataBus().readUnrestricted(0xFF40) & BitUtils.M_SIX) == 0 ? 0x9800 : 0x9C00;
            fetcherY = 32 * (fetcherWindowY / 8);
            tileNumber = gb.getDataBus().readUnrestricted(tileMapArea + ((fetcherX + fetcherY) & 0x03FF));

        } else {

            int tileMapArea = (gb.getDataBus().readUnrestricted(0xFF40) & BitUtils.M_THREE) == 0 ? 0x9800 : 0x9C00;
            int scy = gb.getDataBus().readUnrestricted(0xFF42);
            int scx = gb.getDataBus().readUnrestricted(0xFF43);
            fetcherY = 32 * (((ly + scy) & 0xFF) / 8);
            int x = fetcherX + (scx / 8);
            x &= 0x1F;
            tileNumber = gb.getDataBus().readUnrestricted(tileMapArea + ((x + fetcherY) & 0x03FF));

        }

    }

    @Override
    protected void fetchTileDataLow() {

        int tileDataArea = (gb.getDataBus().readUnrestricted(0xFF40) & BitUtils.M_FOUR) == 0 ? 0x9000 : 0x8000;
        boolean signed = tileDataArea == 0x9000;

        if(windowMode) {

            int offset = 2 * (fetcherWindowY % 8);
            tileDataAddr = tileDataArea + offset + ((signed ? (byte) tileNumber : tileNumber) * 16);
            tileData = gb.getDataBus().readUnrestricted(tileDataAddr++);

        } else {

            int ly = gb.getDataBus().readUnrestricted(0xFF44);
            int scy = gb.getDataBus().readUnrestricted(0xFF42);
            int offset = 2 * ((ly + scy) % 8);
            tileDataAddr = tileDataArea + offset + ((signed ? (byte) tileNumber : tileNumber) * 16);
            tileData = gb.getDataBus().readUnrestricted(tileDataAddr++);

        }

    }

    @Override
    protected void fetchTileDataHigh() {

        tileData |= gb.getDataBus().readUnrestricted(tileDataAddr) << 8;

        if(firstFetch) {
            firstFetch = false;
            step = 0;
        }

    }

    @Override
    protected void convertAndPush() {

        BackgroundPixelFIFO fifo = gb.getPpu().getBackgroundPixelFifo();

        if(fifo.getSize() > 0) {
            step--;
            return;
        }

        int lo = tileData & 0xFF;
        int hi = tileData >> 8;

        Pixel[] pixels = new Pixel[8];
        int palette = gb.getDataBus().readUnrestricted(0xFF47);

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