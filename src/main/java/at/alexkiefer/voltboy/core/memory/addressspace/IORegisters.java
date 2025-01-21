package at.alexkiefer.voltboy.core.memory.addressspace;

import at.alexkiefer.voltboy.core.VoltBoy;

public class IORegisters extends AddressSpace {

    public IORegisters(VoltBoy gb) {
        super(gb, 0xFF00, 0xFF7F);
    }

}
