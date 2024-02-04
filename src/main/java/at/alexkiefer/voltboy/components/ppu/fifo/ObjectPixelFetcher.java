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

        if((gb.getDataBus().readUnrestricted(0xFF40) & BitUtils.M_ONE) == 0) {
            reset();
            gb.getPpu().getObjectPixelFifo().clear();
        }

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
        int size = (gb.getDataBus().readUnrestricted(0xFF40) & BitUtils.M_TWO) == 0 ? 8 : 16;
        if(size == 16) {
            tileNumber &= ~BitUtils.M_ZERO;
        }

    }

    @Override
    protected void fetchTileDataLow() {

        int tileDataArea = 0x8000;

        int ly = gb.getDataBus().readUnrestricted(0xFF44);
        int offset = (ly + 16) - current.getY();
        if(current.getAttributes().isYFlip()) {
            int size = (gb.getDataBus().readUnrestricted(0xFF40) & BitUtils.M_TWO) == 0 ? 8 : 16;
            offset = (size - 1) - offset;
        }
        tileDataAddr = tileDataArea + (2 * offset) + (tileNumber * 16);
        tileData = gb.getDataBus().readUnrestricted(tileDataAddr++);

    }

    @Override
    protected void fetchTileDataHigh() {

        tileData |= gb.getDataBus().readUnrestricted(tileDataAddr) << 8;

    }

    @Override
    protected void convertAndPush() {

        ObjectPixelFIFO fifo = gb.getPpu().getObjectPixelFifo();

        int lo = tileData & 0xFF;
        int hi = tileData >> 8;

        Pixel[] pixels = new Pixel[8];
        int palette = gb.getDataBus().readUnrestricted(current.getAttributes().isObjectPaletteZero() ? 0xFF48 : 0xFF49);

        if(current.getAttributes().isXFlip()) {
            for(int i = 0; i < 8; i++) {
                int loBit = (lo & (1 << i)) >> i;
                int hiBit = (hi & (1 << i)) >> i;
                int color = (loBit | (hiBit << 1));

                color = switch(color) {
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
                    case 0b01 -> (palette & 0b1100) >> 2;
                    case 0b10 -> (palette & 0b110000) >> 4;
                    case 0b11 -> (palette & 0b11000000) >> 6;
                    default -> color;
                };

                pixels[i] = new Pixel(color, 0, current.getAttributes().isPriority() ? 1 : 0);
            }
        }

        fetcherX = (fetcherX + 1) & 0x1F;

        fifo.fill(pixels);

        current = null;

    }

}