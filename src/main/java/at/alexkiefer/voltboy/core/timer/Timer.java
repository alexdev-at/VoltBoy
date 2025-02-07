package at.alexkiefer.voltboy.core.timer;

import at.alexkiefer.voltboy.core.ConnectedInternal;
import at.alexkiefer.voltboy.core.Tickable;
import at.alexkiefer.voltboy.core.VoltBoy;
import at.alexkiefer.voltboy.util.BitMasks;

public class Timer extends ConnectedInternal implements Tickable {

    private int div;
    private int tima;
    private int tma;
    private int tac;

    private boolean delayedTima;
    private boolean tmaReloadJustHappened;
    private int lastAndResult;

    public Timer(VoltBoy gb) {
        super(gb);
        delayedTima = false;
        tmaReloadJustHappened = false;
    }

    public int getDiv() {
        return div >> 8;
    }

    public void setDiv(int div) {
        this.div = div << 8;
    }

    public void resetDiv() {

        int oldDiv = div;

        this.div = 0;

        if ((oldDiv & 0b0001_0000_0000_0000) != 0) {
            gb.getApu().tick();
        }

    }

    public int getTima() {
        return tima;
    }

    public void setTima(int tima) {
        this.tima = tima;
    }

    public int getTma() {
        return tma;
    }

    public void setTma(int tma) {
        this.tma = tma;
    }

    public int getTac() {
        return tac | 0b1111_1000;
    }

    public void setTac(int tac) {
        this.tac = (tac & 0b0000_0111) | 0b1111_1000;
    }

    @Override
    public void tick() {

        incDiv();

        if(delayedTima) {
            delayedTima = false;
            tima = tma;
            tmaReloadJustHappened = true;
            gb.getMemoryBus().write(0xFF0F, gb.getMemoryBus().read(0xFF0F) | BitMasks.TWO);
        }

    }

    private void incDiv() {

        int oldDiv = div;

        div = (div + 4) & 0xFFFF;

        int mode = tac & 0b11;

        int bitPos = 0;

        switch (mode) {
            case 0b00 -> {
                bitPos = 9;
            }
            case 0b01 -> {
                bitPos = 3;
            }
            case 0b10 -> {
                bitPos = 5;
            }
            case 0b11 -> {
                bitPos = 7;
            }
        }

        int tacBit = (tac & 0b100) >> 2;
        int andResult = tacBit & ((div & (1 << bitPos)) >> bitPos);

        if (lastAndResult == 1 && andResult == 0) {
            incTima();
        }

        lastAndResult = andResult;

        if ((oldDiv & 0b0001_0000_0000_0000) != 0 && (div & 0b0001_0000_0000_0000) == 0) {
            gb.getApu().tick();
        }

    }

    private void incTima() {
        tima = (tima + 1) & 0xFF;
        if(tima == 0) {
            delayedTima = true;
        }
    }

}
