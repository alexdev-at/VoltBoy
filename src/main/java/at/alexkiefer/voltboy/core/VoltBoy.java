package at.alexkiefer.voltboy.core;

import at.alexkiefer.voltboy.core.cpu.CPU;
import at.alexkiefer.voltboy.core.dma.DMAController;
import at.alexkiefer.voltboy.core.input.InputHandler;
import at.alexkiefer.voltboy.core.memory.MemoryBus;
import at.alexkiefer.voltboy.core.memory.cartridge.Cartridge;
import at.alexkiefer.voltboy.core.ppu.PPU;
import at.alexkiefer.voltboy.core.timer.Timer;

import java.io.IOException;

public class VoltBoy implements Tickable {

    private final MemoryBus memoryBus;
    private final Cartridge cartridge;
    private final Timer timer;
    private  final DMAController dmaController;
    private final PPU ppu;
    private final CPU cpu;
    private final InputHandler inputHandler;

    public VoltBoy(String romPath) throws IOException {
        dmaController = new DMAController(this);
        cartridge = new Cartridge(this, romPath);
        ppu = new PPU(this);
        timer = new Timer(this);
        inputHandler = new InputHandler(this);
        memoryBus = new MemoryBus(this);
        cpu = new CPU(this);
    }

    public MemoryBus getMemoryBus() {
        return memoryBus;
    }

    public PPU getPpu() {
        return ppu;
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

    public InputHandler getInputHandler() {
        return inputHandler;
    }

    @Override
    public void tick() {
        timer.tick();
        dmaController.tick();
        inputHandler.tick();
        ppu.tick();
        cpu.tick();
    }

}
