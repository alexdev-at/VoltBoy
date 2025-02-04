package at.alexkiefer.voltboy.core.apu.channel;

import at.alexkiefer.voltboy.core.VoltBoy;
import at.alexkiefer.voltboy.util.BitMasks;

public class CH3 extends SoundChannel {

    private int waveRamIndex;

    private int[] waveRam;

    public CH3(VoltBoy gb) {

        super(gb);

        lengthTimerLimit = 256;
        waveRam = new int[0x10];

    }

    @Override
    public int getNRX0() {
        return NRX0 | 0b0111_1111;
    }

    @Override
    public int getNRX1() {
        return NRX1 | 0b1111_1111;
    }

    @Override
    public int getNRX2() {
        return NRX2 | 0b1001_1111;
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

        NRX0 |= 0b0111_1111;

        super.configureNRX0(NRX0);

        if ((NRX0 & BitMasks.SEVEN) == 0) {
            dacEnabled = false;
            enabled = false;
        } else {
            dacEnabled = true;
        }

        this.NRX0 = NRX0;

    }

    @Override
    public void configureNRX1(int NRX1) {

        if ((gb.getApu().getNR52() & BitMasks.SEVEN) == 0) {
            return;
        }

        lengthTimer = lengthTimerLimit - NRX1;
        if (lengthTimer == 0) {
            lengthTimer = lengthTimerLimit;
        }

        this.NRX1 = NRX1;

    }

    @Override
    public void configureNRX2(int NRX2) {

        if ((gb.getApu().getNR52() & BitMasks.SEVEN) == 0) {
            return;
        }

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

        if (!dacEnabled) {
            enabled = false;
        }

    }

    @Override
    public void reset() {

        super.reset();

        waveRamIndex = 0;

    }

    public int readWaveRam(int addr) {
        return waveRam[addr - 0xFF30];
    }

    public int readWaveRamUnrestricted(int addr) {
        return waveRam[addr - 0xFF30];
    }

    public void writeWaveRam(int addr, int value) {
        waveRam[addr - 0xFF30] = value;
    }

    public void writeWaveRamUnrestricted(int addr, int value) {
        waveRam[addr - 0xFF30] = value;
    }

}