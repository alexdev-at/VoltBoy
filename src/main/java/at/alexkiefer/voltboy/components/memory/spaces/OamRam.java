package at.alexkiefer.voltboy.components.memory.spaces;

import at.alexkiefer.voltboy.VoltBoy;
import at.alexkiefer.voltboy.components.memory.AddressSpace;

public class OamRam extends AddressSpace {

    public OamRam(VoltBoy gb) {
        super(gb, 0xFE00, 0xFE9F, false);
    }

    @Override
    public int read(int addr) {
        switch(gb.getPpu().getMode()) {
            case MODE_0, MODE_1 -> {
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
            case MODE_0, MODE_1 -> {
                super.write(addr, data);
            }
        }
    }

}