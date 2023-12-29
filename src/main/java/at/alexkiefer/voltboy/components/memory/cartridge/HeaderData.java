package at.alexkiefer.voltboy.components.memory.cartridge;

import java.util.Arrays;

public class HeaderData {

    private final String title;
    private final String manufacturerCode;
    private final boolean cgb;
    private final int newLicenseeCode;
    private final boolean sgb;
    private final int type;
    private final int romSize;
    private final int actualRomSize;
    private final int actualRomBanks;
    private final int ramSize;
    private final int actualRamSize;
    private final int actualRamBanks;
    private final boolean japanese;
    private final int oldLicenseeCode;
    private final int romVersion;

    public HeaderData(int[] rom) {

        title = Arrays.toString(Arrays.copyOfRange(rom, 0x0134, 0x0143));
        manufacturerCode = Arrays.toString(Arrays.copyOfRange(rom, 0x013F, 0x0143));
        cgb = (rom[0x0143] == 0x80) || (rom[0x0143] == 0xC0);
        newLicenseeCode = 0x00; // TODO
        sgb = rom[0x0146] == 0x03;
        type = rom[0x0147];

        romSize = rom[0x0148];
        switch(romSize) {
            case 0x00 -> {
                actualRomSize = 32 * 1024;
                actualRomBanks = 2;
            }
            case 0x01 -> {
                actualRomSize = 64 * 1024;
                actualRomBanks = 4;
            }
            case 0x02 -> {
                actualRomSize = 128 * 1024;
                actualRomBanks = 8;
            }
            case 0x03 -> {
                actualRomSize = 256 * 1024;
                actualRomBanks = 16;
            }
            case 0x04 -> {
                actualRomSize = 512 * 1024;
                actualRomBanks = 32;
            }
            case 0x05 -> {
                actualRomSize = 1000 * 1024;
                actualRomBanks = 64;
            }
            case 0x06 -> {
                actualRomSize = 2000 * 1024;
                actualRomBanks = 128;
            }
            case 0x07 -> {
                actualRomSize = 4000 * 1024;
                actualRomBanks = 256;
            }
            case 0x08 -> {
                actualRomSize = 8000 * 1024;
                actualRomBanks = 512;
            }
            default -> {
                actualRomSize = 0;
                actualRomBanks = 0;
            }
        }

        ramSize = rom[0x0149];
        switch(ramSize) {
            case 0x02 -> {
                actualRamSize = 8 * 1024;
                actualRamBanks = 1;
            }
            case 0x03 -> {
                actualRamSize = 32 * 1024;
                actualRamBanks = 4;
            }
            case 0x04 -> {
                actualRamSize = 128 * 1024;
                actualRamBanks = 16;
            }
            case 0x05 -> {
                actualRamSize = 64 * 1024;
                actualRamBanks = 8;
            }
            default -> {
                actualRamSize = 0;
                actualRamBanks = 0;
            }
        }

        japanese = rom[0x014A] == 0x00;
        oldLicenseeCode = rom[0x014B];
        romVersion = rom[0x014C];

    }

    public String getTitle() {
        return title;
    }

    public String getManufacturerCode() {
        return manufacturerCode;
    }

    public boolean isCgb() {
        return cgb;
    }

    public int getNewLicenseeCode() {
        return newLicenseeCode;
    }

    public boolean isSgb() {
        return sgb;
    }

    public int getType() {
        return type;
    }

    public int getRomSize() {
        return romSize;
    }

    public int getActualRomSize() {
        return actualRomSize;
    }

    public int getActualRomBanks() {
        return actualRomBanks;
    }

    public int getRamSize() {
        return ramSize;
    }

    public int getActualRamSize() {
        return actualRamSize;
    }

    public int getActualRamBanks() {
        return actualRamBanks;
    }

    public boolean isJapanese() {
        return japanese;
    }

    public int getOldLicenseeCode() {
        return oldLicenseeCode;
    }

    public int getRomVersion() {
        return romVersion;
    }

}