package roomescape.core.controller;

import static org.hamcrest.Matchers.is;
import static roomescape.core.utils.e2eTest.getAccessToken;

import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import roomescape.core.dto.reservation.MemberReservationRequest;
import roomescape.core.dto.reservationtime.ReservationTimeRequest;
import roomescape.core.utils.e2eTest;

/**
 * 로그인 정보 (어드민) { "id": 1 "name": 어드민 "email": test@email.com "password": password "role": ADMIN }
 * <p>
 * 예약 정보 { "date": '2024-05-07', "member_id": 1, "time_id": 1, "theme_id": 1, "status": 'BOOKED' }
 */

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@TestPropertySource(properties = {"spring.config.location = classpath:application-test.yml"})
class ReservationControllerTest {
    private static final String TOMORROW = LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_DATE);
    private static final String DAY_AFTER_TOMORROW = LocalDate.now().plusDays(2).format(DateTimeFormatter.ISO_DATE);

    private String accessToken;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;

        accessToken = getAccessToken();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "abc"})
    @DisplayName("예약 생성 시, date의 형식이 올바르지 않으면 예외가 발생한다.")
    void validateReservationWithDateFormat(final String date) {
        MemberReservationRequest request = new MemberReservationRequest(date, 1L, 1L);

        ValidatableResponse response = e2eTest.post(request, "/reservations", accessToken);
        response.statusCode(400);
    }

    @Test
    @DisplayName("예약 생성 시, date가 이미 지난 날짜면 예외가 발생한다.")
    void validateReservationWithPastDate() {
        MemberReservationRequest request = new MemberReservationRequest("2020-10-10", 1L, 1L);

        ValidatableResponse response = e2eTest.post(request, "/reservations", accessToken);
        response.statusCode(400);
    }

    @Test
    @DisplayName("예약 생성 시, date는 오늘이고 time은 이미 지난 시간이면 예외가 발생한다.")
    void validateReservationWithTodayPastTime() {
        ReservationTimeRequest timeRequest = new ReservationTimeRequest(
                LocalTime.now().minusMinutes(1).format(DateTimeFormatter.ofPattern("HH:mm")));

        ValidatableResponse timesResponse = e2eTest.post(timeRequest, "/admin/times", accessToken);
        timesResponse.statusCode(201);

        MemberReservationRequest memberReservationRequest = new MemberReservationRequest(
                LocalDate.now().format(DateTimeFormatter.ISO_DATE),
                4L, 1L);

        ValidatableResponse reservationsResponse = e2eTest.post(memberReservationRequest, "/reservations", accessToken);
        reservationsResponse.statusCode(400);
    }

    @Test
    @DisplayName("예약 생성 시, timeId가 null이면 예외가 발생한다.")
    void validateReservationWithNullTimeId() {
        MemberReservationRequest request = new MemberReservationRequest(TOMORROW, null, 1L);

        ValidatableResponse response = e2eTest.post(request, "/reservations", accessToken);
        response.statusCode(400);
    }

    @Test
    @DisplayName("예약 생성 시, timeId 값으로 찾을 수 있는 시간이 없으면 예외가 발생한다.")
    void validateReservationWithTimeIdNotFound() {
        MemberReservationRequest request = new MemberReservationRequest(TOMORROW, 0L, 1L);

        ValidatableResponse response = e2eTest.post(request, "/reservations", accessToken);
        response.statusCode(400);
    }

    @Test
    @DisplayName("예약 생성 시, 해당 날짜와 시간에 예약 내역이 있으면 예외가 발생한다.")
    void validateReservationWithDuplicatedDateAndTime() {
        MemberReservationRequest request = new MemberReservationRequest(TOMORROW, 1L, 1L);

        ValidatableResponse successResponse = e2eTest.post(request, "/reservations", accessToken);
        successResponse.statusCode(201);

        ValidatableResponse failResponse = e2eTest.post(request, "/reservations", accessToken);
        failResponse.statusCode(400);
    }

    @Test
    @DisplayName("예약 생성 시, themeId가 null이면 예외가 발생한다.")
    void validateReservationWithNullThemeId() {
        MemberReservationRequest request = new MemberReservationRequest(TOMORROW, 1L, null);

        ValidatableResponse response = e2eTest.post(request, "/reservations", accessToken);
        response.statusCode(400);
    }

    @Test
    @DisplayName("예약 생성 시, themeId 값으로 찾을 수 있는 테마가 없으면 예외가 발생한다.")
    void validateReservationWithThemeIdNotFound() {
        MemberReservationRequest request = new MemberReservationRequest(TOMORROW, 1L, 0L);

        ValidatableResponse response = e2eTest.post(request, "/reservations", accessToken);
        response.statusCode(400);
    }

    @Test
    @DisplayName("모든 예약 내역을 조회한다.")
    void findAllReservations() {
        ValidatableResponse response = e2eTest.get("/reservations", accessToken);
        response.statusCode(200)
                .body("size()", is(2));
    }

    @Test
    @DisplayName("예약을 삭제한다.")
    void validateReservationDelete() {
        ValidatableResponse response = e2eTest.delete("/reservations/1", accessToken);
        response.statusCode(204);
    }

    @Test
    @DisplayName("조건에 따라 예약을 조회한다.")
    void findReservationsByCondition() {
        MemberReservationRequest request = new MemberReservationRequest(TOMORROW, 1L, 1L);

        ValidatableResponse response1 = e2eTest.post(request, "/reservations", accessToken);
        response1.statusCode(201);

        MemberReservationRequest request2 = new MemberReservationRequest(DAY_AFTER_TOMORROW, 1L, 1L);

        ValidatableResponse response2 = e2eTest.post(request2, "/reservations", accessToken);
        response2.statusCode(201);

        RestAssured.given().log().all()
                .cookies("token", accessToken)
                .queryParams(
                        "memberId", 1L,
                        "themeId", 1L,
                        "dateFrom", TOMORROW,
                        "dateTo", DAY_AFTER_TOMORROW
                )
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(2));
    }

    @Test
    @DisplayName("토큰이 유효하지 않을 경우 예외가 발생한다.")
    void validateToken() {
        MemberReservationRequest request = new MemberReservationRequest(TOMORROW, 1L, 1L);

        ValidatableResponse response = e2eTest.post(request, "/reservations", "invalid-token");
        response.statusCode(401);
    }

    @Test
    @DisplayName("현재 로그인된 회원의 예약 목록을 조회한다.")
    void findLoginMemberReservation() {
        ValidatableResponse response = e2eTest.get("/reservations/mine", accessToken);
        response.statusCode(200)
                .body("size()", is(2));
    }

    @Test
    @DisplayName("나의 예약 목록의 예약 대기 상태에 대기 순번을 표시한다.")
    void findReservationWaitingRank() {
        ValidatableResponse response = e2eTest.get("/reservations/mine", accessToken);
        response.body("status", is(List.of("예약", "2번째 예약대기")));
    }

    @Test
    @DisplayName("예약 대기를 요청한다.")
    void createReservationWaiting() {
        MemberReservationRequest request = new MemberReservationRequest(TOMORROW, 1L, 1L);
        ValidatableResponse response = e2eTest.post(request, "/reservations/waiting", accessToken);
        response.statusCode(201);
    }

    @Test
    @DisplayName("예약 대기 요청 시, 현재 로그인된 회원의 예약과 중복된 예약을 대기 요청하면 예외가 발생한다.")
    void duplicateReservationWaiting() {
        String alreadyBookedDate = LocalDate.parse("2024-05-07").format(DateTimeFormatter.ISO_DATE);
        MemberReservationRequest request = new MemberReservationRequest(alreadyBookedDate, 1L, 1L);
        ValidatableResponse response = e2eTest.post(request, "/reservations/waiting", accessToken);
        response.statusCode(400);
    }

    @Test
    @DisplayName("모든 예약 대기 목록을 조회한다.")
    void findReservationWaiting() {
        ValidatableResponse response = e2eTest.get("/reservations/waiting", accessToken);
        response.statusCode(200)
                .body("size()", is(2));
    }
}
