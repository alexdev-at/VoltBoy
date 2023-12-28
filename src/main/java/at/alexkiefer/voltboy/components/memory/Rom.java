package at.alexkiefer.voltboy.components.memory;

import at.alexkiefer.voltboy.VoltBoy;

public class Rom extends AddressSpace {

    public Rom(VoltBoy gb) {
        super(gb, 0x0000, 0x7FFF, true);
    }

}
