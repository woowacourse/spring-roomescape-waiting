package roomescape.member;

import static org.hamcrest.Matchers.equalTo;

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
class AdminIntegrationTest {

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
                "INSERT INTO member (name, role, email, password) values ( '비밥', 'ADMIN', 'admin@naver.com', 'hihi')");

        return RestAssured
                .given().log().all()
                .body(new LoginRequest("admin@naver.com", "hihi"))
                .contentType(ContentType.JSON)
                .when().post("/login")
                .then().log().cookies().extract().cookie("token");
    }


    @Test
    @DisplayName("관리자 권한으로 예약을 생성한다.")
    void createReservationByAdmin() {
        jdbcTemplate.update("insert into theme (name, description, thumbnail) values ('테마이름', '설명', '썸네일')");
        jdbcTemplate.update("insert into reservation_time (start_at) values ('20:00')");
        jdbcTemplate.update(
                "insert into member (name, role, email, password) values ( '몰리', 'USER', 'login@naver.com', 'hihi')");

        Map<String, Object> params = new HashMap<>();
        params.put("date", "2024-11-30");
        params.put("memberId", 1);
        params.put("timeId", 1);
        params.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin())
                .body(params)
                .when().post("/admin/reservations")
                .then().log().all()

                .statusCode(201)
                .body("id", equalTo(1))
                .body("member.name", equalTo("몰리"))
                .body("theme.name", equalTo("테마이름"))
                .body("date", equalTo("2024-11-30"))
                .body("time.startAt", equalTo("20:00"));
    }

    @Test
    @DisplayName("관리자 권한으로 방탈출 예약 생성 시, 날짜가 과거인 경우 예외를 반환한다.")
    void createReservationByAdmin_WhenDimeIsPast() {
        Map<String, Object> params = new HashMap<>();
        params.put("date", "2000-11-30");
        params.put("memberId", 1);
        params.put("timeId", 1);
        params.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin())
                .body(params)
                .when().post("/admin/reservations")
                .then().log().all()

                .statusCode(400)
                .body("detail", equalTo("예약 날짜는 현재보다 과거일 수 없습니다."));
    }

    @Test
    @DisplayName("관리자 권한으로 방탈출 예약 생성 시, 날짜가 없는 경우 예외를 반환한다.")
    void createReservationByAdmin_WhenDimeIsNull() {
        Map<String, Object> params = new HashMap<>();
        params.put("date", null);
        params.put("memberId", 1);
        params.put("timeId", 1);
        params.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin())
                .body(params)
                .when().post("/admin/reservations")
                .then().log().all()

                .statusCode(400)
                .body("detail", equalTo("예약 등록 시 예약 날짜는 필수입니다."));
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    @DisplayName("관리자 권한으로 방탈출 예약 생성 시, 회원 식별자가 양수가 아닌 경우 예외를 반환한다.")
    void createReservationByAdmin_WhenMemberIdIsInvalidType(Long memberId) {
        Map<String, Object> params = new HashMap<>();
        params.put("date", "2024-11-30");
        params.put("memberId", memberId);
        params.put("timeId", 1);
        params.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin())
                .body(params)
                .when().post("/admin/reservations")
                .then().log().all()

                .statusCode(400)
                .body("detail", equalTo("예약하고자 하는 회원 식별자는 양수만 가능합니다."));
    }

    @Test
    @DisplayName("관리자 권한으로 방탈출 예약 생성 시, 회원 식별자가 없는 경우 예외를 반환한다.")
    void createReservationByAdmin_WhenMemberIdIsPast() {
        Map<String, Object> params = new HashMap<>();
        params.put("date", "2024-11-30");
        params.put("memberId", null);
        params.put("timeId", 1);
        params.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin())
                .body(params)
                .when().post("/admin/reservations")
                .then().log().all()

                .statusCode(400)
                .body("detail", equalTo("예약 등록 시 회원은 필수입니다."));
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    @DisplayName("관리자 권한으로 방탈출 예약 생성 시, 시간 식별자가 양수가 아닌 경우 예외를 반환한다.")
    void createReservationByAdmin_WhenTimeIsInvalidType(Long timeId) {
        Map<String, Object> params = new HashMap<>();
        params.put("date", "2024-11-30");
        params.put("memberId", 1);
        params.put("timeId", timeId);
        params.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin())
                .body(params)
                .when().post("/admin/reservations")
                .then().log().all()

                .statusCode(400)
                .body("detail", equalTo("예약 등록 시 시간 식별자는 양수만 가능합니다."));
    }

    @Test
    @DisplayName("관리자 권한으로 방탈출 예약 생성 시, 시간 식별자가 없는 경우 예외를 반환한다.")
    void createReservationByAdmin_WhenTimeIsPast() {
        Map<String, Object> params = new HashMap<>();
        params.put("date", "2024-11-30");
        params.put("memberId", 1);
        params.put("timeId", null);
        params.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin())
                .body(params)
                .when().post("/admin/reservations")
                .then().log().all()

                .statusCode(400)
                .body("detail", equalTo("예약 등록 시 시간은 필수입니다."));
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    @DisplayName("관리자 권한으로 방탈출 예약 생성 시, 테마 식별자가 양수가 아닌 경우 예외를 반환한다.")
    void createReservationByAdmin_WhenThemeIdIsInvalidType(Long themeId) {
        Map<String, Object> params = new HashMap<>();
        params.put("date", "2024-11-30");
        params.put("memberId", 1);
        params.put("timeId", 1);
        params.put("themeId", themeId);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin())
                .body(params)
                .when().post("/admin/reservations")
                .then().log().all()

                .statusCode(400)
                .body("detail", equalTo("예약 등록 시 테마 식별자는 양수만 가능합니다."));
    }

    @Test
    @DisplayName("관리자 권한으로 방탈출 예약 생성 시, 테마 식별자가 없는 경우 예외를 반환한다.")
    void createReservationByAdmin_WhenThemeIdIsPast() {
        Map<String, Object> params = new HashMap<>();
        params.put("date", "2024-11-30");
        params.put("memberId", 1);
        params.put("timeId", 1);
        params.put("themeId", null);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin())
                .body(params)
                .when().post("/admin/reservations")
                .then().log().all()

                .statusCode(400)
                .body("detail", equalTo("예약 등록 시 테마는 필수입니다."));
    }

    @Test
    @DisplayName("관리자 권한으로 예약 생성 시 해당하는 테마가 없는 경우 예외를 반환한다.")
    void createReservationByAdmin_WhenThemeNotExist() {
        // jdbcTemplate.update("insert into theme (name, description, thumbnail) values ('테마이름', '설명', '썸네일')");
        jdbcTemplate.update("insert into reservation_time (start_at) values ('20:00')");
        jdbcTemplate.update(
                "insert into member (name, role, email, password) values ( '몰리', 'USER', 'login@naver.com', 'hihi')");

        Map<String, Object> params = new HashMap<>();
        params.put("date", "2024-11-30");
        params.put("memberId", 1);
        params.put("timeId", 1);
        params.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin())
                .body(params)
                .when().post("/admin/reservations")
                .then().log().all()

                .statusCode(404)
                .body("detail", equalTo("해당하는 테마가 존재하지 않아 예약을 생성할 수 없습니다."));
    }

    @Test
    @DisplayName("관리자 권한으로 예약 생성 시 해당하는 시간이 없는 경우 예외를 반환한다.")
    void createReservationByAdmin_WhenTimeNotExist() {
        jdbcTemplate.update("insert into theme (name, description, thumbnail) values ('테마이름', '설명', '썸네일')");
        // jdbcTemplate.update("insert into reservation_time (start_at) values ('20:00')");
        jdbcTemplate.update(
                "insert into member (name, role, email, password) values ( '몰리', 'USER', 'login@naver.com', 'hihi')");

        Map<String, Object> params = new HashMap<>();
        params.put("date", "2024-11-30");
        params.put("memberId", 1);
        params.put("timeId", 1);
        params.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin())
                .body(params)
                .when().post("/admin/reservations")
                .then().log().all()

                .statusCode(404)
                .body("detail", equalTo("해당하는 시간이 존재하지 않아 예약을 생성할 수 없습니다."));
    }

    @Test
    @DisplayName("관리자 권한으로 예약 생성 시 해당하는 시간이 없는 경우 예외를 반환한다.")
    void createReservationByAdmin_WhenMemberNotExist() {
        jdbcTemplate.update("insert into theme (name, description, thumbnail) values ('테마이름', '설명', '썸네일')");
        jdbcTemplate.update("insert into reservation_time (start_at) values ('20:00')");
        // jdbcTemplate.update("insert into member (name, role, email, password) values ( '몰리', 'USER', 'login@naver.com', 'hihi')");

        Map<String, Object> params = new HashMap<>();
        params.put("date", "2024-11-30");
        params.put("memberId", 2);
        params.put("timeId", 1);
        params.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", getTokenByLogin())
                .body(params)
                .when().post("/admin/reservations")
                .then().log().all()

                .statusCode(404)
                .body("detail", equalTo("해당하는 사용자가 존재하지 않아 예약을 생성할 수 없습니다."));
    }
}
