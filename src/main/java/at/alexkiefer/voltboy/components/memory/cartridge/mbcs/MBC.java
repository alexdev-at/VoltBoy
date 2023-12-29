package at.alexkiefer.voltboy.components.memory.cartridge.mbcs;

import at.alexkiefer.voltboy.ConnectedInternal;
import at.alexkiefer.voltboy.VoltBoy;
import at.alexkiefer.voltboy.components.memory.cartridge.HeaderData;

public abstract class MBC extends ConnectedInternal {

    protected final HeaderData data;

    public MBC(VoltBoy gb, HeaderData data) {
        super(gb);
        this.data = data;
    }

    public static MBC fromData(VoltBoy gb, HeaderData data) {
        switch(data.getType()) {
            case 0x00 -> {
                return new NoMBC(gb, data);
            }
            case 0x01, 0x02, 0x03 -> {
                return new MBC1(gb, data);
            }
            default -> {
                throw new RuntimeException("Invalid MBC type!");
            }
        }
    }

    public abstract int resolveAddress(int addr);

    public abstract int readRam(int addr);

    public abstract void writeRam(int addr, int data);

    public abstract void writeRegister(int addr, int data);

    public boolean hasRam() {
        return data.getActualRamSize() != 0;
    }

}