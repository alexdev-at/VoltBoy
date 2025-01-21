package at.alexkiefer.voltboy.core.memory.addressspace;

import at.alexkiefer.voltboy.core.VoltBoy;

public class ProhibitedSpace extends AddressSpace {

    public ProhibitedSpace(VoltBoy gb) {
        super(gb, 0xFEA0, 0xFEFF);
    }

    @Override
    public int read(int addr) {
        return 0x00;
    }

    @Override
    public void write(int addr, int value) {

    }
}
