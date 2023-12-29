package at.alexkiefer.voltboy.components.timer;

import at.alexkiefer.voltboy.ConnectedInternal;
import at.alexkiefer.voltboy.Tickable;
import at.alexkiefer.voltboy.VoltBoy;
import at.alexkiefer.voltboy.util.BitUtils;

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
        gb.getDataBus().writeUnrestricted(0xFF04, div);
    }

    @Override
    public void tick() {

        incDiv();

        if(delayedTima) {
            delayedTima = false;
            gb.getDataBus().writeUnrestricted(0xFF05, gb.getDataBus().read(0xFF06));
            gb.getDataBus().write(0xFF0F, gb.getDataBus().read(0xFF0F) | BitUtils.M_TWO);
        }

        int tac = gb.getDataBus().read(0xFF07);

        if((tac & BitUtils.M_TWO) != 0) {

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
            gb.getDataBus().writeUnrestricted(0xFF04, div);
        }
    }

    private void incTima() {
        int res = gb.getDataBus().read(0xFF05) + 1;
        // This is implemented according to TCAGBD - Overflow Behavior. For one cycle, TIMA is set to 0, before being updated accordingly
        if(res > 0xFF) {
            delayedTima = true;
        }
        gb.getDataBus().writeUnrestricted(0xFF05, res);
    }

}
