package at.alexkiefer.voltboy.components.memory;

import at.alexkiefer.voltboy.VoltBoy;

public class WorkRam extends AddressSpace {

    public WorkRam(VoltBoy gb) {
        super(gb, 0xC000, 0xDFFF, false);
    }

}