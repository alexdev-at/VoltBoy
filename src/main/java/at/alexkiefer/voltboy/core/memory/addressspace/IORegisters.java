package at.alexkiefer.voltboy.core.memory.addressspace;

import at.alexkiefer.voltboy.core.VoltBoy;
import at.alexkiefer.voltboy.core.apu.APU;
import at.alexkiefer.voltboy.core.apu.channel.CH3;
import at.alexkiefer.voltboy.core.dma.DMAController;
import at.alexkiefer.voltboy.core.input.InputHandler;
import at.alexkiefer.voltboy.core.ppu.PPU;
import at.alexkiefer.voltboy.core.timer.Timer;

public class IORegisters extends AddressSpace {

    private final APU apu;
    private final PPU ppu;
    private final Timer timer;
    private final InputHandler inputHandler;
    private final DMAController dmaController;

    public IORegisters(VoltBoy gb) {
        super(gb, 0xFF00, 0xFF7F);
        this.apu = gb.getApu();
        this.ppu = gb.getPpu();
        this.timer = gb.getTimer();
        this.inputHandler = gb.getInputHandler();
        this.dmaController = gb.getDmaController();
    }

    @Override
    public int read(int addr) {
        switch (addr) {
            case 0xFF00 -> {
                return inputHandler.getJoypad();
            }
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
            case 0xFF0F -> {
                return super.read(addr) | 0b1110_0000;
            }
            case 0xFF10 -> {
                return apu.getSoundChannels()[0].getNRX0();
            }
            case 0xFF11 -> {
                return apu.getSoundChannels()[0].getNRX1();
            }
            case 0xFF12 -> {
                return apu.getSoundChannels()[0].getNRX2();
            }
            case 0xFF13 -> {
                return apu.getSoundChannels()[0].getNRX3();
            }
            case 0xFF14 -> {
                return apu.getSoundChannels()[0].getNRX4();
            }
            case 0xFF16 -> {
                return apu.getSoundChannels()[1].getNRX1();
            }
            case 0xFF17 -> {
                return apu.getSoundChannels()[1].getNRX2();
            }
            case 0xFF18 -> {
                return apu.getSoundChannels()[1].getNRX3();
            }
            case 0xFF19 -> {
                return apu.getSoundChannels()[1].getNRX4();
            }
            case 0xFF1A -> {
                return apu.getSoundChannels()[2].getNRX0();
            }
            case 0xFF1B -> {
                return apu.getSoundChannels()[2].getNRX1();
            }
            case 0xFF1C -> {
                return apu.getSoundChannels()[2].getNRX2();
            }
            case 0xFF1D -> {
                return apu.getSoundChannels()[2].getNRX3();
            }
            case 0xFF1E -> {
                return apu.getSoundChannels()[2].getNRX4();
            }
            case 0xFF20 -> {
                return apu.getSoundChannels()[3].getNRX1();
            }
            case 0xFF21 -> {
                return apu.getSoundChannels()[3].getNRX2();
            }
            case 0xFF22 -> {
                return apu.getSoundChannels()[3].getNRX3();
            }
            case 0xFF23 -> {
                return apu.getSoundChannels()[3].getNRX4();
            }
            case 0xFF24 -> {
                return apu.getNR50();
            }
            case 0xFF25 -> {
                return apu.getNR51();
            }
            case 0xFF26 -> {
                return apu.getNR52();
            }
            case 0xFF30, 0xFF31, 0xFF32, 0xFF33, 0xFF34, 0xFF35, 0xFF36, 0xFF37, 0xFF38, 0xFF39, 0xFF3A, 0xFF3B, 0xFF3C, 0xFF3D, 0xFF3E, 0xFF3F -> {
                return ((CH3) apu.getSoundChannels()[2]).readWaveRam(addr);
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
            case 0xFF46 -> {
                return dmaController.getSourceAddressStart();
            }
            case 0xFF47 -> {
                return ppu.getBgp();
            }
            case 0xFF48 -> {
                return ppu.getObp0();
            }
            case 0xFF49 -> {
                return ppu.getObp1();
            }
            case 0xFF4A -> {
                return ppu.getWy();
            }
            case 0xFF4B -> {
                return ppu.getWx();
            }
            default -> {
                return 0xFF;
            }
        }
    }

    @Override
    public void write(int addr, int value) {
        switch (addr) {
            case 0xFF00 -> {
                inputHandler.selectJoypad(value);
            }
            case 0xFF04 -> {
                timer.resetDiv();
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
            case 0xFF0F -> {
                super.write(addr, value | 0b1110_0000);
            }
            case 0xFF10 -> {
                apu.getSoundChannels()[0].configureNRX0(value);
            }
            case 0xFF11 -> {
                apu.getSoundChannels()[0].configureNRX1(value);
            }
            case 0xFF12 -> {
                apu.getSoundChannels()[0].configureNRX2(value);
            }
            case 0xFF13 -> {
                apu.getSoundChannels()[0].configureNRX3(value);
            }
            case 0xFF14 -> {
                apu.getSoundChannels()[0].configureNRX4(value);
            }
            case 0xFF16 -> {
                apu.getSoundChannels()[1].configureNRX1(value);
            }
            case 0xFF17 -> {
                apu.getSoundChannels()[1].configureNRX2(value);
            }
            case 0xFF18 -> {
                apu.getSoundChannels()[1].configureNRX3(value);
            }
            case 0xFF19 -> {
                apu.getSoundChannels()[1].configureNRX4(value);
            }
            case 0xFF1A -> {
                apu.getSoundChannels()[2].configureNRX0(value);
            }
            case 0xFF1B -> {
                apu.getSoundChannels()[2].configureNRX1(value);
            }
            case 0xFF1C -> {
                apu.getSoundChannels()[2].configureNRX2(value);
            }
            case 0xFF1D -> {
                apu.getSoundChannels()[2].configureNRX3(value);
            }
            case 0xFF1E -> {
                apu.getSoundChannels()[2].configureNRX4(value);
            }
            case 0xFF20 -> {
                apu.getSoundChannels()[3].configureNRX1(value);
            }
            case 0xFF21 -> {
                apu.getSoundChannels()[3].configureNRX2(value);
            }
            case 0xFF22 -> {
                apu.getSoundChannels()[3].configureNRX3(value);
            }
            case 0xFF23 -> {
                apu.getSoundChannels()[3].configureNRX4(value);
            }
            case 0xFF24 -> {
                apu.configureNR50(value);
            }
            case 0xFF25 -> {
                apu.configureNR51(value);
            }
            case 0xFF26 -> {
                apu.configureNR52(value);
            }
            case 0xFF30, 0xFF31, 0xFF32, 0xFF33, 0xFF34, 0xFF35, 0xFF36, 0xFF37, 0xFF38, 0xFF39, 0xFF3A, 0xFF3B, 0xFF3C, 0xFF3D, 0xFF3E, 0xFF3F -> {
                ((CH3) apu.getSoundChannels()[2]).writeWaveRam(addr, value);
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
            }
            case 0xFF45 -> {
                ppu.setLyc(value);
            }
            case 0xFF46 -> {
                gb.getDmaController().scheduleStart(value);
            }
            case 0xFF47 -> {
                ppu.setBgp(value);
            }
            case 0xFF48 -> {
                ppu.setObp0(value);
            }
            case 0xFF49 -> {
                ppu.setObp1(value);
            }
            case 0xFF4A -> {
                ppu.setWy(value);
            }
            case 0xFF4B -> {
                ppu.setWx(value);
            }
            default -> {

            }
        }
    }

    @Override
    public int readUnrestricted(int addr) {
        switch (addr) {
            case 0xFF00 -> {
                return inputHandler.getJoypad();
            }
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
            case 0xFF0F -> {
                return super.readUnrestricted(addr) | 0b1110_0000;
            }
            case 0xFF10 -> {
                return apu.getSoundChannels()[0].getNRX0();
            }
            case 0xFF11 -> {
                return apu.getSoundChannels()[0].getNRX1();
            }
            case 0xFF12 -> {
                return apu.getSoundChannels()[0].getNRX2();
            }
            case 0xFF13 -> {
                return apu.getSoundChannels()[0].getNRX3();
            }
            case 0xFF14 -> {
                return apu.getSoundChannels()[0].getNRX4();
            }
            case 0xFF16 -> {
                return apu.getSoundChannels()[1].getNRX1();
            }
            case 0xFF17 -> {
                return apu.getSoundChannels()[1].getNRX2();
            }
            case 0xFF18 -> {
                return apu.getSoundChannels()[1].getNRX3();
            }
            case 0xFF19 -> {
                return apu.getSoundChannels()[1].getNRX4();
            }
            case 0xFF1A -> {
                return apu.getSoundChannels()[2].getNRX0();
            }
            case 0xFF1B -> {
                return apu.getSoundChannels()[2].getNRX1();
            }
            case 0xFF1C -> {
                return apu.getSoundChannels()[2].getNRX2();
            }
            case 0xFF1D -> {
                return apu.getSoundChannels()[2].getNRX3();
            }
            case 0xFF1E -> {
                return apu.getSoundChannels()[2].getNRX4();
            }
            case 0xFF20 -> {
                return apu.getSoundChannels()[3].getNRX1();
            }
            case 0xFF21 -> {
                return apu.getSoundChannels()[3].getNRX2();
            }
            case 0xFF22 -> {
                return apu.getSoundChannels()[3].getNRX3();
            }
            case 0xFF23 -> {
                return apu.getSoundChannels()[3].getNRX4();
            }
            case 0xFF24 -> {
                return apu.getNR50();
            }
            case 0xFF25 -> {
                return apu.getNR51();
            }
            case 0xFF26 -> {
                return apu.getNR52();
            }
            case 0xFF30, 0xFF31, 0xFF32, 0xFF33, 0xFF34, 0xFF35, 0xFF36, 0xFF37, 0xFF38, 0xFF39, 0xFF3A, 0xFF3B, 0xFF3C, 0xFF3D, 0xFF3E, 0xFF3F -> {
                return ((CH3) apu.getSoundChannels()[2]).readWaveRamUnrestricted(addr);
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
            case 0xFF46 -> {
                return dmaController.getSourceAddressStart();
            }
            case 0xFF47 -> {
                return ppu.getBgp();
            }
            case 0xFF48 -> {
                return ppu.getObp0();
            }
            case 0xFF49 -> {
                return ppu.getObp1();
            }
            case 0xFF4A -> {
                return ppu.getWy();
            }
            case 0xFF4B -> {
                return ppu.getWx();
            }
            default -> {
                return 0xFF;
            }
        }
    }

    @Override
    public void writeUnrestricted(int addr, int value) {
        switch (addr) {
            case 0xFF00 -> {
                inputHandler.setJoypad(value);
            }
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
            case 0xFF0F -> {
                super.writeUnrestricted(addr, value | 0b1110_0000);
            }
            case 0xFF10 -> {
                apu.getSoundChannels()[0].setNRX0(value);
            }
            case 0xFF11 -> {
                apu.getSoundChannels()[0].setNRX1(value);
            }
            case 0xFF12 -> {
                apu.getSoundChannels()[0].setNRX2(value);
            }
            case 0xFF13 -> {
                apu.getSoundChannels()[0].setNRX3(value);
            }
            case 0xFF14 -> {
                apu.getSoundChannels()[0].setNRX4(value);
            }
            case 0xFF16 -> {
                apu.getSoundChannels()[1].setNRX1(value);
            }
            case 0xFF17 -> {
                apu.getSoundChannels()[1].setNRX2(value);
            }
            case 0xFF18 -> {
                apu.getSoundChannels()[1].setNRX3(value);
            }
            case 0xFF19 -> {
                apu.getSoundChannels()[1].setNRX4(value);
            }
            case 0xFF1A -> {
                apu.getSoundChannels()[2].setNRX0(value);
            }
            case 0xFF1B -> {
                apu.getSoundChannels()[2].setNRX1(value);
            }
            case 0xFF1C -> {
                apu.getSoundChannels()[2].setNRX2(value);
            }
            case 0xFF1D -> {
                apu.getSoundChannels()[2].setNRX3(value);
            }
            case 0xFF1E -> {
                apu.getSoundChannels()[2].setNRX4(value);
            }
            case 0xFF20 -> {
                apu.getSoundChannels()[3].setNRX1(value);
            }
            case 0xFF21 -> {
                apu.getSoundChannels()[3].setNRX2(value);
            }
            case 0xFF22 -> {
                apu.getSoundChannels()[3].setNRX3(value);
            }
            case 0xFF23 -> {
                apu.getSoundChannels()[3].setNRX4(value);
            }
            case 0xFF24 -> {
                apu.setNR50(value);
            }
            case 0xFF25 -> {
                apu.setNR51(value);
            }
            case 0xFF26 -> {
                apu.setNR52(value);
            }
            case 0xFF30, 0xFF31, 0xFF32, 0xFF33, 0xFF34, 0xFF35, 0xFF36, 0xFF37, 0xFF38, 0xFF39, 0xFF3A, 0xFF3B, 0xFF3C, 0xFF3D, 0xFF3E, 0xFF3F -> {
                ((CH3) apu.getSoundChannels()[2]).writeWaveRamUnrestricted(addr, value);
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
            case 0xFF46 -> {
                gb.getDmaController().scheduleStart(value);
            }
            case 0xFF47 -> {
                ppu.setBgp(value);
            }
            case 0xFF48 -> {
                ppu.setObp0(value);
            }
            case 0xFF49 -> {
                ppu.setObp1(value);
            }
            case 0xFF4A -> {
                ppu.setWy(value);
            }
            case 0xFF4B -> {
                ppu.setWx(value);
            }
            default -> {

            }
        }
    }
}
