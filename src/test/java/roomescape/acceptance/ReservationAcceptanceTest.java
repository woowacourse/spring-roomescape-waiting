package roomescape.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import roomescape.BasicAcceptanceTest;

class ReservationAcceptanceTest extends BasicAcceptanceTest {
    private String clientToken;
    private String adminToken;

    @BeforeEach
    void SetUp() {
        clientToken = LoginTokenProvider.login("member@wooteco.com", "wootecoCrew6!", 200);
        adminToken = LoginTokenProvider.login("admin@wooteco.com", "wootecoCrew6!", 200);
    }

    @TestFactory
    @DisplayName("관리자 페이지에서 3개의 예약을 추가한다")
    Stream<DynamicTest> adminReservationPostAndGetTest() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        return Stream.of(
                dynamicTest("관리자 페이지에서 예약을 추가한다", () -> ReservationCRD.postAdminReservation(adminToken, tomorrow.toString(), 1L, 1L, 1L, 201)),
                dynamicTest("관리자 페이지에서 예약을 추가한다", () -> ReservationCRD.postAdminReservation(adminToken, tomorrow.toString(), 2L, 2L, 2L, 201)),
                dynamicTest("관리자 페이지에서 예약을 추가한다", () -> ReservationCRD.postAdminReservation(adminToken, tomorrow.toString(), 3L, 3L, 3L, 201)),
                dynamicTest("모든 예약을 조회한다 (총 6개)", () -> ReservationCRD.getReservations(200, 6))
        );
    }

    @TestFactory
    @DisplayName("관리자 페이지에서 조건에 맞는 예약을 검색한다")
    Stream<DynamicTest> adminSearchReservation() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate tomorrow = today.plusDays(1);

        return Stream.of(
                dynamicTest("관리자 페이지에서 예약을 추가한다", () -> ReservationCRD.postAdminReservation(adminToken, tomorrow.toString(), 1L, 1L, 1L, 201)),
                dynamicTest("관리자 페이지에서 예약을 추가한다", () -> ReservationCRD.postAdminReservation(adminToken, tomorrow.toString(), 1L, 2L, 1L, 201)),
                dynamicTest("관리자 페이지에서 예약을 추가한다", () -> ReservationCRD.postAdminReservation(adminToken, tomorrow.toString(), 3L, 3L, 3L, 201)),
                dynamicTest("날짜는 어제부터 내일까지, member_id는 1, theme_id는 1인 예약을 검색한다 (총 2개)", () -> adminSearch(adminToken, 1L, 1L, yesterday.toString(), tomorrow.toString(), 200, 2))
        );
    }

    @TestFactory
    @DisplayName("사용자 페이지에서 3개의 예약을 추가한다")
    Stream<DynamicTest> userReservationPostAndGetTest() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        return Stream.of(
                dynamicTest("사용자 페이지에서 예약을 추가한다", () -> ReservationCRD.postClientReservation(clientToken, tomorrow.toString(), 1L, 1L, 201)),
                dynamicTest("사용자 페이지에서 예약을 추가한다", () -> ReservationCRD.postClientReservation(clientToken, tomorrow.toString(), 2L, 2L, 201)),
                dynamicTest("사용자 페이지에서 예약을 추가한다", () -> ReservationCRD.postClientReservation(clientToken, tomorrow.toString(), 3L, 3L, 201)),
                dynamicTest("모든 예약을 조회한다 (총 6개)", () -> ReservationCRD.getReservations(200, 6)),
                dynamicTest("자신의 예약을 조회한다 (총 3개)", () -> getMyReservations(clientToken, 200, 3))
        );
    }

    @TestFactory
    @DisplayName("과거 시간에 대한 예약을 하면, 예외가 발생한다")
    Stream<DynamicTest> pastTimeReservationTest() {
        LocalDate yesterday = LocalDate.now().minusDays(1);

        return Stream.of(
                dynamicTest("과거 시간에 대한 예약을 추가한다", () -> ReservationCRD.postClientReservation(clientToken, yesterday.toString(), 1L, 1L, 400))
        );
    }

    @TestFactory
    @DisplayName("이미 다른 사람이 예약한 테마와 시간을 예약을 하면, 예외 대기로 등록한다")
    Stream<DynamicTest> duplicateReservationTest() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        return Stream.of(
                dynamicTest("사용자 페이지에서 예약을 추가한다", () -> ReservationCRD.postClientReservation(clientToken, tomorrow.toString(), 1L, 1L, 201)),
                dynamicTest("내가 등록한 예약과 동일한 예약을 추가한다", () -> ReservationCRD.postClientReservation(clientToken, tomorrow.toString(), 1L, 1L, 409)),
                dynamicTest("다른 사람이 등록한 예약과 동일한 예약을 추가한다", () -> ReservationCRD.postClientReservation(clientToken, "2099-04-29", 1L, 1L, 201)),
                dynamicTest("모든 예약을 조회한다 (총 4개)", () -> ReservationCRD.getReservations(200, 4)),
                dynamicTest("예약 대기를 조회한다 (총 1개)", () -> getWaitings(adminToken, 200, 1)),
                dynamicTest("예약 대기와 동일한 예약을 삭제한다", () -> ReservationCRD.deleteReservation(1L, 204)),
                dynamicTest("모든 예약을 조회한다 (총 4개)", () -> ReservationCRD.getReservations(200, 4)),
                dynamicTest("예약 대기를 조회한다 (총 0개)", () -> getWaitings(adminToken, 200, 0))
        );
    }

    @TestFactory
    @DisplayName("예약을 추가하고 삭제한다")
    Stream<DynamicTest> reservationPostAndDeleteTest() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        AtomicLong reservationId = new AtomicLong();

        return Stream.of(
                dynamicTest("사용자 페이지에서 예약을 추가한다", () -> reservationId.set(ReservationCRD.postClientReservation(
                        clientToken, tomorrow.toString(), 1L, 1L, 201))),
                dynamicTest("사용자 페이지에서 예약을 추가한다", () -> ReservationCRD.postClientReservation(clientToken, tomorrow.toString(), 2L, 2L, 201)),
                dynamicTest("모든 예약을 조회한다 (총 5개)", () -> ReservationCRD.getReservations(200, 5)),
                dynamicTest("예약을 삭제한다", () -> ReservationCRD.deleteReservation(reservationId.longValue(), 204)),
                dynamicTest("모든 예약을 조회한다 (총 4개)", () -> ReservationCRD.getReservations(200, 4))
        );
    }

    private void adminSearch(String token, Long themeId, Long memberId, String dateFrom, String dateTo, int expectedHttpCode, int expectedReservationResponsesSize) {
        Response response = RestAssured.given().log().all()
                .cookies("token", token)
                .when().get("/admin/reservations/search?themeId=" + themeId + "&memberId=" + memberId + "&dateFrom=" + dateFrom + "&dateTo=" + dateTo)
                .then().log().all()
                .statusCode(expectedHttpCode)
                .extract().response();

        List<?> reservationResponses = response.as(List.class);

        assertThat(reservationResponses).hasSize(expectedReservationResponsesSize);
    }

    private void getMyReservations(String token, int expectedHttpCode, int expectedReservationResponsesSize) {
        Response response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .when().get("/reservations/mine")
                .then().log().all()
                .statusCode(expectedHttpCode)
                .extract().response();

        List<?> reservationResponses = response.as(List.class);

        assertThat(reservationResponses).hasSize(expectedReservationResponsesSize);
    }

    private void getWaitings(String token, int expectedHttpCode, int expectedReservationResponsesSize) {
        Response response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .when().get("/admin/waitings")
                .then().log().all()
                .statusCode(expectedHttpCode)
                .extract().response();

        List<?> reservationResponses = response.as(List.class);

        assertThat(reservationResponses).hasSize(expectedReservationResponsesSize);
    }
}
