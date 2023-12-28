package at.alexkiefer.voltboy.components.cpu.registers;

public class Register {

    protected final int mask;
    protected int value;

    public Register(RegisterType type, int value) {
        mask = type == RegisterType.EIGHT_BIT ? 0xFF : 0xFFFF;
        this.value = value & mask;
    }

    public void setValue(int value) {
        this.value = value & mask;
    }

    public int getValue() {
        return value;
    }

    public void inc() {
        value = (value + 1) & mask;
    }

    public void dec() {
        value = (value - 1) & mask;
    }

    public int getAndInc() {
        int ret = value;
        value = (value + 1) & mask;
        return ret;
    }

    public int getAndDec() {
        int ret = value;
        value = (value - 1) & mask;
        return ret;
    }

}