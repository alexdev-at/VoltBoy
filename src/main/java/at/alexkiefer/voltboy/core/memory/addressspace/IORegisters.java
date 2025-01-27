package at.alexkiefer.voltboy.core.memory.addressspace;

import at.alexkiefer.voltboy.core.VoltBoy;
import at.alexkiefer.voltboy.core.ppu.PPU;
import at.alexkiefer.voltboy.core.timer.Timer;

public class IORegisters extends AddressSpace {

    private final PPU ppu;
    private final Timer timer;

    public IORegisters(VoltBoy gb) {
        super(gb, 0xFF00, 0xFF7F);
        this.ppu = gb.getPpu();
        this.timer = gb.getTimer();
    }

    @Override
    public int read(int addr) {
        switch (addr) {
            case 0xFF04 -> {
                return timer.getDiv();
            }
            case 0xFF05 -> {
                return timer.getTima();
            }
            case 0xFF06 -> {
                return timer.getTma();
            }
            case 0xFF07 -> {
                return timer.getTac();
            }
            case 0xFF40 -> {
                return ppu.getLcdc();
            }
            case 0xFF41 -> {
                return ppu.getStat();
            }
            case 0xFF42 -> {
                return ppu.getScy();
            }
            case 0xFF43 -> {
                return ppu.getScx();
            }
            case 0xFF44 -> {
                return ppu.getLy();
            }
            case 0xFF45 -> {
                return ppu.getLyc();
            }
            case 0xFF4A -> {
                return ppu.getWy();
            }
            case 0xFF4B -> {
                return ppu.getWx();
            }
            default -> {
                return super.read(addr);
            }
        }
    }

    @Override
    public void write(int addr, int value) {
        switch (addr) {
            case 0xFF04 -> {
                timer.setDiv(0);
            }
            case 0xFF05 -> {

            }
            case 0xFF06 -> {
                timer.setTma(value);
            }
            case 0xFF07 -> {
                timer.setTac(value);
            }
            case 0xFF40 -> {
                ppu.setLcdc(value);
            }
            case 0xFF41 -> {
            }
            case 0xFF42 -> {
                ppu.setScy(value);
            }
            case 0xFF43 -> {
                ppu.setScx(value);
            }
            case 0xFF44 -> {
            }
            case 0xFF45 -> {
                ppu.setLyc(value);
            }
            case 0xFF4A -> {
                ppu.setWy(value);
            }
            case 0xFF4B -> {
                ppu.setWx(value);
            }
            case 0xFF46 -> {
                super.writeUnrestricted(addr, value);
                gb.getDmaController().scheduleStart(value);
            }
            default -> super.write(addr, value);
        }
    }

    @Override
    public int readUnrestricted(int addr) {
        switch (addr) {
            case 0xFF04 -> {
                return timer.getDiv();
            }
            case 0xFF05 -> {
                return timer.getTima();
            }
            case 0xFF06 -> {
                return timer.getTma();
            }
            case 0xFF07 -> {
                return timer.getTac();
            }
            case 0xFF40 -> {
                return ppu.getLcdc();
            }
            case 0xFF41 -> {
                return ppu.getStat();
            }
            case 0xFF42 -> {
                return ppu.getScy();
            }
            case 0xFF43 -> {
                return ppu.getScx();
            }
            case 0xFF44 -> {
                return ppu.getLy();
            }
            case 0xFF45 -> {
                return ppu.getLyc();
            }
            case 0xFF4A -> {
                return ppu.getWy();
            }
            case 0xFF4B -> {
                return ppu.getWx();
            }
            default -> {
                return super.readUnrestricted(addr);
            }
        }
    }

    @Override
    public void writeUnrestricted(int addr, int value) {
        switch (addr) {
            case 0xFF04 -> {
                timer.setDiv(value);
            }
            case 0xFF05 -> {
                timer.setTima(value);
            }
            case 0xFF06 -> {
                timer.setTma(value);
            }
            case 0xFF07 -> {
                timer.setTac(value);
            }
            case 0xFF40 -> {
                ppu.setLcdc(value);
            }
            case 0xFF41 -> {
                ppu.setStat(value);
            }
            case 0xFF42 -> {
                ppu.setScy(value);
            }
            case 0xFF43 -> {
                ppu.setScx(value);
            }
            case 0xFF44 -> {
                ppu.setLy(value);
            }
            case 0xFF45 -> {
                ppu.setLyc(value);
            }
            case 0xFF4A -> {
                ppu.setWy(value);
            }
            case 0xFF4B -> {
                ppu.setWx(value);
            }
            default -> {
                super.writeUnrestricted(addr, value);
            }
        }
    }
}
