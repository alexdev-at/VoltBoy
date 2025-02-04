package at.alexkiefer.voltboy.core.apu.channel;

import at.alexkiefer.voltboy.core.ConnectedInternal;
import at.alexkiefer.voltboy.core.VoltBoy;
import at.alexkiefer.voltboy.core.apu.APU;
import at.alexkiefer.voltboy.util.BitMasks;
import at.alexkiefer.voltboy.util.FormatUtils;

public abstract class SoundChannel extends ConnectedInternal {

    protected boolean enabled;
    protected boolean dacEnabled;

    protected int lengthTimer;
    protected int lengthTimerLimit;

    protected int envelopeSweepStep;
    protected int volume;

    protected int NRX0;
    protected int NRX1;
    protected int NRX2;
    protected int NRX3;
    protected int NRX4;

    protected int output;

    public SoundChannel(VoltBoy gb) {

        super(gb);

        enabled = false;
        dacEnabled = false;

        lengthTimer = 0;
        lengthTimerLimit = 64;

        output = 0;

    }

    public int getNRX0() {
        return NRX0;
    }

    public int getNRX1() {
        return NRX1;
    }

    public int getNRX2() {
        return NRX2;
    }

    public int getNRX3() {
        return NRX3;
    }

    public int getNRX4() {
        return NRX4;
    }

    public void setNRX0(int NRX0) {
        configureNRX0(NRX0);
        this.NRX0 = NRX0;
    }

    public void setNRX1(int NRX1) {
        configureNRX1(NRX1);
        this.NRX1 = NRX1;
    }

    public void setNRX2(int NRX2) {
        configureNRX2(NRX2);
        this.NRX2 = NRX2;
    }

    public void setNRX3(int NRX3) {
        configureNRX3(NRX3);
        this.NRX3 = NRX3;
    }

    public void setNRX4(int NRX4) {
        configureNRX4(NRX4);
        this.NRX4 = NRX4;
    }

    public void configureNRX0(int NRX0) {

    }

    public void configureNRX1(int NRX1) {

        lengthTimer = lengthTimerLimit - NRX1 & 0b0011_1111;
        if (lengthTimer == 0) {
            lengthTimer = lengthTimerLimit;
        }

    }

    public void configureNRX2(int NRX2) {

        if ((NRX2 & 0xF8) == 0) {
            dacEnabled = false;
            enabled = false;
        } else {
            dacEnabled = true;
        }

    }

    public void configureNRX3(int NRX3) {

    }

    public void configureNRX4(int NRX4) {

        if (lengthTimer != 0 && ((this.NRX4 & BitMasks.SIX) == 0) && ((NRX4 & BitMasks.SIX) != 0) && gb.getApu().getStep() % 2 != 0) {

            lengthTimer--;

            if (lengthTimer == 0 && (NRX4 & BitMasks.SEVEN) == 0) {
                enabled = false;
            }

        }

        if ((NRX4 & BitMasks.SEVEN) != 0) {

            this.NRX4 = NRX4;
            trigger();

        }

    }

    public void trigger() {

        if (lengthTimer == 0) {

            lengthTimer = lengthTimerLimit;

            if ((NRX4 & BitMasks.SIX) != 0 && gb.getApu().getStep() % 2 != 0) {
                lengthTimer--;
            }

        }

    }

    public void reset() {

        setNRX0(0);
        if (!(this instanceof CH3)) {
            setNRX1(NRX1 &= 0b0011_1111);
        }
        setNRX2(0);
        setNRX3(0);
        setNRX4(0);
        enabled = false;
        dacEnabled = false;
        lengthTimer = 0;

    }

    public int getOutput() {
        return output;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isDacEnabled() {
        return dacEnabled;
    }

    public void tickLength() {

        if ((NRX4 & BitMasks.SIX) == 0) {
            return;
        }

        if (lengthTimer > 0 && --lengthTimer == 0) {
            enabled = false;
        }

    }

    public void tickEnvelopeSweep() {



    }

}
