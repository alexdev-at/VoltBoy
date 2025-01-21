package at.alexkiefer.voltboy.core.timer;

import at.alexkiefer.voltboy.core.ConnectedInternal;
import at.alexkiefer.voltboy.core.Tickable;
import at.alexkiefer.voltboy.core.VoltBoy;
import at.alexkiefer.voltboy.util.BitMasks;

public class Timer extends ConnectedInternal implements Tickable {

    private int div;
    private int timerSteps;
    private int lastMode;
    private boolean delayedTima;

    public Timer(VoltBoy gb) {
        super(gb);
        div = 0xABCC;
        timerSteps = 0;
        delayedTima = false;
        gb.getMemoryBus().writeUnrestricted(0xFF04, div);
    }

    @Override
    public void tick() {

        incDiv();

        if(delayedTima) {
            delayedTima = false;
            gb.getMemoryBus().writeUnrestricted(0xFF05, gb.getMemoryBus().read(0xFF06));
            gb.getMemoryBus().write(0xFF0F, gb.getMemoryBus().read(0xFF0F) | BitMasks.TWO);
        }

        int tac = gb.getMemoryBus().read(0xFF07);

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

    public void resetDiv() {
        div = 0;
    }

    private void incDiv() {
        int old = div;
        div = (div + 1) & 0xFFFF;
        if((div & 0xFF00) != (old & 0xFF00)) {
            gb.getMemoryBus().writeUnrestricted(0xFF04, div);
        }
    }

    private void incTima() {
        int res = gb.getMemoryBus().read(0xFF05) + 1;
        if(res > 0xFF) {
            delayedTima = true;
        }
        gb.getMemoryBus().writeUnrestricted(0xFF05, res);
    }

}
