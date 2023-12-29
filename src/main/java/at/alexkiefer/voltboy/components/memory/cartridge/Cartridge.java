package at.alexkiefer.voltboy.components.memory.cartridge;

import at.alexkiefer.voltboy.ConnectedInternal;
import at.alexkiefer.voltboy.VoltBoy;
import at.alexkiefer.voltboy.components.memory.cartridge.mbcs.MBC;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Cartridge extends ConnectedInternal {

    private HeaderData data;
    private final int[] mem;
    private final MBC mbc;

    public Cartridge(VoltBoy gb, String path) {

        super(gb);

        try {

            byte[] bytes = Files.readAllBytes(Path.of(path));
            mem = new int[bytes.length];
            for(int i = 0; i < bytes.length; i++) {
                mem[i] = Byte.toUnsignedInt(bytes[i]);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        data = new HeaderData(mem);
        mbc = MBC.fromData(gb, data);

    }

    public int read(int addr) {
        return mem[mbc.resolveAddress(addr)];
    }

    public void write(int addr, int data) {
        mbc.writeRegister(addr, data);
    }

    public int readRam(int addr) {
        return mbc.readRam(addr);
    }

    public void writeRam(int addr, int data) {
        mbc.writeRam(addr, data);
    }

}