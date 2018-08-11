package csl.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
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
            String hostname = null;
            try {
                hostname = InetAddress.getLocalHost().getHostName();
                LOGGER.debug(hostname);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            Properties p = getProperties(hostname);
            CONNECTION_URL = getFromEnvOrPropertyFile("spring.datasource.url",p);
            DB_USER = getFromEnvOrPropertyFile("spring.datasource.username",p);
            DB_PASSWORD = getFromEnvOrPropertyFile("spring.datasource.password",p);
            instance = new DatabaseHelper();
        }
        return instance;
    }
    private static String getFromEnvOrPropertyFile(String property, Properties p) {
        String fromEnvironment = System.getenv(property);
        LOGGER.debug("fromEnvironment = " + property + " : " + fromEnvironment);
        return (fromEnvironment == null || fromEnvironment.equals(""))?p.getProperty(property):fromEnvironment;
    }

    private static Properties getProperties(String hostname) {
        Properties p = new Properties();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        String namePropertiesFile = "application.properties";
        if ("LAPTOP-HPA3TJNH".equals(hostname)) {
//            namePropertiesFile = "application-arjan.properties";
                namePropertiesFile = "application-heroku.properties";
        }

        InputStream stream = loader.getResourceAsStream(namePropertiesFile);
        try {
            p.load(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return p;
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
