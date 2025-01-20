package at.alexkiefer.voltboy.core;

import at.alexkiefer.voltboy.core.cpu.CPU;
import at.alexkiefer.voltboy.core.memory.MemoryBus;

public class VoltBoy {

    private final MemoryBus memoryBus;
    private final CPU cpu;

    public VoltBoy() {
        memoryBus = new MemoryBus(this);
        cpu = new CPU(this);
    }

}
