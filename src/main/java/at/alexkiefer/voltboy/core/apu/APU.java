package at.alexkiefer.voltboy.core.apu;

import at.alexkiefer.voltboy.core.ConnectedInternal;
import at.alexkiefer.voltboy.core.Tickable;
import at.alexkiefer.voltboy.core.VoltBoy;
import at.alexkiefer.voltboy.core.apu.channel.*;
import at.alexkiefer.voltboy.util.BitMasks;

public class APU extends ConnectedInternal implements Tickable {

    private int NR50;
    private int NR51;
    private int NR52;

    private int step;

    private final SoundChannel[] soundChannels;

    public APU(VoltBoy gb) {

        super(gb);

        step = 0;

        soundChannels = new SoundChannel[] {
                new CH1(gb),
                new CH2(gb),
                new CH3(gb),
                new CH4(gb)
        };

    }

    public int getStep() {
        return step;
    }

    public int getNR50() {
        return NR50;
    }

    public int getNR51() {
        return NR51;
    }

    public int getNR52() {
        return NR52 | 0b0111_0000 | ((soundChannels[3].isEnabled() ? 1 : 0) << 3) | ((soundChannels[2].isEnabled() ? 1 : 0) << 2) | ((soundChannels[1].isEnabled() ? 1 : 0) << 1) | ((soundChannels[0].isEnabled() ? 1 : 0) << 0);
    }

    public void setNR50(int NR50) {
        this.NR50 = NR50;
    }

    public void setNR51(int NR51) {
        this.NR51 = NR51;
    }

    public void setNR52(int NR52) {
        this.NR52 = NR52;
    }

    public void configureNR50(int NR50) {
        if ((this.NR52 & BitMasks.SEVEN) == 0) {
            return;
        }
        this.NR50 = NR50;
    }

    public void configureNR51(int NR51) {
        if ((this.NR52 & BitMasks.SEVEN) == 0) {
            return;
        }
        this.NR51 = NR51;
    }

    public void configureNR52(int NR52) {

        this.NR52 = (NR52 & 0b1000_0000);

        if ((this.NR52 & BitMasks.SEVEN) == 0) {
            for (SoundChannel ch : soundChannels) {
                ch.reset();
            }
            NR51 = 0;
            NR50 = 0;
            step = 0;
        }

    }

    @Override
    public void tick() {

        if ((NR52 & BitMasks.SEVEN) == 0) {
            return;
        }

        if (step % 2 == 0) {

            soundChannels[0].tickLength();
            soundChannels[1].tickLength();
            soundChannels[2].tickLength();
            soundChannels[3].tickLength();

        }

        if (step % 4 == 0) {

            ((CH1) soundChannels[0]).tickFrequencySweep();

        }

        if (step % 8 == 0) {

            soundChannels[0].tickEnvelopeSweep();
            soundChannels[1].tickEnvelopeSweep();
            // soundChannels[2].tickEnvelopeSweep();
            soundChannels[3].tickEnvelopeSweep();

        }

        step = (step + 1) % 8;

    }

    public SoundChannel[] getSoundChannels() {
        return soundChannels;
    }

}
