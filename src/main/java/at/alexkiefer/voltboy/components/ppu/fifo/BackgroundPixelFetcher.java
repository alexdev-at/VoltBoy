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
        fetcherX = 0;
        step = 1;
        tileNumber = 0;
        tileDataAddr = 0;
        tileData = 0;
    }

    @Override
    public void tick() {

        switch(step) {
            case 1 -> fetchTileNumber();
            case 2 -> fetchTileDataLow();
            case 3 -> fetchTileDataHigh();
            case 4 -> convertAndPush();
        }

        step++;

        if(step == 5) {
            step = 1;
        }

    }

    public void startWindowMode() {
        windowMode = true;
        softReset();
    }

    public void stopWindowMode() {
        windowMode = false;
        softReset();
    }

    public boolean isWindowMode() {
        return windowMode;
    }

    public void incWindowCounter() {
        fetcherWindowY++;
    }

    public void resetWindowCounter() {
        fetcherWindowY = 0;
    }

    @Override
    protected void fetchTileNumber() {

        int ly = gb.getDataBus().read(0xFF44);

        if(windowMode) {

            int tileMapArea = (gb.getDataBus().read(0xFF40) & BitUtils.M_SIX) == 0 ? 0x9800 : 0x9C00;
            fetcherY = 32 * (fetcherWindowY / 8);
            tileNumber = gb.getDataBus().read(tileMapArea + ((fetcherX + fetcherY) & 0x03FF));

        } else {

            int tileMapArea = (gb.getDataBus().read(0xFF40) & BitUtils.M_THREE) == 0 ? 0x9800 : 0x9C00;
            int scy = gb.getDataBus().read(0xFF42);
            int scx = gb.getDataBus().read(0xFF43);
            fetcherY = 32 * (((ly + scy) & 0xFF) / 8);
            int x = fetcherX + (scx / 8);
            x &= 0x1F;
            tileNumber = gb.getDataBus().read(tileMapArea + ((x + fetcherY) & 0x03FF));

        }

    }

    @Override
    protected void fetchTileDataLow() {

        int tileDataArea = (gb.getDataBus().read(0xFF40) & BitUtils.M_FOUR) == 0 ? 0x9000 : 0x8000;
        boolean signed = tileDataArea == 0x9000;

        if(windowMode) {

            int offset = 2 * (fetcherWindowY % 8);
            tileDataAddr = tileDataArea + offset + ((signed ? (byte) tileNumber : tileNumber) * 16);
            tileData = gb.getDataBus().read(tileDataAddr++);

        } else {

            int ly = gb.getDataBus().read(0xFF44);
            int scy = gb.getDataBus().read(0xFF42);
            int offset = 2 * ((ly + scy) % 8);
            tileDataAddr = tileDataArea + offset + ((signed ? (byte) tileNumber : tileNumber) * 16);
            tileData = gb.getDataBus().read(tileDataAddr++);

        }

    }

    @Override
    protected void fetchTileDataHigh() {

        tileData |= gb.getDataBus().read(tileDataAddr) << 8;

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

        for(int i = 0; i < 8; i++) {
            int loBit = (lo & (1 << (7 - i))) >> (7 - i);
            int hiBit = (hi & (1 << (7 - i))) >> (7 - i);
            int color = (loBit | (hiBit << 1));
            fifo.push(new Pixel(color, 0, 0));
            //fifo.push(new Pixel(gb.getDataBus().read(0xFF44) % 2 == 0 ? 0 : 3, 0, 0));
        }

        fetcherX = (fetcherX + 1) & 0x1F;

    }

}