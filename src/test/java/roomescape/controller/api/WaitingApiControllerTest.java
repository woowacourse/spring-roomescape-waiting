package roomescape.controller.api;

import io.restassured.RestAssured;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.service.dto.request.ReservationSaveRequest;
import roomescape.util.TokenGenerator;

import java.time.LocalDate;

import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class WaitingApiControllerTest {

    @Test
    @DisplayName("예약 대기 요청을 정상적으로 수행한다.")
    void createWaiting_Success() {
        RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(new ReservationSaveRequest(LocalDate.now().plusDays(1L), 1L, 2L))
                .cookie("token", TokenGenerator.makeAdminToken())
                .when().post("/waiting")
                .then().log().all()
                .statusCode(201);
    }

    @Test
    @DisplayName("이미 사용자 본인이 예약 대기 요청을 한 경우 예외를 반환한다.")
    void alreadyWaitedByUser() {
        RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(new ReservationSaveRequest(LocalDate.now().plusDays(1L), 1L, 1L))
                .cookie("token", TokenGenerator.makeAdminToken())
                .when().post("/waiting")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    @DisplayName("모든 예약 대기 목록을 가져온다.")
    void selectWaitings() {
        RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .cookie("token", TokenGenerator.makeAdminToken())
                .when().get("/admin/waitings")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(4));
    }
}
