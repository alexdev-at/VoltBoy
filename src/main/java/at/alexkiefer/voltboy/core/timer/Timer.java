package at.alexkiefer.voltboy.core.timer;

import at.alexkiefer.voltboy.core.ConnectedInternal;
import at.alexkiefer.voltboy.core.Tickable;
import at.alexkiefer.voltboy.core.VoltBoy;
import at.alexkiefer.voltboy.util.BitMasks;
import at.alexkiefer.voltboy.util.HexUtils;

public class Timer extends ConnectedInternal implements Tickable {

    private int div;
    private int tima;
    private int tma;
    private int tac;

    private int timerSteps;
    private int lastMode;
    private boolean delayedTima;

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

        div = (div + 1) & 0xFFFF;

        if(delayedTima) {
            delayedTima = false;
            tima = tma;
            gb.getMemoryBus().write(0xFF0F, gb.getMemoryBus().read(0xFF0F) | BitMasks.TWO);
        }

        if((tac & BitMasks.TWO) != 0) {

            timerSteps++;

            int mode = tac & 0b11;

            if(mode != lastMode) {
                timerSteps = 0;
                lastMode = mode;
            }

            switch(mode) {
                case 0b00 -> {
                    if(timerSteps == 256) {
                        timerSteps -= 256;
                        incTima();
                    }
                }
                case 0b01 -> {
                    if(timerSteps == 4) {
                        timerSteps -= 4;
                        incTima();
                    }
                }
                case 0b10 -> {
                    if(timerSteps == 16) {
                        timerSteps -= 16;
                        incTima();
                    }
                }
                case 0b11 -> {
                    if(timerSteps == 64) {
                        timerSteps -= 64;
                        incTima();
                    }
                }
            }

        } else {
            timerSteps = 0;
        }

    }

    private void incTima() {
        int res = (tima + 1) & 0xFF;
        if(res == 0x00) {
            delayedTima = true;
        }
    }

}
