package roomescape.reservation;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.auth.dto.request.LoginRequest;
import roomescape.testutil.IntegrationTest;

@IntegrationTest
class ReservationIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @LocalServerPort
    private int port;

    @BeforeEach
    void init() {
        RestAssured.port = this.port;
    }

    private String getTokenByLogin() {
        jdbcTemplate.update(
                "INSERT INTO member (name, role, email, password) values ( '몰리', 'USER', 'login@naver.com', 'hihi')");

        return RestAssured
                .given().log().all()
                .body(new LoginRequest("login@naver.com", "hihi"))
                .contentType(ContentType.JSON)
                .when().post("/login")
                .then().log().cookies().extract().cookie("token");
    }

    private void saveTimeThemeMemberForReservation() {
        jdbcTemplate.update("insert into theme (name, description, thumbnail) values ('테마이름', '설명', '썸네일')");
        jdbcTemplate.update("insert into reservation_time (start_at) values ('20:00')");
        jdbcTemplate.update("insert into member (name, role, email, password) values ( '몰리', 'USER', 'login@naver.com', 'hihi')");
    }

    @Test
    @DisplayName("방탈출 예약 생성 성공 시, 생성된 시간대의 정보를 반환한다.")
    void createReservationTime() {
        jdbcTemplate.update("insert into theme (name, description, thumbnail) values ('테마이름', '설명', '썸네일')");
        jdbcTemplate.update("insert into reservation_time (start_at) values ('20:00')");

        Map<String, Object> params = new HashMap<>();
        params.put("date", "2024-11-30");
        params.put("timeId", 1);
        params.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin())
                .body(params)
                .when().post("/reservations")
                .then().log().all()

                .statusCode(201)
                .body("id", equalTo(1))
                .body("member.name", equalTo("몰리"))
                .body("theme.name", equalTo("테마이름"))
                .body("date", equalTo("2024-11-30"))
                .body("time.startAt", equalTo("20:00"));
    }

    @Test
    @DisplayName("방탈출 예약 생성 시, 날짜가 형식에 맞지 않을 경우 예외를 반환한다.")
    void createReservationTime_WhenDimeIsInvalidType() {
        Map<String, Object> params = new HashMap<>();
        params.put("date", "asdf-11-30");
        params.put("timeId", 1);
        params.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin())
                .body(params)
                .when().post("/reservations")
                .then().log().all()

                .statusCode(400)
                .body("detail", equalTo("입력값의 형식이 올바르지 않습니다. 다시 시도해주세요."));
    }

    @Test
    @DisplayName("방탈출 예약 생성 시, 날짜가 과거인 경우 예외를 반환한다.")
    void createReservationTime_WhenDimeIsPast() {
        Map<String, Object> params = new HashMap<>();
        params.put("date", "2000-11-30");
        params.put("timeId", 1);
        params.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin())
                .body(params)
                .when().post("/reservations")
                .then().log().all()

                .statusCode(400)
                .body("detail", equalTo("예약 날짜는 현재보다 과거일 수 없습니다."));
    }

    @Test
    @DisplayName("방탈출 예약 생성 시, 날짜가 없는 경우 예외를 반환한다.")
    void createReservationTime_WhenDimeIsNull() {
        Map<String, Object> params = new HashMap<>();
        params.put("date", null);
        params.put("timeId", 1);
        params.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin())
                .body(params)
                .when().post("/reservations")
                .then().log().all()

                .statusCode(400)
                .body("detail", equalTo("예약 등록 시 예약 날짜는 필수입니다."));
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    @DisplayName("방탈출 예약 생성 시, 시간 식별자가 양수가 아닌 경우 예외를 반환한다.")
    void createReservationTime_WhenTimeIsInvalidType(Long timeId) {
        Map<String, Object> params = new HashMap<>();
        params.put("date", "2024-11-30");
        params.put("timeId", timeId);
        params.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin())
                .body(params)
                .when().post("/reservations")
                .then().log().all()

                .statusCode(400)
                .body("detail", equalTo("예약 시간 식별자는 양수만 가능합니다."));
    }

    @Test
    @DisplayName("방탈출 예약 생성 시, 시간 식별자가 없는 경우 예외를 반환한다.")
    void createReservationTime_WhenTimeIsPast() {
        Map<String, Object> params = new HashMap<>();
        params.put("date", "2024-11-30");
        params.put("timeId", null);
        params.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin())
                .body(params)
                .when().post("/reservations")
                .then().log().all()

                .statusCode(400)
                .body("detail", equalTo("예약 등록 시 시간은 필수입니다."));
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    @DisplayName("방탈출 예약 생성 시, 테마 식별자가 양수가 아닌 경우 예외를 반환한다.")
    void createReservationTime_WhenThemeIdIsInvalidType(Long themeId) {
        Map<String, Object> params = new HashMap<>();
        params.put("date", "2024-11-30");
        params.put("timeId", 1);
        params.put("themeId", themeId);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin())
                .body(params)
                .when().post("/reservations")
                .then().log().all()

                .statusCode(400)
                .body("detail", equalTo("예약 테마 식별자는 양수만 가능합니다."));
    }

    @Test
    @DisplayName("방탈출 예약 생성 시, 테마 식별자가 없는 경우 예외를 반환한다.")
    void createReservationTime_WhenThemeIdIsPast() {
        Map<String, Object> params = new HashMap<>();
        params.put("date", "2024-11-30");
        params.put("timeId", 1);
        params.put("themeId", null);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin())
                .body(params)
                .when().post("/reservations")
                .then().log().all()

                .statusCode(400)
                .body("detail", equalTo("예약 등록 시 테마는 필수입니다."));
    }

    @Test
    @DisplayName("예약 생성 시 해당하는 테마가 없는 경우 예외를 반환한다.")
    void createReservation_WhenThemeNotExist() {
        // jdbcTemplate.update("insert into theme (name, description, thumbnail) values ('테마이름', '설명', '썸네일')");
        jdbcTemplate.update("insert into reservation_time (start_at) values ('20:00')");

        Map<String, Object> params = new HashMap<>();
        params.put("date", "2024-11-30");
        params.put("timeId", 1);
        params.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin())
                .body(params)
                .when().post("/reservations")
                .then().log().all()

                .statusCode(404)
                .body("detail", equalTo("식별자 1에 해당하는 테마가 존재하지 않아 예약을 생성할 수 없습니다."));
    }

    @Test
    @DisplayName("예약 생성 시 해당하는 시간이 없는 경우 예외를 반환한다.")
    void createReservation_WhenTimeNotExist() {
        jdbcTemplate.update("insert into theme (name, description, thumbnail) values ('테마이름', '설명', '썸네일')");
        // jdbcTemplate.update("insert into reservation_time (start_at) values ('20:00')");

        Map<String, Object> params = new HashMap<>();
        params.put("date", "2024-11-30");
        params.put("timeId", 1);
        params.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin())
                .body(params)
                .when().post("/reservations")
                .then().log().all()

                .statusCode(404)
                .body("detail", equalTo("식별자 1에 해당하는 시간이 존재하지 않아 예약을 생성할 수 없습니다."));
    }

    @Test
    @DisplayName("예약 생성 시 이미 같은 테마, 같은 날짜, 같은 시간에 예약이 있는 경우 예외를 반환한다.")
    void createReservation_WhenTimeAndDateAndThemeExist() {
        jdbcTemplate.update(
                "INSERT INTO member (name, role, email, password) values ( '몰리', 'USER', 'asd@naver.com', 'hihi')");
        jdbcTemplate.update("insert into theme (name, description, thumbnail) values ('홀리몰리', '설명', '썸네일')");
        jdbcTemplate.update("insert into reservation_time (start_at) values ('20:00')");
        jdbcTemplate.update("insert into reservation (member_id, date, time_id, theme_id) values ( 1, '2024-12-23', 1, 1 )");

        Map<String, Object> params = new HashMap<>();
        params.put("date", "2024-12-23");
        params.put("timeId", 1);
        params.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin())
                .body(params)
                .when().post("/reservations")
                .then().log().all()

                .statusCode(400)
                .body("detail", equalTo("이미 2024-12-23의 홀리몰리 테마에는 20:00 시의 예약이 존재하여 예약을 생성할 수 없습니다."));
    }

    @Test
    @DisplayName("방탈출 예약 목록을 조회한다.")
    void getReservationTimes() {
        saveTimeThemeMemberForReservation();
        jdbcTemplate.update("insert into reservation (member_id, date, time_id, theme_id) values ( 1, '2024-11-23', 1, 1 )");
        jdbcTemplate.update("insert into reservation (member_id, date, time_id, theme_id) values ( 1, '2024-12-23', 1, 1 )");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/reservations")
                .then().log().all()

                .statusCode(200)
                .body("id", hasItems(1, 2))
                .body("size()", equalTo(2));
    }

    @Test
    @DisplayName("방탈출 예약 하나를 조회한다.")
    void getReservationTime() {
        saveTimeThemeMemberForReservation();
        jdbcTemplate.update("insert into reservation (member_id, date, time_id, theme_id) values ( 1, '2024-11-23', 1, 1 )");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/reservations/1")
                .then().log().all()

                .statusCode(200)
                .body("id", equalTo(1))
                .body("member.name", equalTo("몰리"))
                .body("date", equalTo("2024-11-23"));
    }

    @Test
    @DisplayName("방탈출 예약 조회 시, 조회하려는 예약이 없는 경우 예외를 반환한다.")
    void getReservationTime_WhenTimeNotExist() {
        // jdbcTemplate.update("insert into reservation (member_id, date, time_id, theme_id) values ( 1, '2024-11-23', 1, 1 )");
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/reservations/1")
                .then().log().all()

                .statusCode(404)
                .body("detail", equalTo("식별자 1에 해당하는 예약이 존재하지 않아 예약을 조회할 수 없습니다."));
    }

    @Test
    @DisplayName("해당 날짜와 테마를 통해 예약 가능한 시간 조회한다.")
    void getAvailableTimes() {
        saveTimeThemeMemberForReservation();
        jdbcTemplate.update("insert into reservation_time (start_at) values ('10:00')");
        jdbcTemplate.update("insert into reservation (member_id, date, time_id, theme_id) values ( 1, '2024-11-23', 1, 1 )");
        jdbcTemplate.update("insert into reservation (member_id, date, time_id, theme_id) values ( 1, '2024-12-23', 1, 1 )");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/reservations/times?date=2024-11-23&themeId=1")
                .then().log().all()

                .statusCode(200)
                .body("[0].id", equalTo(1))
                .body("[0].alreadyBooked", equalTo(true))

                .body("[1].id", equalTo(2))
                .body("[1].alreadyBooked", equalTo(false))
                .body("size()", equalTo(2));
    }

    @Test
    @DisplayName("해당 날짜와 테마, 기간에 해당하는 예약을 검색한다.")
    void searchBy() {
        saveTimeThemeMemberForReservation();
        jdbcTemplate.update("insert into reservation_time (start_at) values ('10:00')");
        jdbcTemplate.update("insert into reservation (member_id, date, time_id, theme_id) values ( 1, '2024-11-23', 1, 1 )");
        jdbcTemplate.update("insert into reservation (member_id, date, time_id, theme_id) values ( 1, '2024-12-23', 1, 1 )");
        jdbcTemplate.update("insert into reservation (member_id, date, time_id, theme_id) values ( 1, '2025-1-23', 1, 1 )");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/reservations/search?themeId=1&dateFrom=2024-12-23")
                .then().log().all()

                .statusCode(200)
                .body("[0].id", equalTo(2))
                .body("[0].date", equalTo("2024-12-23"))

                .body("[1].id", equalTo(3))
                .body("[1].date", equalTo("2025-01-23"));
    }

    @Test
    @DisplayName("방탈출 예약 하나를 삭제한다.")
    void deleteReservationTime() {
        saveTimeThemeMemberForReservation();
        jdbcTemplate.update("insert into reservation (member_id, date, time_id, theme_id) values ( 1, '2024-11-23', 1, 1 )");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().delete("/reservations/1")
                .then().log().all()

                .statusCode(204);
    }

    @Test
    @DisplayName("방탈출 예약 조회 시, 조회하려는 예약이 없는 경우 예외를 반환한다.")
    void deleteReservationTime_WhenTimeNotExist() {
        // jdbcTemplate.update("insert into reservation (member_id, date, time_id, theme_id) values ( 1, '2024-11-23', 1, 1 )");
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().delete("/reservations/1")
                .then().log().all()

                .statusCode(404)
                .body("detail", equalTo("식별자 1에 해당하는 예약이 존재하지 않습니다. 삭제가 불가능합니다."));
    }
}
