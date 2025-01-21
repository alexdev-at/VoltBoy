package at.alexkiefer.voltboy.core.memory.addressspace;

import at.alexkiefer.voltboy.core.VoltBoy;

public class OamRam extends AddressSpace {

    public OamRam(VoltBoy gb) {
        super(gb, 0xFE00, 0xFE9F);
    }

}
