package at.alexkiefer.voltboy.core.apu.channel;

import at.alexkiefer.voltboy.core.VoltBoy;
import at.alexkiefer.voltboy.util.BitMasks;

public class CH1 extends SoundChannel {

    private int sweepPace;
    private int sweepPaceTimer;
    private int shadowPeriod;
    private boolean sweepEnabled;
    private boolean negated;

    private int[][] dutyCycles = {
            {0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 1, 1, 1},
            {0, 1, 1, 1, 1, 1, 1, 0}
    };

    private int dutyStep;

    private int periodDivider;
    private int sampleRate;

    private int currentPeriodValue;

    public CH1(VoltBoy gb) {

        super(gb);

    }

    @Override
    public int getNRX0() {
        return NRX0 | 0b1000_0000;
    }

    @Override
    public int getNRX1() {
        return NRX1 | 0b0011_1111;
    }

    @Override
    public int getNRX2() {
        return NRX2;
    }

    @Override
    public int getNRX3() {
        return NRX3 | 0b1111_1111;
    }

    @Override
    public int getNRX4() {
        return NRX4 | 0b1011_1111;
    }

    @Override
    public void configureNRX0(int NRX0) {

        if ((gb.getApu().getNR52() & BitMasks.SEVEN) == 0) {
            return;
        }

        if (negated && (this.NRX0 & BitMasks.THREE) != 0 && (NRX0 & BitMasks.THREE) == 0) {
            enabled = false;
        }

        if ((NRX0 & 0b0111_0000) >> 4 == 0) {
            sweepPace = 0;
        } else {
            if (sweepPace == 0) {
                sweepPace = (NRX0 & 0b0111_0000) >> 4;
            }
        }

        super.configureNRX0(NRX0);

        this.NRX0 = NRX0;

    }

    @Override
    public void configureNRX1(int NRX1) {

        super.configureNRX1(NRX1);

        if ((gb.getApu().getNR52() & BitMasks.SEVEN) == 0) {
            return;
        }

        this.NRX1 = NRX1;

    }

    @Override
    public void configureNRX2(int NRX2) {

        if ((gb.getApu().getNR52() & BitMasks.SEVEN) == 0) {
            return;
        }

        super.configureNRX2(NRX2);
        this.NRX2 = NRX2;

    }

    @Override
    public void configureNRX3(int NRX3) {

        if ((gb.getApu().getNR52() & BitMasks.SEVEN) == 0) {
            return;
        }

        super.configureNRX3(NRX3);
        this.NRX3 = NRX3;

    }

    @Override
    public void configureNRX4(int NRX4) {

        if ((gb.getApu().getNR52() & BitMasks.SEVEN) == 0) {
            return;
        }

        NRX4 |= 0b0011_1000;

        super.configureNRX4(NRX4);

        this.NRX4 = NRX4;

    }

    @Override
    public void trigger() {

        super.trigger();

        enabled = true;

        sweepPace = (NRX0 & 0b0111_0000) >> 4;
        sweepPaceTimer = sweepPace;
        currentPeriodValue = ((NRX4 & 0b0000_0111) << 8) | NRX3;
        shadowPeriod = currentPeriodValue;
        sweepEnabled = (NRX0 & 0b0111_0111) != 0;
        negated = false;

        sampleRate = 1048576 / (2048 - ((currentPeriodValue << 20) >> 20));
        periodDivider = 0;

        volume = (NRX2 & 0b1111_0000) >> 4;

        if ((NRX0 & 0b0000_0111) != 0) {
            calculateFrequencySweep(false);
        }

        if (!dacEnabled) {
            enabled = false;
        }

    }

    @Override
    public void reset() {

        super.reset();

        dutyStep = 0;
        periodDivider = 0;

    }

    @Override
    public void tick() {

        if (enabled) {

            if (++periodDivider > 0x07FF) {
                currentPeriodValue = ((NRX4 & 0b0000_0111) << 8) | NRX3;
                periodDivider = currentPeriodValue;
                sampleRate = 1048576 / (2048 - ((currentPeriodValue << 20) >> 20));
            }

            dutyStep = (dutyStep + 1) & 0b111;

        }

    }

    public void tickFrequencySweep() {

        if (enabled && sweepEnabled) {

            sweepPaceTimer = (sweepPaceTimer - 1) & 0b111;

            if (sweepPaceTimer == 0) {

                if (sweepPace != 0) {
                    calculateFrequencySweep(true);
                    calculateFrequencySweep(false);
                }

                sweepPace = (NRX0 & 0b0111_0000) >> 4;
                sweepPaceTimer = sweepPace;

            }

        }

    }

    private void calculateFrequencySweep(boolean writeBack) {

        int individualStep = (NRX0 & 0b0000_0111);

        int newPeriod;

        if ((NRX0 & 0b0000_1000) == 0) {
            newPeriod = shadowPeriod + (shadowPeriod >> individualStep);
        } else {
            negated = true;
            newPeriod = shadowPeriod - (shadowPeriod >> individualStep);
        }

        if (newPeriod > 0x07FF) {
            enabled = false;
        } else {

            if (writeBack && individualStep != 0) {
                shadowPeriod = newPeriod;
                NRX3 = shadowPeriod & 0xFF;
                NRX4 = (NRX4 & 0b1100_0000) | (shadowPeriod >> 8);
            }

        }

    }

}
