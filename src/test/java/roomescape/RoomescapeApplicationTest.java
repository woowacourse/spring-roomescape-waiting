package roomescape;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RoomescapeApplicationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final static LocalDate tomorrow = LocalDate.now().plusDays(1);

    @BeforeEach
    void setUp() {
        RestAssured.port = port;

        jdbcTemplate.update("delete from waiting");
        jdbcTemplate.update("delete from reservation");
        jdbcTemplate.update("delete from slot");
        jdbcTemplate.update("delete from reservation_time");
        jdbcTemplate.update("delete from theme");

        jdbcTemplate.update("alter table waiting alter column id restart with 1");
        jdbcTemplate.update("alter table reservation alter column id restart with 1");
        jdbcTemplate.update("alter table slot alter column id restart with 1");
        jdbcTemplate.update("alter table reservation_time alter column id restart with 1");
        jdbcTemplate.update("alter table theme alter column id restart with 1");

        jdbcTemplate.update("insert into reservation_time (start_at) values ('10:00')");
        jdbcTemplate.update("insert into theme (name, description, url) values ('테스트', '설명', 'url')");
        jdbcTemplate.update("insert into slot (date, time_id, theme_id) values (?, 1, 1)", tomorrow);
        jdbcTemplate.update("insert into reservation (slot_id, name, created_at) values (1, '다른사람', '2026-05-15 10:30:00')");
        jdbcTemplate.update("insert into waiting (slot_id, name, created_at) values (1, '테스트', '2026-05-15 10:30:00')");
    }

    @Test
    void 예약_대기열이_정상_생성된다() {
        Map<String, Object> body = new HashMap<>();
        body.put("name", "테스트2");
        body.put("date", tomorrow.toString());
        body.put("timeId", 1L);
        body.put("themeId", 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/reservations/waitings")
                .then().log().all()
                .statusCode(201)
                .body("name", is("테스트2"))
                .body("date", is(tomorrow.toString()))
                .body("time.id", is(1))
                .body("time.startAt", is("10:00"))
                .body("theme.id", is(1))
                .body("theme.name", is("테스트"))
                .body("sequence", notNullValue());
    }

    @Test
    void 예약_대기열_정상_삭제된다() {
        RestAssured.given().log().all()
                .when().delete("/reservations/waitings/1")
                .then().log().all()
                .statusCode(204);
    }

    @Test
    void 전체_예약_대기열이_정상적으로_조회된다() {
        RestAssured.given().log().all()
                .when().get("/reservations/waitings")
                .then().log().all()
                .statusCode(200)
                .body("[0].id", is(1))
                .body("[0].name", is("테스트"))
                .body("[0].date", is(tomorrow.toString()))
                .body("[0].time.id", is(1))
                .body("[0].time.startAt", is("10:00"))
                .body("[0].theme.id", is(1))
                .body("[0].theme.name", is("테스트"))
                .body("[0].sequence", is(1));
    }

    @Test
    void 이름으로_예약_대기열_조회가_정상적으로_반환된다() {
        RestAssured.given().log().all()
                .queryParam("name", "테스트")
                .when().get("/reservations/waitings/mine")
                .then().log().all()
                .statusCode(200)
                .body("[0].id", is(1))
                .body("[0].name", is("테스트"))
                .body("[0].date", is(tomorrow.toString()))
                .body("[0].time.id", is(1))
                .body("[0].time.startAt", is("10:00"))
                .body("[0].theme.id", is(1))
                .body("[0].theme.name", is("테스트"))
                .body("[0].sequence", is(1));
    }
}
