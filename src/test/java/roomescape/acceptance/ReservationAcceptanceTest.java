package roomescape.acceptance;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Cookie;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import roomescape.global.dto.ErrorResponse;
import roomescape.member.domain.Member;
import roomescape.reservation.dto.request.ReservationSaveRequest;
import roomescape.reservation.dto.response.MyReservationResponse;
import roomescape.reservation.dto.response.ReservationResponse;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static roomescape.TestFixture.MIA_NAME;
import static roomescape.TestFixture.MIA_RESERVATION_DATE;
import static roomescape.reservation.domain.ReservationStatus.BOOKING;
import static roomescape.reservation.domain.ReservationStatus.WAITING;

class ReservationAcceptanceTest extends AcceptanceTest {
    @Test
    @DisplayName("예약 목록을 조회한다.")
    void findReservations() {
        // given & when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .extract();
        List<ReservationResponse> reservationResponses = Arrays.stream(response.as(ReservationResponse[].class))
                .toList();

        // then
        assertSoftly(softly -> {
            checkHttpStatusOk(softly, response);
            softly.assertThat(reservationResponses).hasSize(0);
        });
    }

    @Test
    @DisplayName("예약을 추가한다.")
    void createOneReservation() {
        // given
        Member member = createTestMember();
        String token = createTestToken(member);
        Long themeId = createTestTheme();
        Long timeId = createTestReservationTime();

        ReservationSaveRequest request = new ReservationSaveRequest(MIA_RESERVATION_DATE, timeId, themeId, BOOKING.getIdentifier());
        Cookie cookie = new Cookie.Builder("token", token).build();

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie(cookie)
                .body(request)
                .when().post("/reservations")
                .then().log().all()
                .extract();
        ReservationResponse reservationResponse = response.as(ReservationResponse.class);

        // then
        assertSoftly(softly -> {
            checkHttpStatusCreated(softly, response);
            softly.assertThat(reservationResponse.id()).isNotNull();
            softly.assertThat(reservationResponse.memberName()).isEqualTo(MIA_NAME);
        });
    }

    @Test
    @DisplayName("예약 대기를 추가한다.")
    void createWaitingReservation() {
        // given
        Member member = createTestMember();
        String token = createTestToken(member);
        Long themeId = createTestTheme();
        Long timeId = createTestReservationTime();

        createTestReservation(MIA_RESERVATION_DATE, timeId, themeId, token, BOOKING);
        createTestReservation(MIA_RESERVATION_DATE, timeId, themeId, token, WAITING);

        ReservationSaveRequest request = new ReservationSaveRequest(
                MIA_RESERVATION_DATE, timeId, themeId, WAITING.getIdentifier());
        Cookie cookie = new Cookie.Builder("token", token).build();

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie(cookie)
                .body(request)
                .when().post("/reservations")
                .then().log().all()
                .extract();
        ReservationResponse reservationResponse = response.as(ReservationResponse.class);

        // then
        assertSoftly(softly -> {
            checkHttpStatusCreated(softly, response);
            softly.assertThat(reservationResponse.id()).isNotNull();
        });
    }

    @Test
    @DisplayName("동일한 시간에 중복 예약을 추가한다.")
    void createDuplicatedReservation() {
        // given
        Member member = createTestMember();
        String token = createTestToken(member);
        Long themeId = createTestTheme();
        Long timeId = createTestReservationTime();

        createTestReservation(MIA_RESERVATION_DATE, timeId, themeId, token, BOOKING);

        ReservationSaveRequest request = new ReservationSaveRequest(MIA_RESERVATION_DATE, timeId, themeId, BOOKING.getIdentifier());
        Cookie cookie = new Cookie.Builder("token", token).build();

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie(cookie)
                .body(request)
                .when().post("/reservations")
                .then().log().all()
                .extract();
        ErrorResponse errorResponse = response.as(ErrorResponse.class);

        // then
        assertSoftly(softly -> {
            checkHttpStatusBadRequest(softly, response);
            softly.assertThat(errorResponse.message()).isNotNull();
        });
    }

