package net.fast.packet;

public class TestPacket extends Packet {

    private String name;

    public TestPacket() {

    }
    public TestPacket(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
