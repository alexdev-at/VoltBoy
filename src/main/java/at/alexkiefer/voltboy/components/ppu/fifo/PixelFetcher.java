package at.alexkiefer.voltboy.components.ppu.fifo;

import at.alexkiefer.voltboy.ConnectedInternal;
import at.alexkiefer.voltboy.Tickable;
import at.alexkiefer.voltboy.VoltBoy;
import at.alexkiefer.voltboy.util.BitUtils;

public class PixelFetcher extends ConnectedInternal implements Tickable {

    private int fetcherX;
    private int fetcherY;

    private int step;

    private int tileNumber;
    private int tileDataAddr;
    private int tileData;

    private boolean firstFetch;
    private boolean skip;

    public PixelFetcher(VoltBoy gb) {

        super(gb);

        reset();

    }

    public void reset() {
        fetcherX = 0;
        fetcherY = 0;
        step = 1;
        tileNumber = 0;
        tileDataAddr = 0;
        tileData = 0;
        firstFetch = true;
        skip = false;
    }

    public int getFetcherX() {
        return fetcherX;
    }

    public int getFetcherY() {
        return fetcherY;
    }

    @Override
    public void tick() {

        if(skip) {
            skip = false;
            return;
        }

        switch(step) {
            case 1 -> fetchTileNumber();
            case 2 -> fetchTileDataLow();
            case 3 -> fetchTileDataHigh();
            // case 4 -> sleep
            case 5 -> convertAndPush();
        }

        step++;

        if(step == 6) {
            step = 1;
        }

    }

    private void fetchTileNumber() {

        // TODO: Scrolling
        int tileMapArea = (gb.getDataBus().read(0xFF40) & BitUtils.M_THREE) == 0 ? 0x9800 : 0x9C00;
        int ly = gb.getDataBus().read(0xFF44);
        fetcherY = ly;
        tileNumber = gb.getDataBus().read(tileMapArea + fetcherX + fetcherY);

        skip = true;

    }

    private void fetchTileDataLow() {

        int tileDataArea = (gb.getDataBus().read(0xFF40) & BitUtils.M_FOUR) == 0 ? 0x9000 : 0x8000;
        boolean signed = tileDataArea == 0x9000;

        // TODO: Scrolling
        int ly = gb.getDataBus().read(0xFF44);
        int offset = 2 * (ly % 8);
        tileDataAddr = tileDataArea + offset + ((signed ? (byte) tileNumber : tileNumber) * 16);
        tileData = gb.getDataBus().read(tileDataAddr++);

        skip = true;

    }

    private void fetchTileDataHigh() {

        tileData |= gb.getDataBus().read(tileDataAddr) << 8;

        if(firstFetch) {
            firstFetch = false;
            step = 0;
        }

        skip = false;

    }

    private void convertAndPush() {

        FIFO fifo = gb.getPpu().getBackgroundPixelFifo();

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
        }

        fetcherX = (fetcherX + 1) & 0x1F;

        skip = true;

    }

}