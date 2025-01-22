package at.alexkiefer.voltboy.core.memory;

import at.alexkiefer.voltboy.core.ConnectedInternal;
import at.alexkiefer.voltboy.core.VoltBoy;
import at.alexkiefer.voltboy.core.memory.addressspace.*;

public class MemoryBus extends ConnectedInternal {

    private final StringBuffer serialBuffer;

    private int dataBus;

    private AddressSpace[] addressSpaces;

    public MemoryBus(VoltBoy gb) {

        super(gb);
        serialBuffer = new StringBuffer();

        addressSpaces = new AddressSpace[] {
                new Rom(gb),
                new VideoRam(gb),
                new ExternalRam(gb),
                new WorkRam(gb),
                new OamRam(gb),
                new ProhibitedSpace(gb),
                new IORegisters(gb),
                new HighRam(gb),
                new IERegister(gb)
        };

    }

    public int read(int addr) {
        if (gb.getDmaController().isActive() && (addr < 0xFF80 || addr > 0xFFFE)) {
            return 0xFF;
        }
        addr &= 0xFFFF;
        for (AddressSpace addressSpace : addressSpaces) {
            if (addr >= addressSpace.getStart() && addr <= addressSpace.getEnd()) {
                return addressSpace.read(addr);
            }
        }
        return 0xFF;
    }

    public void write(int addr, int value) {
        if (addr == 0xFF01) {
            serialBuffer.append((char) value);
        }
        if (gb.getDmaController().isActive() && (addr < 0xFF80 || addr > 0xFFFE)) {
            return;
        }
        addr &= 0xFFFF;
        for (AddressSpace addressSpace : addressSpaces) {
            if (addr >= addressSpace.getStart() && addr <= addressSpace.getEnd()) {
                addressSpace.write(addr, value & 0xFF);
                return;
            }
        }
    }

    public int readUnrestricted(int addr) {
        addr &= 0xFFFF;
        for (AddressSpace addressSpace : addressSpaces) {
            if (addr >= addressSpace.getStart() && addr <= addressSpace.getEnd()) {
                return addressSpace.readUnrestricted(addr);
            }
        }
        return 0xFF;
    }

    public void writeUnrestricted(int addr, int value) {
        if (addr == 0xFF01) {
            serialBuffer.append((char) value);
        }
        addr &= 0xFFFF;
        for (AddressSpace addressSpace : addressSpaces) {
            if (addr >= addressSpace.getStart() && addr <= addressSpace.getEnd()) {
                addressSpace.writeUnrestricted(addr, value & 0xFF);
                return;
            }
        }
    }

    public StringBuffer getSerialBuffer() {
        return serialBuffer;
    }

}
