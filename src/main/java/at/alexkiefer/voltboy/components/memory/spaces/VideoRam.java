package at.alexkiefer.voltboy.components.memory.spaces;

import at.alexkiefer.voltboy.VoltBoy;
import at.alexkiefer.voltboy.components.memory.AddressSpace;

public class VideoRam extends AddressSpace {

    public VideoRam(VoltBoy gb) {
        super(gb, 0x8000, 0x9FFF, false);
    }

    @Override
    public int read(int addr) {
        switch(gb.getPpu().getMode()) {
            case MODE_0, MODE_1, MODE_2 -> {
                return super.read(addr);
            }
            default -> {
                return 0xFF;
            }
        }
    }

    @Override
    public void write(int addr, int data) {
        switch(gb.getPpu().getMode()) {
            case MODE_0, MODE_1, MODE_2 -> {
                super.write(addr, data);
            }
        }
    }

}