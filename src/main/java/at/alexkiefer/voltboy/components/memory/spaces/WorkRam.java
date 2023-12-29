package at.alexkiefer.voltboy.components.memory.spaces;

import at.alexkiefer.voltboy.VoltBoy;
import at.alexkiefer.voltboy.components.memory.AddressSpace;

public class WorkRam extends AddressSpace {

    public WorkRam(VoltBoy gb) {
        super(gb, 0xC000, 0xFDFF, false);
    }

    @Override
    public int read(int addr) {
        if(addr >= 0xE000) {
            return super.read(addr - 0x2000);
        } else {
            return super.read(addr);
        }
    }
}