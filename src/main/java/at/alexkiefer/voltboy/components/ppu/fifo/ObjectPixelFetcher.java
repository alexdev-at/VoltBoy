package at.alexkiefer.voltboy.components.ppu.fifo;

import at.alexkiefer.voltboy.VoltBoy;
import at.alexkiefer.voltboy.components.ppu.objects.OAMObject;
import at.alexkiefer.voltboy.util.BitUtils;

public class ObjectPixelFetcher extends PixelFetcher {

    private OAMObject current;

    public ObjectPixelFetcher(VoltBoy gb) {

        super(gb);

        reset();

    }

    public void setCurrent(OAMObject current) {
        this.current = current;
    }

    public OAMObject getCurrent() {
        return current;
    }

    @Override
    public void reset() {
        fetcherX = 0;
        step = 1;
        tileDataAddr = 0;
        tileData = 0;
        tileNumber = 0;
        current = null;
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

    @Override
    protected void fetchTileNumber() {

        tileNumber = current.getTileIndex();
        if(current.getSize() == 16) {
            tileNumber &= ~BitUtils.M_ZERO;
        }

    }

    @Override
    protected void fetchTileDataLow() {

        int tileDataArea = 0x8000;

        int ly = gb.getDataBus().read(0xFF44);
        int offset;
        if(current.getAttributes().isYFlip()) {
            offset = 2 * (7 - (ly % 8));
        } else {
            offset = 2 * (ly % 8);
        }
        tileDataAddr = tileDataArea + offset + (tileNumber * 16);
        tileData = gb.getDataBus().read(tileDataAddr++);

    }

    @Override
    protected void fetchTileDataHigh() {

        tileData |= gb.getDataBus().read(tileDataAddr) << 8;

    }

    @Override
    protected void convertAndPush() {

        ObjectPixelFIFO fifo = gb.getPpu().getObjectPixelFifo();

        int lo = tileData & 0xFF;
        int hi = tileData >> 8;

        Pixel[] pixels = new Pixel[8];

        if(current.getAttributes().isXFlip()) {
            for(int i = 0; i < 8; i++) {
                int loBit = (lo & (1 << i)) >> i;
                int hiBit = (hi & (1 << i)) >> i;
                int color = (loBit | (hiBit << 1));
                pixels[i] = new Pixel(color, 0, current.getAttributes().isPriority() ? 1 : 0);
            }
        } else {
            for(int i = 0; i < 8; i++) {
                int loBit = (lo & (1 << (7 - i))) >> (7 - i);
                int hiBit = (hi & (1 << (7 - i))) >> (7 - i);
                int color = (loBit | (hiBit << 1));
                pixels[i] = new Pixel(color, 0, current.getAttributes().isPriority() ? 1 : 0);
            }
        }

        fetcherX = (fetcherX + 1) & 0x1F;

        fifo.fill(pixels);

        current = null;

    }

}