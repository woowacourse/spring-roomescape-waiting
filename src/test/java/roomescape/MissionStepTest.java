package roomescape;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class MissionStepTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() {
        jdbcTemplate.update("DELETE FROM reservation;");
        jdbcTemplate.update("ALTER TABLE reservation ALTER COLUMN id RESTART WITH 1;");
    }

    @Test
    void 예약_추가_및_삭제() {
        Map<String, String> params = new HashMap<>();
        params.put("name", "브라운");
        params.put("date", LocalDate.now().plusDays(1).toString());
        params.put("timeId", "1");
        params.put("themeId", "1");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("id", is(1));

        RestAssured.given().log().all()
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));

        RestAssured.given().log().all()
                .when().delete("/admin/reservations/1")
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0));
    }

    @Test
    void 중복_예약_생성_시_에러_응답() {
        Map<String, String> params = new HashMap<>();
        params.put("name", "브라운");
        params.put("date", LocalDate.now().plusDays(1).toString());
        params.put("timeId", "1");
        params.put("themeId", "1");
        Map<String, String> duplicateParams = new HashMap<>(params);
        duplicateParams.put("name", "구구");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(duplicateParams)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(409)
                .body("code", is("DUPLICATE_RESERVATION"))
                .body("detail", is("이미 예약된 시간입니다."));
    }

    @Test
    void 관리자_중복_예약_생성_시_에러_응답() {
        Map<String, String> params = new HashMap<>();
        params.put("name", "브라운");
        params.put("date", LocalDate.now().plusDays(1).toString());
        params.put("timeId", "1");
        params.put("themeId", "1");
        Map<String, String> duplicateParams = new HashMap<>(params);
        duplicateParams.put("name", "구구");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(duplicateParams)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(409)
                .body("code", is("DUPLICATE_RESERVATION"))
                .body("detail", is("이미 예약된 시간입니다."));
    }

    @Test
    void 시간_관리_API() {
        jdbcTemplate.update("DELETE FROM reservation_time;");
        jdbcTemplate.update("ALTER TABLE reservation_time ALTER COLUMN id RESTART WITH 1;");
        Map<String, String> params = new HashMap<>();
        params.put("startAt", "10:00");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(201)
                .header("Location", endsWith("/admin/times/1"));

        RestAssured.given().log().all()
                .when().get("/admin/times")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));

        RestAssured.given().log().all()
                .when().delete("/admin/times/1")
                .then().log().all()
                .statusCode(204);
    }

    @Test
    void 예약이_존재하는_시간_삭제_테스트() {
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", "브라운");
        reservation.put("date", LocalDate.now().plusDays(1).toString());
        reservation.put("timeId", 1);
        reservation.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .when().delete("/admin/times/1")
                .then().log().all()
                .statusCode(409)
                .body("code", is("RESOURCE_IN_USE"))
                .body("detail", is("예약이 존재하는 시간은 삭제할 수 없습니다."));
    }

    @Test
    void 예약이_존재하는_테마_삭제_테스트() {
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("name", "브라운");
        reservation.put("date", LocalDate.now().plusDays(1).toString());
        reservation.put("timeId", 1);
        reservation.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .when().delete("/admin/themes/1")
                .then().log().all()
                .statusCode(409)
                .body("code", is("RESOURCE_IN_USE"))
                .body("detail", is("예약이 존재하는 테마는 삭제할 수 없습니다."));
    }

    @Test
    void 테마_관리_API() {
        jdbcTemplate.update("DELETE FROM theme;");
        jdbcTemplate.update("ALTER TABLE theme ALTER COLUMN id RESTART WITH 1;");
        Map<String, String> params = new HashMap<>();
        params.put("name", "테마 이름");
        params.put("description", "테마 설명");
        params.put("thumbnail", "썸네일 주소");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(201)
                .header("Location", endsWith("/admin/themes/1"));

        RestAssured.given().log().all()
                .when().get("/themes")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));

        RestAssured.given().log().all()
                .when().delete("/admin/themes/1")
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .when().get("/themes/1/times?date=2026-05-05")
                .then().log().all()
                .statusCode(404)
                .body("code", is("NOT_FOUND"))
                .body("detail", is("존재하지 않는 테마입니다."));

        RestAssured.given().log().all()
                .when().get("/themes/popular")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    void 존재하지_않는_URL_요청() {
        RestAssured.given().log().all()
                .when().get("/not-found")
                .then().log().all()
                .statusCode(404)
                .body("code", is("NOT_FOUND"))
                .body("detail", is("존재하지 않는 리소스입니다."));
    }
}
