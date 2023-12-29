package at.alexkiefer.voltboy.components.memory.spaces;

import at.alexkiefer.voltboy.VoltBoy;

public class OamRam extends AddressSpace {

    public OamRam(VoltBoy gb) {
        super(gb, 0xFE00, 0xFE9F, false);
    }

}