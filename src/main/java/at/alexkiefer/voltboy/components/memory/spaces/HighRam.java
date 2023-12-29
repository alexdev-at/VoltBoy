package at.alexkiefer.voltboy.components.memory.spaces;

import at.alexkiefer.voltboy.VoltBoy;

public class HighRam extends AddressSpace {

    public HighRam(VoltBoy gb) {
        super(gb, 0xFF80, 0xFFFE, false);
    }

}