package at.alexkiefer.voltboy.components.memory.spaces;

import at.alexkiefer.voltboy.VoltBoy;
import at.alexkiefer.voltboy.components.memory.AddressSpace;

public class ExternalRam extends AddressSpace {

    public ExternalRam(VoltBoy gb) {
        super(gb, 0xA000, 0xBFFF, false);
    }

    @Override
    public int read(int addr) {
        return gb.getCartridge().readRam(addr);
    }

    @Override
    public void write(int addr, int data) {
        gb.getCartridge().writeRam(addr, data);
    }

}