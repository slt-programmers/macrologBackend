package slt.database;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import slt.config.DatabaseConfig;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

/**
 * Created by Carmen on 17-3-2018.
 */
@Slf4j
@AutoConfigureBefore(DatabaseConfig.class)
@EnableConfigurationProperties(DatabaseConfig.class)
public class DatabaseHelper implements DataSource {

    private String CONNECTION_URL;
    private String DB_USER;
    private String DB_PASSWORD;

    public DatabaseHelper(DatabaseConfig properties) {
        CONNECTION_URL = properties.getUrl();
        DB_USER = properties.getUsername();
        DB_PASSWORD = properties.getPassword();
        if (CONNECTION_URL == null || DB_USER == null || DB_PASSWORD == null) {
            throw new RuntimeException("Environment variables not set. Please use a different profile or set the environment variables");
        }
        log.debug(properties.toString());
    }

    @Override
    public Connection getConnection() throws SQLException {

        return getConnection(DB_USER, DB_PASSWORD);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return DriverManager.getConnection(CONNECTION_URL, username, password);
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {

    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {

    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return 0;
    }

    @Override
    public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }
}

