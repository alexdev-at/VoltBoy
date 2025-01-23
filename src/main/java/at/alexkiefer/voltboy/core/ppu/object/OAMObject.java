package at.alexkiefer.voltboy.core.ppu.object;

public class OAMObject {

    private final int x;
    private final int y;
    private final int tileIndex;
    private final OAMObjectAttributes attributes;

    public OAMObject(int x, int y, int tileIndex, OAMObjectAttributes attributes) {
        this.x = x;
        this.y = y;
        this.tileIndex = tileIndex;
        this.attributes = attributes;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }


    public int getTileIndex() {
        return tileIndex;
    }

    public OAMObjectAttributes getAttributes() {
        return attributes;
    }

}
