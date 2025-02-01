package at.alexkiefer.voltboy.core.memory.cartridge.mbc;

import at.alexkiefer.voltboy.core.VoltBoy;
import at.alexkiefer.voltboy.core.memory.cartridge.CartridgeHeaderData;

public class NoMBC extends MBC {

    public NoMBC(VoltBoy gb, CartridgeHeaderData headerData) {
        super(gb, headerData);
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

    }

    @Override
    public void writeRegister(int addr, int data) {

    }

}
