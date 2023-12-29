package at.alexkiefer.voltboy.components.memory.spaces;

import at.alexkiefer.voltboy.VoltBoy;

public class ExternalRam extends AddressSpace {

    public ExternalRam(VoltBoy gb) {
        super(gb, 0xA000, 0xBFFF, false);
    }

}