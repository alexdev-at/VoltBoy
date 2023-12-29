package at.alexkiefer.voltboy.components.memory.spaces;

import at.alexkiefer.voltboy.VoltBoy;
import at.alexkiefer.voltboy.components.memory.AddressSpace;

public class ProhibitedSpace extends AddressSpace {

    public ProhibitedSpace(VoltBoy gb) {
        super(gb, 0xFEA0, 0xFEFF, false);
    }

    @Override
    public int read(int addr) {
        return 0x00;
    }

}