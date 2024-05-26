package roomescape.controller.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.domain.ReservationStatus;
import roomescape.service.dto.request.ReservationSaveRequest;
import roomescape.util.TokenGenerator;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ReservationApiControllerTest {

    @Autowired
    private ReservationApiController reservationApiController;

    @Test
    @DisplayName("예약 목록 조회 요청이 정상석으로 수행된다.")
    void selectReservationListRequest_Success() {
        RestAssured.given().log().all()
                .when().get("/api/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(6));
    }

    @Test
    @DisplayName("유저 예약 목록 조회를 정상적으로 수행한다.")
    void selectUserReservationListRequest_Success() {
        RestAssured.given().log().all()
                .cookie("token", TokenGenerator.makeMemberToken())
                .when().get("/api/reservations-mine")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(2));
    }

    @Test
    @DisplayName("예약 추가, 조회를 정상적으로 수행한다.")
    void ReservationTime_CREATE_READ_Success() {
        Map<String, Object> reservation = Map.of("name", "브라운",
                "date", LocalDate.now().plusDays(2L).toString(),
                "timeId", 1,
                "themeId", 1,
                "reservationStatus", "RESERVED"
        );

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", TokenGenerator.makeMemberToken())
                .body(reservation)
                .when().post("/api/reservations")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .when().get("/api/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(7));
    }

    @Test
    @DisplayName("DB에 저장된 예약을 정상적으로 삭제한다.")
    void deleteReservation_InDatabase_Success() {
        RestAssured.given().log().all()
                .cookie("token", TokenGenerator.makeAdminToken())
                .when().delete("/api/admin/reservations/1")
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .when().get("/api/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(5));
    }

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

    @Test
    @DisplayName("예약 대기 요청을 정상적으로 수행한다.")
    void createWaiting_Success() {
        RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(new ReservationSaveRequest(LocalDate.now().plusDays(1L), 1L, 2L, ReservationStatus.WAITING))
                .cookie("token", TokenGenerator.makeAdminToken())
                .when().post("/api/reservations")
                .then().log().all()
                .statusCode(201);
    }

    @Test
    @DisplayName("이미 사용자 본인이 예약 대기 요청을 한 경우 예외를 반환한다.")
    void alreadyWaitedByUser() {
        RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(new ReservationSaveRequest(LocalDate.now().plusDays(1L), 1L, 1L, ReservationStatus.WAITING))
                .cookie("token", TokenGenerator.makeAdminToken())
                .when().post("/api/reservations")
                .then().log().all()
                .statusCode(400);
    }
}
