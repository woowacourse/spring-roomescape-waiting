package roomescape.acceptance;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import roomescape.service.dto.request.ReservationTimeRequest;
import roomescape.service.dto.request.ThemeRequest;

class ReservationTimeTest extends AcceptanceTest {

    @DisplayName("ADMIN 예약 시간 CRUD 테스트")
    @TestFactory
    Stream<DynamicTest> reservationByAdmin() {
        return Stream.of(
                dynamicTest("예약 시간을 추가한다.", () -> {
                    ReservationTimeRequest reservationTimeRequest = new ReservationTimeRequest(LocalTime.of(10, 0));
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookies("token", adminToken)
                            .body(reservationTimeRequest)
                            .post("/times");
                }),

                dynamicTest("올바르지 않은 시간 형식으로 입력시 예외처리", () -> {
                    Map<String, String> reservationTimeRequest = new HashMap<>();
                    reservationTimeRequest.put("startAt", "aaa11");

                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookies("token", adminToken)
                            .body(reservationTimeRequest)
                            .when().post("/times")
                            .then().log().all()
                            .statusCode(400);
                }),

                dynamicTest("예약 시간을 조회한다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .when().get("/times")
                            .then().log().all()
                            .statusCode(200)
                            .body("size()", is(1));
                }),

                dynamicTest("예약 시간을 삭제한다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .when().delete("/times/1")
                            .then().log().all()
                            .statusCode(204);
                })
        );
    }

    @DisplayName("시간 예약 가능 여부를 조회한다.")
    @TestFactory
    Stream<DynamicTest> availabilityTime() {
        return Stream.of(
                dynamicTest("테마를 추가한다.", () -> {
                    ThemeRequest themeRequest = new ThemeRequest("hi", "happy", "abcd.html");
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookies("token", adminToken)
                            .body(themeRequest)
                            .post("/themes");
                }),

                dynamicTest("예약 시간을 추가한다.", () -> {
                    ReservationTimeRequest reservationTimeRequest = new ReservationTimeRequest(LocalTime.of(10, 0));
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookies("token", adminToken)
                            .body(reservationTimeRequest)
                            .post("/times");
                }),

                dynamicTest("테마와 날짜 정보를 주면 시간별 예약가능 여부를 반환한다.", () -> {
                    RestAssured.given().log().all()
                            .cookies("token", adminToken)
                            .when().get("/times/availability?themeId=1&date=2999-12-12")
                            .then().log().all()
                            .statusCode(200)
                            .body("size()", is(1));
                })
        );
    }
}
