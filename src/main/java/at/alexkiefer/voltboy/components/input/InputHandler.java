package at.alexkiefer.voltboy.components.input;

import at.alexkiefer.voltboy.ConnectedInternal;
import at.alexkiefer.voltboy.Tickable;
import at.alexkiefer.voltboy.VoltBoy;
import at.alexkiefer.voltboy.util.BitUtils;

public class InputHandler extends ConnectedInternal implements Tickable {

    private boolean joypadUp;
    private boolean joypadDown;
    private boolean joypadLeft;
    private boolean joypadRight;
    private boolean a;
    private boolean b;
    private boolean start;
    private boolean select;

    public InputHandler(VoltBoy gb) {
        super(gb);
        joypadUp = false;
        joypadDown = false;
        joypadLeft = false;
        joypadRight = false;
        a = false;
        b = false;
        start = false;
        select = false;
    }

    public void pressJoypadUp() {
        joypadUp = true;
    }

    public void pressJoypadDown() {
        joypadDown = true;
    }

    public void pressJoypadLeft() {
        joypadLeft = true;
    }

    public void pressJoypadRight() {
        joypadRight = true;
    }

    public void pressA() {
        a = true;
    }

    public void pressB() {
        b = true;
    }

    public void pressStart() {
        start = true;
    }

    public void pressSelect() {
        select = true;
    }

    public void releaseJoypadUp() {
        joypadUp = false;
    }

    public void releaseJoypadDown() {
        joypadDown = false;
    }

    public void releaseJoypadLeft() {
        joypadLeft = false;
    }

    public void releaseJoypadRight() {
        joypadRight = false;
    }

    public void releaseA() {
        a = false;
    }

    public void releaseB() {
        b = false;
    }

    public void releaseStart() {
        start = false;
    }

    public void releaseSelect() {
        select = false;
    }

    @Override
    public void tick() {

        int joypadReg = gb.getDataBus().readUnrestricted(0xFF00);
        int mask = joypadReg;

        if((joypadReg & BitUtils.M_FOUR) == 0) {

            if(joypadDown) {
                if((joypadReg & BitUtils.M_THREE) != 0) {
                    gb.getDataBus().writeUnrestricted(0xFF0F, gb.getDataBus().readUnrestricted(0xFF0F) | BitUtils.M_FOUR);
                }
                mask &= ~BitUtils.M_THREE;
            } else {
                mask |= BitUtils.M_THREE;
            }

            if(joypadUp) {
                if((joypadReg & BitUtils.M_TWO) != 0) {
                    gb.getDataBus().writeUnrestricted(0xFF0F, gb.getDataBus().readUnrestricted(0xFF0F) | BitUtils.M_FOUR);
                }
                mask &= ~BitUtils.M_TWO;
            } else {
                mask |= BitUtils.M_TWO;
            }

            if(joypadLeft) {
                if((joypadReg & BitUtils.M_ONE) != 0) {
                    gb.getDataBus().writeUnrestricted(0xFF0F, gb.getDataBus().readUnrestricted(0xFF0F) | BitUtils.M_FOUR);
                }
                mask &= ~BitUtils.M_ONE;
            } else {
                mask |= BitUtils.M_ONE;
            }

            if(joypadRight) {
                if((joypadReg & BitUtils.M_ZERO) != 0) {
                    gb.getDataBus().writeUnrestricted(0xFF0F, gb.getDataBus().readUnrestricted(0xFF0F) | BitUtils.M_FOUR);
                }
                mask &= ~BitUtils.M_ZERO;
            } else {
                mask |= BitUtils.M_ZERO;
            }

        }

        if((joypadReg & BitUtils.M_FIVE) == 0) {

            if(select) {
                if((joypadReg & BitUtils.M_THREE) != 0) {
                    gb.getDataBus().writeUnrestricted(0xFF0F, gb.getDataBus().readUnrestricted(0xFF0F) | BitUtils.M_FOUR);
                }
                mask &= ~BitUtils.M_THREE;
            } else {
                mask |= BitUtils.M_THREE;
            }

            if(start) {
                if((joypadReg & BitUtils.M_TWO) != 0) {
                    gb.getDataBus().writeUnrestricted(0xFF0F, gb.getDataBus().readUnrestricted(0xFF0F) | BitUtils.M_FOUR);
                }
                mask &= ~BitUtils.M_TWO;
            } else {
                mask |= BitUtils.M_TWO;
            }

            if(b) {
                if((joypadReg & BitUtils.M_ONE) != 0) {
                    gb.getDataBus().writeUnrestricted(0xFF0F, gb.getDataBus().readUnrestricted(0xFF0F) | BitUtils.M_FOUR);
                }
                mask &= ~BitUtils.M_ONE;
            } else {
                mask |= BitUtils.M_ONE;
            }

            if(a) {
                if((joypadReg & BitUtils.M_ZERO) != 0) {
                    gb.getDataBus().writeUnrestricted(0xFF0F, gb.getDataBus().readUnrestricted(0xFF0F) | BitUtils.M_FOUR);
                }
                mask &= ~BitUtils.M_ZERO;
            } else {
                mask |= BitUtils.M_ZERO;
            }

        }

        gb.getDataBus().writeUnrestricted(0xFF00, mask);

    }

}