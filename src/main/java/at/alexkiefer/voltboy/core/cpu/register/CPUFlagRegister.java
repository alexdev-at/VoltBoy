package at.alexkiefer.voltboy.core.cpu.register;

import at.alexkiefer.voltboy.util.BitMasks;

public class CPUFlagRegister extends CPURegister {

    public CPUFlagRegister(int value) {
        super(CPURegisterWidth.EIGHT_BIT, value);
    }

    public void setZero(boolean condition) {
        if(condition) {
            value |= BitMasks.SEVEN;
        } else {
            value &= ~BitMasks.SEVEN;
        }
    }

    public boolean isZero() {
        return (value & BitMasks.SEVEN) != 0;
    }

    public void setSubtraction(boolean condition) {
        if(condition) {
            value |= BitMasks.SIX;
        } else {
            value &= ~BitMasks.SIX;
        }
    }

    public boolean isSubtraction() {
        return (value & BitMasks.SIX) != 0;
    }

    public void isHalfCarry(boolean condition) {
        if(condition) {
            value |= BitMasks.FIVE;
        } else {
            value &= ~BitMasks.FIVE;
        }
    }

    public boolean isHalfCarry() {
        return (value & BitMasks.FIVE) != 0;
    }

    public void setCarry(boolean condition) {
        if(condition) {
            value |= BitMasks.FOUR;
        } else {
            value &= ~BitMasks.FOUR;
        }
    }

    public boolean isCarry() {
        return (value & BitMasks.FOUR) != 0;
    }

}
