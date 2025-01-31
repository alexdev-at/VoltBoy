package at.alexkiefer.voltboy.core.ppu;

import at.alexkiefer.voltboy.core.ConnectedInternal;
import at.alexkiefer.voltboy.core.Tickable;
import at.alexkiefer.voltboy.core.VoltBoy;
import at.alexkiefer.voltboy.core.ppu.object.OAMObject;
import at.alexkiefer.voltboy.core.ppu.object.OAMObjectAttributes;
import at.alexkiefer.voltboy.core.ppu.pixel.*;
import at.alexkiefer.voltboy.util.BitMasks;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class PPU extends ConnectedInternal implements Tickable {

    private final BackgroundPixelFetcher backgroundPixelFetcher;
    private final ObjectPixelFetcher objectPixelFetcher;
    private final BackgroundPixelFIFO backgroundPixelFifo;
    private final ObjectPixelFIFO objectPixelFifo;

    private final Pixel[][] lcd;

    private int dot;

    private PPUMode mode;

    private int lx;
    private int ly;
    private int lyc;
    private int scy;
    private int scx;
    private int wy;
    private int wx;
    private int stat;
    private int lcdc;

    private List<OAMObject> oamBuffer;
    private int oamAddress;

    public PPU(VoltBoy gb) {

        super(gb);

        backgroundPixelFifo = new BackgroundPixelFIFO(gb);
        objectPixelFifo = new ObjectPixelFIFO(gb);
        backgroundPixelFetcher = new BackgroundPixelFetcher(gb);
        objectPixelFetcher = new ObjectPixelFetcher(gb);

        mode = PPUMode.MODE_2_OAMSCAN;

        oamBuffer =  new ArrayList<>();
        oamAddress = 0xFE00;

        lcd = new Pixel[144][160];
        for(int i = 0; i < 144; i++) {
            for(int j = 0; j < 160; j++) {
                lcd[i][j] = new Pixel(0, 0, 0);
            }
        }

    }

    public Pixel[][] getLcd() {
        return lcd;
    }

    public PPUMode getMode() {
        return mode;
    }

    public BackgroundPixelFIFO getBackgroundPixelFifo() {
        return backgroundPixelFifo;
    }

    public BackgroundPixelFetcher getBackgroundPixelFetcher() {
        return backgroundPixelFetcher;
    }

    public ObjectPixelFIFO getObjectPixelFifo() {
        return objectPixelFifo;
    }

    public ObjectPixelFetcher getObjectPixelFetcher() {
        return objectPixelFetcher;
    }

    public int getLx() {
        return lx;
    }

    public void setLx(int lx) {
        this.lx = lx;
    }

    public int getLy() {
        return ly;
    }

    public void setLy(int ly) {
        this.ly = ly;
    }

    public int getLyc() {
        return lyc;
    }

    public void setLyc(int lyc) {
        this.lyc = lyc;
    }

    public int getScy() {
        return scy;
    }

    public void setScy(int scy) {
        this.scy = scy;
    }

    public int getScx() {
        return scx;
    }

    public void setScx(int scx) {
        this.scx = scx;
    }

    public int getWy() {
        return wy;
    }

    public void setWy(int wy) {
        this.wy = wy;
    }

    public int getWx() {
        return wx;
    }

    public void setWx(int wx) {
        this.wx = wx;
    }

    public int getStat() {
        return stat;
    }

    public void setStat(int stat) {
        this.stat = stat & 0b0111_1100;
    }

    public int getLcdc() {
        return lcdc;
    }

    public void setLcdc(int lcdc) {
        this.lcdc = lcdc;
    }

    @Override
    public void tick() {

        if ((lcdc & BitMasks.SEVEN) == 0) {
            return;
        }

        tickPpu();
        tickPpu();
        tickPpu();
        tickPpu();

    }

    private void tickPpu() {

        if (dot == 0) {
            backgroundPixelFifo.clear();
            backgroundPixelFifo.discardPixels(scx % 8);
            backgroundPixelFetcher.reset();
            objectPixelFifo.clear();
            objectPixelFetcher.reset();
            oamAddress = 0xFE00;
            oamBuffer.clear();
        }

        PPUMode oldMode = mode;

        if(ly <= 143) {

            if(dot == 80) {
                mode = PPUMode.MODE_3_RENDERING;
                oamBuffer.sort(Comparator.comparingInt(OAMObject::getX));
                Set<Integer> seen = ConcurrentHashMap.newKeySet();
                oamBuffer = oamBuffer.stream()
                        .filter(o -> seen.add(o.getX()))
                        .collect(Collectors.toList());
            } else if(lx == 160) {
                lx = 0;
                mode = PPUMode.MODE_0_HBLANK;
            } else if(dot == 0) {
                mode = PPUMode.MODE_2_OAMSCAN;
            }

        } else if(ly == 144) {
            backgroundPixelFetcher.resetFetcherWindowY();
            mode = PPUMode.MODE_1_VBLANK;
        }

        switch (mode) {
            case MODE_0_HBLANK, MODE_1_VBLANK -> {}
            case MODE_2_OAMSCAN -> oamScan();
            case MODE_3_RENDERING -> render();
        }

        if(mode != oldMode) {

            stat = (stat & 0b11111100) | mode.getValue();

            switch(mode) {
                case MODE_0_HBLANK -> {
                    if((stat & BitMasks.THREE) != 0) {
                        gb.getMemoryBus().writeUnrestricted(0xFF0F, gb.getMemoryBus().readUnrestricted(0xFF0F) | BitMasks.ONE);
                    }
                }
                case MODE_1_VBLANK -> {
                    gb.getMemoryBus().writeUnrestricted(0xFF0F, gb.getMemoryBus().readUnrestricted(0xFF0F) | BitMasks.ZERO);
                    if((stat & BitMasks.FOUR) != 0) {
                        gb.getMemoryBus().writeUnrestricted(0xFF0F, gb.getMemoryBus().readUnrestricted(0xFF0F) | BitMasks.ONE);
                    }
                }
                case MODE_2_OAMSCAN -> {
                    if((stat & BitMasks.FIVE) != 0) {
                        gb.getMemoryBus().writeUnrestricted(0xFF0F, gb.getMemoryBus().readUnrestricted(0xFF0F) | BitMasks.ONE);
                    }
                }
            }

        }

        dot++;

        if(ly == lyc) {
            stat |= BitMasks.TWO;
        } else {
            stat &= ~BitMasks.TWO;
        }

        if(dot == 456) {

            dot = 0;
            lx = 0;
            ly = (ly + 1) % 154;

            if(ly == lyc && (stat & BitMasks.SIX) != 0) {
                gb.getMemoryBus().writeUnrestricted(0xFF0F, gb.getMemoryBus().readUnrestricted(0xFF0F) | BitMasks.ONE);
            }

            if(backgroundPixelFetcher.isWindowMode()) {
                backgroundPixelFetcher.incFetcherWindowY();
            }

        }

    }

    private void oamScan() {

        if (dot % 2 != 0) {
            return;
        }

        int objectY = gb.getMemoryBus().readUnrestricted(oamAddress++);
        int objectX = gb.getMemoryBus().readUnrestricted(oamAddress++);
        int objectTileIndex = gb.getMemoryBus().readUnrestricted(oamAddress++);
        OAMObjectAttributes objectAttributes = new OAMObjectAttributes(gb.getMemoryBus().readUnrestricted(oamAddress++));

        int size = (lcdc & BitMasks.TWO) == 0 ? 8 : 16;
        if((ly + 16) >= objectY && (ly + 16) < (objectY + size) && oamBuffer.size() < 10) {
            oamBuffer.add(new OAMObject(objectX, objectY, objectTileIndex, objectAttributes));
        }

    }

    private void render() {

        if(!oamBuffer.isEmpty()) {
            OAMObject obj = oamBuffer.getFirst();
            if(obj.getX() <= lx + 8) {
                oamBuffer.remove(obj);
                objectPixelFetcher.reset();
                objectPixelFetcher.setCurrent(obj);
            }
        }

        if(objectPixelFetcher.getCurrent() != null && backgroundPixelFetcher.getStep() >= 6) {
            objectPixelFetcher.tick();
        } else {
            backgroundPixelFetcher.tick();
        }

        if(objectPixelFetcher.getCurrent() == null && objectPixelFifo.getSize() > 0 && backgroundPixelFifo.getSize() > 0) {

            Pixel bp = backgroundPixelFifo.pop();

            if(bp != null) {

                if((lcdc & BitMasks.ZERO) == 0) {
                    bp = null;
                }

                Pixel op = objectPixelFifo.pop();

                if((lcdc & BitMasks.ONE) == 0) {
                    op = null;
                }

                if(op == null && bp == null) {
                    lcd[ly][lx++] = new Pixel(0, 0, 0);
                } else if(op == null) {
                    lcd[ly][lx++] = bp;
                } else if(bp == null) {
                    lcd[ly][lx++] = op;
                } else {
                    if(op.getColor() == -1) {
                        lcd[ly][lx++] = bp;
                    } else if(op.getBackgroundPriority() != 0 && bp.getColor() != 0b00) {
                        lcd[ly][lx++] = bp;
                    } else {
                        lcd[ly][lx++] = op;
                    }
                }

            }

            if((lcdc & BitMasks.FIVE) != 0) {
                if(!backgroundPixelFetcher.isWindowMode() && ly >= wy && lx >= (wx - 7)) {
                    backgroundPixelFifo.clear();
                    backgroundPixelFetcher.startWindowMode();
                }
            }

        } else if(objectPixelFetcher.getCurrent() == null && backgroundPixelFifo.getSize() > 0) {

            Pixel p = backgroundPixelFifo.pop();
            if(p != null) {
                lcd[ly][lx++] = (lcdc & BitMasks.ZERO) == 0 ? new Pixel(0, 0, 0) : p;
            }

            if((lcdc & BitMasks.FIVE) != 0) {
                if(!backgroundPixelFetcher.isWindowMode() && ly >= wy && lx >= (wx - 7)) {
                    backgroundPixelFifo.clear();
                    backgroundPixelFetcher.startWindowMode();
                }
            }

        }

    }

}
