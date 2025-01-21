package at.alexkiefer.voltboy.core.memory.addressspace;

import at.alexkiefer.voltboy.core.VoltBoy;

public class IERegister extends AddressSpace {

    public IERegister(VoltBoy gb) {
        super(gb, 0xFFFF, 0xFFFF);
    }

}
