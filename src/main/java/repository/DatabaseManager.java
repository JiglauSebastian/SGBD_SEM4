package repository;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseManager {
    private static final Properties props = new Properties();

    static {
        try (InputStream input = DatabaseManager.class.getClassLoader().getResourceAsStream("bd.config")) {
            if (input == null) {
                System.out.println("Scuze, nu am putut gasi bd.config");
            } else {
                props.load(input);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                props.getProperty("db.url"),
                props.getProperty("db.user"),
                props.getProperty("db.pass")
        );
    }

    public static String getProperty(String key) {
        return props.getProperty(key);
    }
}