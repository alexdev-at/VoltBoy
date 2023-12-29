package at.alexkiefer.voltboy.components.memory.spaces;

import at.alexkiefer.voltboy.VoltBoy;

public class InterruptEnableRegister extends AddressSpace {

    public InterruptEnableRegister(VoltBoy gb) {
        super(gb, 0xFFFF, 0xFFFF, false);
    }

}