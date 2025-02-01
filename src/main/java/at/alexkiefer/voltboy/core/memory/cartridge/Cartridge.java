package at.alexkiefer.voltboy.core.memory.cartridge;

import at.alexkiefer.voltboy.core.ConnectedInternal;
import at.alexkiefer.voltboy.core.VoltBoy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Cartridge extends ConnectedInternal {

    private final int[] rom;

    private final CartridgeHeaderData headerData;

    public Cartridge(VoltBoy gb, String romPath) throws IOException {

        super(gb);

        byte[] romBytes = Files.readAllBytes(Path.of(romPath));
        rom = new int[romBytes.length];
        for (int i = 0; i < romBytes.length; i++) {
            rom[i] = Byte.toUnsignedInt(romBytes[i]);
        }

        headerData = new CartridgeHeaderData(rom);

    }

    public int[] getRom() {
        return rom;
    }

    public CartridgeHeaderData getHeaderData() {
        return headerData;
    }

    public int read(int addr) {
        return rom[addr & 0xFFFF];
    }

    public void write(int addr, int value) {
        // TODO
    }

    public int readRam(int addr) {
        // TODO
        return 0x00;
    }

    public void writeRam(int addr, int value) {
        // TODO
    }

}
