package at.alexkiefer.voltboy.core.ppu.pixel;

import at.alexkiefer.voltboy.core.VoltBoy;
import at.alexkiefer.voltboy.core.ppu.object.OAMObject;
import at.alexkiefer.voltboy.util.BitMasks;

public class ObjectPixelFetcher extends PixelFetcher {

    private OAMObject current;

    public ObjectPixelFetcher(VoltBoy gb) {

        super(gb);

        reset();

    }

    public OAMObject getCurrent() {
        return current;
    }

    public void setCurrent(OAMObject current) {
        this.current = current;
    }

    @Override
    public void tick() {

        if((gb.getPpu().getLcdc() & BitMasks.SEVEN) == 0) {
            return;
        }

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
        current = null;

    }

    @Override
    protected void fetchTileId() {

        tileId = current.getTileIndex();
        int size = (gb.getPpu().getLcdc() & BitMasks.TWO) == 0 ? 8 : 16;
        if (size == 16) {
            tileId &= ~BitMasks.ZERO;
        }

    }

    @Override
    protected void fetchTileDataLow() {

        int tileDataArea = 0x8000;

        int ly = gb.getPpu().getLy();
        int offset = (ly + 16) - current.getY();
        if (current.getAttributes().isYFlip()) {
            int size = (gb.getPpu().getLcdc() & BitMasks.TWO) == 0 ? 8 : 16;
            offset = (size - 1) - offset;
        }
        tileDataAddress = tileDataArea + (2 * offset) + (tileId * 16);
        tileData = gb.getMemoryBus().readUnrestricted(tileDataAddress++);

    }

    @Override
    protected void fetchTileDataHigh() {

        tileData |= gb.getMemoryBus().readUnrestricted(tileDataAddress) << 8;

    }

    @Override
    protected void pushPixels() {

        ObjectPixelFIFO fifo = gb.getPpu().getObjectPixelFifo();

        int lo = tileData & 0xFF;
        int hi = tileData >> 8;

        Pixel[] pixels = new Pixel[8];
        int palette = gb.getMemoryBus().readUnrestricted(current.getAttributes().isObjectPaletteZero() ? 0xFF48 : 0xFF49);

        if(current.getAttributes().isXFlip()) {
            for(int i = 0; i < 8; i++) {
                int loBit = (lo & (1 << i)) >> i;
                int hiBit = (hi & (1 << i)) >> i;
                int color = (loBit | (hiBit << 1));

                color = switch(color) {
                    case 0b00 -> -1;
                    case 0b01 -> (palette & 0b1100) >> 2;
                    case 0b10 -> (palette & 0b110000) >> 4;
                    case 0b11 -> (palette & 0b11000000) >> 6;
                    default -> color;
                };

                pixels[i] = new Pixel(color, 0, current.getAttributes().isPriority() ? 1 : 0);
            }
        } else {
            for(int i = 0; i < 8; i++) {
                int loBit = (lo & (1 << (7 - i))) >> (7 - i);
                int hiBit = (hi & (1 << (7 - i))) >> (7 - i);
                int color = (loBit | (hiBit << 1));

                color = switch(color) {
                    case 0b00 -> -1;
                    case 0b01 -> (palette & 0b1100) >> 2;
                    case 0b10 -> (palette & 0b110000) >> 4;
                    case 0b11 -> (palette & 0b11000000) >> 6;
                    default -> color;
                };

                pixels[i] = new Pixel(color, 0, current.getAttributes().isPriority() ? 1 : 0);
            }
        }

        fetcherX = (fetcherX + 1) & 0x1F;

        if(current.getX() < 8) {
            fifo.fillSize(pixels, 8 - (8 - current.getX()));
        } else {
            fifo.fill(pixels);
        }

        current = null;

    }

}
