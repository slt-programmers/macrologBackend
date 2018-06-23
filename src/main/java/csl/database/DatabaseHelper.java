package csl.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.*;
import java.util.Properties;

/**
 * Created by Carmen on 17-3-2018.
 */
public class DatabaseHelper implements DataSource{

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseHelper.class);

    private static DatabaseHelper instance;
    private static String CONNECTION_URL;
    private static String DB_USER;
    private static String DB_PASSWORD;

    private DatabaseHelper() {
    }

    public static DatabaseHelper getInstance() {
        if (instance == null) {

            Properties p = new Properties();
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            InputStream stream = loader.getResourceAsStream("application.properties");
            try {
                p.load(stream);
            } catch (IOException e) {
                e.printStackTrace();
            }
            CONNECTION_URL = p.getProperty("spring.datasource.url");
            DB_USER = p.getProperty("spring.datasource.username");
            DB_PASSWORD = p.getProperty("spring.datasource.password");
            instance = new DatabaseHelper();
        }
        return instance;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return getConnection(DB_USER ,DB_PASSWORD);
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
