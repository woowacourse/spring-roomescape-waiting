package roomescape.domain.reservationtime;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationTimeControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    void 시간_정상_생성_확인_테스트() {
        Map<String, String> params = new HashMap<>();
        params.put("startAt", "10:00");
        params.put("finishAt", "11:00");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/times")
                .then().log().all()
                .statusCode(201)
                .body("id", notNullValue())
                .body("startAt", is("10:00:00"));
    }

    @Test
    void createTime_중복된_시작_시간인경우_에러_반환_테스트() {
        Map<String, String> params = new HashMap<>();
        params.put("startAt", "10:00");
        params.put("finishAt", "11:00");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/times")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/times")
                .then().log().all()
                .statusCode(409)
                .body("message", is("이미 등록된 시작 시간입니다."));
    }

    @Test
    void 시간_전체_조회_정상_동작_테스트() {
        jdbcTemplate.update(
                "INSERT INTO reservation_time (start_at, finish_at) VALUES (?, ?)",
                "10:00", "11:00"
        );
        jdbcTemplate.update(
                "INSERT INTO reservation_time (start_at, finish_at) VALUES (?, ?)",
                "11:00", "12:00"
        );

        RestAssured.given().log().all()
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(2))
                .body("[0].startAt", is("10:00:00"))
                .body("[1].startAt", is("11:00:00"));
    }

    @Test
    void 특정_시간_삭제_정상_동작_테스트() {
        jdbcTemplate.update(
                "INSERT INTO reservation_time (start_at, finish_at) VALUES (?, ?)",
                "10:00", "11:00"
        );
        Long timeId = jdbcTemplate.queryForObject(
                "SELECT id FROM reservation_time WHERE start_at = ?", Long.class, "10:00"
        );

        RestAssured.given().log().all()
                .when().delete("/times/" + timeId)
                .then().log().all()
                .statusCode(204);

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation_time WHERE id = ?", Integer.class, timeId
        );
        org.junit.jupiter.api.Assertions.assertEquals(0, count);
    }

    @Test
    void deleteTime_존재하지_않는_id인경우_에러_반환_테스트() {
        RestAssured.given().log().all()
                .when().delete("/times/999")
                .then().log().all()
                .statusCode(404)
                .body("message", is("해당 time id를 찾을 수 없습니다."));
    }
}
