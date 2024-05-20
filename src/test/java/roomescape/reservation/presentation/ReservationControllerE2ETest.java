package roomescape.reservation.presentation;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
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
class ReservationControllerE2ETest {

    private static String token;

    @LocalServerPort
    int serverPort;

    @BeforeEach
    public void beforeEach() {
        RestAssured.port = serverPort;
        Map<String, String> loginParams = Map.of("email", "andole@test.com", "password", "123");
        token = RestAssured.given().log().all()
                .when().body(loginParams)
                .contentType(ContentType.JSON).post("/login")
                .then().log().all()
                .extract().cookie("token");
    }

    @DisplayName("예약 내역 조회 API 작동을 확인한다")
    @Test
    void checkReservations() {
        RestAssured.given().log().all()
                .when().get("reservations")
                .then().log().all()
                .statusCode(200).body("size()", is(5));
    }

    @DisplayName("특정 유저의 예약 내역 조회 API 작동을 확인한다")
    @Test
    void checkMyReservations() {
        RestAssured.given().log().all()
                .when().cookie("token", token).get("reservations/my")
                .then().log().all()
                .statusCode(200).body("size()", is(2));
    }

    @DisplayName("예약 추가와 삭제의 작동을 확인한다")
    @TestFactory
    Stream<DynamicTest> checkReservationCreateAndDelete() {
        Map<String, String> reservationParams = Map.of(
                "date", "2025-08-05",
                "timeId", "1",
                "themeId", "1",
                "memberId", "1"
        );

        return Stream.of(
                dynamicTest("현재 예약 개수를 확인한다", () -> {
                    RestAssured.given().log().all()
                            .when().cookie("token", token).get("/reservations")
                            .then().log().all()
                            .statusCode(200).body("size()", is(5));
                }),

                dynamicTest("예약을 추가한다", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON).body(reservationParams)
                            .when().cookie("token", token).post("/reservations")
                            .then().log().all()
                            .statusCode(201)
                            .header("Location", "/reservations/6");
                }),

                dynamicTest("예약이 정상적으로 추가되었는지 확인한다", () -> {
                    RestAssured.given().log().all()
                            .when().cookie("token", token).get("/reservations")
                            .then().log().all()
                            .statusCode(200).body("size()", is(6));
                }),

                dynamicTest("id가 1인 예약을 삭제한다", () -> {
                    RestAssured.given().log().all()
                            .when().cookie("token", token).delete("/reservations/1")
                            .then().log().all()
                            .statusCode(204);
                }),

                dynamicTest("예약이 정상적으로 삭제되었는지 확인한다", () -> {
                    RestAssured.given().log().all()
                            .when().cookie("token", token).get("/reservations")
                            .then().log().all()
                            .statusCode(200).body("size()", is(5));
                })
        );
    }

    @DisplayName("예약 날짜가 누락된 경우 응답 코드 400을 반환한다.")
    @TestFactory
    Stream<DynamicTest> checkReservationDate() {

        Map<String, String> reservationParams = Map.of(
                "date", "",
                "timeId", "1",
                "themeId", "1",
                "memberId", "1"
        );

        return Stream.of(
                dynamicTest("예약을 추가한다", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON).body(reservationParams)
                            .when().cookie("token", token).post("/reservations")
                            .then().log().all()
                            .statusCode(400);
                })
        );
    }

    @DisplayName("예약 시간이 누락된 경우 응답 코드 400을 반환한다.")
    @TestFactory
    Stream<DynamicTest> checkReservationTime() {

        Map<String, String> reservationParams = Map.of(
                "date", "2024-10-10",
                "timeId", "시간 선택",
                "themeId", "1",
                "memberId", "1"
        );

        return Stream.of(
                dynamicTest("예약을 추가한다", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON).body(reservationParams)
                            .when().post("/reservations")
                            .then().log().all()
                            .statusCode(400);
                })
        );
    }

    @DisplayName("예약 시간이 현재 시간보다 이전 시간이면 예약이 불가능한지 확인한다.")
    @TestFactory
    Stream<DynamicTest> checkIsPassedReservationTime() {

        Map<String, String> reservationParams = Map.of(
                "date", "2020-05-01",
                "timeId", "1",
                "themeId", "1",
                "memberId", "1"
        );

        return Stream.of(
                dynamicTest("예약을 추가한다", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON).body(reservationParams)
                            .when().cookie("token", token).post("/reservations")
                            .then().log().all()
                            .statusCode(400);
                })
        );
    }

    @DisplayName("중복된 시간에 예약이 불가능한지 확인한다.")
    @TestFactory
    Stream<DynamicTest> checkDuplicatedReservationDateTime() {

        Map<String, String> reservationParams1 = Map.of(
                "date", "2025-05-01",
                "timeId", "1",
                "themeId", "1",
                "memberId", "1"
        );

        Map<String, String> reservationParams2 = Map.of(
                "date", "2025-05-01",
                "timeId", "1",
                "themeId", "1",
                "memberId", "2"
        );

        return Stream.of(
                dynamicTest("예약을 추가한다", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON).body(reservationParams1)
                            .when().cookie("token", token).post("/reservations")
                            .then().log().all()
                            .statusCode(201);
                }),

                dynamicTest("중복된 시간에 예약을 추가한다", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON).body(reservationParams2)
                            .when().cookie("token", token).post("/reservations")
                            .then().log().all()
                            .statusCode(400);
                })
        );
    }

    @DisplayName("존재하지 않는 예약의 삭제가 불가능한지 확인한다")
    @Test
    void checkNotExistReservationDelete() {
        RestAssured.given().log().all()
                .when().delete("reservations/100")
                .then().log().all()
                .statusCode(204);
    }
}
