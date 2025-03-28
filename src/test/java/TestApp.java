import net.fast.DatabaseConnector;
import net.fast.packet.TestPacket;

public class TestApp {

    public static void main(String[] args) {
        DatabaseConnector databaseConnector = new DatabaseConnector();

        databaseConnector.subscribe("test",new TestPacketListener());

        databaseConnector.publish("test",new TestPacket("morda"));
    }
}
