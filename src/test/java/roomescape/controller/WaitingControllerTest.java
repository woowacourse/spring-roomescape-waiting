package roomescape.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.springframework.transaction.annotation.Transactional;
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
import static roomescape.domain.reservation.ReservationStatus.WAITING;

@Transactional
class WaitingControllerTest extends IntegrationTestSupport {

    long createdId;
    int userReservationSize;

    @DisplayName("유저의 예약/예약 대기 생성")
    @TestFactory
    Stream<DynamicTest> dynamicUserTestsFromCollection() {
        return Stream.of(
                dynamicTest("내 예약/예약 대기 목록을 조회한다.", () -> {
                    userReservationSize = RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", USER_TOKEN)
                            .when().get("/reservations-mine")
                            .then().log().all()
                            .statusCode(200).extract()
                            .response().jsonPath().getObject("$", UserReservationResponses.class)
                            .userReservationResponses()
                            .size();
                }),
                dynamicTest("예약을 추가한다.", () -> {
                    Map<String, Object> params = Map.of(
                            "date", LocalDate.now().plusDays(1L).toString(),
                            "timeId", 1L,
                            "themeId", 1L);

                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", USER_TOKEN)
                            .body(params)
                            .when().post("/reservations")
                            .then().log().all()
                            .statusCode(201);
                }),
                dynamicTest("예약되지 않은 날짜에 대기할 수 없다.", () -> {
                    Map<String, Object> params = Map.of(
                            "date", LocalDate.now().plusDays(5L).toString(),
                            "timeId", 1L,
                            "themeId", 1L);

                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", USER_TOKEN)
                            .body(params)
                            .when().post("/waitings")
                            .then().log().all()
                            .statusCode(400);
                }),
                dynamicTest("예약에 성공한 유저는 대기할 수 없다.", () -> {
                    Map<String, Object> params = Map.of(
                            "date", LocalDate.now().plusDays(1L).toString(),
                            "timeId", 1L,
                            "themeId", 1L);

                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", USER_TOKEN)
                            .body(params)
                            .when().post("/waitings")
                            .then().log().all()
                            .statusCode(400);
                }),
                dynamicTest("예약 대기를 추가할 수 있다.", () -> {
                    Map<String, Object> params = Map.of(
                            "date", LocalDate.now().plusDays(1L).toString(),
                            "timeId", 1L,
                            "themeId", 1L);

                    createdId = RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", USER2_TOKEN)
                            .body(params)
                            .when().post("/waitings")
                            .then().log().all()
                            .statusCode(200)
                            .extract().as(WaitingResponse.class)
                            .id();
                }),
                dynamicTest("내 예약 목록에는 대기 목록도 포함한다.", () -> {
                    List<UserReservationResponse> userReservationResponses = RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", USER2_TOKEN)
                            .when().get("/reservations-mine")
                            .then().log().all()
                            .statusCode(200)
                            .extract().as(UserReservationResponses.class)
                            .userReservationResponses();

                    int waitingSize = userReservationResponses
                            .stream()
                            .filter(response -> response.status().contains(WAITING.name()))
                            .toList()
                            .size();

                    assertThat(waitingSize).isEqualTo(1);
                }),
                dynamicTest("중복 예약 대기는 불가하다.", () -> {
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
                            .statusCode(400);
                }),
                dynamicTest("유저는 다른 유저의 예약 대기를 삭제할 수 없다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", USER_TOKEN)
                            .when().delete("/waitings?id=" + createdId)
                            .then().log().all()
                            .statusCode(400);
                }),
                dynamicTest("유저는 예약 대기를 삭제할 수 있다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .cookie("token", USER2_TOKEN)
                            .when().delete("/waitings?id=" + createdId)
                            .then().log().all()
                            .statusCode(204);
                })
        );
    }
}