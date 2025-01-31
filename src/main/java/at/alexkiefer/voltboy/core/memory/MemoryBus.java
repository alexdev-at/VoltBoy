package at.alexkiefer.voltboy.core.memory;

import at.alexkiefer.voltboy.core.ConnectedInternal;
import at.alexkiefer.voltboy.core.VoltBoy;
import at.alexkiefer.voltboy.core.memory.addressspace.*;
import at.alexkiefer.voltboy.util.FormatUtils;

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

        writeUnrestricted(0xFF00, 0xCF);
        writeUnrestricted(0xFF01, 0x00);
        writeUnrestricted(0xFF02, 0x7E);
        writeUnrestricted(0xFF04, 0xAB);
        writeUnrestricted(0xFF05, 0x00);
        writeUnrestricted(0xFF06, 0x00);
        writeUnrestricted(0xFF07, 0xF8);
        writeUnrestricted(0xFF0F, 0xE1);
        writeUnrestricted(0xFF10, 0x80);
        writeUnrestricted(0xFF11, 0xBF);
        writeUnrestricted(0xFF12, 0xF3);
        writeUnrestricted(0xFF13, 0xFF);
        writeUnrestricted(0xFF14, 0xBF);
        writeUnrestricted(0xFF16, 0x3F);
        writeUnrestricted(0xFF17, 0x00);
        writeUnrestricted(0xFF18, 0xFF);
        writeUnrestricted(0xFF19, 0xBF);
        writeUnrestricted(0xFF1A, 0x7F);
        writeUnrestricted(0xFF1B, 0xFF);
        writeUnrestricted(0xFF1C, 0x9F);
        writeUnrestricted(0xFF1D, 0xFF);
        writeUnrestricted(0xFF1E, 0xBF);
        writeUnrestricted(0xFF20, 0xFF);
        writeUnrestricted(0xFF21, 0x00);
        writeUnrestricted(0xFF22, 0x00);
        writeUnrestricted(0xFF23, 0xBF);
        writeUnrestricted(0xFF24, 0x77);
        writeUnrestricted(0xFF25, 0xF3);
        writeUnrestricted(0xFF26, 0xF1);
        writeUnrestricted(0xFF40, 0x91);
        writeUnrestricted(0xFF41, 0x81);
        writeUnrestricted(0xFF42, 0x00);
        writeUnrestricted(0xFF43, 0x00);
        writeUnrestricted(0xFF44, 0x91);
        writeUnrestricted(0xFF45, 0x00);
        writeUnrestricted(0xFF46, 0xFF);
        writeUnrestricted(0xFF47, 0xFC);
        writeUnrestricted(0xFF48, 0x00);
        writeUnrestricted(0xFF49, 0x00);
        writeUnrestricted(0xFF4A, 0x00);
        writeUnrestricted(0xFF4B, 0x00);
        writeUnrestricted(0xFF4D, 0xFF);
        writeUnrestricted(0xFF4F, 0xFF);
        writeUnrestricted(0xFF51, 0xFF);
        writeUnrestricted(0xFF52, 0xFF);
        writeUnrestricted(0xFF53, 0xFF);
        writeUnrestricted(0xFF54, 0xFF);
        writeUnrestricted(0xFF55, 0xFF);
        writeUnrestricted(0xFF56, 0xFF);
        writeUnrestricted(0xFF68, 0xFF);
        writeUnrestricted(0xFF69, 0xFF);
        writeUnrestricted(0xFF6A, 0xFF);
        writeUnrestricted(0xFF6B, 0xFF);
        writeUnrestricted(0xFF70, 0xFF);
        writeUnrestricted(0xFFFF, 0x00);

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
