package at.alexkiefer.voltboy.core.memory.cartridge;

import at.alexkiefer.voltboy.core.ConnectedInternal;
import at.alexkiefer.voltboy.core.VoltBoy;
import at.alexkiefer.voltboy.core.memory.cartridge.mbc.MBC;
import at.alexkiefer.voltboy.core.memory.cartridge.mbc.MBC1;
import at.alexkiefer.voltboy.core.memory.cartridge.mbc.NoMBC;
import at.alexkiefer.voltboy.util.FormatUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Cartridge extends ConnectedInternal {

    private final CartridgeHeaderData headerData;

    private final int[] rom;
    private final MBC mbc;

    public Cartridge(VoltBoy gb, String romPath) throws IOException {

        super(gb);

        byte[] romBytes = Files.readAllBytes(Path.of(romPath));
        rom = new int[romBytes.length];


        for (int i = 0; i < romBytes.length; i++) {
            rom[i] = Byte.toUnsignedInt(romBytes[i]);
        }

        headerData = new CartridgeHeaderData(rom);

        switch (headerData.getCartridgeType()) {
            case 0x00 -> {
                mbc = new NoMBC(gb, headerData);
            }
            case 0x01, 0x02, 0x03 -> {
                mbc = new MBC1(gb, headerData);
            }
            default -> throw new IllegalArgumentException("Invalid or unimplemented MBC: 0x" + FormatUtils.toHex(headerData.getCartridgeType()));
        }

    }

    public int[] getRom() {
        return rom;
    }

    public CartridgeHeaderData getHeaderData() {
        return headerData;
    }

    public int read(int addr) {
        return rom[mbc.resolveAddress(addr)];
    }

    public void write(int addr, int value) {
        mbc.writeRegister(addr, value);
    }

    public int readRam(int addr) {
        return mbc.readRam(addr);
    }

    public void writeRam(int addr, int value) {
        mbc.writeRam(addr, value);
    }

}