    @Test
    @DisplayName("동일한 시간에 예약이 없을 때 예약 대기를 추가한다.")
    void createInvalidWaitingReservation() {
        // given
        Member member = createTestMember();
        String token = createTestToken(member);
        Long themeId = createTestTheme();
        Long timeId = createTestReservationTime();

        ReservationSaveRequest request = new ReservationSaveRequest(
                MIA_RESERVATION_DATE, timeId, themeId, WAITING.getIdentifier());
        Cookie cookie = new Cookie.Builder("token", token).build();

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie(cookie)
                .body(request)
                .when().post("/reservations")
                .then().log().all()
                .extract();
        ErrorResponse errorResponse = response.as(ErrorResponse.class);

        // then
        assertSoftly(softly -> {
            checkHttpStatusBadRequest(softly, response);
            softly.assertThat(errorResponse.message()).isNotNull();
        });
    }

    @Test
    @DisplayName("예약 날짜가 없는 예약을 추가한다.")
    void createInvalidReservation() {
        // given
        Member member = createTestMember();
        String token = createTestToken(member);
        Long themeId = createTestTheme();
        Long timeId = createTestReservationTime();

        ReservationSaveRequest request = new ReservationSaveRequest(null, timeId, themeId, BOOKING.getIdentifier());
        Cookie cookie = new Cookie.Builder("token", token).build();

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie(cookie)
                .body(request)
                .when().post("/reservations")
                .then().log().all()
                .extract();
        ErrorResponse errorResponse = response.as(ErrorResponse.class);

        // then
        assertSoftly(softly -> {
            checkHttpStatusBadRequest(softly, response);
            softly.assertThat(errorResponse.message()).isNotNull();
        });
    }

    @Test
    @DisplayName("존재하지 않는 예약 시간에 예약을 추가한다.")
    void createReservationWithNotExistingTime() {
        // given
        Member member = createTestMember();
        String token = createTestToken(member);
        Long notExistingTimeId = 1L;
        Long themeId = createTestTheme();

        ReservationSaveRequest request = new ReservationSaveRequest(
                MIA_RESERVATION_DATE, notExistingTimeId, themeId, BOOKING.getIdentifier());
        Cookie cookie = new Cookie.Builder("token", token).build();

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie(cookie)
                .body(request)
                .when().post("/reservations")
                .then().log().all()
                .extract();
        ErrorResponse errorResponse = response.as(ErrorResponse.class);

        // then
        assertSoftly(softly -> {
            checkHttpStatusNotFound(softly, response);
            softly.assertThat(errorResponse.message()).isNotNull();
        });
    }

    @TestFactory
    @DisplayName("예약을 추가하고 삭제한다.")
    Stream<DynamicTest> createThenDeleteReservation() {
        return Stream.of(
                dynamicTest("예약을 하나 생성한다.", this::createOneReservation),
                dynamicTest("예약이 하나 생성된 예약 목록을 조회한다.", () -> findReservationsWithSize(1)),
                dynamicTest("예약을 하나 삭제한다.", this::deleteOneReservation),
                dynamicTest("예약 목록을 조회한다.", () -> findReservationsWithSize(0))
        );
    }

    void deleteOneReservation() {
        // given & when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when().delete("/reservations/1")
                .then().log().all()
                .extract();

        // then
        checkHttpStatusNoContent(response);
    }

    void findReservationsWithSize(int size) {
        // given & when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .extract();
        List<ReservationResponse> reservationResponses = Arrays.stream(response.as(ReservationResponse[].class))
                .toList();

        // then
        assertSoftly(softly -> {
            checkHttpStatusOk(softly, response);
            softly.assertThat(reservationResponses).hasSize(size)
                    .extracting(ReservationResponse::id)
                    .isNotNull();
        });
    }

    @Test
    @DisplayName("사용자의 예약 목록을 조회한다.")
    void findMyReservations() {
        // given
        Long themeId = createTestTheme();
        Long timeId = createTestReservationTime();
        Member member = createTestMember();
        String token = createTestToken(member);
        Cookie cookie = new Cookie.Builder("token", token).build();

        createTestReservation(MIA_RESERVATION_DATE, timeId, themeId, token, BOOKING);

        //  when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .cookie(cookie)
                .when().get("/reservations/mine")
                .then().log().all()
                .extract();
        List<MyReservationResponse> myReservationResponses = Arrays.stream(response.as(MyReservationResponse[].class))
                .toList();

        // then
        assertSoftly(softly -> {
            checkHttpStatusOk(softly, response);
            softly.assertThat(myReservationResponses).hasSize(1)
                    .extracting(MyReservationResponse::reservationId)
                    .isNotNull();
        });

    }
}
