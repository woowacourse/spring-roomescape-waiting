package roomescape.time.presentation;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationTimeControllerE2ETest {

    private static String token;

    @LocalServerPort
    int serverPort;

    @BeforeEach
    public void beforeEach() {
        RestAssured.port = serverPort;
        Map<String, String> loginParams = Map.of("email", "admin@test.com", "password", "123");
        token = RestAssured.given().log().all()
                .when().body(loginParams)
                .contentType(ContentType.JSON).post("/login")
                .then().log().all()
                .extract().cookie("token");
    }

    @DisplayName("예약 시간 조회 API 작동을 확인한다")
    @Test
    void checkReservations() {
        RestAssured.given().log().all()
                .when().cookie("token", token).get("themes")
                .then().log().all()
                .statusCode(200).body("size()", is(3));
    }

    @DisplayName("예약 시간 추가와 삭제의 작동을 확인한다")
    @TestFactory
    Stream<DynamicTest> checkReservationTimeCreateAndDelete() {
        Map<String, String> reservationTimesParam = Map.of(
                "id", "4",
                "startAt", "13:00"
        );

        return Stream.of(
                dynamicTest("현재 예약 시간 개수를 확인한다", () -> {
                    RestAssured.given().log().all()
                            .when().cookie("token", token).get("/times")
                            .then().log().all()
                            .statusCode(200).body("size()", is(3));
                }),

                dynamicTest("예약 시간를 추가한다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .body(reservationTimesParam)
                            .when().cookie("token", token).post("/times")
                            .then().log().all()
                            .statusCode(201);
                }),

                dynamicTest("예약 시간이 정상적으로 추가되었는지 확인한다", () -> {
                    RestAssured.given().log().all()
                            .when().cookie("token", token).get("/times")
                            .then().log().all()
                            .statusCode(200).body("size()", is(4));
                }),

                dynamicTest("id가 4인 예약 시간을 삭제한다", () -> {
                    RestAssured.given().log().all()
                            .when().cookie("token", token).delete("/times/4")
                            .then().log().all()
                            .statusCode(204);
                }),

                dynamicTest("예약 시간이 정상적으로 삭제되었는지 확인한다", () -> {
                    RestAssured.given().log().all()
                            .when().cookie("token", token).get("/times")
                            .then().log().all()
                            .statusCode(200).body("size()", is(3));
                })
        );
    }

    @DisplayName("날짜와 테마로 예약 가능한 시간 조회 기능을 확인한다")
    @Test
    void checkAvailableTimeByDateAndThemeId() {
        String today = LocalDate.now().plusDays(2).toString();

        RestAssured.given().log().all()
                .queryParam("date", today)
                .queryParam("themeId", 1)
                .when().get("times/available")
                .then().log().all()
                .statusCode(200).body("size()", is(3));
    }
}
