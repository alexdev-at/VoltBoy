package at.alexkiefer.voltboy.core;

import at.alexkiefer.voltboy.core.cpu.CPU;
import at.alexkiefer.voltboy.core.memory.MemoryBus;

public class VoltBoy implements Tickable {

    private final MemoryBus memoryBus;
    private final CPU cpu;

    public VoltBoy() {
        memoryBus = new MemoryBus(this);
        cpu = new CPU(this);
    }

    public MemoryBus getMemoryBus() {
        return memoryBus;
    }

    public CPU getCpu() {
        return cpu;
    }

    @Override
    public void tick() {
        cpu.tick();
    }

}
