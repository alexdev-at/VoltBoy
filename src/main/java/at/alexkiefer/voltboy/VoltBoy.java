package at.alexkiefer.voltboy;

import at.alexkiefer.voltboy.components.cpu.CPU;
import at.alexkiefer.voltboy.components.memory.DataBus;
import at.alexkiefer.voltboy.components.memory.cartridge.Cartridge;

public class VoltBoy {

    private final CPU cpu;
    private final DataBus dataBus;
    private final Cartridge cart;

    public VoltBoy() {
        cart = new Cartridge(this, "C:\\Users\\Alex\\IdeaProjects\\VoltBoy\\testroms\\blargg\\cpu_instrs\\individual\\10-bit ops.gb");
        dataBus = new DataBus(this);
        cpu = new CPU(this);
    }

    public void run() {
        while(true) {
            cpu.tick();
        }
    }

    public CPU getCpu() {
        return cpu;
    }

    public DataBus getDataBus() {
        return dataBus;
    }

    public Cartridge getCartridge() {
        return cart;
    }

}