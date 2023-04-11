package ru.clevertec.servlet;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.clevertec.servlet.exception.DBException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import static ru.clevertec.servlet.Constants.*;
import static ru.clevertec.servlet.Constants.PASSWORD;

public class SQLPoolConnection  {

    private static final Logger LOGGER = LoggerFactory.getLogger(SQLPoolConnection.class);
    private static SQLPoolConnection pool = null;
    private final BlockingQueue<Connection> connections;
    private int maxPoolSize = 2;
    private final List<Connection> occupiedPool;

    private SQLPoolConnection(Map<String, Object> param) throws SQLException, ClassNotFoundException {
        if (param.get(Constants.POOL_SIZE) != null) {
            maxPoolSize = (int) param.get(Constants.POOL_SIZE);
        }
        connections = new LinkedBlockingQueue<>(maxPoolSize);
        occupiedPool = new CopyOnWriteArrayList<>();
        Class.forName(param.get(DRIVER).toString());
        for (int i = 0; i < maxPoolSize; i++) {
            connections.add(create(param));
        }
    }

    public static SQLPoolConnection createPool(Map<String, Object> param) throws DBException {
        if (pool == null) {
            try {
                pool = new SQLPoolConnection(param);
            } catch (SQLException | ClassNotFoundException e) {
                LOGGER.error("Error on Connection creating", e);
                throw new DBException(e);
            }
        }
        return pool;
    }

    public static SQLPoolConnection getInstance() {
        return pool;
    }

    public Connection getConnection() {
        while (true) {
            try {
                Connection con = connections.take();
                occupiedPool.add(con);
                return con;
            } catch (InterruptedException ex) {
                LOGGER.warn("Interrupt during getting connection waiting from queue");
            }
        }
    }

    public void closeConnection(Connection conn) {
        if (conn != null && occupiedPool.remove(conn)) {
            connections.add(conn);
        } else {
            LOGGER.error("Getting not knowing connection");
        }
    }

    private Connection create(Map<String, Object> param) throws SQLException {
        Connection con = null;
        String url = param.get(URL).toString();
        String user = param.get(USER).toString();
        String password = param.get(PASSWORD).toString();
        con = DriverManager.getConnection(url, user, password);
        return con;
    }

    public void closePool() throws DBException {
        for (Connection con : connections) {
            try {
                con.close();
            } catch (SQLException e) {
                LOGGER.error("Error of connection closing at Pool destroy", e);
                throw new DBException(e);
            }
        }
    }
}
