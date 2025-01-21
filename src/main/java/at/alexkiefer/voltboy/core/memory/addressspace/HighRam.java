package at.alexkiefer.voltboy.core.memory.addressspace;

import at.alexkiefer.voltboy.core.VoltBoy;

public class HighRam extends AddressSpace {

    public HighRam(VoltBoy gb) {
        super(gb, 0xFF80, 0xFFFE);
    }

}
