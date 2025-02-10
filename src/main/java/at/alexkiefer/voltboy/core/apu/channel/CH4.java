package at.alexkiefer.voltboy.core.apu.channel;

import at.alexkiefer.voltboy.core.VoltBoy;
import at.alexkiefer.voltboy.util.BitMasks;

public class CH4 extends SoundChannel {

    public CH4(VoltBoy gb) {

        super(gb);

    }

    @Override
    public int getNRX1() {
        return NRX1 | 0b1111_1111;
    }

    @Override
    public int getNRX2() {
        return NRX2;
    }

    @Override
    public int getNRX3() {
        return NRX3;
    }

    @Override
    public int getNRX4() {
        return NRX4 | 0b1011_1111;
    }

    @Override
    public void configureNRX1(int NRX1) {

        NRX1 |= 0b1100_0000;

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

        NRX4 |= 0b0011_1111;

        super.configureNRX4(NRX4);

        this.NRX4 = NRX4;

    }

    @Override
    public void trigger() {

        super.trigger();

        enabled = true;

        if (!dacEnabled) {
            enabled = false;
        }

    }

    @Override
    public void reset() {

        super.reset();

    }

    @Override
    public void tick() {

    }

}