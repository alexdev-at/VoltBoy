package at.alexkiefer.voltboy;

import at.alexkiefer.voltboy.components.cpu.CPU;
import at.alexkiefer.voltboy.components.memory.DataBus;
import at.alexkiefer.voltboy.components.memory.cartridge.Cartridge;
import at.alexkiefer.voltboy.components.ppu.PPU;
import at.alexkiefer.voltboy.components.timer.Timer;

public class VoltBoy {

    private final CPU cpu;
    private final PPU ppu;
    private final DataBus dataBus;
    private final Cartridge cart;
    private final Timer timer;

    public VoltBoy() {
        cart = new Cartridge(this, "D:\\GBGames\\gb-draw\\build\\gb-draw.gb");
        dataBus = new DataBus(this);
        cpu = new CPU(this);
        timer = new Timer(this);
        ppu = new PPU(this);
    }

    public void run() {
        while(true) {
            timer.tick();
            cpu.tick();
            ppu.tick();
        }
    }

    public void tick() {
        timer.tick();
        cpu.tick();
        ppu.tick();
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

    public Cartridge getCartridge() {
        return cart;
    }

    public Timer getTimer() {
        return timer;
    }

}