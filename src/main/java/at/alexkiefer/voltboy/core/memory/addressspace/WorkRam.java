package at.alexkiefer.voltboy.core.memory.addressspace;

import at.alexkiefer.voltboy.core.VoltBoy;

public class WorkRam extends AddressSpace {

    public WorkRam(VoltBoy gb) {
        super(gb, 0xC000, 0xFDFF);
    }

    @Override
    public int read(int addr) {
        if(addr >= 0xE000) {
            return super.read(addr - 0x2000);
        } else {
            return super.read(addr);
        }
    }

    @Override
    public void write(int addr, int value) {
        if(addr >= 0xE000) {
            super.write(addr - 0x2000, value);
        } else {
            super.write(addr, value);
        }
    }
}
