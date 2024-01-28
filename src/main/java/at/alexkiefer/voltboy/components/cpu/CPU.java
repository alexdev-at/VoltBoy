package at.alexkiefer.voltboy.components.cpu;

import at.alexkiefer.voltboy.ConnectedInternal;
import at.alexkiefer.voltboy.Tickable;
import at.alexkiefer.voltboy.VoltBoy;
import at.alexkiefer.voltboy.components.cpu.instructions.Instruction;
import at.alexkiefer.voltboy.components.cpu.registers.Register;
import at.alexkiefer.voltboy.components.cpu.registers.Registers;
import at.alexkiefer.voltboy.util.BitUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

public class CPU extends ConnectedInternal implements Tickable {

    private final Registers reg;

    private boolean ime;
    private int imeScheduleCount;
    private boolean halt;
    private boolean haltBug;

    private int cycle;

    private boolean prefixed;
    private int opCode;

    private int data;
    private int addr;

    private final Instruction[] instr;
    private final Instruction[] cbInstr;
    private final Instruction[] isrInstr;
    private Instruction currInstr;

    private final BufferedWriter bw;

    public CPU(VoltBoy gb) {

        super(gb);

        try {
            bw = new BufferedWriter(new FileWriter("log.txt"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        reg = new Registers();

        ime = false;
        imeScheduleCount = 0;
        halt = false;
        haltBug = false;

        cycle = 1;

        prefixed = false;
        fetch();

        data = 0x00;
        addr = 0x00;

        instr = new Instruction[0x100];
        cbInstr = new Instruction[0x100];
        isrInstr = new Instruction[0x05];

        initInstructions();

    }

    @Override
    public void tick() {

        if(halt) {
            handleInterrupts();
            return;
        }

        if(imeScheduleCount > 0) {
            imeScheduleCount--;
            if(imeScheduleCount == 0) {
                ime = true;
            }
        }

        if(cycle == 1) {
            if(haltBug) {
                haltBug = false;
                reg.PC.dec();
            }
            currInstr = prefixed ? cbInstr[opCode] : instr[opCode];
            handleInterrupts();
            prefixed = false;
        }

        cycle++;

        currInstr.step();

    }

    private int read(int addr) {
        return gb.getDataBus().read(addr);
    }

    private void write(int addr, int data) {
        gb.getDataBus().write(addr, data);
    }

    private void fetch() {
        cycle = 1;
        opCode = read(reg.PC.getAndInc());
        if(opCode != 0xCB) {
            //debugLog();
        }
    }

    private void cbFetch() {
        prefixed = true;
        cycle = 1;
        opCode = read(reg.PC.getAndInc());
        reg.PC.dec();
        //debugLog();
        reg.PC.inc();
    }

    private void handleInterrupts() {

        int interrupts = read(0xFFFF) & read(0xFF0F) & 0x1F;

        if(halt && (interrupts != 0)) {
            halt = false;
        }

        if(ime && !prefixed) {

            if((interrupts & BitUtils.M_ZERO) != 0) {
                currInstr = isrInstr[0x00];
                write(0xFF0F, interrupts & ~BitUtils.M_ZERO);
            } else if((interrupts & BitUtils.M_ONE) != 0) {
                currInstr = isrInstr[0x01];
                write(0xFF0F, interrupts & ~BitUtils.M_ONE);
            } else if((interrupts & BitUtils.M_TWO) != 0) {
                currInstr = isrInstr[0x02];
                write(0xFF0F, interrupts & ~BitUtils.M_TWO);
            } else if((interrupts & BitUtils.M_THREE) != 0) {
                currInstr = isrInstr[0x03];
                write(0xFF0F, interrupts & ~BitUtils.M_THREE);
            } else if((interrupts & BitUtils.M_FOUR) != 0) {
                currInstr = isrInstr[0x04];
                write(0xFF0F, interrupts & ~BitUtils.M_FOUR);
            }

        }

    }

    private void initInstructions() {

        Arrays.fill(instr, new Instruction(this::INVALID));

        instr[0x00] = new Instruction(this::NOP);
        instr[0x01] = new Instruction(() -> LD_r16_u16(reg.B, reg.C));
        instr[0x02] = new Instruction(() -> LD__r16__A(reg.B, reg.C));
        instr[0x03] = new Instruction(() -> INC_r16(reg.B, reg.C));
        instr[0x04] = new Instruction(() -> INC_r8(reg.B));
        instr[0x05] = new Instruction(() -> DEC_r8(reg.B));
        instr[0x06] = new Instruction(() -> LD_r8_u8(reg.B));
        instr[0x07] = new Instruction(this::RLCA);
        instr[0x08] = new Instruction(this::LD__u16__SP);
        instr[0x09] = new Instruction(() -> ADD_HL_r16(reg.B, reg.C));
        instr[0x0A] = new Instruction(() -> LD_A__r16_(reg.B, reg.C));
        instr[0x0B] = new Instruction(() -> DEC_r16(reg.B, reg.C));
        instr[0x0C] = new Instruction(() -> INC_r8(reg.C));
        instr[0x0D] = new Instruction(() -> DEC_r8(reg.C));
        instr[0x0E] = new Instruction(() -> LD_r8_u8(reg.C));
        instr[0x0F] = new Instruction(this::RRCA);
        instr[0x10] = new Instruction(this::STOP);
        instr[0x11] = new Instruction(() -> LD_r16_u16(reg.D, reg.E));
        instr[0x12] = new Instruction(() -> LD__r16__A(reg.D, reg.E));
        instr[0x13] = new Instruction(() -> INC_r16(reg.D, reg.E));
        instr[0x14] = new Instruction(() -> INC_r8(reg.D));
        instr[0x15] = new Instruction(() -> DEC_r8(reg.D));
        instr[0x16] = new Instruction(() -> LD_r8_u8(reg.D));
        instr[0x17] = new Instruction(this::RLA);
        instr[0x18] = new Instruction(this::JR_i8);
        instr[0x19] = new Instruction(() -> ADD_HL_r16(reg.D, reg.E));
        instr[0x1A] = new Instruction(() -> LD_A__r16_(reg.D, reg.E));
        instr[0x1B] = new Instruction(() -> DEC_r16(reg.D, reg.E));
        instr[0x1C] = new Instruction(() -> INC_r8(reg.E));
        instr[0x1D] = new Instruction(() -> DEC_r8(reg.E));
        instr[0x1E] = new Instruction(() -> LD_r8_u8(reg.E));
        instr[0x1F] = new Instruction(this::RRA);
        instr[0x20] = new Instruction(() -> JR_i8_cond(!reg.F.isZero()));
        instr[0x21] = new Instruction(() -> LD_r16_u16(reg.H, reg.L));
        instr[0x22] = new Instruction(this::LD__HLI__A);
        instr[0x23] = new Instruction(() -> INC_r16(reg.H, reg.L));
        instr[0x24] = new Instruction(() -> INC_r8(reg.H));
        instr[0x25] = new Instruction(() -> DEC_r8(reg.H));
        instr[0x26] = new Instruction(() -> LD_r8_u8(reg.H));
        instr[0x27] = new Instruction(this::DAA);
        instr[0x28] = new Instruction(() -> JR_i8_cond(reg.F.isZero()));
        instr[0x29] = new Instruction(() -> ADD_HL_r16(reg.H, reg.L));
        instr[0x2A] = new Instruction(this::LD_A__HLI_);
        instr[0x2B] = new Instruction(() -> DEC_r16(reg.H, reg.L));
        instr[0x2C] = new Instruction(() -> INC_r8(reg.L));
        instr[0x2D] = new Instruction(() -> DEC_r8(reg.L));
        instr[0x2E] = new Instruction(() -> LD_r8_u8(reg.L));
        instr[0x2F] = new Instruction(this::CPL);
        instr[0x30] = new Instruction(() -> JR_i8_cond(!reg.F.isCarry()));
        instr[0x31] = new Instruction(this::LD_SP_u16);
        instr[0x32] = new Instruction(this::LD__HLD__A);
        instr[0x33] = new Instruction(this::INC_SP);
        instr[0x34] = new Instruction(this::INC__HL_);
        instr[0x35] = new Instruction(this::DEC__HL_);
        instr[0x36] = new Instruction(this::LD__HL__u8);
        instr[0x37] = new Instruction(this::SCF);
        instr[0x38] = new Instruction(() -> JR_i8_cond(reg.F.isCarry()));
        instr[0x39] = new Instruction(this::ADD_HL_SP);
        instr[0x3A] = new Instruction(this::LD_A__HLD_);
        instr[0x3B] = new Instruction(this::DEC_SP);
        instr[0x3C] = new Instruction(() -> INC_r8(reg.A));
        instr[0x3D] = new Instruction(() -> DEC_r8(reg.A));
        instr[0x3E] = new Instruction(() -> LD_r8_u8(reg.A));
        instr[0x3F] = new Instruction(this::CCF);
        instr[0x40] = new Instruction(() -> LD_r8_r8(reg.B, reg.B));
        instr[0x41] = new Instruction(() -> LD_r8_r8(reg.B, reg.C));
        instr[0x42] = new Instruction(() -> LD_r8_r8(reg.B, reg.D));
        instr[0x43] = new Instruction(() -> LD_r8_r8(reg.B, reg.E));
        instr[0x44] = new Instruction(() -> LD_r8_r8(reg.B, reg.H));
        instr[0x45] = new Instruction(() -> LD_r8_r8(reg.B, reg.L));
        instr[0x46] = new Instruction(() -> LD_r8__HL_(reg.B));
        instr[0x47] = new Instruction(() -> LD_r8_r8(reg.B, reg.A));
        instr[0x48] = new Instruction(() -> LD_r8_r8(reg.C, reg.B));
        instr[0x49] = new Instruction(() -> LD_r8_r8(reg.C, reg.C));
        instr[0x4A] = new Instruction(() -> LD_r8_r8(reg.C, reg.D));
        instr[0x4B] = new Instruction(() -> LD_r8_r8(reg.C, reg.E));
        instr[0x4C] = new Instruction(() -> LD_r8_r8(reg.C, reg.H));
        instr[0x4D] = new Instruction(() -> LD_r8_r8(reg.C, reg.L));
        instr[0x4E] = new Instruction(() -> LD_r8__HL_(reg.C));
        instr[0x4F] = new Instruction(() -> LD_r8_r8(reg.C, reg.A));
        instr[0x50] = new Instruction(() -> LD_r8_r8(reg.D, reg.B));
        instr[0x51] = new Instruction(() -> LD_r8_r8(reg.D, reg.C));
        instr[0x52] = new Instruction(() -> LD_r8_r8(reg.D, reg.D));
        instr[0x53] = new Instruction(() -> LD_r8_r8(reg.D, reg.E));
        instr[0x54] = new Instruction(() -> LD_r8_r8(reg.D, reg.H));
        instr[0x55] = new Instruction(() -> LD_r8_r8(reg.D, reg.L));
        instr[0x56] = new Instruction(() -> LD_r8__HL_(reg.D));
        instr[0x57] = new Instruction(() -> LD_r8_r8(reg.D, reg.A));
        instr[0x58] = new Instruction(() -> LD_r8_r8(reg.E, reg.B));
        instr[0x59] = new Instruction(() -> LD_r8_r8(reg.E, reg.C));
        instr[0x5A] = new Instruction(() -> LD_r8_r8(reg.E, reg.D));
        instr[0x5B] = new Instruction(() -> LD_r8_r8(reg.E, reg.E));
        instr[0x5C] = new Instruction(() -> LD_r8_r8(reg.E, reg.H));
        instr[0x5D] = new Instruction(() -> LD_r8_r8(reg.E, reg.L));
        instr[0x5E] = new Instruction(() -> LD_r8__HL_(reg.E));
        instr[0x5F] = new Instruction(() -> LD_r8_r8(reg.E, reg.A));
        instr[0x60] = new Instruction(() -> LD_r8_r8(reg.H, reg.B));
        instr[0x61] = new Instruction(() -> LD_r8_r8(reg.H, reg.C));
        instr[0x62] = new Instruction(() -> LD_r8_r8(reg.H, reg.D));
        instr[0x63] = new Instruction(() -> LD_r8_r8(reg.H, reg.E));
        instr[0x64] = new Instruction(() -> LD_r8_r8(reg.H, reg.H));
        instr[0x65] = new Instruction(() -> LD_r8_r8(reg.H, reg.L));
        instr[0x66] = new Instruction(() -> LD_r8__HL_(reg.H));
        instr[0x67] = new Instruction(() -> LD_r8_r8(reg.H, reg.A));
        instr[0x68] = new Instruction(() -> LD_r8_r8(reg.L, reg.B));
        instr[0x69] = new Instruction(() -> LD_r8_r8(reg.L, reg.C));
        instr[0x6A] = new Instruction(() -> LD_r8_r8(reg.L, reg.D));
        instr[0x6B] = new Instruction(() -> LD_r8_r8(reg.L, reg.E));
        instr[0x6C] = new Instruction(() -> LD_r8_r8(reg.L, reg.H));
        instr[0x6D] = new Instruction(() -> LD_r8_r8(reg.L, reg.L));
        instr[0x6E] = new Instruction(() -> LD_r8__HL_(reg.L));
        instr[0x6F] = new Instruction(() -> LD_r8_r8(reg.L, reg.A));
        instr[0x70] = new Instruction(() -> LD__HL__r8(reg.B));
        instr[0x71] = new Instruction(() -> LD__HL__r8(reg.C));
        instr[0x72] = new Instruction(() -> LD__HL__r8(reg.D));
        instr[0x73] = new Instruction(() -> LD__HL__r8(reg.E));
        instr[0x74] = new Instruction(() -> LD__HL__r8(reg.H));
        instr[0x75] = new Instruction(() -> LD__HL__r8(reg.L));
        instr[0x76] = new Instruction(this::HALT);
        instr[0x77] = new Instruction(() -> LD__HL__r8(reg.A));
        instr[0x78] = new Instruction(() -> LD_r8_r8(reg.A, reg.B));
        instr[0x79] = new Instruction(() -> LD_r8_r8(reg.A, reg.C));
        instr[0x7A] = new Instruction(() -> LD_r8_r8(reg.A, reg.D));
        instr[0x7B] = new Instruction(() -> LD_r8_r8(reg.A, reg.E));
        instr[0x7C] = new Instruction(() -> LD_r8_r8(reg.A, reg.H));
        instr[0x7D] = new Instruction(() -> LD_r8_r8(reg.A, reg.L));
        instr[0x7E] = new Instruction(() -> LD_r8__HL_(reg.A));
        instr[0x7F] = new Instruction(() -> LD_r8_r8(reg.A, reg.A));
        instr[0x80] = new Instruction(() -> ADD_r8(reg.B));
        instr[0x81] = new Instruction(() -> ADD_r8(reg.C));
        instr[0x82] = new Instruction(() -> ADD_r8(reg.D));
        instr[0x83] = new Instruction(() -> ADD_r8(reg.E));
        instr[0x84] = new Instruction(() -> ADD_r8(reg.H));
        instr[0x85] = new Instruction(() -> ADD_r8(reg.L));
        instr[0x86] = new Instruction(this::ADD__HL_);
        instr[0x87] = new Instruction(() -> ADD_r8(reg.A));
        instr[0x88] = new Instruction(() -> ADC_r8(reg.B));
        instr[0x89] = new Instruction(() -> ADC_r8(reg.C));
        instr[0x8A] = new Instruction(() -> ADC_r8(reg.D));
        instr[0x8B] = new Instruction(() -> ADC_r8(reg.E));
        instr[0x8C] = new Instruction(() -> ADC_r8(reg.H));
        instr[0x8D] = new Instruction(() -> ADC_r8(reg.L));
        instr[0x8E] = new Instruction(this::ADC__HL_);
        instr[0x8F] = new Instruction(() -> ADC_r8(reg.A));
        instr[0x90] = new Instruction(() -> SUB_r8(reg.B));
        instr[0x91] = new Instruction(() -> SUB_r8(reg.C));
        instr[0x92] = new Instruction(() -> SUB_r8(reg.D));
        instr[0x93] = new Instruction(() -> SUB_r8(reg.E));
        instr[0x94] = new Instruction(() -> SUB_r8(reg.H));
        instr[0x95] = new Instruction(() -> SUB_r8(reg.L));
        instr[0x96] = new Instruction(this::SUB__HL_);
        instr[0x97] = new Instruction(() -> SUB_r8(reg.A));
        instr[0x98] = new Instruction(() -> SBC_r8(reg.B));
        instr[0x99] = new Instruction(() -> SBC_r8(reg.C));
        instr[0x9A] = new Instruction(() -> SBC_r8(reg.D));
        instr[0x9B] = new Instruction(() -> SBC_r8(reg.E));
        instr[0x9C] = new Instruction(() -> SBC_r8(reg.H));
        instr[0x9D] = new Instruction(() -> SBC_r8(reg.L));
        instr[0x9E] = new Instruction(this::SBC__HL_);
        instr[0x9F] = new Instruction(() -> SBC_r8(reg.A));
        instr[0xA0] = new Instruction(() -> AND_r8(reg.B));
        instr[0xA1] = new Instruction(() -> AND_r8(reg.C));
        instr[0xA2] = new Instruction(() -> AND_r8(reg.D));
        instr[0xA3] = new Instruction(() -> AND_r8(reg.E));
        instr[0xA4] = new Instruction(() -> AND_r8(reg.H));
        instr[0xA5] = new Instruction(() -> AND_r8(reg.L));
        instr[0xA6] = new Instruction(this::AND__HL_);
        instr[0xA7] = new Instruction(() -> AND_r8(reg.A));
        instr[0xA8] = new Instruction(() -> XOR_r8(reg.B));
        instr[0xA9] = new Instruction(() -> XOR_r8(reg.C));
        instr[0xAA] = new Instruction(() -> XOR_r8(reg.D));
        instr[0xAB] = new Instruction(() -> XOR_r8(reg.E));
        instr[0xAC] = new Instruction(() -> XOR_r8(reg.H));
        instr[0xAD] = new Instruction(() -> XOR_r8(reg.L));
        instr[0xAE] = new Instruction(this::XOR__HL_);
        instr[0xAF] = new Instruction(() -> XOR_r8(reg.A));
        instr[0xB0] = new Instruction(() -> OR_r8(reg.B));
        instr[0xB1] = new Instruction(() -> OR_r8(reg.C));
        instr[0xB2] = new Instruction(() -> OR_r8(reg.D));
        instr[0xB3] = new Instruction(() -> OR_r8(reg.E));
        instr[0xB4] = new Instruction(() -> OR_r8(reg.H));
        instr[0xB5] = new Instruction(() -> OR_r8(reg.L));
        instr[0xB6] = new Instruction(this::OR__HL_);
        instr[0xB7] = new Instruction(() -> OR_r8(reg.A));
        instr[0xB8] = new Instruction(() -> CP_r8(reg.B));
        instr[0xB9] = new Instruction(() -> CP_r8(reg.C));
        instr[0xBA] = new Instruction(() -> CP_r8(reg.D));
        instr[0xBB] = new Instruction(() -> CP_r8(reg.E));
        instr[0xBC] = new Instruction(() -> CP_r8(reg.H));
        instr[0xBD] = new Instruction(() -> CP_r8(reg.L));
        instr[0xBE] = new Instruction(this::CP__HL_);
        instr[0xBF] = new Instruction(() -> CP_r8(reg.A));
        instr[0xC0] = new Instruction(() -> RET_cond(!reg.F.isZero()));
        instr[0xC1] = new Instruction(() -> POP_r16(reg.B, reg.C));
        instr[0xC2] = new Instruction(() -> JP_u16_cond(!reg.F.isZero()));
        instr[0xC3] = new Instruction(this::JP_u16);
        instr[0xC4] = new Instruction(() -> CALL_u16_cond(!reg.F.isZero()));
        instr[0xC5] = new Instruction(() -> PUSH_r16(reg.B, reg.C));
        instr[0xC6] = new Instruction(this::ADD_u8);
        instr[0xC7] = new Instruction(() -> RST(0x00));
        instr[0xC8] = new Instruction(() -> RET_cond(reg.F.isZero()));
        instr[0xC9] = new Instruction(this::RET);
        instr[0xCA] = new Instruction(() -> JP_u16_cond(reg.F.isZero()));
        instr[0xCB] = new Instruction(this::CB);
        instr[0xCC] = new Instruction(() -> CALL_u16_cond(reg.F.isZero()));
        instr[0xCD] = new Instruction(this::CALL_u16);
        instr[0xCE] = new Instruction(this::ADC_u8);
        instr[0xCF] = new Instruction(() -> RST(0x08));
        instr[0xD0] = new Instruction(() -> RET_cond(!reg.F.isCarry()));
        instr[0xD1] = new Instruction(() -> POP_r16(reg.D, reg.E));
        instr[0xD2] = new Instruction(() -> JP_u16_cond(!reg.F.isCarry()));
        // instr[0xD3]
        instr[0xD4] = new Instruction(() -> CALL_u16_cond(!reg.F.isCarry()));
        instr[0xD5] = new Instruction(() -> PUSH_r16(reg.D, reg.E));
        instr[0xD6] = new Instruction(this::SUB_u8);
        instr[0xD7] = new Instruction(() -> RST(0x10));
        instr[0xD8] = new Instruction(() -> RET_cond(reg.F.isCarry()));
        instr[0xD9] = new Instruction(this::RETI);
        instr[0xDA] = new Instruction(() -> JP_u16_cond(reg.F.isCarry()));
        // instr[0xDB]
        instr[0xDC] = new Instruction(() -> CALL_u16_cond(reg.F.isCarry()));
        // instr[0xDD]
        instr[0xDE] = new Instruction(this::SBC_u8);
        instr[0xDF] = new Instruction(() -> RST(0x18));
        instr[0xE0] = new Instruction(this::LDH__u8__A);
        instr[0xE1] = new Instruction(() -> POP_r16(reg.H, reg.L));
        instr[0xE2] = new Instruction(this::LDH__C__A);
        // instr[0xE3]
        // instr[0xE4]
        instr[0xE5] = new Instruction(() -> PUSH_r16(reg.H, reg.L));
        instr[0xE6] = new Instruction(this::AND_u8);
        instr[0xE7] = new Instruction(() -> RST(0x20));
        instr[0xE8] = new Instruction(this::ADD_SP_i8);
        instr[0xE9] = new Instruction(this::JP_HL);
        instr[0xEA] = new Instruction(this::LD__u16__A);
        // instr[0xEB]
        // instr[0xEC]
        // instr[0xED]
        instr[0xEE] = new Instruction(this::XOR_u8);
        instr[0xEF] = new Instruction(() -> RST(0x28));
        instr[0xF0] = new Instruction(this::LDH_A__u8_);
        instr[0xF1] = new Instruction(() -> POP_r16(reg.A, reg.F));
        instr[0xF2] = new Instruction(this::LDH_A__C_);
        instr[0xF3] = new Instruction(this::DI);
        // instr[0xF4]
        instr[0xF5] = new Instruction(() -> PUSH_r16(reg.A, reg.F));
        instr[0xF6] = new Instruction(this::OR_u8);
        instr[0xF7] = new Instruction(() -> RST(0x30));
        instr[0xF8] = new Instruction(this::ADD_HL_SPpi8);
        instr[0xF9] = new Instruction(this::LD_SP_HL);
        instr[0xFA] = new Instruction(this::LD_A__u16_);
        instr[0xFB] = new Instruction(this::EI);
        // instr[0xFC]
        // instr[0xFD]
        instr[0xFE] = new Instruction(this::CP_u8);
        instr[0xFF] = new Instruction(() -> RST(0x38));

        cbInstr[0x00] = new Instruction(() -> RLC_r8(reg.B));
        cbInstr[0x01] = new Instruction(() -> RLC_r8(reg.C));
        cbInstr[0x02] = new Instruction(() -> RLC_r8(reg.D));
        cbInstr[0x03] = new Instruction(() -> RLC_r8(reg.E));
        cbInstr[0x04] = new Instruction(() -> RLC_r8(reg.H));
        cbInstr[0x05] = new Instruction(() -> RLC_r8(reg.L));
        cbInstr[0x06] = new Instruction(this::RLC__HL_);
        cbInstr[0x07] = new Instruction(() -> RLC_r8(reg.A));
        cbInstr[0x08] = new Instruction(() -> RRC_r8(reg.B));
        cbInstr[0x09] = new Instruction(() -> RRC_r8(reg.C));
        cbInstr[0x0A] = new Instruction(() -> RRC_r8(reg.D));
        cbInstr[0x0B] = new Instruction(() -> RRC_r8(reg.E));
        cbInstr[0x0C] = new Instruction(() -> RRC_r8(reg.H));
        cbInstr[0x0D] = new Instruction(() -> RRC_r8(reg.L));
        cbInstr[0x0E] = new Instruction(this::RRC__HL_);
        cbInstr[0x0F] = new Instruction(() -> RRC_r8(reg.A));
        cbInstr[0x10] = new Instruction(() -> RL_r8(reg.B));
        cbInstr[0x11] = new Instruction(() -> RL_r8(reg.C));
        cbInstr[0x12] = new Instruction(() -> RL_r8(reg.D));
        cbInstr[0x13] = new Instruction(() -> RL_r8(reg.E));
        cbInstr[0x14] = new Instruction(() -> RL_r8(reg.H));
        cbInstr[0x15] = new Instruction(() -> RL_r8(reg.L));
        cbInstr[0x16] = new Instruction(this::RL__HL_);
        cbInstr[0x17] = new Instruction(() -> RL_r8(reg.A));
        cbInstr[0x18] = new Instruction(() -> RR_r8(reg.B));
        cbInstr[0x19] = new Instruction(() -> RR_r8(reg.C));
        cbInstr[0x1A] = new Instruction(() -> RR_r8(reg.D));
        cbInstr[0x1B] = new Instruction(() -> RR_r8(reg.E));
        cbInstr[0x1C] = new Instruction(() -> RR_r8(reg.H));
        cbInstr[0x1D] = new Instruction(() -> RR_r8(reg.L));
        cbInstr[0x1E] = new Instruction(this::RR__HL_);
        cbInstr[0x1F] = new Instruction(() -> RR_r8(reg.A));
        cbInstr[0x20] = new Instruction(() -> SLA_r8(reg.B));
        cbInstr[0x21] = new Instruction(() -> SLA_r8(reg.C));
        cbInstr[0x22] = new Instruction(() -> SLA_r8(reg.D));
        cbInstr[0x23] = new Instruction(() -> SLA_r8(reg.E));
        cbInstr[0x24] = new Instruction(() -> SLA_r8(reg.H));
        cbInstr[0x25] = new Instruction(() -> SLA_r8(reg.L));
        cbInstr[0x26] = new Instruction(this::SLA__HL_);
        cbInstr[0x27] = new Instruction(() -> SLA_r8(reg.A));
        cbInstr[0x28] = new Instruction(() -> SRA_r8(reg.B));
        cbInstr[0x29] = new Instruction(() -> SRA_r8(reg.C));
        cbInstr[0x2A] = new Instruction(() -> SRA_r8(reg.D));
        cbInstr[0x2B] = new Instruction(() -> SRA_r8(reg.E));
        cbInstr[0x2C] = new Instruction(() -> SRA_r8(reg.H));
        cbInstr[0x2D] = new Instruction(() -> SRA_r8(reg.L));
        cbInstr[0x2E] = new Instruction(this::SRA__HL_);
        cbInstr[0x2F] = new Instruction(() -> SRA_r8(reg.A));
        cbInstr[0x30] = new Instruction(() -> SWAP_r8(reg.B));
        cbInstr[0x31] = new Instruction(() -> SWAP_r8(reg.C));
        cbInstr[0x32] = new Instruction(() -> SWAP_r8(reg.D));
        cbInstr[0x33] = new Instruction(() -> SWAP_r8(reg.E));
        cbInstr[0x34] = new Instruction(() -> SWAP_r8(reg.H));
        cbInstr[0x35] = new Instruction(() -> SWAP_r8(reg.L));
        cbInstr[0x36] = new Instruction(this::SWAP__HL_);
        cbInstr[0x37] = new Instruction(() -> SWAP_r8(reg.A));
        cbInstr[0x38] = new Instruction(() -> SRL_r8(reg.B));
        cbInstr[0x39] = new Instruction(() -> SRL_r8(reg.C));
        cbInstr[0x3A] = new Instruction(() -> SRL_r8(reg.D));
        cbInstr[0x3B] = new Instruction(() -> SRL_r8(reg.E));
        cbInstr[0x3C] = new Instruction(() -> SRL_r8(reg.H));
        cbInstr[0x3D] = new Instruction(() -> SRL_r8(reg.L));
        cbInstr[0x3E] = new Instruction(this::SRL__HL_);
        cbInstr[0x3F] = new Instruction(() -> SRL_r8(reg.A));
        cbInstr[0x40] = new Instruction(() -> BIT_r8(reg.B, 0));
        cbInstr[0x41] = new Instruction(() -> BIT_r8(reg.C, 0));
        cbInstr[0x42] = new Instruction(() -> BIT_r8(reg.D, 0));
        cbInstr[0x43] = new Instruction(() -> BIT_r8(reg.E, 0));
        cbInstr[0x44] = new Instruction(() -> BIT_r8(reg.H, 0));
        cbInstr[0x45] = new Instruction(() -> BIT_r8(reg.L, 0));
        cbInstr[0x46] = new Instruction(() -> BIT__HL_(0));
        cbInstr[0x47] = new Instruction(() -> BIT_r8(reg.A, 0));
        cbInstr[0x48] = new Instruction(() -> BIT_r8(reg.B, 1));
        cbInstr[0x49] = new Instruction(() -> BIT_r8(reg.C, 1));
        cbInstr[0x4A] = new Instruction(() -> BIT_r8(reg.D, 1));
        cbInstr[0x4B] = new Instruction(() -> BIT_r8(reg.E, 1));
        cbInstr[0x4C] = new Instruction(() -> BIT_r8(reg.H, 1));
        cbInstr[0x4D] = new Instruction(() -> BIT_r8(reg.L, 1));
        cbInstr[0x4E] = new Instruction(() -> BIT__HL_(1));
        cbInstr[0x4F] = new Instruction(() -> BIT_r8(reg.A, 1));
        cbInstr[0x50] = new Instruction(() -> BIT_r8(reg.B, 2));
        cbInstr[0x51] = new Instruction(() -> BIT_r8(reg.C, 2));
        cbInstr[0x52] = new Instruction(() -> BIT_r8(reg.D, 2));
        cbInstr[0x53] = new Instruction(() -> BIT_r8(reg.E, 2));
        cbInstr[0x54] = new Instruction(() -> BIT_r8(reg.H, 2));
        cbInstr[0x55] = new Instruction(() -> BIT_r8(reg.L, 2));
        cbInstr[0x56] = new Instruction(() -> BIT__HL_(2));
        cbInstr[0x57] = new Instruction(() -> BIT_r8(reg.A, 2));
        cbInstr[0x58] = new Instruction(() -> BIT_r8(reg.B, 3));
        cbInstr[0x59] = new Instruction(() -> BIT_r8(reg.C, 3));
        cbInstr[0x5A] = new Instruction(() -> BIT_r8(reg.D, 3));
        cbInstr[0x5B] = new Instruction(() -> BIT_r8(reg.E, 3));
        cbInstr[0x5C] = new Instruction(() -> BIT_r8(reg.H, 3));
        cbInstr[0x5D] = new Instruction(() -> BIT_r8(reg.L, 3));
        cbInstr[0x5E] = new Instruction(() -> BIT__HL_(3));
        cbInstr[0x5F] = new Instruction(() -> BIT_r8(reg.A, 3));
        cbInstr[0x60] = new Instruction(() -> BIT_r8(reg.B, 4));
        cbInstr[0x61] = new Instruction(() -> BIT_r8(reg.C, 4));
        cbInstr[0x62] = new Instruction(() -> BIT_r8(reg.D, 4));
        cbInstr[0x63] = new Instruction(() -> BIT_r8(reg.E, 4));
        cbInstr[0x64] = new Instruction(() -> BIT_r8(reg.H, 4));
        cbInstr[0x65] = new Instruction(() -> BIT_r8(reg.L, 4));
        cbInstr[0x66] = new Instruction(() -> BIT__HL_(4));
        cbInstr[0x67] = new Instruction(() -> BIT_r8(reg.A, 4));
        cbInstr[0x68] = new Instruction(() -> BIT_r8(reg.B, 5));
        cbInstr[0x69] = new Instruction(() -> BIT_r8(reg.C, 5));
        cbInstr[0x6A] = new Instruction(() -> BIT_r8(reg.D, 5));
        cbInstr[0x6B] = new Instruction(() -> BIT_r8(reg.E, 5));
        cbInstr[0x6C] = new Instruction(() -> BIT_r8(reg.H, 5));
        cbInstr[0x6D] = new Instruction(() -> BIT_r8(reg.L, 5));
        cbInstr[0x6E] = new Instruction(() -> BIT__HL_(5));
        cbInstr[0x6F] = new Instruction(() -> BIT_r8(reg.A, 5));
        cbInstr[0x70] = new Instruction(() -> BIT_r8(reg.B, 6));
        cbInstr[0x71] = new Instruction(() -> BIT_r8(reg.C, 6));
        cbInstr[0x72] = new Instruction(() -> BIT_r8(reg.D, 6));
        cbInstr[0x73] = new Instruction(() -> BIT_r8(reg.E, 6));
        cbInstr[0x74] = new Instruction(() -> BIT_r8(reg.H, 6));
        cbInstr[0x75] = new Instruction(() -> BIT_r8(reg.L, 6));
        cbInstr[0x76] = new Instruction(() -> BIT__HL_(6));
        cbInstr[0x77] = new Instruction(() -> BIT_r8(reg.A, 6));
        cbInstr[0x78] = new Instruction(() -> BIT_r8(reg.B, 7));
        cbInstr[0x79] = new Instruction(() -> BIT_r8(reg.C, 7));
        cbInstr[0x7A] = new Instruction(() -> BIT_r8(reg.D, 7));
        cbInstr[0x7B] = new Instruction(() -> BIT_r8(reg.E, 7));
        cbInstr[0x7C] = new Instruction(() -> BIT_r8(reg.H, 7));
        cbInstr[0x7D] = new Instruction(() -> BIT_r8(reg.L, 7));
        cbInstr[0x7E] = new Instruction(() -> BIT__HL_(7));
        cbInstr[0x7F] = new Instruction(() -> BIT_r8(reg.A, 7));
        cbInstr[0x80] = new Instruction(() -> RES_r8(reg.B, 0));
        cbInstr[0x81] = new Instruction(() -> RES_r8(reg.C, 0));
        cbInstr[0x82] = new Instruction(() -> RES_r8(reg.D, 0));
        cbInstr[0x83] = new Instruction(() -> RES_r8(reg.E, 0));
        cbInstr[0x84] = new Instruction(() -> RES_r8(reg.H, 0));
        cbInstr[0x85] = new Instruction(() -> RES_r8(reg.L, 0));
        cbInstr[0x86] = new Instruction(() -> RES__HL_(0));
        cbInstr[0x87] = new Instruction(() -> RES_r8(reg.A, 0));
        cbInstr[0x88] = new Instruction(() -> RES_r8(reg.B, 1));
        cbInstr[0x89] = new Instruction(() -> RES_r8(reg.C, 1));
        cbInstr[0x8A] = new Instruction(() -> RES_r8(reg.D, 1));
        cbInstr[0x8B] = new Instruction(() -> RES_r8(reg.E, 1));
        cbInstr[0x8C] = new Instruction(() -> RES_r8(reg.H, 1));
        cbInstr[0x8D] = new Instruction(() -> RES_r8(reg.L, 1));
        cbInstr[0x8E] = new Instruction(() -> RES__HL_(1));
        cbInstr[0x8F] = new Instruction(() -> RES_r8(reg.A, 1));
        cbInstr[0x90] = new Instruction(() -> RES_r8(reg.B, 2));
        cbInstr[0x91] = new Instruction(() -> RES_r8(reg.C, 2));
        cbInstr[0x92] = new Instruction(() -> RES_r8(reg.D, 2));
        cbInstr[0x93] = new Instruction(() -> RES_r8(reg.E, 2));
        cbInstr[0x94] = new Instruction(() -> RES_r8(reg.H, 2));
        cbInstr[0x95] = new Instruction(() -> RES_r8(reg.L, 2));
        cbInstr[0x96] = new Instruction(() -> RES__HL_(2));
        cbInstr[0x97] = new Instruction(() -> RES_r8(reg.A, 2));
        cbInstr[0x98] = new Instruction(() -> RES_r8(reg.B, 3));
        cbInstr[0x99] = new Instruction(() -> RES_r8(reg.C, 3));
        cbInstr[0x9A] = new Instruction(() -> RES_r8(reg.D, 3));
        cbInstr[0x9B] = new Instruction(() -> RES_r8(reg.E, 3));
        cbInstr[0x9C] = new Instruction(() -> RES_r8(reg.H, 3));
        cbInstr[0x9D] = new Instruction(() -> RES_r8(reg.L, 3));
        cbInstr[0x9E] = new Instruction(() -> RES__HL_(3));
        cbInstr[0x9F] = new Instruction(() -> RES_r8(reg.A, 3));
        cbInstr[0xA0] = new Instruction(() -> RES_r8(reg.B, 4));
        cbInstr[0xA1] = new Instruction(() -> RES_r8(reg.C, 4));
        cbInstr[0xA2] = new Instruction(() -> RES_r8(reg.D, 4));
        cbInstr[0xA3] = new Instruction(() -> RES_r8(reg.E, 4));
        cbInstr[0xA4] = new Instruction(() -> RES_r8(reg.H, 4));
        cbInstr[0xA5] = new Instruction(() -> RES_r8(reg.L, 4));
        cbInstr[0xA6] = new Instruction(() -> RES__HL_(4));
        cbInstr[0xA7] = new Instruction(() -> RES_r8(reg.A, 4));
        cbInstr[0xA8] = new Instruction(() -> RES_r8(reg.B, 5));
        cbInstr[0xA9] = new Instruction(() -> RES_r8(reg.C, 5));
        cbInstr[0xAA] = new Instruction(() -> RES_r8(reg.D, 5));
        cbInstr[0xAB] = new Instruction(() -> RES_r8(reg.E, 5));
        cbInstr[0xAC] = new Instruction(() -> RES_r8(reg.H, 5));
        cbInstr[0xAD] = new Instruction(() -> RES_r8(reg.L, 5));
        cbInstr[0xAE] = new Instruction(() -> RES__HL_(5));
        cbInstr[0xAF] = new Instruction(() -> RES_r8(reg.A, 5));
        cbInstr[0xB0] = new Instruction(() -> RES_r8(reg.B, 6));
        cbInstr[0xB1] = new Instruction(() -> RES_r8(reg.C, 6));
        cbInstr[0xB2] = new Instruction(() -> RES_r8(reg.D, 6));
        cbInstr[0xB3] = new Instruction(() -> RES_r8(reg.E, 6));
        cbInstr[0xB4] = new Instruction(() -> RES_r8(reg.H, 6));
        cbInstr[0xB5] = new Instruction(() -> RES_r8(reg.L, 6));
        cbInstr[0xB6] = new Instruction(() -> RES__HL_(6));
        cbInstr[0xB7] = new Instruction(() -> RES_r8(reg.A, 6));
        cbInstr[0xB8] = new Instruction(() -> RES_r8(reg.B, 7));
        cbInstr[0xB9] = new Instruction(() -> RES_r8(reg.C, 7));
        cbInstr[0xBA] = new Instruction(() -> RES_r8(reg.D, 7));
        cbInstr[0xBB] = new Instruction(() -> RES_r8(reg.E, 7));
        cbInstr[0xBC] = new Instruction(() -> RES_r8(reg.H, 7));
        cbInstr[0xBD] = new Instruction(() -> RES_r8(reg.L, 7));
        cbInstr[0xBE] = new Instruction(() -> RES__HL_(7));
        cbInstr[0xBF] = new Instruction(() -> RES_r8(reg.A, 7));
        cbInstr[0xC0] = new Instruction(() -> SET_r8(reg.B, 0));
        cbInstr[0xC1] = new Instruction(() -> SET_r8(reg.C, 0));
        cbInstr[0xC2] = new Instruction(() -> SET_r8(reg.D, 0));
        cbInstr[0xC3] = new Instruction(() -> SET_r8(reg.E, 0));
        cbInstr[0xC4] = new Instruction(() -> SET_r8(reg.H, 0));
        cbInstr[0xC5] = new Instruction(() -> SET_r8(reg.L, 0));
        cbInstr[0xC6] = new Instruction(() -> SET__HL_(0));
        cbInstr[0xC7] = new Instruction(() -> SET_r8(reg.A, 0));
        cbInstr[0xC8] = new Instruction(() -> SET_r8(reg.B, 1));
        cbInstr[0xC9] = new Instruction(() -> SET_r8(reg.C, 1));
        cbInstr[0xCA] = new Instruction(() -> SET_r8(reg.D, 1));
        cbInstr[0xCB] = new Instruction(() -> SET_r8(reg.E, 1));
        cbInstr[0xCC] = new Instruction(() -> SET_r8(reg.H, 1));
        cbInstr[0xCD] = new Instruction(() -> SET_r8(reg.L, 1));
        cbInstr[0xCE] = new Instruction(() -> SET__HL_(1));
        cbInstr[0xCF] = new Instruction(() -> SET_r8(reg.A, 1));
        cbInstr[0xD0] = new Instruction(() -> SET_r8(reg.B, 2));
        cbInstr[0xD1] = new Instruction(() -> SET_r8(reg.C, 2));
        cbInstr[0xD2] = new Instruction(() -> SET_r8(reg.D, 2));
        cbInstr[0xD3] = new Instruction(() -> SET_r8(reg.E, 2));
        cbInstr[0xD4] = new Instruction(() -> SET_r8(reg.H, 2));
        cbInstr[0xD5] = new Instruction(() -> SET_r8(reg.L, 2));
        cbInstr[0xD6] = new Instruction(() -> SET__HL_(2));
        cbInstr[0xD7] = new Instruction(() -> SET_r8(reg.A, 2));
        cbInstr[0xD8] = new Instruction(() -> SET_r8(reg.B, 3));
        cbInstr[0xD9] = new Instruction(() -> SET_r8(reg.C, 3));
        cbInstr[0xDA] = new Instruction(() -> SET_r8(reg.D, 3));
        cbInstr[0xDB] = new Instruction(() -> SET_r8(reg.E, 3));
        cbInstr[0xDC] = new Instruction(() -> SET_r8(reg.H, 3));
        cbInstr[0xDD] = new Instruction(() -> SET_r8(reg.L, 3));
        cbInstr[0xDE] = new Instruction(() -> SET__HL_(3));
        cbInstr[0xDF] = new Instruction(() -> SET_r8(reg.A, 3));
        cbInstr[0xE0] = new Instruction(() -> SET_r8(reg.B, 4));
        cbInstr[0xE1] = new Instruction(() -> SET_r8(reg.C, 4));
        cbInstr[0xE2] = new Instruction(() -> SET_r8(reg.D, 4));
        cbInstr[0xE3] = new Instruction(() -> SET_r8(reg.E, 4));
        cbInstr[0xE4] = new Instruction(() -> SET_r8(reg.H, 4));
        cbInstr[0xE5] = new Instruction(() -> SET_r8(reg.L, 4));
        cbInstr[0xE6] = new Instruction(() -> SET__HL_(4));
        cbInstr[0xE7] = new Instruction(() -> SET_r8(reg.A, 4));
        cbInstr[0xE8] = new Instruction(() -> SET_r8(reg.B, 5));
        cbInstr[0xE9] = new Instruction(() -> SET_r8(reg.C, 5));
        cbInstr[0xEA] = new Instruction(() -> SET_r8(reg.D, 5));
        cbInstr[0xEB] = new Instruction(() -> SET_r8(reg.E, 5));
        cbInstr[0xEC] = new Instruction(() -> SET_r8(reg.H, 5));
        cbInstr[0xED] = new Instruction(() -> SET_r8(reg.L, 5));
        cbInstr[0xEE] = new Instruction(() -> SET__HL_(5));
        cbInstr[0xEF] = new Instruction(() -> SET_r8(reg.A, 5));
        cbInstr[0xF0] = new Instruction(() -> SET_r8(reg.B, 6));
        cbInstr[0xF1] = new Instruction(() -> SET_r8(reg.C, 6));
        cbInstr[0xF2] = new Instruction(() -> SET_r8(reg.D, 6));
        cbInstr[0xF3] = new Instruction(() -> SET_r8(reg.E, 6));
        cbInstr[0xF4] = new Instruction(() -> SET_r8(reg.H, 6));
        cbInstr[0xF5] = new Instruction(() -> SET_r8(reg.L, 6));
        cbInstr[0xF6] = new Instruction(() -> SET__HL_(6));
        cbInstr[0xF7] = new Instruction(() -> SET_r8(reg.A, 6));
        cbInstr[0xF8] = new Instruction(() -> SET_r8(reg.B, 7));
        cbInstr[0xF9] = new Instruction(() -> SET_r8(reg.C, 7));
        cbInstr[0xFA] = new Instruction(() -> SET_r8(reg.D, 7));
        cbInstr[0xFB] = new Instruction(() -> SET_r8(reg.E, 7));
        cbInstr[0xFC] = new Instruction(() -> SET_r8(reg.H, 7));
        cbInstr[0xFD] = new Instruction(() -> SET_r8(reg.L, 7));
        cbInstr[0xFE] = new Instruction(() -> SET__HL_(7));
        cbInstr[0xFF] = new Instruction(() -> SET_r8(reg.A, 7));

        isrInstr[0x00] = new Instruction(() -> ISR(0x40));
        isrInstr[0x01] = new Instruction(() -> ISR(0x48));
        isrInstr[0x02] = new Instruction(() -> ISR(0x50));
        isrInstr[0x03] = new Instruction(() -> ISR(0x58));
        isrInstr[0x04] = new Instruction(() -> ISR(0x60));

    }

    // 8-Bit Loads

    private void LD_r8_r8(Register dest, Register src) {
        switch(cycle) {
            case 2 -> {
                dest.setValue(src.getValue());
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void LD_r8_u8(Register dest) {
        switch(cycle) {
            case 2 -> {
                data = read(reg.PC.getAndInc());
            }
            case 3 -> {
                dest.setValue(data);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void LD_r8__HL_(Register dest) {
        switch(cycle) {
            case 2 -> {
                data = read(reg.getHLValue());
            }
            case 3 -> {
                dest.setValue(data);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void LD__HL__r8(Register src) {
        switch(cycle) {
            case 2 -> {
                write(reg.getHLValue(), src.getValue());
            }
            case 3 -> {
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void LD__HL__u8() {
        switch(cycle) {
            case 2 -> {
                data = read(reg.PC.getAndInc());
            }
            case 3 -> {
                write(reg.getHLValue(), data);
            }
            case 4 -> {
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void LD_A__r16_(Register hi, Register lo) {
        switch(cycle) {
            case 2 -> {
                data = read((hi.getValue() << 8) | lo.getValue());
            }
            case 3 -> {
                reg.A.setValue(data);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void LD__r16__A(Register hi, Register lo) {
        switch(cycle) {
            case 2 -> {
                write((hi.getValue() << 8) | lo.getValue(), reg.A.getValue());
            }
            case 3 -> {
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void LD_A__u16_() {
        switch(cycle) {
            case 2 -> {
                addr = read(reg.PC.getAndInc());
            }
            case 3 -> {
                addr |= read(reg.PC.getAndInc()) << 8;
            }
            case 4 -> {
                data = read(addr);
            }
            case 5 -> {
                reg.A.setValue(data);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void LD__u16__A() {
        switch(cycle) {
            case 2 -> {
                addr = read(reg.PC.getAndInc());
            }
            case 3 -> {
                addr |= read(reg.PC.getAndInc()) << 8;
            }
            case 4 -> {
                write(addr, reg.A.getValue());
            }
            case 5 -> {
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void LDH_A__C_() {
        switch(cycle) {
            case 2 -> {
                data = read(0xFF00 | reg.C.getValue());
            }
            case 3 -> {
                reg.A.setValue(data);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void LDH__C__A() {
        switch(cycle) {
            case 2 -> {
                write(0xFF00 | reg.C.getValue(), reg.A.getValue());
            }
            case 3 -> {
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void LDH_A__u8_() {
        switch(cycle) {
            case 2 -> {
                addr = read(reg.PC.getAndInc());
            }
            case 3 -> {
                data = read(0xFF00 | addr);
            }
            case 4 -> {
                reg.A.setValue(data);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void LDH__u8__A() {
        switch(cycle) {
            case 2 -> {
                addr = read(reg.PC.getAndInc());
            }
            case 3 -> {
                write(0xFF00 | addr, reg.A.getValue());
            }
            case 4 -> {
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void LD_A__HLD_() {
        switch(cycle) {
            case 2 -> {
                data = read(reg.getHLValue());
                reg.setHLValue(reg.getHLValue() - 1);
            }
            case 3 -> {
                reg.A.setValue(data);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void LD__HLD__A() {
        switch(cycle) {
            case 2 -> {
                write(reg.getHLValue(), reg.A.getValue());
                reg.setHLValue(reg.getHLValue() - 1);
            }
            case 3 -> {
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void LD_A__HLI_() {
        switch(cycle) {
            case 2 -> {
                data = read(reg.getHLValue());
                reg.setHLValue(reg.getHLValue() + 1);
            }
            case 3 -> {
                reg.A.setValue(data);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void LD__HLI__A() {
        switch(cycle) {
            case 2 -> {
                write(reg.getHLValue(), reg.A.getValue());
                reg.setHLValue(reg.getHLValue() + 1);
            }
            case 3 -> {
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    // 16-Bit Loads

    private void LD_r16_u16(Register hi, Register lo) {
        switch(cycle) {
            case 2 -> {
                data = read(reg.PC.getAndInc());
            }
            case 3 -> {
                data |= read(reg.PC.getAndInc()) << 8;
            }
            case 4 -> {
                hi.setValue(data >> 8);
                lo.setValue(data & 0xFF);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void LD_SP_u16() {
        switch(cycle) {
            case 2 -> {
                data = read(reg.PC.getAndInc());
            }
            case 3 -> {
                data |= read(reg.PC.getAndInc()) << 8;
            }
            case 4 -> {
                reg.SP.setValue(data);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void LD__u16__SP() {
        switch(cycle) {
            case 2 -> {
                addr = read(reg.PC.getAndInc());
            }
            case 3 -> {
                addr |= read(reg.PC.getAndInc()) << 8;
            }
            case 4 -> {
                write(addr++, reg.SP.getValue() & 0xFF);
            }
            case 5 -> {
                write(addr, reg.SP.getValue() >> 8);
            }
            case 6 -> {
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void LD_SP_HL() {
        switch(cycle) {
            case 2 -> {
                // BUS IDLE
                reg.SP.setValue(reg.getHLValue());
            }
            case 3 -> {
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void PUSH_r16(Register hi, Register lo) {
        switch(cycle) {
            case 2 -> {
                // BUS IDLE
                reg.SP.dec();
            }
            case 3 -> {
                write(reg.SP.getAndDec(), hi.getValue());
            }
            case 4 -> {
                write(reg.SP.getValue(), lo.getValue());
            }
            case 5 -> {
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void POP_r16(Register hi, Register lo) {
        switch(cycle) {
            case 2 -> {
                data = read(reg.SP.getAndInc());
            }
            case 3 -> {
                data |= read(reg.SP.getAndInc()) << 8;
            }
            case 4 -> {
                hi.setValue(data >> 8);
                lo.setValue(data & 0xFF);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    // 8-Bit ALU

    private void ADD_r8(Register r) {
        switch(cycle) {
            case 2 -> {
                int a = reg.A.getValue();
                int b = r.getValue();
                int c = 0;
                int res = a + b;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(halfCarryEight(a, b, c, res));
                reg.F.setCarry(res > 0xFF);
                reg.A.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void ADD_u8() {
        switch(cycle) {
            case 2 -> {
                data = read(reg.PC.getAndInc());
            }
            case 3 -> {
                int a = reg.A.getValue();
                int b = data;
                int c = 0;
                int res = a + b;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(halfCarryEight(a, b, c, res));
                reg.F.setCarry(res > 0xFF);
                reg.A.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void ADD__HL_() {
        switch(cycle) {
            case 2 -> {
                data = read(reg.getHLValue());
            }
            case 3 -> {
                int a = reg.A.getValue();
                int b = data;
                int c = 0;
                int res = a + b;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(halfCarryEight(a, b, c, res));
                reg.F.setCarry(res > 0xFF);
                reg.A.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void ADC_r8(Register r) {
        switch(cycle) {
            case 2 -> {
                int a = reg.A.getValue();
                int b = r.getValue();
                int c = reg.F.isCarry() ? 1 : 0;
                int res = a + b + c;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(halfCarryEight(a, b, c, res));
                reg.F.setCarry(res > 0xFF);
                reg.A.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void ADC_u8() {
        switch(cycle) {
            case 2 -> {
                data = read(reg.PC.getAndInc());
            }
            case 3 -> {
                int a = reg.A.getValue();
                int b = data;
                int c = reg.F.isCarry() ? 1 : 0;
                int res = a + b + c;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(halfCarryEight(a, b, c, res));
                reg.F.setCarry(res > 0xFF);
                reg.A.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void ADC__HL_() {
        switch(cycle) {
            case 2 -> {
                data = read(reg.getHLValue());
            }
            case 3 -> {
                int a = reg.A.getValue();
                int b = data;
                int c = reg.F.isCarry() ? 1 : 0;
                int res = a + b + c;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(halfCarryEight(a, b, c, res));
                reg.F.setCarry(res > 0xFF);
                reg.A.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void SUB_r8(Register r) {
        switch(cycle) {
            case 2 -> {
                int a = reg.A.getValue();
                int b = r.getValue();
                int c = 0;
                int res = a - b;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(true);
                reg.F.setHalfCarry(halfCarryEight(a, b, c, res));
                reg.F.setCarry(res < 0x00);
                reg.A.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void SUB_u8() {
        switch(cycle) {
            case 2 -> {
                data = read(reg.PC.getAndInc());
            }
            case 3 -> {
                int a = reg.A.getValue();
                int b = data;
                int c = 0;
                int res = a - b;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(true);
                reg.F.setHalfCarry(halfCarryEight(a, b, c, res));
                reg.F.setCarry(res < 0x00);
                reg.A.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void SUB__HL_() {
        switch(cycle) {
            case 2 -> {
                data = read(reg.getHLValue());
            }
            case 3 -> {
                int a = reg.A.getValue();
                int b = data;
                int c = 0;
                int res = a - b;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(true);
                reg.F.setHalfCarry(halfCarryEight(a, b, c, res));
                reg.F.setCarry(res < 0x00);
                reg.A.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void SBC_r8(Register r) {
        switch(cycle) {
            case 2 -> {
                int a = reg.A.getValue();
                int b = r.getValue();
                int c = reg.F.isCarry() ? 1 : 0;
                int res = a - b - c;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(true);
                reg.F.setHalfCarry(halfCarryEight(a, b, c, res));
                reg.F.setCarry(res < 0x00);
                reg.A.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void SBC_u8() {
        switch(cycle) {
            case 2 -> {
                data = read(reg.PC.getAndInc());
            }
            case 3 -> {
                int a = reg.A.getValue();
                int b = data;
                int c = reg.F.isCarry() ? 1 : 0;
                int res = a - b - c;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(true);
                reg.F.setHalfCarry(halfCarryEight(a, b, c, res));
                reg.F.setCarry(res < 0x00);
                reg.A.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void SBC__HL_() {
        switch(cycle) {
            case 2 -> {
                data = read(reg.getHLValue());
            }
            case 3 -> {
                int a = reg.A.getValue();
                int b = data;
                int c = reg.F.isCarry() ? 1 : 0;
                int res = a - b - c;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(true);
                reg.F.setHalfCarry(halfCarryEight(a, b, c, res));
                reg.F.setCarry(res < 0x00);
                reg.A.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void CP_r8(Register r) {
        switch(cycle) {
            case 2 -> {
                int a = reg.A.getValue();
                int b = r.getValue();
                int c = 0;
                int res = a - b;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(true);
                reg.F.setHalfCarry(halfCarryEight(a, b, c, res));
                reg.F.setCarry(res < 0x00);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void CP_u8() {
        switch(cycle) {
            case 2 -> {
                data = read(reg.PC.getAndInc());
            }
            case 3 -> {
                int a = reg.A.getValue();
                int b = data;
                int c = 0;
                int res = a - b;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(true);
                reg.F.setHalfCarry(halfCarryEight(a, b, c, res));
                reg.F.setCarry(res < 0x00);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void CP__HL_() {
        switch(cycle) {
            case 2 -> {
                data = read(reg.getHLValue());
            }
            case 3 -> {
                int a = reg.A.getValue();
                int b = data;
                int c = 0;
                int res = a - b;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(true);
                reg.F.setHalfCarry(halfCarryEight(a, b, c, res));
                reg.F.setCarry(res < 0x00);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void INC_r8(Register r) {
        switch(cycle) {
            case 2 -> {
                int a = r.getValue();
                int b = 1;
                int c = 0;
                int res = a + b;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(halfCarryEight(a, b, c, res));
                r.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void INC__HL_() {
        switch(cycle) {
            case 2 -> {
                data = read(reg.getHLValue());
            }
            case 3 -> {
                int a = data;
                int b = 1;
                int c = 0;
                int res = a + b;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(halfCarryEight(a, b, c, res));
                write(reg.getHLValue(), res);
            }
            case 4 -> {
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void DEC_r8(Register r) {
        switch(cycle) {
            case 2 -> {
                int a = r.getValue();
                int b = 1;
                int c = 0;
                int res = a - b;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(true);
                reg.F.setHalfCarry(halfCarryEight(a, b, c, res));
                r.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void DEC__HL_() {
        switch(cycle) {
            case 2 -> {
                data = read(reg.getHLValue());
            }
            case 3 -> {
                int a = data;
                int b = 1;
                int c = 0;
                int res = a - b;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(true);
                reg.F.setHalfCarry(halfCarryEight(a, b, c, res));
                write(reg.getHLValue(), res);
            }
            case 4 -> {
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void AND_r8(Register r) {
        switch(cycle) {
            case 2 -> {
                int a = reg.A.getValue();
                int b = r.getValue();
                int res = a & b;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(true);
                reg.F.setCarry(false);
                reg.A.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void AND_u8() {
        switch(cycle) {
            case 2 -> {
                data = read(reg.PC.getAndInc());
            }
            case 3 -> {
                int a = reg.A.getValue();
                int b = data;
                int res = a & b;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(true);
                reg.F.setCarry(false);
                reg.A.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void AND__HL_() {
        switch(cycle) {
            case 2 -> {
                data = read(reg.getHLValue());
            }
            case 3 -> {
                int a = reg.A.getValue();
                int b = data;
                int res = a & b;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(true);
                reg.F.setCarry(false);
                reg.A.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void OR_r8(Register r) {
        switch(cycle) {
            case 2 -> {
                int a = reg.A.getValue();
                int b = r.getValue();
                int res = a | b;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(false);
                reg.F.setCarry(false);
                reg.A.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void OR_u8() {
        switch(cycle) {
            case 2 -> {
                data = read(reg.PC.getAndInc());
            }
            case 3 -> {
                int a = reg.A.getValue();
                int b = data;
                int res = a | b;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(false);
                reg.F.setCarry(false);
                reg.A.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void OR__HL_() {
        switch(cycle) {
            case 2 -> {
                data = read(reg.getHLValue());
            }
            case 3 -> {
                int a = reg.A.getValue();
                int b = data;
                int res = a | b;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(false);
                reg.F.setCarry(false);
                reg.A.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void XOR_r8(Register r) {
        switch(cycle) {
            case 2 -> {
                int a = reg.A.getValue();
                int b = r.getValue();
                int res = a ^ b;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(false);
                reg.F.setCarry(false);
                reg.A.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void XOR_u8() {
        switch(cycle) {
            case 2 -> {
                data = read(reg.PC.getAndInc());
            }
            case 3 -> {
                int a = reg.A.getValue();
                int b = data;
                int res = a ^ b;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(false);
                reg.F.setCarry(false);
                reg.A.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void XOR__HL_() {
        switch(cycle) {
            case 2 -> {
                data = read(reg.getHLValue());
            }
            case 3 -> {
                int a = reg.A.getValue();
                int b = data;
                int res = a ^ b;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(false);
                reg.F.setCarry(false);
                reg.A.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void CCF() {
        switch(cycle) {
            case 2 -> {
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(false);
                reg.F.setCarry(!reg.F.isCarry());
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void SCF() {
        switch(cycle) {
            case 2 -> {
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(false);
                reg.F.setCarry(true);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void DAA() {
        switch(cycle) {
            case 2 -> {
                if(!reg.F.isSubtraction()) {
                    if(reg.F.isCarry() || reg.A.getValue() > 0x99) {
                        reg.A.setValue(reg.A.getValue() + 0x60);
                        reg.F.setCarry(true);
                    }
                    if(reg.F.isHalfCarry() || (reg.A.getValue() & 0x0F) > 0x09) {
                        reg.A.setValue(reg.A.getValue() + 0x06);
                    }
                } else {
                    if(reg.F.isCarry()) {
                        reg.A.setValue(reg.A.getValue() - 0x60);
                    }
                    if(reg.F.isHalfCarry()) {
                        reg.A.setValue(reg.A.getValue() - 0x06);
                    }
                }
                reg.F.setZero(reg.A.getValue() == 0);
                reg.F.setHalfCarry(false);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void CPL() {
        switch(cycle) {
            case 2 -> {
                reg.A.setValue(~reg.A.getValue());
                reg.F.setSubtraction(true);
                reg.F.setHalfCarry(true);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    // 16-Bit ALU
    private void INC_r16(Register hi, Register lo) {
        switch(cycle) {
            case 2 -> {
                lo.inc();
                if(lo.getValue() == 0x00) {
                    hi.inc();
                }
            }
            case 3 -> {
                // Cycle punishment due to IDU unit being used already for the 16-Bit increment, therefore we can't increment PC in the cycle before
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void INC_SP() {
        switch(cycle) {
            case 2 -> {
                reg.SP.inc();
            }
            case 3 -> {
                // Cycle punishment due to IDU unit being used already for the 16-Bit increment, therefore we can't increment PC in the cycle before
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void DEC_r16(Register hi, Register lo) {
        switch(cycle) {
            case 2 -> {
                lo.dec();
                if(lo.getValue() == 0xFF) {
                    hi.dec();
                }
            }
            case 3 -> {
                // Cycle punishment due to IDU unit being used already for the 16-Bit increment, therefore we can't increment PC in the cycle before
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void DEC_SP() {
        switch(cycle) {
            case 2 -> {
                reg.SP.dec();
            }
            case 3 -> {
                // Cycle punishment due to IDU unit being used already for the 16-Bit increment, therefore we can't increment PC in the cycle before
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void ADD_HL_r16(Register hi, Register lo) {
        switch(cycle) {
            case 2 -> {
                int a = reg.L.getValue();
                int b = lo.getValue();
                int c = 0;
                int res = a + b;
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(halfCarryEight(a, b, c, res));
                reg.F.setCarry(res > 0xFF);
                reg.L.setValue(res);
            }
            case 3 -> {
                int a = reg.H.getValue();
                int b = hi.getValue();
                int c = reg.F.isCarry() ? 1 : 0;
                int res = a + b + c;
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(halfCarryEight(a, b, c, res));
                reg.F.setCarry(res > 0xFF);
                reg.H.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void ADD_HL_SP() {
        switch(cycle) {
            case 2 -> {
                int a = reg.L.getValue();
                int b = reg.SP.getValue() & 0xFF;
                int c = 0;
                int res = a + b;
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(halfCarryEight(a, b, c, res));
                reg.F.setCarry(res > 0xFF);
                reg.L.setValue(res);
            }
            case 3 -> {
                int a = reg.H.getValue();
                int b = reg.SP.getValue() >> 8;
                int c = reg.F.isCarry() ? 1 : 0;
                int res = a + b + c;
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(halfCarryEight(a, b, c, res));
                reg.F.setCarry(res > 0xFF);
                reg.H.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void ADD_SP_i8() {
        switch(cycle) {
            case 2 -> {
                data = read(reg.PC.getAndInc());
            }
            case 3 -> {
                int a = reg.SP.getValue() & 0xFF;
                int b = (byte) data;
                int c = 0;
                int res = a + b;
                reg.F.setZero(false);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(halfCarryEight(a, b, c, res));
                reg.F.setCarry((reg.SP.getValue() & 0xFF) + data > 0xFF);
                if(res > 0xFF) {
                    // Here we use addr instead of creating an extra variable, because we need to know if we have to subtract or add 1
                    addr = 1;
                } else if(res < 0x00) {
                    addr = -1;
                } else {
                    addr = 0;
                }
                data = res & 0xFF;
            }
            case 4 -> {
                data |= (reg.SP.getValue() & 0xFF00) + (addr << 8);
            }
            case 5 -> {
                reg.SP.setValue(data);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void ADD_HL_SPpi8() {
        switch(cycle) {
            case 2 -> {
                data = read(reg.PC.getAndInc());
            }
            case 3 -> {
                int a = reg.SP.getValue() & 0xFF;
                int b = (byte) data;
                int c = 0;
                int res = a + b;
                reg.F.setZero(false);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(halfCarryEight(a, data, c, res));
                reg.F.setCarry((reg.SP.getValue() & 0xFF) + data > 0xFF);
                if(res > 0xFF) {
                    addr = 1;
                } else if(res < 0x00) {
                    addr = -1;
                } else {
                    addr = 0;
                }
                data = res & 0xFF;
                reg.L.setValue(res);
            }
            case 4 -> {
                data |= (reg.SP.getValue() & 0xFF00) + (addr << 8);
                reg.H.setValue(data >> 8);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    // Branches

    private void JP_u16() {
        switch(cycle) {
            case 2 -> {
                addr = read(reg.PC.getAndInc());
            }
            case 3 -> {
                addr |= read(reg.PC.getAndInc()) << 8;
            }
            case 4 -> {
                // IDLE
            }
            case 5 -> {
                reg.PC.setValue(addr);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void JP_HL() {
        switch(cycle) {
            case 2 -> {
                reg.PC.setValue(reg.getHLValue());
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void JP_u16_cond(boolean cond) {
        switch(cycle) {
            case 2 -> {
                addr = read(reg.PC.getAndInc());
            }
            case 3 -> {
                addr |= read(reg.PC.getAndInc()) << 8;
            }
            case 4 -> {
                if(cond) {
                    // IDLE
                } else {
                    fetch();
                }
            }
            case 5 -> {
                reg.PC.setValue(addr);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void JR_i8() {
        switch(cycle) {
            case 2 -> {
                data = read(reg.PC.getAndInc());
            }
            case 3 -> {
                // IDLE, or more specifically some ALU IDU magic, explained in https://gist.github.com/SonoSooS/c0055300670d678b5ae8433e20bea595#jr-e8 -> Not necessary to emulate in this detail
            }
            case 4 -> {
                reg.PC.setValue(reg.PC.getValue() + (byte) data);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void JR_i8_cond(boolean cond) {
        switch(cycle) {
            case 2 -> {
                data = read(reg.PC.getAndInc());
            }
            case 3 -> {
                if(cond) {
                    // IDLE, or more specifically some ALU IDU magic, explained in https://gist.github.com/SonoSooS/c0055300670d678b5ae8433e20bea595#jr-e8 -> Not necessary to emulate in this detail
                } else {
                    fetch();
                }
            }
            case 4 -> {
                reg.PC.setValue(reg.PC.getValue() + (byte) data);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void CALL_u16() {
        switch(cycle) {
            case 2 -> {
                addr = read(reg.PC.getAndInc());
            }
            case 3 -> {
                addr |= read(reg.PC.getAndInc()) << 8;
            }
            case 4 -> {
                // BUS IDLE
                reg.SP.dec();
            }
            case 5 -> {
                write(reg.SP.getAndDec(), reg.PC.getValue() >> 8);
            }
            case 6 -> {
                write(reg.SP.getValue(), reg.PC.getValue() & 0xFF);
            }
            case 7 -> {
                reg.PC.setValue(addr);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void CALL_u16_cond(boolean cond) {
        switch(cycle) {
            case 2 -> {
                addr = read(reg.PC.getAndInc());
            }
            case 3 -> {
                addr |= read(reg.PC.getAndInc()) << 8;
            }
            case 4 -> {
                if(cond) {
                    // BUS IDLE
                    reg.SP.dec();
                } else {
                    fetch();
                }
            }
            case 5 -> {
                write(reg.SP.getAndDec(), reg.PC.getValue() >> 8);
            }
            case 6 -> {
                write(reg.SP.getValue(), reg.PC.getValue() & 0xFF);
            }
            case 7 -> {
                reg.PC.setValue(addr);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void RET() {
        switch(cycle) {
            case 2 -> {
                addr = read(reg.SP.getAndInc());
            }
            case 3 -> {
                addr |= read(reg.SP.getAndInc()) << 8;
            }
            case 4 -> {
                // BUS IDLE
                reg.PC.setValue(addr);
            }
            case 5 -> {
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void RET_cond(boolean cond) {
        switch(cycle) {
            case 2 -> {
                // IDLE -> Also more happening here, as stated in https://gist.github.com/SonoSooS/c0055300670d678b5ae8433e20bea595#ret-cc -> Also not necessary to implement
            }
            case 3 -> {
                if(cond) {
                    addr = read(reg.SP.getAndInc());
                } else {
                    fetch();
                }
            }
            case 4 -> {
                addr |= read(reg.SP.getAndInc()) << 8;
            }
            case 5 -> {
                // BUS IDLE
                reg.PC.setValue(addr);
            }
            case 6 -> {
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void RETI() {
        switch(cycle) {
            case 2 -> {
                addr = read(reg.SP.getAndInc());
            }
            case 3 -> {
                addr |= read(reg.SP.getAndInc()) << 8;
            }
            case 4 -> {
                // BUS IDLE
                if(!ime && imeScheduleCount == 0) {
                    imeScheduleCount = 2;
                }
                reg.PC.setValue(addr);
            }
            case 5 -> {
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void RST(int vec) {
        switch(cycle) {
            case 2 -> {
                // BUS IDLE
                reg.SP.dec();
            }
            case 3 -> {
                write(reg.SP.getAndDec(), reg.PC.getValue() >> 8);
            }
            case 4 -> {
                write(reg.SP.getValue(), reg.PC.getValue() & 0xFF);
            }
            case 5 -> {
                reg.PC.setValue(vec);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    // Misc

    private void NOP() {
        switch(cycle) {
            case 2 -> {
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void EI() {
        switch(cycle) {
            case 2 -> {
                if(!ime && imeScheduleCount == 0) {
                    imeScheduleCount = 2;
                }
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void DI() {
        switch(cycle) {
            case 2 -> {
                ime = false;
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void HALT() {
        switch(cycle) {
            case 2 -> {
                if(ime) {
                    halt = true;
                } else {
                    int interrupts = read(0xFFFF) & read(0xFF0F) & 0x1F;
                    if(interrupts == 0) {
                        halt = true;
                    } else {
                        haltBug = true;
                    }
                }
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void STOP() {
        switch(cycle) {
            case 2 -> {
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void CB() {
        switch(cycle) {
            case 2 -> {
                cbFetch();
            }
        }
    }

    private void ISR(int vec) {
        switch(cycle) {
            case 2 -> {
                reg.PC.dec();
            }
            case 3 -> {
                reg.SP.dec();
            }
            case 4 -> {
                write(reg.SP.getAndDec(), reg.PC.getValue() >> 8);
            }
            case 5 -> {
                write(reg.SP.getValue(), reg.PC.getValue() & 0xFF);
            }
            case 6 -> {
                reg.PC.setValue(vec);
                fetch();
            }
        }
    }

    private void INVALID() {
        throw new RuntimeException("Invalid instruction - Opcode " + BitUtils.toHex(opCode) + " is not supported!");
    }

    // 8-Bit Bits

    private void RLCA() {
        switch(cycle) {
            case 2 -> {
                int val = reg.A.getValue();
                int bit = (val & BitUtils.M_SEVEN) >> 7;
                int res = ((val << 1) & 0xFF) | bit;
                reg.F.setZero(false);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(false);
                reg.F.setCarry(bit != 0);
                reg.A.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void RLC_r8(Register r) {
        switch(cycle) {
            case 2 -> {
                int val = r.getValue();
                int bit = (val & BitUtils.M_SEVEN) >> 7;
                int res = ((val << 1) & 0xFF) | bit;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(false);
                reg.F.setCarry(bit != 0);
                r.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void RLC__HL_() {
        switch(cycle) {
            case 2 -> {
                data = read(reg.getHLValue());
            }
            case 3 -> {
                int val = data;
                int bit = (val & BitUtils.M_SEVEN) >> 7;
                int res = ((val << 1) & 0xFF) | bit;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(false);
                reg.F.setCarry(bit != 0);
                write(reg.getHLValue(), res);
            }
            case 4 -> {
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void RRCA() {
        switch(cycle) {
            case 2 -> {
                int val = reg.A.getValue();
                int bit = val & BitUtils.M_ZERO;
                int res = ((val >> 1) & 0xFF) | (bit << 7);
                reg.F.setZero(false);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(false);
                reg.F.setCarry(bit != 0);
                reg.A.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void RRC_r8(Register r) {
        switch(cycle) {
            case 2 -> {
                int val = r.getValue();
                int bit = val & BitUtils.M_ZERO;
                int res = ((val >> 1) & 0xFF) | (bit << 7);
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(false);
                reg.F.setCarry(bit != 0);
                r.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void RRC__HL_() {
        switch(cycle) {
            case 2 -> {
                data = read(reg.getHLValue());
            }
            case 3 -> {
                int val = data;
                int bit = val & BitUtils.M_ZERO;
                int res = ((val >> 1) & 0xFF) | (bit << 7);
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(false);
                reg.F.setCarry(bit != 0);
                write(reg.getHLValue(), res);
            }
            case 4 -> {
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void RLA() {
        switch(cycle) {
            case 2 -> {
                int val = reg.A.getValue();
                int bit = (val & BitUtils.M_SEVEN) >> 7;
                int carry = reg.F.isCarry() ? 1 : 0;
                int res = ((val << 1) & 0xFF) | carry;
                reg.F.setZero(false);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(false);
                reg.F.setCarry(bit != 0);
                reg.A.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void RL_r8(Register r) {
        switch(cycle) {
            case 2 -> {
                int val = r.getValue();
                int bit = (val & BitUtils.M_SEVEN) >> 7;
                int carry = reg.F.isCarry() ? 1 : 0;
                int res = ((val << 1) & 0xFF) | carry;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(false);
                reg.F.setCarry(bit != 0);
                r.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void RL__HL_() {
        switch(cycle) {
            case 2 -> {
                data = read(reg.getHLValue());
            }
            case 3 -> {
                int val = data;
                int bit = (val & BitUtils.M_SEVEN) >> 7;
                int carry = reg.F.isCarry() ? 1 : 0;
                int res = ((val << 1) & 0xFF) | carry;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(false);
                reg.F.setCarry(bit != 0);
                write(reg.getHLValue(), res);
            }
            case 4 -> {
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void RRA() {
        switch(cycle) {
            case 2 -> {
                int val = reg.A.getValue();
                int bit = val & BitUtils.M_ZERO;
                int carry = reg.F.isCarry() ? 1 : 0;
                int res = ((val >> 1) & 0xFF) | (carry << 7);
                reg.F.setZero(false);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(false);
                reg.F.setCarry(bit != 0);
                reg.A.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void RR_r8(Register r) {
        switch(cycle) {
            case 2 -> {
                int val = r.getValue();
                int bit = val & BitUtils.M_ZERO;
                int carry = reg.F.isCarry() ? 1 : 0;
                int res = ((val >> 1) & 0xFF) | (carry << 7);
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(false);
                reg.F.setCarry(bit != 0);
                r.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void RR__HL_() {
        switch(cycle) {
            case 2 -> {
                data = read(reg.getHLValue());
            }
            case 3 -> {
                int val = data;
                int bit = val & BitUtils.M_ZERO;
                int carry = reg.F.isCarry() ? 1 : 0;
                int res = ((val >> 1) & 0xFF) | (carry << 7);
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(false);
                reg.F.setCarry(bit != 0);
                write(reg.getHLValue(), res);
            }
            case 4 -> {
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void SLA_r8(Register r) {
        switch(cycle) {
            case 2 -> {
                int val = r.getValue();
                int bit = (val & BitUtils.M_SEVEN) >> 7;
                int res = (val << 1) & 0xFF;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(false);
                reg.F.setCarry(bit != 0);
                r.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void SLA__HL_() {
        switch(cycle) {
            case 2 -> {
                data = read(reg.getHLValue());
            }
            case 3 -> {
                int val = data;
                int bit = (val & BitUtils.M_SEVEN) >> 7;
                int res = (val << 1) & 0xFF;
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(false);
                reg.F.setCarry(bit != 0);
                write(reg.getHLValue(), res);
            }
            case 4 -> {
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void SRA_r8(Register r) {
        switch(cycle) {
            case 2 -> {
                int val = r.getValue();
                int bit = val & BitUtils.M_ZERO;
                int last = (val & BitUtils.M_SEVEN) >> 7;
                int res = ((val >> 1) & 0xFF) | (last << 7);
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(false);
                reg.F.setCarry(bit != 0);
                r.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void SRA__HL_() {
        switch(cycle) {
            case 2 -> {
                data = read(reg.getHLValue());
            }
            case 3 -> {
                int val = data;
                int bit = val & BitUtils.M_ZERO;
                int last = (val & BitUtils.M_SEVEN) >> 7;
                int res = ((val >> 1) & 0xFF) | (last << 7);
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(false);
                reg.F.setCarry(bit != 0);
                write(reg.getHLValue(), res);
            }
            case 4 -> {
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void SRL_r8(Register r) {
        switch(cycle) {
            case 2 -> {
                int val = r.getValue();
                int bit = val & BitUtils.M_ZERO;
                int res = ((val >> 1) & 0xFF);
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(false);
                reg.F.setCarry(bit != 0);
                r.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void SRL__HL_() {
        switch(cycle) {
            case 2 -> {
                data = read(reg.getHLValue());
            }
            case 3 -> {
                int val = data;
                int bit = val & BitUtils.M_ZERO;
                int res = ((val >> 1) & 0xFF);
                reg.F.setZero((res & 0xFF) == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(false);
                reg.F.setCarry(bit != 0);
                write(reg.getHLValue(), res);
            }
            case 4 -> {
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void BIT_r8(Register r, int bit) {
        switch(cycle) {
            case 2 -> {
                int res = r.getValue() & (1 << bit);
                reg.F.setZero(res == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(true);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void BIT__HL_(int bit) {
        switch(cycle) {
            case 2 -> {
                data = read(reg.getHLValue());
            }
            case 3 -> {
                int res = data & (1 << bit);
                reg.F.setZero(res == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(true);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void SET_r8(Register r, int bit) {
        switch(cycle) {
            case 2 -> {
                int res = r.getValue() | (1 << bit);
                r.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void SET__HL_(int bit) {
        switch(cycle) {
            case 2 -> {
                data = read(reg.getHLValue());
            }
            case 3 -> {
                int res = data | (1 << bit);
                write(reg.getHLValue(), res);
            }
            case 4 -> {
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void RES_r8(Register r, int bit) {
        switch(cycle) {
            case 2 -> {
                int res = r.getValue() & ~(1 << bit);
                r.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void RES__HL_(int bit) {
        switch(cycle) {
            case 2 -> {
                data = read(reg.getHLValue());
            }
            case 3 -> {
                int res = data & ~(1 << bit);
                write(reg.getHLValue(), res);
            }
            case 4 -> {
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void SWAP_r8(Register r) {
        switch(cycle) {
            case 2 -> {
                int hi = r.getValue() >> 4;
                int lo = r.getValue() & 0x0F;
                int res = (lo << 4) | hi;
                reg.F.setZero(res == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(false);
                reg.F.setCarry(false);
                r.setValue(res);
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    private void SWAP__HL_() {
        switch(cycle) {
            case 2 -> {
                data = read(reg.getHLValue());
            }
            case 3 -> {
                int hi = data >> 4;
                int lo = data & 0x0F;
                int res = (lo << 4) | hi;
                reg.F.setZero(res == 0);
                reg.F.setSubtraction(false);
                reg.F.setHalfCarry(false);
                reg.F.setCarry(false);
                write(reg.getHLValue(), res);
            }
            case 4 -> {
                fetch();
            }
            default -> throw new RuntimeException("Opcode " + BitUtils.toHex(opCode) + " does not have a cycle #" + cycle + "!");
        }
    }

    // Helpers

    private boolean halfCarryEight(int a, int b, int c, int res) {
        return ((a ^ b ^ c ^ res) & 0x10) != 0;
    }

    private void debugLog() {
        try {
            bw.write("A: " + BitUtils.toHex(reg.A.getValue()) + " F: " + BitUtils.toHex(reg.F.getValue()) + " B: " + BitUtils.toHex(reg.B.getValue()) + " C: " + BitUtils.toHex(reg.C.getValue()) + " D: " + BitUtils.toHex(reg.D.getValue()) + " E: " + BitUtils.toHex(reg.E.getValue()) + " H: " + BitUtils.toHex(reg.H.getValue()) + " L: " + BitUtils.toHex(reg.L.getValue()) + " SP: " + BitUtils.toHex(reg.SP.getValue()) + " PC: 00:" + BitUtils.toHex(reg.PC.getValue() - 1) + " (" + BitUtils.toHex(read(reg.PC.getValue() - 1)) + " " + BitUtils.toHex(read(reg.PC.getValue())) + " " + BitUtils.toHex(read(reg.PC.getValue() + 1)) + " " + BitUtils.toHex(read(reg.PC.getValue() + 2)) + ")\n");
            bw.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}