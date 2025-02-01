package at.alexkiefer.voltboy.core.memory.cartridge.mbc;

import at.alexkiefer.voltboy.core.ConnectedInternal;
import at.alexkiefer.voltboy.core.VoltBoy;
import at.alexkiefer.voltboy.core.memory.cartridge.CartridgeHeaderData;

public abstract class MBC extends ConnectedInternal {

    protected final CartridgeHeaderData headerData;

    public MBC(VoltBoy gb, CartridgeHeaderData headerData) {
        super(gb);
        this.headerData = headerData;
    }

    public abstract int resolveAddress(int addr);

    public abstract int readRam(int addr);

    public abstract void writeRam(int addr, int data);

    public abstract void writeRegister(int addr, int data);

}
