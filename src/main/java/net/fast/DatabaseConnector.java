package net.fast;

import com.google.gson.Gson;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.fast.packet.Packet;
import net.fast.packet.PacketListener;
import net.fast.packet.TestPacket;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseConnector {

    private HikariDataSource dataSource;

    private Gson gson;

    private Map<PacketListener<? extends Packet>,String> subscribed = new ConcurrentHashMap<>();

    public DatabaseConnector() {
        this.dataSource = new HikariDataSource();

        this.dataSource.setJdbcUrl("jdbc:mysql://shinkansen.proxy.rlwy.net:32191/railway?useUnicode=yes&characterEncoding=UTF-8&useSSL=false");
        this.dataSource.setUsername("root");
        this.dataSource.setPassword("cNLmrpftEhjKJvilOZOJwGHUQlUrgCYn");

        HikariConfig config = new HikariConfig();
        config.setMaximumPoolSize(20);
        config.setConnectionTimeout(300000);
        config.setConnectionTimeout(120000);
        config.setLeakDetectionThreshold(300000);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");

        config.setDataSource(this.dataSource);

        this.gson = new Gson();

        if(!this.dataSource.isClosed()) {
            this.executeUpdate("CREATE TABLE IF NOT EXISTS packets (id INT PRIMARY KEY AUTO_INCREMENT, packet TEXT, channel VARCHAR(255))");

            new Thread(this::receivePackets).start();
        }
    }


    private void receivePackets() {
        try (Connection connection = this.dataSource.getConnection()) {
            String sqlQuery = "SELECT * FROM packets";

            while (true) {
                try (Statement statement = connection.createStatement();
                     ResultSet resultSet = statement.executeQuery(sqlQuery)) {

                    while (resultSet.next()) {
                        String channel = resultSet.getString("channel");

                        TestPacket packet = this.gson.fromJson(resultSet.getString("packet"), TestPacket.class);

                        for (PacketListener packetListener : this.subscribed.keySet()) {
                            if (packetListener.getPacketClass().isAssignableFrom(packet.getClass())) {
                                packetListener.message(channel, packet);
                            }
                        }

                        PreparedStatement deleteStatment = connection.prepareStatement("DELETE FROM packets WHERE packet=?");

                        deleteStatment.setString(1, this.gson.toJson(packet));

                        deleteStatment.executeUpdate();

                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Błąd podczas komunikacji z bazą danych:");
            e.printStackTrace();
        }
    }

    public void publish(String channel, Packet packet) {
        String sql = "INSERT INTO packets (packet, channel) VALUES (?, ?)";

        try (Connection con = this.dataSource.getConnection();
             PreparedStatement statement = con.prepareStatement(sql)) {

            statement.setString(1, this.gson.toJson(packet));
            statement.setString(2, channel);

            statement.executeUpdate();
        } catch (SQLException e) {
            Logger.getAnonymousLogger().log(Level.SEVERE, "Update failed", e);
        }
    }

    public void subscribe(String channel, PacketListener<? extends Packet> packetListener) {
        if(this.subscribed.containsKey(packetListener)) {
            Logger.getAnonymousLogger().log(Level.SEVERE,"aha");
            return;
        }

        this.subscribed.put(packetListener,channel);

    }

    public void executeUpdate(String sql) {
        try (Connection con = this.dataSource.getConnection();
             PreparedStatement statement = con.prepareStatement(sql)) {

            statement.executeUpdate();

        } catch (SQLException e) {
            Logger.getAnonymousLogger().log(Level.SEVERE, "Update failed", e);
        }
    }
}
