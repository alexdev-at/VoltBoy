package at.alexkiefer.voltboy.core.memory.cartridge;

import at.alexkiefer.voltboy.util.FormatUtils;

import java.util.Arrays;

public class CartridgeHeaderData {

    private final String fullTitle;
    private final String manufacturerCode;
    private final boolean cgbFlag;
    private final String newLicenseeCode;
    private final boolean sgbFlag;
    private final int cartridgeType;
    private final int romSize;
    private final int romSizeBytes;
    private final int romBankCount;
    private final int ramSize;
    private final int ramSizeBytes;
    private final int ramBankCount;
    private final int destinationCode;
    private final int oldLicenseeCode;
    private final int romVersion;
    private final int headerChecksum;
    private final int globalChecksum;

    public CartridgeHeaderData(int[] rom) {

        StringBuilder titleBuilder = new StringBuilder();
        for (int value : Arrays.copyOfRange(rom, 0x0134, 0x0143)) {
            titleBuilder.append((char) value);
        }
        fullTitle = titleBuilder.toString();

        manufacturerCode = fullTitle.substring(11, 14);

        cgbFlag = rom[0x0143] == 0xC0;

        newLicenseeCode = ((char) rom[0x0144]) + "" + ((char) rom[0x0145]);

        sgbFlag = rom[0x0146] == 0x03;

        cartridgeType = rom[0x0147];

        romSize = rom[0x0148];
        switch (romSize) {
            case 0x00 -> {
                romSizeBytes = 32 * 1024;
                romBankCount = 2;
            }
            case 0x01 -> {
                romSizeBytes = 64 * 1024;
                romBankCount = 4;
            }
            case 0x02 -> {
                romSizeBytes = 128 * 1024;
                romBankCount = 8;
            }
            case 0x03 -> {
                romSizeBytes = 256 * 1024;
                romBankCount = 16;
            }
            case 0x04 -> {
                romSizeBytes = 512 * 1024;
                romBankCount = 32;
            }
            case 0x05 -> {
                romSizeBytes = 1 * 1024 * 1024;
                romBankCount = 64;
            }
            case 0x06 -> {
                romSizeBytes = 2 * 1024 * 1024;
                romBankCount = 128;
            }
            case 0x07 -> {
                romSizeBytes = 4 * 1024 * 1024;
                romBankCount = 256;
            }
            case 0x08 -> {
                romSizeBytes = 8 * 1024 * 1024;
                romBankCount = 512;
            }
            default -> throw new IllegalArgumentException("Invalid value for rom size in header (0x0148): 0x" + FormatUtils.toHex(romSize));
        }

        ramSize = rom[0x0149];
        switch (ramSize) {
            case 0x00 -> {
                ramSizeBytes = 0;
                ramBankCount = 0;
            }
            case 0x02 -> {
                ramSizeBytes = 8 * 1024;
                ramBankCount = 1;
            }
            case 0x03 -> {
                ramSizeBytes = 32 * 1024;
                ramBankCount = 4;
            }
            case 0x04 -> {
                ramSizeBytes = 128 * 1024;
                ramBankCount = 16;
            }
            case 0x05 -> {
                ramSizeBytes = 64 * 1024;
                ramBankCount = 8;
            }
            default -> throw new IllegalArgumentException("Invalid value for ram size in header (0x0149): 0x" + FormatUtils.toHex(ramSize));
        }

        destinationCode = rom[0x014A];

        oldLicenseeCode = rom[0x014B];

        romVersion = rom[0x014C];

        headerChecksum = rom[0x014D];
        int checksum = 0;
        for (int i = 0x0134; i <= 0x014C; i++) {
            checksum = (checksum - rom[i] - 1) & 0xFF;
        }
        if (headerChecksum != checksum) {
            throw new IllegalArgumentException("Invalid checksum value after computation: 0x" + FormatUtils.toHex(checksum) + " (Excpected 0x" + FormatUtils.toHex(headerChecksum) + ")");
        }

        globalChecksum = (rom[0x014E] << 8) | rom[0x014F];

    }

    public String getFullTitle() {
        return fullTitle;
    }

    public String getManufacturerCode() {
        return manufacturerCode;
    }

    public boolean isCgbFlag() {
        return cgbFlag;
    }

    public String getNewLicenseeCode() {
        return newLicenseeCode;
    }

    public boolean isSgbFlag() {
        return sgbFlag;
    }

    public int getCartridgeType() {
        return cartridgeType;
    }

    public int getRomSize() {
        return romSize;
    }

    public int getRomSizeBytes() {
        return romSizeBytes;
    }

    public int getRomBankCount() {
        return romBankCount;
    }

    public int getRamSize() {
        return ramSize;
    }

    public int getRamSizeBytes() {
        return ramSizeBytes;
    }

    public int getRamBankCount() {
        return ramBankCount;
    }

    public int getDestinationCode() {
        return destinationCode;
    }

    public int getOldLicenseeCode() {
        return oldLicenseeCode;
    }

    public int getRomVersion() {
        return romVersion;
    }

    public int getHeaderChecksum() {
        return headerChecksum;
    }

    public int getGlobalChecksum() {
        return globalChecksum;
    }

}
