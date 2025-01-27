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

    private int timerSteps;
    private int lastMode;
    private boolean delayedTima;
    private int lastAndResult;

    public Timer(VoltBoy gb) {
        super(gb);
        timerSteps = 0;
        delayedTima = false;
    }

    public int getDiv() {
        return div >> 8;
    }

    public void setDiv(int div) {
        this.div = div << 8;
    }

    public void resetDiv() {
        this.div = 0;
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
        return tac;
    }

    public void setTac(int tac) {
        this.tac = tac & 0b0000_0111;
    }

    @Override
    public void tick() {

        incDiv();
        incDiv();
        incDiv();
        incDiv();

    }

    private void incDiv() {

        div = (div + 1) & 0xFFFF;

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

    }

    private void incTima() {
        tima = (tima + 1) & 0xFF;
        if(tima == 0) {
            delayedTima = true;
        }
    }

}
