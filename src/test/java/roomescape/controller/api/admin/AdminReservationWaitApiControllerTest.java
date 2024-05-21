package roomescape.controller.api.admin;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.controller.BaseControllerTest;
import roomescape.util.TokenGenerator;

class AdminReservationWaitApiControllerTest extends BaseControllerTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail) VALUES ('방탈출1', '1번 방탈출', '썸네일1')");
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES ('10:00')");
        jdbcTemplate.update(
                "INSERT INTO member (name, email, password, `role`) VALUES ('사용자1', 'user@wooteco.com', '1234', 'USER')");
        jdbcTemplate.update(
                "INSERT INTO member (name, email, password, `role`) VALUES ('관리자1', 'admin@wooteco.com', '1234', 'ADMIN')");
    }

    @Test
    @DisplayName("관리자가 예약 대기 목록 조회를 정상적으로 수행한다.")
    void getReservationWaits_Success() {
        jdbcTemplate.update(
                "INSERT INTO reservation_wait (member_id, date, time_id, theme_id, status) VALUES (1, CURRENT_DATE + INTERVAL '1' DAY , 1, 1, 'WAITING')");
        jdbcTemplate.update(
                "INSERT INTO reservation_wait (member_id, date, time_id, theme_id, status) VALUES (1, CURRENT_DATE + INTERVAL '1' DAY , 1, 1, 'WAITING')");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", TokenGenerator.makeAdminToken())
                .when().get("/admin/reservations/wait")
                .then().log().all()
                .statusCode(200)
                .body("reservationWaits.size()", is(2));
    }

    @Test
    @DisplayName("관리자가 예약 대기 목록 조회를 정상적으로 수행한다.")
    void deleteReservationWaits_Success() {
        jdbcTemplate.update(
                "INSERT INTO reservation_wait (member_id, date, time_id, theme_id, status) VALUES (1, CURRENT_DATE + INTERVAL '1' DAY , 1, 1, 'WAITING')");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", TokenGenerator.makeAdminToken())
                .when().delete("/admin/reservations/1")
                .then().log().all()
                .statusCode(204);
    }
}
