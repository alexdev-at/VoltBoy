package at.alexkiefer.voltboy.core;

import at.alexkiefer.voltboy.core.cpu.CPU;
import at.alexkiefer.voltboy.core.memory.MemoryBus;
import at.alexkiefer.voltboy.core.memory.cartridge.Cartridge;

import java.io.IOException;

public class VoltBoy implements Tickable {

    private final MemoryBus memoryBus;
    private Cartridge cartridge;
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

    public Cartridge getCartridge() {
        return cartridge;
    }

    public void loadRom(String romPath) throws IOException {
        cartridge = new Cartridge(this, romPath);
    }

    @Override
    public void tick() {
        cpu.tick();
    }

}
