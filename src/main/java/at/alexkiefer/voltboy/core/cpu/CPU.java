package at.alexkiefer.voltboy.core.cpu;

import at.alexkiefer.voltboy.core.VoltBoy;
import at.alexkiefer.voltboy.core.cpu.register.CPURegisters;

public class CPU {

    private final VoltBoy gb;
    private final CPURegisters registers;

    public CPU(VoltBoy gb) {
        this.gb = gb;
        registers = new CPURegisters();
    }

    public CPURegisters getRegisters() {
        return registers;
    }

}
