package at.alexkiefer.voltboy.components.ppu;

import at.alexkiefer.voltboy.ConnectedInternal;
import at.alexkiefer.voltboy.Tickable;
import at.alexkiefer.voltboy.VoltBoy;
import at.alexkiefer.voltboy.components.ppu.fifo.*;
import at.alexkiefer.voltboy.components.ppu.objects.OAMObject;
import at.alexkiefer.voltboy.components.ppu.objects.OAMObjectAttributes;
import at.alexkiefer.voltboy.util.BitUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class PPU extends ConnectedInternal implements Tickable {

    private final BackgroundPixelFetcher backgroundPixelFetcher;
    private final ObjectPixelFetcher objectPixelFetcher;
    private final BackgroundPixelFIFO backgroundPixelFifo;
    private final ObjectPixelFIFO objectPixelFifo;

    private final Pixel[][] lcd;

    private int lx;

    private PPUMode mode;
    private int dot;
    private int addr;
    private int oamIndex;

    private List<OAMObject> oamBuffer;

    public PPU(VoltBoy gb) {

        super(gb);

        backgroundPixelFetcher = new BackgroundPixelFetcher(gb);
        objectPixelFetcher = new ObjectPixelFetcher(gb);
        backgroundPixelFifo = new BackgroundPixelFIFO(gb);
        objectPixelFifo = new ObjectPixelFIFO(gb);

        lx = 0;

        mode = PPUMode.MODE_2;
        dot = 0;
        addr = 0x0000;
        oamIndex = 0;

        oamBuffer =  new ArrayList<>();

        lcd = new Pixel[144][160];
        for(int i = 0; i < 144; i++) {
            for(int j = 0; j < 160; j++) {
                lcd[i][j] = new Pixel(0, 0, 0);
            }
        }

    }

    public PPUMode getMode() {
        return mode;
    }

    public BackgroundPixelFetcher getBackgroundPixelFetcher() {
        return backgroundPixelFetcher;
    }

    public ObjectPixelFetcher getObjectPixelFetcher() {
        return objectPixelFetcher;
    }

    public BackgroundPixelFIFO getBackgroundPixelFifo() {
        return backgroundPixelFifo;
    }

    public ObjectPixelFIFO getObjectPixelFifo() {
        return objectPixelFifo;
    }

    public Pixel[][] getLcd() {
        return lcd;
    }

    @Override
    public void tick() {

        if((gb.getDataBus().read(0xFF40) & BitUtils.M_SEVEN) == 0) {
            return;
        }

        tickPpu();
        tickPpu();
        tickPpu();
        tickPpu();

    }

    private void tickPpu() {

        if(dot == 0) {
            backgroundPixelFifo.clear();
            backgroundPixelFifo.discardPixels(gb.getDataBus().read(0xFF43) % 8);
            backgroundPixelFetcher.reset();
            objectPixelFifo.clear();
            objectPixelFetcher.reset();
            oamBuffer.clear();
            addr = 0xFE00;
            oamIndex = 0;
        }

        int ly = gb.getDataBus().read(0xFF44);

        int stat = gb.getDataBus().read(0xFF41);
        PPUMode oldMode = mode;

        if(ly >= 144) {
            backgroundPixelFetcher.resetWindowCounter();
            mode = PPUMode.MODE_1;
        } else {

            if(dot == 80) {
                mode = PPUMode.MODE_3;
                oamBuffer.sort(Comparator.comparingInt(OAMObject::getX));
                // Trick from StackOverflow
                Set<Integer> seen = ConcurrentHashMap.newKeySet();
                oamBuffer = oamBuffer.stream()
                        .filter(o -> seen.add(o.getX()))
                        .collect(Collectors.toList());
            } else if(lx == 160) {
                lx = 0;
                mode = PPUMode.MODE_0;
            } else if(dot == 0) {
                mode = PPUMode.MODE_2;
            }

        }

        switch(mode) {
            case MODE_2 -> oamScan();
            case MODE_3 -> render();
            case MODE_0 -> hBlank();
            case MODE_1 -> vBlank();
        }

        dot++;

        ly++;

        if(mode != oldMode) {

            stat = (stat & 0b11111100) | mode.getValue();
            gb.getDataBus().write(0xFF41, stat);

            switch(mode) {
                case MODE_0 -> {
                    if((stat & BitUtils.M_THREE) != 0) {
                        gb.getDataBus().writeUnrestricted(0xFF0F, gb.getDataBus().read(0xFF0F) | BitUtils.M_ONE);
                    }
                }
                case MODE_1 -> {
                    gb.getDataBus().writeUnrestricted(0xFF0F, gb.getDataBus().read(0xFF0F) | BitUtils.M_ZERO);
                    if((stat & BitUtils.M_FOUR) != 0) {
                        gb.getDataBus().writeUnrestricted(0xFF0F, gb.getDataBus().read(0xFF0F) | BitUtils.M_ONE);
                    }
                }
                case MODE_2 -> {
                    if((stat & BitUtils.M_FIVE) != 0) {
                        gb.getDataBus().writeUnrestricted(0xFF0F, gb.getDataBus().read(0xFF0F) | BitUtils.M_ONE);
                    }
                }
            }

        }

        if(ly == gb.getDataBus().read(0xFF45)) {
            gb.getDataBus().writeUnrestricted(0xFF41, stat | BitUtils.M_TWO);
        } else {
            gb.getDataBus().writeUnrestricted(0xFF41, stat & ~BitUtils.M_TWO);
        }

        if(dot == 456) {

            dot = 0;
            lx = 0;

            if(ly == gb.getDataBus().read(0xFF45) && (stat & BitUtils.M_SIX) != 0) {
                gb.getDataBus().writeUnrestricted(0xFF0F, gb.getDataBus().read(0xFF0F) | BitUtils.M_ONE);
            }

            gb.getDataBus().writeUnrestricted(0xFF44, ly);

            if(backgroundPixelFetcher.isWindowMode()) {
                backgroundPixelFetcher.incWindowCounter();
            }

        }

    }

    private void oamScan() {

        // Only fetch a new object every two dots
        if(dot % 2 != 0) {
            return;
        }

        int y = gb.getDataBus().read(addr++);
        int x = gb.getDataBus().read(addr++);
        int tileIndex = gb.getDataBus().read(addr++);
        OAMObjectAttributes attributes = new OAMObjectAttributes(gb.getDataBus().read(addr++));

        int ly = gb.getDataBus().read(0xFF44);
        int size = (gb.getDataBus().read(0xFF40) & BitUtils.M_TWO) == 0 ? 8 : 16;

        if(x > 0 && (ly + 16) >= y && (ly + 16) < (y + size) && oamBuffer.size() < 10) {
            oamBuffer.add(new OAMObject(x, y, size, oamIndex++, tileIndex, attributes));
        }

    }

    private void render() {

        int ly = gb.getDataBus().read(0xFF44);
        int lcdc = gb.getDataBus().read(0xFF40);

        OAMObject obj = oamBuffer.isEmpty() ? null : oamBuffer.getFirst();
        if(obj != null && obj.getX() <= lx + 8) {
            oamBuffer.remove(obj);
            objectPixelFetcher.reset();
            objectPixelFetcher.setCurrent(obj);
        }

        if(objectPixelFetcher.getCurrent() != null && backgroundPixelFetcher.getStep() >= 6) {
            objectPixelFetcher.tick();
        } else {
            backgroundPixelFetcher.tick();
        }

        if(objectPixelFetcher.getCurrent() == null && objectPixelFifo.getSize() > 0 && backgroundPixelFifo.getSize() > 0) {

            Pixel bp = backgroundPixelFifo.pop();
            if((lcdc & BitUtils.M_ZERO) == 0) {
                bp = null;
            }

            Pixel op = objectPixelFifo.pop();
            if((lcdc & BitUtils.M_ONE) == 0) {
                op = null;
            }

            if(op == null && bp == null) {
                lcd[ly][lx++] = new Pixel(0, 0, 0);
            } else if(op == null) {
                lcd[ly][lx++] = bp;
            } else if(bp == null) {
                lcd[ly][lx++] = op;
            } else {
                if(op.getColor() == 0b00) {
                    lcd[ly][lx++] = bp;
                } else if(op.getBackgroundPriority() != 0 && bp.getColor() != 0b00) {
                    lcd[ly][lx++] = bp;
                } else {
                    lcd[ly][lx++] = op;
                }
            }

            if((lcdc & BitUtils.M_FIVE) != 0) {
                if(!backgroundPixelFetcher.isWindowMode() && ly >= gb.getDataBus().read(0xFF4A) && lx >= (gb.getDataBus().read(0xFF4B) - 7)) {
                    backgroundPixelFifo.clear();
                    backgroundPixelFetcher.startWindowMode();
                }
            }

        } else if(objectPixelFetcher.getCurrent() == null && backgroundPixelFifo.getSize() > 0) {

            Pixel p = backgroundPixelFifo.pop();
            if(p != null) {
                lcd[ly][lx++] = (lcdc & BitUtils.M_ZERO) == 0 ? new Pixel(0, 0, 0) : p;
            }

            if((lcdc & BitUtils.M_FIVE) != 0) {
                if(!backgroundPixelFetcher.isWindowMode() && ly >= gb.getDataBus().read(0xFF4A) && lx >= (gb.getDataBus().read(0xFF4B) - 7)) {
                    backgroundPixelFifo.clear();
                    backgroundPixelFetcher.startWindowMode();
                }
            }

        }

    }

    private void hBlank() {

    }

    private void vBlank() {

    }

    private void debugPrint() {
        int lcdc = gb.getDataBus().read(0xFF40);
        int stat = gb.getDataBus().read(0xFF41);
        int ir = gb.getDataBus().read(0xFF0F);
        int ie = gb.getDataBus().read(0xFFFF);
        System.out.println("LCDC: " + BitUtils.toBinary(lcdc) + " - STAT: " + BitUtils.toBinary(stat) + " - MODE: " + mode + " - IR: " + BitUtils.toBinary(ir) + " - IE: " + BitUtils.toBinary(ie) + " - IME: " + (gb.getCpu().isIme() ? "1" : "0"));
    }

}
