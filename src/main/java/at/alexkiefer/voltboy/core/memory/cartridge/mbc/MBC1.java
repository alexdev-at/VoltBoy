package at.alexkiefer.voltboy.core.memory.cartridge.mbc;

import at.alexkiefer.voltboy.core.VoltBoy;
import at.alexkiefer.voltboy.core.memory.cartridge.CartridgeHeaderData;

public class MBC1 extends MBC {

    private final int[] ram;

    private boolean ramEnable;
    private int romBankNumber;
    private int ramBankNumber;
    private boolean simpleBankingMode;

    public MBC1(VoltBoy gb, CartridgeHeaderData headerData) {

        super(gb, headerData);

        ram = new int[headerData.getRamSizeBytes()];

        ramEnable = false;
        romBankNumber = 1;
        ramBankNumber = 0;
        simpleBankingMode = true;

    }

    @Override
    public int resolveAddress(int addr) {
        if (addr >= 0x0000 && addr <= 0x3FFF) {
            if (simpleBankingMode) {
                return addr;
            } else {
                return addr + (0x4000 * (romBankNumber - 1));
            }
        } else {
            return addr + (0x4000 * (romBankNumber - 1));
        }
    }

    @Override
    public int readRam(int addr) {
        if (ramEnable) {
            if (simpleBankingMode) {
                return ram[addr - 0xA000];
            } else {
                return ram[(addr - 0xA000) + (0x2000 * (ramBankNumber))];
            }
        } else {
            return 0xFF;
        }
    }

    @Override
    public void writeRam(int addr, int data) {
        if(ramEnable) {
            if(simpleBankingMode) {
                ram[addr - 0xA000] = data;
            } else {
                ram[(addr - 0xA000) + (0x2000 * (ramBankNumber))] = data;
            }
        }
    }

    @Override
    public void writeRegister(int addr, int data) {
        if (addr >= 0x0000 && addr <= 0x1FFF) {
            ramEnable = (data & 0x0F) == 0x0A;
        } else if (addr >= 0x2000 && addr <= 0x3FFF) {
            data &= 0b0001_1111;
            if (data == 0x00) {
                data = 0x01;
            }
            romBankNumber = data;
        } else if (addr >= 0x4000 && addr <= 0x5FFF) {
            data &= 0b0000_0011;
            if(data < headerData.getRamBankCount()) {
                ramBankNumber = data;
            }
        } else if(addr >= 0x6000 && addr <= 0x7FFF) {
            data &= 0b0000_0001;
            if (headerData.getRamBankCount() > 0 && headerData.getRomBankCount() > 1) {
                simpleBankingMode = data == 0;
            }
        }
    }

}
