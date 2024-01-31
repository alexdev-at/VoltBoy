package at.alexkiefer.voltboy.components.ppu;

import at.alexkiefer.voltboy.ConnectedInternal;
import at.alexkiefer.voltboy.Tickable;
import at.alexkiefer.voltboy.VoltBoy;
import at.alexkiefer.voltboy.components.ppu.fifo.FIFO;
import at.alexkiefer.voltboy.components.ppu.fifo.Pixel;
import at.alexkiefer.voltboy.components.ppu.fifo.PixelFetcher;
import at.alexkiefer.voltboy.components.ppu.objects.OAMObject;
import at.alexkiefer.voltboy.components.ppu.objects.OAMObjectAttributes;
import at.alexkiefer.voltboy.util.BitUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PPU extends ConnectedInternal implements Tickable {

    private final PixelFetcher backgroundPixelFetcher;
    private final FIFO backgroundPixelFifo;

    private final Pixel[][] lcd;

    private int lx;

    private PPUMode mode;
    private int dot;
    private int penaltyDots;
    private int addr;

    private final List<OAMObject> oamBuffer;

    public PPU(VoltBoy gb) {

        super(gb);

        backgroundPixelFetcher = new PixelFetcher(gb);
        backgroundPixelFifo = new FIFO(gb);

        lx = 0;

        mode = PPUMode.MODE_2;
        dot = 0;
        penaltyDots = 0;
        addr = 0x0000;

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

    public PixelFetcher getBackgroundPixelFetcher() {
        return backgroundPixelFetcher;
    }

    public FIFO getBackgroundPixelFifo() {
        return backgroundPixelFifo;
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
            backgroundPixelFetcher.reset();
            oamBuffer.clear();
        }

        switch(mode) {
            case MODE_2 -> oamScan();
            case MODE_3 -> render();
            case MODE_0 -> hBlank();
            case MODE_1 -> vBlank();
        }

        //System.out.println(mode.toString());

        dot++;

        int ly = gb.getDataBus().read(0xFF44);

        int stat = gb.getDataBus().read(0xFF41);
        PPUMode oldMode = mode;

        ly++;

        if(ly > 144) {

            if(dot == 456) {

            }

        } else {

            if(dot == 80) {
                mode = PPUMode.MODE_3;
            } else if(lx == 160) {
                lx = 0;
                mode = PPUMode.MODE_0;
            } else if(dot == 456) {
                if(ly == 144) {
                    mode = PPUMode.MODE_1;
                } else {
                    mode = PPUMode.MODE_2;
                }
            }

        }

        if(mode != oldMode) {

            stat |= mode.getValue();
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

        if(dot == 456) {

            dot = 0;

            System.out.println("LCDC.0=" + (gb.getDataBus().read(0xFF40) & BitUtils.M_ZERO));

            if(ly == gb.getDataBus().read(0xFF45)) {
                gb.getDataBus().writeUnrestricted(0xFF41, stat | BitUtils.M_TWO);
                if((stat & BitUtils.M_SIX) != 0) {
                    gb.getDataBus().writeUnrestricted(0xFF0F, gb.getDataBus().read(0xFF0F) | BitUtils.M_ONE);
                }
            } else {
                gb.getDataBus().writeUnrestricted(0xFF41, stat & ~BitUtils.M_TWO);
            }

            gb.getDataBus().writeUnrestricted(0xFF44, ly % 154);

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

        if((ly + 16) >= y && (ly + 16) < (y + size) && oamBuffer.size() < 10) {
            oamBuffer.add(new OAMObject(x, y, tileIndex, attributes));
        }

    }

    private void render() {

        int ly = gb.getDataBus().read(0xFF44);

        if(backgroundPixelFifo.getSize() > 0) {
            Pixel p = backgroundPixelFifo.pop();
            if((gb.getDataBus().read(0xFF40) & BitUtils.M_ZERO) != 0) {
                lcd[ly][lx++] = p;
            } else {
                lcd[ly][lx++] = new Pixel(0, 0, 0);
            }
        }

        // Should still be okay because nothing can cause a delay yet
        if(dot % 2 == 0) {
            backgroundPixelFetcher.tick();
        }

        //System.out.println("FIFO after dot #" + dot);
        //backgroundPixelFifo.print();

    }

    private void renderBg() {

    }

    private void hBlank() {

    }

    private void vBlank() {

    }

    private void debugPrint() {
        int lcdc = gb.getDataBus().read(0xFF40);
        System.out.println("LCDC:");
    }

}
