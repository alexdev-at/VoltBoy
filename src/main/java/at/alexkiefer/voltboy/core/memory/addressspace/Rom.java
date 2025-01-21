package at.alexkiefer.voltboy.core.memory.addressspace;

import at.alexkiefer.voltboy.core.VoltBoy;

public class Rom extends AddressSpace {

    public Rom(VoltBoy gb) {
        super(gb, 0x0000, 0x7FFF);
    }

    @Override
    public int read(int addr) {
        return gb.getCartridge().read(addr);
    }

    @Override
    public void write(int addr, int value) {
        gb.getCartridge().write(addr, value);
    }
}
