package roomescape.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.repository.WaitingRepository;
import roomescape.service.dto.request.ReservationTimeRequest;
import roomescape.service.dto.request.ThemeRequest;
import roomescape.service.dto.request.UserReservationRequest;
import roomescape.service.dto.response.MyReservationEntryResponse;

class WaitingTest extends AcceptanceTest {

    @Autowired
    WaitingRepository waitingRepository;

    private UserReservationRequest userReservationRequest = new UserReservationRequest(
            LocalDate.of(2099, 12, 12), 1L, 1L);

    @BeforeEach
    void insert() {
        ReservationTimeRequest reservationTimeRequest = new ReservationTimeRequest(LocalTime.of(10, 0));
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies("token", adminToken)
                .body(reservationTimeRequest)
                .post("/times")
                .then().log().all()
                .extract();

        ThemeRequest themeRequest = new ThemeRequest("hi", "happy", "abcd.html");
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies("token", adminToken)
                .body(themeRequest)
                .post("/themes");
    }

    @DisplayName("USER 예약 대기 CRUD 테스트")
    @TestFactory
    Stream<DynamicTest> waitingByUser() {
        return Stream.of(
                dynamicTest("사용자 qwe가 예약을 추가한다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookies("token", otherUserToken)
                            .body(userReservationRequest)
                            .when().post("/reservations")
                            .then().log().all()
                            .statusCode(201);
                }),

                dynamicTest("사용자 asd가 예약 대기를 추가한다.", () -> {
                    UserReservationRequest userReservationRequest = new UserReservationRequest(
                            LocalDate.of(2099, 12, 12), 1L, 1L);

                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookies("token", userToken)
                            .body(userReservationRequest)
                            .when().post("/waitings")
                            .then().log().all()
                            .statusCode(201)
                            .body("id", is(1));

                    assertThat(waitingRepository.findAll()).hasSize(1);
                }),

                dynamicTest("사용자 asd가 자신의 예약 및 예약 대기를 조회한다.", () -> {
                    List<MyReservationEntryResponse> myReservationEntryRespons = RestAssured.given().log().all()
                            .cookies("token", userToken)
                            .when().get("/reservations/mine")
                            .then().log().all()
                            .statusCode(200).extract()
                            .jsonPath().getList(".", MyReservationEntryResponse.class);

                    assertThat(myReservationEntryRespons).contains(
                            new MyReservationEntryResponse(1L, LocalDate.of(2099, 12, 12), LocalTime.of(10, 0), "hi",
                                    "1번째 예약대기"));
                }),

                dynamicTest("사용자 asd가 예약 대기를 삭제한다.", () -> {
                    RestAssured.given().log().all()
                            .cookies("token", userToken)
                            .when().delete("/waitings/1")
                            .then().log().all()
                            .statusCode(204);

                    assertThat(waitingRepository.findAll()).isEmpty();
                })
        );
    }

    @DisplayName("USER 예약 대기 번호 자동 변경 테스트")
    @TestFactory
    Stream<DynamicTest> waitingNumberChangeByUser() {
        return Stream.of(
                dynamicTest("관리자가 예약을 추가한다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookies("token", adminToken)
                            .body(userReservationRequest)
                            .when().post("/reservations")
                            .then().log().all()
                            .statusCode(201);
                }),

                dynamicTest("사용자 qwe가 예약 대기를 추가한다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookies("token", otherUserToken)
                            .body(userReservationRequest)
                            .when().post("/waitings")
                            .then().log().all()
                            .statusCode(201);
                }),

                dynamicTest("사용자 asd가 예약 대기를 추가한다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookies("token", userToken)
                            .body(userReservationRequest)
                            .when().post("/waitings")
                            .then().log().all()
                            .statusCode(201);
                }),

                dynamicTest("사용자 qwe가 예약 대기를 삭제한다.", () -> {
                    RestAssured.given().log().all()
                            .cookies("token", otherUserToken)
                            .when().delete("/waitings/1")
                            .then().log().all()
                            .statusCode(204);
                }),

                dynamicTest("사용자 asd가 자신의 예약 대기순서가 1번이 됐음을 확인한다.", () -> {
                    List<MyReservationEntryResponse> myReservationEntryRespons = RestAssured.given().log().all()
                            .cookies("token", userToken)
                            .when().get("/reservations/mine")
                            .then().log().all()
                            .statusCode(200).extract()
                            .jsonPath().getList(".", MyReservationEntryResponse.class);

                    assertThat(myReservationEntryRespons).contains(
                            new MyReservationEntryResponse(2L, LocalDate.of(2099, 12, 12), LocalTime.of(10, 0), "hi",
                                    "1번째 예약대기"));
                })
        );
    }

    @DisplayName("USER 예약 대기 자동 승인 테스트")
    @TestFactory
    Stream<DynamicTest> waitingAutoConvertToReservationByUser() {
        return Stream.of(
                dynamicTest("사용자 qwe가 예약을 추가한다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookies("token", otherUserToken)
                            .body(userReservationRequest)
                            .when().post("/reservations")
                            .then().log().all()
                            .statusCode(201);
                }),

                dynamicTest("사용자 asd가 예약 대기를 추가한다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookies("token", userToken)
                            .body(userReservationRequest)
                            .when().post("/waitings")
                            .then().log().all()
                            .statusCode(201);
                }),

                dynamicTest("사용자 qwe가 예약을 삭제한다.", () -> {
                    RestAssured.given().log().all()
                            .cookies("token", otherUserToken)
                            .when().delete("/reservations/1")
                            .then().log().all()
                            .statusCode(204);
                }),

                dynamicTest("사용자 asd가 자신의 예약 대기가 예약으로 자동 승인됐음을 확인한다.", () -> {
                    List<MyReservationEntryResponse> myReservationEntryRespons = RestAssured.given().log().all()
                            .cookies("token", userToken)
                            .when().get("/reservations/mine")
                            .then().log().all()
                            .statusCode(200).extract()
                            .jsonPath().getList(".", MyReservationEntryResponse.class);

                    assertThat(myReservationEntryRespons).contains(
                            new MyReservationEntryResponse(1L, LocalDate.of(2099, 12, 12), LocalTime.of(10, 0), "hi",
                                    "예약"));
                })
        );
    }

    @DisplayName("ADMIN 예약 대기 CRUD 테스트")
    @TestFactory
    Stream<DynamicTest> waitingByAdmin() {
        return Stream.of(
                dynamicTest("사용자 qwe가 예약을 추가한다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookies("token", otherUserToken)
                            .body(userReservationRequest)
                            .when().post("/reservations")
                            .then().log().all()
                            .statusCode(201);
                }),

                dynamicTest("사용자 asd가 예약 대기를 추가한다.", () -> {
                    UserReservationRequest userReservationRequest = new UserReservationRequest(
                            LocalDate.of(2099, 12, 12), 1L, 1L);

                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookies("token", userToken)
                            .body(userReservationRequest)
                            .when().post("/waitings")
                            .then().log().all()
                            .statusCode(201);
                }),

                dynamicTest("관리자가 예약 대기를 조회한다.", () -> {
                    RestAssured.given().log().all()
                            .cookies("token", adminToken)
                            .when().get("/admin/waitings")
                            .then().log().all()
                            .statusCode(200)
                            .body("size()", is(1));
                }),

                dynamicTest("관리자가 예약 대기를 삭제한다.", () -> {
                    RestAssured.given().log().all()
                            .cookies("token", adminToken)
                            .when().delete("/admin/waitings/1")
                            .then().log().all()
                            .statusCode(204);

                    assertThat(waitingRepository.findAll()).isEmpty();
                })
        );
    }

    @DisplayName("예약 후, 동일한 날짜, 시간, 테마에 예약 대기를 하려고 하면 예외를 발생한다. ")
    @TestFactory
    Stream<DynamicTest> duplicateWaitingWithReservation() {
  return Stream.of(
                dynamicTest("예약을 추가한다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookies("token", userToken)
                            .body(userReservationRequest)
                            .when().post("/reservations")
                            .then().log().all()
                            .statusCode(201);
                }),

                dynamicTest("동일한 날짜, 시간, 테마에 예약 대기를 추가한다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookies("token", userToken)
                            .body(userReservationRequest)
                            .when().post("/waitings")
                            .then().log().all()
                            .statusCode(400);
                })
        );
    }

    @DisplayName("예약 대기 후, 동일한 날짜, 시간, 테마에 예약 대기를 하려고 하면 예외를 발생한다. ")
    @TestFactory
    Stream<DynamicTest> duplicateWaiting() {
        return Stream.of(
                dynamicTest("예약을 추가한다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookies("token", userToken)
                            .body(userReservationRequest)
                            .when().post("/reservations")
                            .then().log().all()
                            .statusCode(201);
                }),

                dynamicTest("동일한 날짜, 시간, 테마에 예약 대기를 추가한다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookies("token", userToken)
                            .body(userReservationRequest)
                            .when().post("/waitings")
                            .then().log().all()
                            .statusCode(400);
                })
        );
    }

    @DisplayName("올바르지 않은 날짜 형식으로 입력시 예외처리")
    @Test
    void invalidDateFormat() {
        Map<String, String> reservationRequest = new HashMap<>();
        reservationRequest.put("name", "1234567890");
        reservationRequest.put("date", "2025-aa-bb");
        reservationRequest.put("timeId", "1");
        reservationRequest.put("themeId", "1");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies("token", userToken)
                .body(reservationRequest)
                .when().post("/waitings")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("올바르지 않은 예약자명 형식으로 입력시 예외처리")
    @Test
    void invalidTimeIdFormat() {
        Map<String, String> reservationRequest = new HashMap<>();
        reservationRequest.put("name", "12345678900");
        reservationRequest.put("date", "2030-12-12");
        reservationRequest.put("timeId", "a");
        reservationRequest.put("themeId", "1");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies("token", userToken)
                .body(reservationRequest)
                .when().post("/waitings")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("지나간 시점에 대한 예약시 예외처리")
    @Test
    void pastTimeSlotReservation() {
        Map<String, String> reservationRequest = new HashMap<>();
        reservationRequest.put("name", "1234567890");
        reservationRequest.put("date", "1999-12-12");
        reservationRequest.put("timeId", "1");
        reservationRequest.put("themeId", "1");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookies("token", userToken)
                .body(reservationRequest)
                .when().post("/waitings")
                .then().log().all()
                .statusCode(400);
    }
}
