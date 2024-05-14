package roomescape.integration;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class AdminIntegrationTest {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    @LocalServerPort
    private int port;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String adminToken;

    @BeforeEach
    void init() {
        jdbcTemplate.update("delete from reservation");
        jdbcTemplate.update("ALTER TABLE reservation alter column id restart with 1");
        jdbcTemplate.update("delete from reservation_time");
        jdbcTemplate.update("ALTER TABLE reservation_time alter column id restart with 1");
        jdbcTemplate.update("insert into reservation_time(start_at) values('11:56')");
        jdbcTemplate.update("delete from THEME");
        jdbcTemplate.update("ALTER TABLE THEME alter column id restart with 1");
        jdbcTemplate.update("insert into THEME values ( 1,'a','a','a')");
        jdbcTemplate.update("delete from MEMBER");
        jdbcTemplate.update("ALTER TABLE MEMBER alter column id restart with 1");
        jdbcTemplate.update(
                "INSERT INTO MEMBER (NAME, ROLE, EMAIL, PASSWORD) VALUES ( 'name', 'ADMIN', 'email@email.com', 'password' )");
        RestAssured.port = port;

        adminToken = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "email", "email@email.com",
                        "password", "password"
                ))
                .post("/login")
                .then().log().all()
                .extract().cookie("token");
    }

    @Test
    @DisplayName("관리자 예약 페이지가 잘 동작한다.")
    void adminReservationPageWork() {
        Map<String, Object> params = new HashMap<>();
        params.put("date", LocalDate.now().plusDays(1).format(DATE_FORMATTER));
        params.put("timeId", 1);
        params.put("themeId", 1);
        params.put("memberId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", adminToken)
                .body(params)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(201)
                .body("id", is(1));

        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));

        RestAssured.given().log().all()
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0));
    }

    @Test
    @DisplayName("관리자 예약 페이지가 DB와 함께 잘 동작한다.")
    void adminReservationPageWorkWithDB() {
        Map<String, Object> params = new HashMap<>();
        params.put("date", LocalDate.now().plusDays(1).format(DATE_FORMATTER));
        params.put("timeId", 1);
        params.put("themeId", 1);
        params.put("memberId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", adminToken)
                .body(params)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(201);

        Integer count = jdbcTemplate.queryForObject("SELECT count(1) from reservation", Integer.class);
        Assertions.assertThat(count).isEqualTo(1);

        RestAssured.given().log().all()
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(204);

        Integer countAfterDelete = jdbcTemplate.queryForObject("SELECT count(1) from reservation", Integer.class);
        Assertions.assertThat(countAfterDelete).isEqualTo(0);
    }

    @Test
    @DisplayName("시간 관리 페이지가 잘 동작한다.")
    void reservationTimePageWork() {
        Map<String, String> params = new HashMap<>();
        params.put("startAt", "10:00");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/times")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(2));

        RestAssured.given().log().all()
                .when().delete("/times/2")
                .then().log().all()
                .statusCode(204);
    }
}
