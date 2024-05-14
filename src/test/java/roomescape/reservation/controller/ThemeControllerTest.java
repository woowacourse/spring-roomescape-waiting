package roomescape.reservation.controller;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ThemeControllerTest {
    @DisplayName("테마를 정상적으로 추가한다.")
    @Test
    void save() {
        // given
        Map<String, String> params = new HashMap<>();
        params.put("name", "테마 이름");
        params.put("description", "테마 설명");
        params.put("thumbnail", "테마 썸네일");

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/themes")
                .then().log().all()
                .statusCode(201);
    }

    @DisplayName("모든 테마를 조회한다.")
    @Test
    void getAll() {
        // given
        Map<String, String> params = new HashMap<>();
        params.put("name", "테마 이름");
        params.put("description", "테마 설명");
        params.put("thumbnail", "테마 썸네일");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/themes");

        // when & then
        RestAssured.given().log().all()
                .when().get("/themes")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(5));
    }

    @DisplayName("테마를 삭제한다.")
    @Test
    void delete() {
        // given
        Map<String, String> params = new HashMap<>();
        params.put("theme_name", "테마 이름");
        params.put("description", "테마 설명");
        params.put("thumbnail", "테마 썸네일");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/themes");

        // when &then
        RestAssured.given().log().all()
                .when().delete("/themes/4")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("존재하지 않는 테마를 삭제하려는 경우 실패한다.")
    @Test
    void doesNotExistsToDelete() {
        // given
        Map<String, String> params = new HashMap<>();
        params.put("theme_name", "테마 이름");
        params.put("description", "테마 설명");
        params.put("thumbnail", "테마 썸네일");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/themes");

        // when & then
        RestAssured.given().log().all()
                .when().delete("/themes/2")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("이미 방탈출이 예약되어있는 테마인 경우 삭제할 수 없다.")
    @Test
    void cannotDeleteAlreadyReservedTheme() {
        // given
        // time
        Map<String, String> times = new HashMap<>();
        times.put("startAt", "10:00");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(times)
                .when().post("/times");

        // theme
        Map<String, String> params = new HashMap<>();
        params.put("theme_name", "테마 이름");
        params.put("description", "테마 설명");
        params.put("thumbnail", "테마 썸네일");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/themes");

        // reservation
        Map<String, Object> reservations = new HashMap<>();
        reservations.put("member_name", "브라운");
        reservations.put("date", LocalDate.now().plusYears(1).format(DateTimeFormatter.ISO_LOCAL_DATE));
        reservations.put("timeId", 1);
        reservations.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservations)
                .when().post("/reservations");

        // when & then
        RestAssured.given().log().all()
                .when().delete("/themes/1")
                .then().log().all()
                .statusCode(400);
    }
}
