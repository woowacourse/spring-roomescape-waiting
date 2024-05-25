package roomescape.controller.admin;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import roomescape.IntegrationTestSupport;
import roomescape.service.dto.response.UserReservationResponse;
import roomescape.service.dto.response.UserReservationResponses;
import roomescape.service.dto.response.WaitingResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static roomescape.domain.reservation.ReservationStatus.RESERVED;
import static roomescape.domain.reservation.ReservationStatus.WAITING;

class AdminWaitingControllerTest extends IntegrationTestSupport {

    private String createdId;
    private long createdWaitingId;

    @DisplayName("예약이 취소되면 1번 대기자가 자동예약 승인된다")
    @TestFactory
    Stream<DynamicTest> autoReservationScenario() {
        return Stream.of(
                dynamicTest("예약을 추가한다.", () -> {
                    Map<String, Object> params = Map.of(
                            "date", LocalDate.now().plusDays(1L).toString(),
                            "timeId", 1L,
                            "themeId", 1L);

                    createdId = RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", USER_TOKEN)
                            .body(params)
                            .when().post("/reservations")
                            .then().log().all()
                            .statusCode(201).extract().header("location").split("/")[2];
                }),
                dynamicTest("예약 대기를 추가한다.", () -> {
                    Map<String, Object> params = Map.of(
                            "date", LocalDate.now().plusDays(1L).toString(),
                            "timeId", 1L,
                            "themeId", 1L);

                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", USER2_TOKEN)
                            .body(params)
                            .when().post("/waitings")
                            .then().log().all()
                            .statusCode(200);
                }),
                dynamicTest("어드민이 USER1의 예약을 삭제한다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", ADMIN_TOKEN)
                            .when().delete("/admin/reservations/" + createdId)
                            .then().log().all()
                            .statusCode(204);
                }),
                dynamicTest("대기자인 USER2가 예약된다.", () -> {
                    List<UserReservationResponse> userReservationResponses = RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", USER2_TOKEN)
                            .when().get("/reservations-mine")
                            .then().log().all()
                            .statusCode(200)
                            .extract().as(UserReservationResponses.class)
                            .userReservationResponses();

                    boolean hasReservation = userReservationResponses.stream()
                            .filter(response -> response.status().equals(RESERVED.name()))
                            .anyMatch(response -> createdId.equals(String.valueOf(response.id())));

                    assertThat(hasReservation).isTrue();
                })
        );
    }

    @DisplayName("어드민은 예약대기를 취소할 수 있다")
    @TestFactory
    Stream<DynamicTest> deleteWaitingScenario() {
        return Stream.of(
                dynamicTest("예약을 추가한다.", () -> {
                    Map<String, Object> params = Map.of(
                            "date", LocalDate.now().plusDays(1L).toString(),
                            "timeId", 1L,
                            "themeId", 1L);

                    createdId = RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", USER_TOKEN)
                            .body(params)
                            .when().post("/reservations")
                            .then().log().all()
                            .statusCode(201).extract().header("location").split("/")[2];
                }),
                dynamicTest("예약 대기를 추가한다.", () -> {
                    Map<String, Object> params = Map.of(
                            "date", LocalDate.now().plusDays(1L).toString(),
                            "timeId", 1L,
                            "themeId", 1L);

                    createdWaitingId = RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", USER2_TOKEN)
                            .body(params)
                            .when().post("/waitings")
                            .then().log().all()
                            .statusCode(200)
                            .extract()
                            .jsonPath()
                            .getObject(".", WaitingResponse.class)
                            .id();
                }),
                dynamicTest("어드민이 USER2의 예약대기를 삭제한다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", ADMIN_TOKEN)
                            .when().delete("/admin/waitings?id=" + createdWaitingId)
                            .then().log().all()
                            .statusCode(204);
                }),
                dynamicTest("USER2의 예약대기가 삭제된다.", () -> {
                    List<UserReservationResponse> userReservationResponses = RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", USER2_TOKEN)
                            .when().get("/reservations-mine")
                            .then().log().all()
                            .statusCode(200)
                            .extract().as(UserReservationResponses.class)
                            .userReservationResponses();

                    boolean hasWaiting = userReservationResponses.stream()
                            .filter(response -> response.status().contains(WAITING.name()))
                            .anyMatch(response -> createdWaitingId == response.id());

                    assertThat(hasWaiting).isFalse();
                })
        );
    }
}