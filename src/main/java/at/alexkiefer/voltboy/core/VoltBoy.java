package at.alexkiefer.voltboy.core;

import at.alexkiefer.voltboy.core.cpu.CPU;
import at.alexkiefer.voltboy.core.dma.DMAController;
import at.alexkiefer.voltboy.core.memory.MemoryBus;
import at.alexkiefer.voltboy.core.memory.cartridge.Cartridge;
import at.alexkiefer.voltboy.core.timer.Timer;

import java.io.IOException;

public class VoltBoy implements Tickable {

    private final MemoryBus memoryBus;
    private final Cartridge cartridge;
    private final Timer timer;
    private  final DMAController dmaController;
    private final CPU cpu;

    public VoltBoy(String romPath) throws IOException {
        dmaController = new DMAController(this);
        cartridge = new Cartridge(this, romPath);
        memoryBus = new MemoryBus(this);
        timer = new Timer(this);
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

    public Timer getTimer() {
        return timer;
    }

    public DMAController getDmaController() {
        return dmaController;
    }

    @Override
    public void tick() {
        timer.tick();
        dmaController.tick();
        cpu.tick();
    }

}
