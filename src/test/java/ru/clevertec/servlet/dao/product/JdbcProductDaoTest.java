package ru.clevertec.servlet.dao.product;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.yaml.snakeyaml.Yaml;
import ru.clevertec.servlet.dao.SimpleDao;
import ru.clevertec.servlet.enities.Product;
import ru.clevertec.servlet.exception.ServerException;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.clevertec.servlet.Constants.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JdbcProductDaoTest {
    private static SimpleDao<Product> dao;
    @BeforeAll
    public static void createConnection() throws Exception {
        Connection con;
        ClassLoader classLoader = JdbcProductDaoTest.class.getClassLoader();
        InputStream is = classLoader.getResourceAsStream("appt.yaml");
        Yaml yaml = new Yaml();
        Map<String, Object> obj = yaml.load(is);
        Map<String, Object> db = (Map<String, Object>) obj.get("db");
        if (Objects.isNull(db)) {
            throw new ServerException("Data for DB initialize is absent");
        }
        try {
            Class.forName(db.get(DRIVER).toString());
            String url = db.get(URL).toString();
            String user = db.get(USER).toString();
            String password = db.get(PASSWORD).toString();
            con = DriverManager.getConnection(url, user, password);
            String sql = "";
            try (Statement st = con.createStatement()) {
                if (!Objects.isNull(db.get(SCHEMA))) {
                    Path path = Paths.get(classLoader.getResource(db.get(SCHEMA).toString()).toURI());
                    sql = Files.lines(path).collect(Collectors.joining());
                    st.execute(sql);
                    if (!Objects.isNull(db.get(DATA))) {
                        path = Paths.get(classLoader.getResource(db.get(DATA).toString()).toURI());
                        sql = Files.lines(path).collect(Collectors.joining());
                        st.execute(sql);
                    }
                }
            }
            dao = new JdbcProductDao(con);
        } catch (Exception e) {
            System.err.println("Can't init test DB");
            throw new Exception(e);
        }
    }

    @Test
    @Order(1)
    void getById() {
        Product product = dao.getById(1).orElseThrow();
        Product productExp = new Product(1, "Milk", 160, 0);
        Assertions.assertThat(product).isEqualTo(productExp);
    }

    @Test
    @Order(2)
    void getAll() {
        List<Product> list = dao.getAll(-1, 0);
        Assertions.assertThat(list).hasSize(2);
    }

    @Test
    @Order(3)
    void create() {
        Product productToCreate = new Product(0, "Icecream", 190, 0);
        Product product = dao.create(productToCreate);
        Product createdProduct = dao.getById(product.getId()).orElseThrow();

        Assertions.assertThat(createdProduct.getName()).isEqualTo(productToCreate.getName());
        Assertions.assertThat(createdProduct.getPrice()).isEqualTo(productToCreate.getPrice());
        Assertions.assertThat(createdProduct.getDiscountType()).isEqualTo(productToCreate.getDiscountType());
    }

    @Test
    @Order(4)
    void update() {
        Product productToChange = new Product(1, "Milk", 190, 0);
        dao.update(productToChange);
        Product product = dao.getById(1).orElseThrow();
        Assertions.assertThat(product).isEqualTo(productToChange);
    }

    @Test
    @Order(5)
    void deleteById() {
        dao.deleteById(1);
        Optional<Product> product = dao.getById(1);
        Assertions.assertThat(product.isEmpty()).isTrue();
    }
}