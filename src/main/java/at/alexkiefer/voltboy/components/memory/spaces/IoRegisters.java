package at.alexkiefer.voltboy.components.memory.spaces;

import at.alexkiefer.voltboy.VoltBoy;
import at.alexkiefer.voltboy.components.memory.AddressSpace;

public class IoRegisters extends AddressSpace {

    public IoRegisters(VoltBoy gb) {
        super(gb, 0xFF00, 0xFF7F, false);
    }

    @Override
    public int read(int addr) {
        switch(addr) {
            case 0xFF00 -> {
                if((readUnrestricted(0xFF00) & 0b00110000) == 0b00110000) {
                    return 0x0F;
                } else {
                    return super.read(addr) & 0b00111111;
                }
            }
            case 0xFF07 -> {
                return super.read(addr) & 0b111;
            }
            default -> {
                return super.read(addr);
            }
        }
    }

    @Override
    public void write(int addr, int data) {
        switch(addr) {
            case 0xFF00 -> {
                super.writeUnrestricted(addr, (data & 0b00110000) | (readUnrestricted(0xFF00) & 0b00001111));
            }
            case 0xFF04 -> {
                super.writeUnrestricted(addr, 0);
                gb.getTimer().resetDiv();
            }
            case 0xFF07 -> {
                super.writeUnrestricted(addr, data & 0b111);
            }
            case 0xFF46 -> {
                gb.getCpu().getDma().scheduleDmaTransfer(data << 8);
            }
            default -> super.write(addr, data);
        }
    }

}