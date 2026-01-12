package com.brickroad.starcreator_webservice.database;

import com.brickroad.starcreator_webservice.database.repos.FactionRepo;

import java.sql.*;

public class StarCreatorDAO {

    private static final String DEV_IP = "10.0.0.67";
    private static final String PORT = "5432";
    private static final String DEV_USER = "dev";
    private static final String DEV_PWD = "dev";
    private static final String DEV_DBNAME = "starcreator_dev";

    private final FactionRepo factionRepo;

    public StarCreatorDAO(FactionRepo factionRepo) {
        this.factionRepo =  factionRepo;
    }

    public static void connect() throws SQLException {
        String url = "jdbc:postgresql://" + DEV_IP + ":" + PORT + "/" + DEV_DBNAME;

        try (Connection conn = DriverManager.getConnection(url, DEV_USER, DEV_PWD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT version()")) {

            while (rs.next()) {
                System.out.println(rs.getString(1));
            }
        }
    }

}
