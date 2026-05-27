package roomescape.time.controller;

import static org.hamcrest.Matchers.is;
import static roomescape.time.exception.ReservationTimeErrorInformation.START_AT_IS_NULL;
import static roomescape.time.fixture.ReservationTimeApiFixture.createReservationTime;
import static roomescape.time.fixture.ReservationTimeApiFixture.updateTimeStatus;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import roomescape.common.AcceptanceTest;

class ReservationTimeAdminControllerTest extends AcceptanceTest {

    private final String startAt1 = "10:00:00";
    private static String startAt2 = "11:00:00";


    @Nested
    @DisplayName("getReservationTimes 메서드는")
    class GetReservationTimesTest {


        @Test
        @DisplayName("예약 시간을 조회한다")
        void 성공() {
            RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, managerToken)
                .when().get("/admin/times")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(0));
        }
    }

    @Nested
    @DisplayName("readAvailableTimes 메서드는")
    class ReadAvailableTimesTest {


        @Test
        @DisplayName("예약 가능한 시간을 조회한다")
        void 성공() {
            Integer activeTimeId = createReservationTime(managerToken, startAt1);
            Integer inactiveTimeId = createReservationTime(managerToken, startAt2);
            updateTimeStatus(managerToken, activeTimeId, true);
            updateTimeStatus(managerToken, inactiveTimeId, false);

            RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, managerToken)
                .when().get("/admin/times")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(2));
        }
    }

    @Nested
    @DisplayName("create 메서드는")
    class CreateTest {


        @Test
        @DisplayName("예약 시간을 생성한다")
        void 성공() {
            Integer timeId = createReservationTime(managerToken, startAt1);

            RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, managerToken)
                .when().get("/admin/times")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].id", is(timeId))
                .body("[0].startAt", is(startAt1));
        }


        @Test
        @DisplayName("startAt이 null이면 예외가 발생한다")
        void 실패() {
            Map<String, Object> params = new HashMap<>();
            params.put("startAt", null);

            RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, managerToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(START_AT_IS_NULL.getHttpStatus().value())
                .body("message", is("요청 값 검증에 실패했습니다."));
        }
    }
}
