package roomescape.controller.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.controller.BaseControllerTest;
import roomescape.util.TokenGenerator;

public class ReservationApiControllerTest extends BaseControllerTest {

    @Autowired
    private ReservationApiController reservationApiController;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    @DisplayName("특정 유저 예약 목록 조회를 정상적으로 수행한다.")
    void selectUserReservationListRequest_Success() {
        RestAssured.given().log().all()
                .cookie("token", TokenGenerator.makeUserToken())
                .when().get("/reservations-mine")
                .then().log().all()
                .statusCode(200)
                .body("reservations.size()", is(1));
    }

    @Test
    @DisplayName("유저가 예약 추가, 조회를 정상적으로 수행한다.")
    void Reservation_CREATE_READ_Success() {
        Map<String, Object> reservation = Map.of(
                "date", LocalDate.now().plusDays(2L).toString(),
                "timeId", 1,
                "themeId", 1
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", TokenGenerator.makeUserToken())
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .cookie("token", TokenGenerator.makeAdminToken())
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200)
                .body("reservations.size()", is(2));
    }

    @Test
    @DisplayName("DB에 저장된 예약을 정상적으로 삭제한다.")
    void deleteReservation_InDatabase_Success() {
        RestAssured.given().log().all()
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .cookie("token", TokenGenerator.makeAdminToken())
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200)
                .body("reservations.size()", is(0));
    }

    // TODO: 불필요한 테스트 삭제
    @Test
    @DisplayName("데이터베이스 관련 로직을 컨트롤러에서 분리하였다.")
    void layerRefactoring() {
        boolean isJdbcTemplateInjected = false;

        for (Field field : reservationApiController.getClass().getDeclaredFields()) {
            if (field.getType().equals(JdbcTemplate.class)) {
                isJdbcTemplateInjected = true;
                break;
            }
        }

        assertThat(isJdbcTemplateInjected).isFalse();
    }
}
