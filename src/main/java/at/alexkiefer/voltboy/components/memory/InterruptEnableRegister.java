package at.alexkiefer.voltboy.components.memory;

import at.alexkiefer.voltboy.VoltBoy;

public class InterruptEnableRegister extends AddressSpace {

    public InterruptEnableRegister(VoltBoy gb) {
        super(gb, 0xFFFF, 0xFFFF, false);
    }

}