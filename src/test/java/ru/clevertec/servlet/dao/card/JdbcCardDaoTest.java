package ru.clevertec.servlet.dao.card;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.yaml.snakeyaml.Yaml;
import ru.clevertec.servlet.dao.SimpleDao;

import ru.clevertec.servlet.enities.DiscountCard;
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
class JdbcCardDaoTest {
    private static SimpleDao<DiscountCard> dao;
    @BeforeAll
    public static void createConnection() throws Exception {
        Connection con;
        ClassLoader classLoader = JdbcCardDaoTest.class.getClassLoader();
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
            dao = new JdbcCardDao(con);
        } catch (Exception e) {
            System.err.println("Can't init test DB");
            throw new Exception(e);
        }
    }

    @Test
    @Order(1)
    void getById() {
        DiscountCard card = dao.getById(1234).orElseThrow();
        DiscountCard cardExp = new DiscountCard(1234, 5);
        Assertions.assertThat(card).isEqualTo(cardExp);
    }

    @Test
    @Order(2)
    void getAll() {
        List<DiscountCard> list = dao.getAll(-1, 0);
        Assertions.assertThat(list).hasSize(2);
    }

    @Test
    @Order(3)
    void create() {
        DiscountCard DiscountCardToCreate = new DiscountCard(0, 8);
        DiscountCard DiscountCard = dao.create(DiscountCardToCreate);
        DiscountCard createdDiscountCard = dao.getById(DiscountCard.getNumber()).orElseThrow();

        Assertions.assertThat(createdDiscountCard.getNumber()).isEqualTo(DiscountCardToCreate.getNumber());
        Assertions.assertThat(createdDiscountCard.getDiscount()).isEqualTo(DiscountCardToCreate.getDiscount());

    }

    @Test
    @Order(4)
    void update() {
        DiscountCard DiscountCardToChange = new DiscountCard(1235, 2);
        dao.update(DiscountCardToChange);
        DiscountCard DiscountCard = dao.getById(1235).orElseThrow();
        Assertions.assertThat(DiscountCard).isEqualTo(DiscountCardToChange);
    }

    @Test
    @Order(5)
    void deleteById() {
        dao.deleteById(1234);
        Optional<DiscountCard> DiscountCard = dao.getById(1234);
        Assertions.assertThat(DiscountCard.isEmpty()).isTrue();
    }
}