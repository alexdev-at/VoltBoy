package at.alexkiefer.voltboy;

import at.alexkiefer.voltboy.components.cpu.CPU;
import at.alexkiefer.voltboy.components.memory.DataBus;

public class VoltBoy {

    private final CPU cpu;
    private final DataBus dataBus;

    public VoltBoy() {
        cpu = new CPU(this);
        dataBus = new DataBus(this);
    }

    public CPU getCpu() {
        return cpu;
    }

    public DataBus getDataBus() {
        return dataBus;
    }

}