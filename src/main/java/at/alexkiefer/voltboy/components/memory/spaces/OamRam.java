package at.alexkiefer.voltboy.components.memory.spaces;

import at.alexkiefer.voltboy.VoltBoy;
import at.alexkiefer.voltboy.components.memory.AddressSpace;

public class OamRam extends AddressSpace {

    public OamRam(VoltBoy gb) {
        super(gb, 0xFE00, 0xFE9F, false);
    }

}