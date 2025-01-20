package at.alexkiefer.voltboy.core.memory;

import at.alexkiefer.voltboy.core.VoltBoy;

public class MemoryBus {

    private final VoltBoy gb;

    private final int[] memory;

    public MemoryBus(VoltBoy gb) {
        this.gb = gb;
        memory = new int[0x10000];
    }

    public int read(int addr) {
        return memory[addr & 0xFFFF];
    }

    public void write(int addr, int value) {
        memory[addr & 0xFFFF] = value & 0xFF;
    }

}
