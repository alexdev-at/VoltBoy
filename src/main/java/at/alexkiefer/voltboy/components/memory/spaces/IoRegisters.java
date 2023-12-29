package at.alexkiefer.voltboy.components.memory.spaces;

import at.alexkiefer.voltboy.VoltBoy;

public class IoRegisters extends AddressSpace {

    public IoRegisters(VoltBoy gb) {
        super(gb, 0xFF00, 0xFF7F, false);
    }

}