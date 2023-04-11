package ru.clevertec.servlet;

import org.yaml.snakeyaml.Yaml;
import ru.clevertec.servlet.exception.DBException;
import ru.clevertec.servlet.exception.ServerException;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static ru.clevertec.servlet.Constants.*;

@WebListener
public class AppInitializer implements ServletContextListener {
    private final String YamlInitFile = "appt.yaml";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext sc = sce.getServletContext();
        SQLPoolConnection pool = initDB(sc);
        sc.setAttribute(CONNECTION, pool);
    }

    private SQLPoolConnection initDB(ServletContext sc) throws ServerException {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(YamlInitFile);
        Yaml yaml = new Yaml();
        Map<String, Object> obj = yaml.load(is);
        Map<String, Object> db = (Map<String, Object>) obj.get("db");
        if (Objects.isNull(db)) {
            throw new ServerException("Data for DB initialize is absent");
        }
        SQLPoolConnection pool;
        Connection con;
        try {
            pool = SQLPoolConnection.createPool(db);
            con = pool.getConnection();  //getConnection(db);
            String sql = "";
            try (Statement st = con.createStatement()) {
                if (!Objects.isNull(db.get(SCHEMA))) {
                    sql = getSqlStingFromFile(db.get(SCHEMA).toString());
                    st.execute(sql);
                    if (!Objects.isNull(db.get(DATA))) {
                        sql = getSqlStingFromFile(db.get(DATA).toString());
                        st.execute(sql);
                    }
                }
            } catch (SQLException ex) {
                throw new ServerException("Error of DB initializer", ex);
            }catch (URISyntaxException | IOException ex) {
                throw new ServerException("Can't read .sql files", ex);
            }
            finally {
               pool.closeConnection(con);
            }
            return pool;

        } catch (DBException ex) {
            throw new ServerException("Error of pool connection creating", ex);
        }
    }

    private Connection getConnection(Map<String, Object> param) {
        Connection con = null;
        try {
            Class.forName(param.get(DRIVER).toString());
            String url = param.get(URL).toString();
            String user = param.get(USER).toString();
            String password = param.get(PASSWORD).toString();
            con = DriverManager.getConnection(url, user, password);
        } catch (SQLException | ClassNotFoundException e) {
            throw new ServerException("Can't get Connection to DB", e);
        }
        return con;
    }

    private static String getSqlStingFromFile(String filename) throws URISyntaxException, IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Path path = Paths.get(classLoader.getResource(filename).toURI());
        String s = Files.lines(path).collect(Collectors.joining());
        return s;
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        Connection con = (Connection) sce.getServletContext().getAttribute("Connection");
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
