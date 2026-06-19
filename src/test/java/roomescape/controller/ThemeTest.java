package roomescape.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ThemeTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;

        jdbcTemplate.update("delete from waiting");
        jdbcTemplate.update("delete from reservation_order");
        jdbcTemplate.update("delete from reservation");
        jdbcTemplate.update("delete from slot");
        jdbcTemplate.update("delete from reservation_time");
        jdbcTemplate.update("delete from theme");

        jdbcTemplate.update("alter table waiting alter column id restart with 1");
        jdbcTemplate.update("alter table reservation alter column id restart with 1");
        jdbcTemplate.update("alter table slot alter column id restart with 1");
        jdbcTemplate.update("alter table reservation_time alter column id restart with 1");
        jdbcTemplate.update("alter table theme alter column id restart with 1");
    }

    @Test
    void 테마_관리_API() {
        Map<String, String> params = new HashMap<>();
        params.put("name", "무서운 이야기");
        params.put("description", "공포");
        params.put("url", "http://example.com");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .when().get("/themes")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));

        RestAssured.given().log().all()
                .when().delete("/admin/themes/1")
                .then().log().all()
                .statusCode(204);
    }

    @Test
    void 예약이_존재하는_테마_삭제시_400을_반환한다() {
        Map<String, String> theme = new HashMap<>();
        theme.put("name", "무서운 이야기");
        theme.put("description", "공포");
        theme.put("url", "http://example.com");

        RestAssured.given().contentType(ContentType.JSON).body(theme)
                .when().post("/admin/themes").then().statusCode(201);

        Map<String, String> time = new HashMap<>();
        time.put("startAt", "10:00");

        RestAssured.given().contentType(ContentType.JSON).body(time)
                .when().post("/admin/times").then().statusCode(201);

        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", "브라운");
        reservation.put("date", LocalDate.now().plusDays(1).toString());
        reservation.put("timeId", 1);
        reservation.put("themeId", 1);

        RestAssured.given().contentType(ContentType.JSON).body(reservation)
                .when().post("/reservations").then().statusCode(201);

        RestAssured.given().log().all()
                .when().delete("/admin/themes/1")
                .then().log().all()
                .statusCode(400)
                .body("errorCode", is("DIFFERENCE_DATA_EXISTS"))
                .body("message", is("해당 테마에 예약이 존재하여 삭제할 수 없습니다."));
    }

}
