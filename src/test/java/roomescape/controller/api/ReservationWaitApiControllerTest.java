package roomescape.controller.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.controller.BaseControllerTest;
import roomescape.util.TokenGenerator;

class ReservationWaitApiControllerTest extends BaseControllerTest {

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
    }

    @Test
    @DisplayName("예약 대기 추가를 정상적으로 수행한다.")
    void reservationWaitCreate_Success() {
        Map<String, Object> reservationWait = Map.of(
                "date", LocalDate.now().plusDays(1L).toString(),
                "timeId", 1,
                "themeId", 1
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", TokenGenerator.makeUserToken())
                .body(reservationWait)
                .when().post("/reservations/wait")
                .then().log().all()
                .statusCode(201);
    }

    @Test
    @DisplayName("예약 대기 삭제를 정상적으로 수행한다.")
    void reservationDelete_Success() {
        jdbcTemplate.update(
                "INSERT INTO member (name, email, password, `role`) VALUES ('사용자1', 'user@wooteco.com', '1234', 'USER')");
        jdbcTemplate.update(
                "INSERT INTO reservation_wait (member_id, date, time_id, theme_id) VALUES (1, CURRENT_DATE + INTERVAL '1' DAY , 1, 1)");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", TokenGenerator.makeUserToken())
                .when().delete("/reservations/wait/{reservationWaitId}", 1)
                .then().log().all()
                .statusCode(204);
    }
}
