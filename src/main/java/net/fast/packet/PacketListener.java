package net.fast.packet;

public abstract class PacketListener<T extends Packet> {

    private Class<T> packetClass;

    public PacketListener(Class<T> packetClass) {
        this.packetClass = packetClass;
    }

    public abstract void handle(T packet);

    public void message(String channel, Packet packet) {
        if (packet.getClass().isAssignableFrom(packetClass)) {
            this.handle((T) packet);
        }
    }

    public Class<T> getPacketClass() {
        return packetClass;
    }
}
