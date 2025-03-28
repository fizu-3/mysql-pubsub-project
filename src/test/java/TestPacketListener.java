import net.fast.packet.PacketListener;
import net.fast.packet.TestPacket;

public class TestPacketListener extends PacketListener<TestPacket> {
    public TestPacketListener() {
        super(TestPacket.class);
    }

    @Override
    public void handle(TestPacket packet) {
        System.out.println(packet.getName());
    }
}
