package at.alexkiefer.voltboy.components.ppu.objects;

public class OAMObject {

    private final int x;
    private final int y;
    private final int size;
    private final int tileIndex;
    private final OAMObjectAttributes attributes;

    public OAMObject(int x, int y, int size, int tileIndex, OAMObjectAttributes attributes) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.tileIndex = tileIndex;
        this.attributes = attributes;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getSize() {
        return size;
    }

    public int getTileIndex() {
        return tileIndex;
    }

    public OAMObjectAttributes getAttributes() {
        return attributes;
    }

}
