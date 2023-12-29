package at.alexkiefer.voltboy.components.memory.cartridge.mbcs;

import at.alexkiefer.voltboy.VoltBoy;
import at.alexkiefer.voltboy.components.memory.cartridge.HeaderData;

public class MBC1 extends MBC {

    private int romBank;
    private int ramBank;
    private boolean ramEnabled;
    private int[] ram;
    private int mode;

    public MBC1(VoltBoy gb, HeaderData data) {
        super(gb, data);
        romBank = 0x01;
        ramBank = 0x00;
        ramEnabled = false;
        mode = 0x00;
        ram = new int[data.getActualRamSize()];
    }

    @Override
    public int resolveAddress(int addr) {
        if(addr >= 0x0000 && addr <= 0x3FFF) {
            if(mode == 0) {
                return addr;
            } else {
                return addr + (0x4000 * (romBank - 1));
            }
        } else {
            return addr + (0x4000 * (romBank - 1));
        }
    }

    @Override
    public int readRam(int addr) {
        if(hasRam() && ramEnabled) {
            if(mode == 0) {
                return ram[addr - 0xA000];
            } else {
                return ram[(addr - 0xA000) + (0x2000 * (ramBank))];
            }
        } else {
            return 0xFF;
        }
    }

    @Override
    public void writeRam(int addr, int data) {
        if(hasRam() && ramEnabled) {
            if(mode == 0) {
                ram[addr - 0xA000] = data;
            } else {
                ram[(addr - 0xA000) + (0x2000 * (ramBank))] = data;
            }
        }
    }

    @Override
    public void writeRegister(int addr, int data) {
        if(addr >= 0x0000 && addr <= 0x1FFF) {
            ramEnabled = (data & 0x0F) == 0x0A;
        } else if(addr >= 0x2000 && addr <= 0x3FFF) {
            data &= 0x1F;
            if((data & 0x0F) == 0x00) {
                data = 0x01;
            }
            romBank = data;
        } else if(addr >= 0x4000 && addr <= 0x5FFF) {
            data &= 0x03;
            if(data < this.data.getActualRamBanks()) {
                ramBank = data;
            }
        } else if(addr >= 0x6000 && addr <= 0x7FFF) {
            data &= 0x01;
            if(this.data.getActualRamBanks() != 0 && this.data.getActualRomBanks() > 1) {
                mode = data;
            }
        }
    }

}
