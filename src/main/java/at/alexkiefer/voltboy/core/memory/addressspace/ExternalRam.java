package at.alexkiefer.voltboy.core.memory.addressspace;

import at.alexkiefer.voltboy.core.VoltBoy;

public class ExternalRam extends AddressSpace {

    public ExternalRam(VoltBoy gb) {
        super(gb, 0xA000, 0xBFFF);
    }

    @Override
    public int read(int addr) {
        return gb.getCartridge().readRam(addr);
    }

    @Override
    public void write(int addr, int value) {
        gb.getCartridge().writeRam(addr, value);
    }
}
