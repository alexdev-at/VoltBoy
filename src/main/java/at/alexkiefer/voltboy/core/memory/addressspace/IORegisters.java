package at.alexkiefer.voltboy.core.memory.addressspace;

import at.alexkiefer.voltboy.core.VoltBoy;

public class IORegisters extends AddressSpace {

    public IORegisters(VoltBoy gb) {
        super(gb, 0xFF00, 0xFF7F);
    }

    @Override
    public int read(int addr) {
        switch(addr) {
            case 0xFF07 -> {
                return super.read(addr) & 0b111;
            }
            default -> {
                return super.read(addr);
            }
        }
    }

    @Override
    public void write(int addr, int value) {
        switch(addr) {
            case 0xFF04 -> {
                super.writeUnrestricted(addr, 0);
                gb.getTimer().resetDiv();
            }
            case 0xFF07 -> {
                super.writeUnrestricted(addr, value & 0b111);
            }
            case 0xFF46 -> {
                super.writeUnrestricted(addr, value);
                gb.getDmaController().scheduleStart(value);
            }
            default -> super.write(addr, value);
        }
    }
}
