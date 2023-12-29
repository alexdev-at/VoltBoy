package at.alexkiefer.voltboy.components.memory.cartridge.mbcs;

import at.alexkiefer.voltboy.VoltBoy;
import at.alexkiefer.voltboy.components.memory.cartridge.HeaderData;

public class NoMBC extends MBC {

    public NoMBC(VoltBoy gb, HeaderData data) {
        super(gb, data);
    }

    @Override
    public int resolveAddress(int addr) {
        return addr;
    }

    @Override
    public int readRam(int addr) {
        return 0xFF;
    }

    @Override
    public void writeRam(int addr, int data) {
        return;
    }

    @Override
    public void writeRegister(int addr, int data) {
        return;
    }

}