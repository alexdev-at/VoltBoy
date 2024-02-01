package at.alexkiefer.voltboy.components.ppu.fifo;

import at.alexkiefer.voltboy.VoltBoy;
import at.alexkiefer.voltboy.components.ppu.objects.OAMObject;

public class ObjectPixelFetcher extends PixelFetcher {

    OAMObject current;

    public ObjectPixelFetcher(VoltBoy gb) {

        super(gb);

        reset();

    }

    public void setCurrent(OAMObject current) {
        this.current = current;
    }

    @Override
    public void reset() {
        fetcherX = 0;
        step = 1;
        tileDataAddr = 0;
        tileData = 0;
        firstFetch = true;
        current = null;
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

    @Override
    protected void fetchTileNumber() {

        tileNumber = current.getTileIndex();

    }

    @Override
    protected void fetchTileDataLow() {

        int tileDataArea = 0x8000;

        int ly = gb.getDataBus().read(0xFF44);
        int scy = gb.getDataBus().read(0xFF42);
        int offset = 2 * ((ly + scy) % 8);
        tileDataAddr = tileDataArea + offset + (tileNumber * 16);
        tileData = gb.getDataBus().read(tileDataAddr++);

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