package roomescape.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.common.BaseTest;
import roomescape.member.controller.request.TokenLoginCreateRequest;
import roomescape.member.service.AuthService;
import roomescape.reservation.controller.response.ReservationResponse;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.ThemeRepository;

public class ReservationTest extends BaseTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private AuthService authService;

    private Theme theme;

    private Map<String, Object> reservation;

    private Map<String, String> reservationTime;

    private String token;

    @BeforeEach
    void setUp() {
        //포트 설정
        RestAssured.port = port;
        //테마 추가
        theme = themeRepository.save("테마1", "설명1", "썸네일1");
        //회원추가
        jdbcTemplate.update(
                "INSERT INTO users (name, email, password, role) VALUES (?, ?, ?, ?)",
                "매트", "matt.kakao", "1234", "ADMIN"
        );

        //토큰추출
        token = authService.tokenLogin(new TokenLoginCreateRequest("matt.kakao", "1234")).tokenResponse();

        reservation = new HashMap<>();
        reservation.put("date", "2025-08-05");
        reservation.put("timeId", 1);
        reservation.put("themeId", 1);
        reservation.put("memberId", 1);
        reservationTime = Map.of("startAt", "10:00");
    }

    @Test
    void 방탈출_예약을_생성_조회_삭제한다() {
        Map<String, String> params = new HashMap<>();
        params.put("startAt", "10:00");

        RestAssured.given().log().all()
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/times")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("admin/reservations")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .cookie("token", token)
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));

        RestAssured.given().log().all()
                .cookie("token", token)
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .cookie("token", token)
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0));
    }

    @Test
    void 예약_시간을_생성_조회_삭제한다() {

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservationTime)
                .when().post("/times")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));

        RestAssured.given().log().all()
                .when().delete("/times/1")
                .then().log().all()
                .statusCode(204);
    }

    @Test
    void 관리자_페이지를_응답한다() {
        RestAssured.given().log().all()
                .cookie("token", token)
                .when().get("/admin")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    void 방탈출_예약_페이지를_응답한다() {
        RestAssured.given().log().all()
                .cookie("token", token)
                .when().get("/admin/reservation")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    void 방탈출_예약_목록을_응답한다() {
        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0));
    }

    @Test
    void 예약_삭제시_존재하지_않는_예약이면_예외를_응답한다() {
        RestAssured.given().log().all()
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(404);
    }

    @Test
    void 예약_시간_삭제시_존재하지_않는_예약시간이면_예외를_응답한다() {
        RestAssured.given().log().all()
                .when().delete("/times/1")
                .then().log().all()
                .statusCode(404);
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void 데이터베이스_연결을_검증한다() {
        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            assertThat(connection).isNotNull();
            assertThat(connection.getCatalog()).isEqualTo("DATABASE");
            assertThat(connection.getMetaData().getTables(null, null, "RESERVATION", null).next()).isTrue();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void 방탈출_예약_목록을_조회한다() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)",
                "10:00");
        jdbcTemplate.update("INSERT INTO reservation (date, time_id, theme_id, user_id) VALUES (?, ?, ?, ?)",
                "2025-08-05", 1, 1, 1);

        List<ReservationResponse> response = RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200).extract()
                .jsonPath().getList(".", ReservationResponse.class);

        Integer count = jdbcTemplate.queryForObject("SELECT count(1) from reservation", Integer.class);

        assertThat(response.size()).isEqualTo(count);
    }

    @Test
    void 방탈출_예약_목록을_생성_조회_삭제한다() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)",
                "10:00");

        RestAssured.given().log().all()
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("admin/reservations")
                .then().log().all()
                .statusCode(201);

        Integer count = jdbcTemplate.queryForObject("SELECT count(1) from reservation", Integer.class);
        assertThat(count).isEqualTo(1);

        RestAssured.given().log().all()
                .cookie("token", token)
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(204);
        Integer countAfterDelete = jdbcTemplate.queryForObject("SELECT count(1) from reservation", Integer.class);
        assertThat(countAfterDelete).isEqualTo(0);
    }

    @Test
    void 날짜가_null이면_예외를_응답한다() {
        Map<String, Object> reservationFail = new HashMap<>();
        reservationFail.put("name", "브라운");
        reservationFail.put("timeId", 1);
        reservationFail.put("themeId", 1);

        RestAssured.given().log().all()
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .body(reservationFail)
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    void 시간이_null이면_예외를_응답한다() {
        Map<String, String> reservationFail = new HashMap<>();

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservationFail)
                .when().post("/times")
                .then().log().all()
                .statusCode(400);
    }
}
