package at.alexkiefer.voltboy.components.cpu.registers;

public class Registers {

    public final Register A;
    public final FlagRegister F;
    public final Register B;
    public final Register C;
    public final Register D;
    public final Register E;
    public final Register H;
    public final Register L;

    public final Register W;
    public final Register Z;

    public final Register PC;
    public final Register SP;

    public Registers() {

        A = new Register(RegisterType.EIGHT_BIT, 0x01);
        F = new FlagRegister(0xB0);
        B = new Register(RegisterType.EIGHT_BIT, 0x00);
        C = new Register(RegisterType.EIGHT_BIT, 0x13);
        D = new Register(RegisterType.EIGHT_BIT, 0x00);
        E = new Register(RegisterType.EIGHT_BIT, 0xD8);
        H = new Register(RegisterType.EIGHT_BIT, 0x01);
        L = new Register(RegisterType.EIGHT_BIT, 0x4D);

        W = new Register(RegisterType.EIGHT_BIT, 0x00);
        Z = new Register(RegisterType.EIGHT_BIT, 0x00);

        PC = new Register(RegisterType.SIXTEEN_BIT, 0x0100);
        SP = new Register(RegisterType.SIXTEEN_BIT, 0xFFFE);

    }

    public void setHLValue(int value) {
        value &= 0xFFFF;
        H.setValue(value >> 8);
        L.setValue(value & 0xFF);
    }

    public int getHLValue() {
        return (H.getValue() << 8) | L.getValue();
    }

    public void setWZValue(int value) {
        value &= 0xFFFF;
        W.setValue(value >> 8);
        Z.setValue(value & 0xFF);
    }

    public int getWZValue() {
        return (W.getValue() << 8) |Z.getValue();
    }

}