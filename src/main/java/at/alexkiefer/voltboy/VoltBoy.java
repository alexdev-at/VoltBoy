package at.alexkiefer.voltboy;

import at.alexkiefer.voltboy.components.cpu.CPU;
import at.alexkiefer.voltboy.components.input.InputHandler;
import at.alexkiefer.voltboy.components.memory.DataBus;
import at.alexkiefer.voltboy.components.memory.cartridge.Cartridge;
import at.alexkiefer.voltboy.components.ppu.PPU;
import at.alexkiefer.voltboy.components.timer.Timer;

public class VoltBoy {

    private final CPU cpu;
    private final PPU ppu;
    private final DataBus dataBus;
    private final Cartridge cart;
    private final InputHandler inputHandler;
    private final Timer timer;

    public VoltBoy() {
        cart = new Cartridge(this, "C:\\Users\\Alex\\Downloads\\numism_rom_sym_2023-12-24\\numism-oneatatime.gb");
        dataBus = new DataBus(this);
        inputHandler = new InputHandler(this);
        cpu = new CPU(this);
        timer = new Timer(this);
        ppu = new PPU(this);
    }

    public void tick() {
        timer.tick();
        inputHandler.tick();
        ppu.tick();
        cpu.tick();
    }

    public CPU getCpu() {
        return cpu;
    }

    public PPU getPpu() {
        return ppu;
    }

    public DataBus getDataBus() {
        return dataBus;
    }

    public InputHandler getInputHandler() {
        return inputHandler;
    }

    public Cartridge getCartridge() {
        return cart;
    }

    public Timer getTimer() {
        return timer;
    }

}