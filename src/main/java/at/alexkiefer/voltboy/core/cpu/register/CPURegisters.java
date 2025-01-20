package at.alexkiefer.voltboy.core.cpu.register;

public class CPURegisters {

    public final CPURegister A;
    public final CPUFlagRegister F;
    public final CPURegister B;
    public final CPURegister C;
    public final CPURegister D;
    public final CPURegister E;
    public final CPURegister H;
    public final CPURegister L;
    public final CPURegister W;
    public final CPURegister Z;
    public final CPURegister SP;
    public final CPURegister PC;

    public CPURegisters() {
        A = new CPURegister(CPURegisterWidth.EIGHT_BIT, 0x00);
        F = new CPUFlagRegister(0x00);
        B = new CPURegister(CPURegisterWidth.EIGHT_BIT, 0x00);
        C = new CPURegister(CPURegisterWidth.EIGHT_BIT, 0x00);
        D = new CPURegister(CPURegisterWidth.EIGHT_BIT, 0x00);
        E = new CPURegister(CPURegisterWidth.EIGHT_BIT, 0x00);
        H = new CPURegister(CPURegisterWidth.EIGHT_BIT, 0x00);
        L = new CPURegister(CPURegisterWidth.EIGHT_BIT, 0x00);
        W = new CPURegister(CPURegisterWidth.EIGHT_BIT, 0x00);
        Z = new CPURegister(CPURegisterWidth.EIGHT_BIT, 0x00);
        SP = new CPURegister(CPURegisterWidth.SIXTEEN_BIT, 0x00);
        PC = new CPURegister(CPURegisterWidth.SIXTEEN_BIT, 0x00);
    }

    public void setAFValue(int value) {
        A.setValue(value >> 8);
        F.setValue(value & 0xFF);
    }

    public int getAFValue() {
        return (A.value << 8) | F.value;
    }

    public void setBCValue(int value) {
        B.setValue(value >> 8);
        C.setValue(value & 0xFF);
    }

    public int getBCValue() {
        return (B.value << 8) | C.value;
    }
    public void setDEValue(int value) {
        D.setValue(value >> 8);
        E.setValue(value & 0xFF);
    }

    public int getDEValue() {
        return (D.value << 8) | E.value;
    }
    public void setHLValue(int value) {
        H.setValue(value >> 8);
        L.setValue(value & 0xFF);
    }

    public int getHLValue() {
        return (H.value << 8) | L.value;
    }
    public void setWZValue(int value) {
        W.setValue(value >> 8);
        Z.setValue(value & 0xFF);
    }

    public int getWZValue() {
        return (W.value << 8) | Z.value;
    }

}
