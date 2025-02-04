package at.alexkiefer.voltboy.core.apu.channel;

import at.alexkiefer.voltboy.core.VoltBoy;
import at.alexkiefer.voltboy.util.BitMasks;
import at.alexkiefer.voltboy.util.FormatUtils;

public class CH1 extends SoundChannel {

    private int sweepTimer;
    private int sweepPace;
    private int shadowPeriod;
    private boolean sweepEnabled;

    private int dutyStep;

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

        super.configureNRX0(NRX0);

        int pace = (NRX0 & 0b0111_0000) >> 4;

        if (pace == 0) {
            sweepPace = 0;
        }

        this.NRX0 = NRX0;

    }

    @Override
    public void configureNRX1(int NRX1) {

        if ((gb.getApu().getNR52() & BitMasks.SEVEN) == 0) {
            return;
        }

        super.configureNRX1(NRX1);
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

        sweepTimer = 0;
        sweepPace = NRX2 & 0b0000_0111;
        sweepEnabled = (NRX0 & 0b0111_0111) != 0;
        shadowPeriod = ((NRX4 & 0b0000_0111) << 8) | NRX3;

        volume = (NRX2 & 0b1111_0000) >> 4;

        envelopeSweepStep = 0;

        if (!dacEnabled) {
            enabled = false;
        }

    }

    @Override
    public void reset() {

        super.reset();

    }

    public void tickFrequencySweep() {

        if (sweepEnabled) {

            int period = shadowPeriod + ((NRX0 & BitMasks.THREE) == 0 ? 1 : -1) * (shadowPeriod >> NRX0 & 0b0000_0111);

            if (period > 0x07FF) {
                enabled = false;
                return;
            }

            if (sweepPace != 0 && sweepTimer == 0) {
                shadowPeriod = period;
                NRX3 = shadowPeriod & 0xFF;
                NRX4 = (NRX4 & 0b1100_0111) | (shadowPeriod >> 8);
                sweepTimer = sweepPace;
            }

            sweepTimer--;

        }

    }

}
