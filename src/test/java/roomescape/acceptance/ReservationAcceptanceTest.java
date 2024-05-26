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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static roomescape.TestFixture.ADMIN_EMAIL;
import static roomescape.TestFixture.MIA_EMAIL;
import static roomescape.TestFixture.MIA_NAME;
import static roomescape.TestFixture.MIA_RESERVATION_DATE;
import static roomescape.TestFixture.TOMMY_EMAIL;
import static roomescape.TestFixture.TOMMY_NAME;
import static roomescape.TestFixture.TOMMY_RESERVATION_DATE;
import static roomescape.reservation.domain.ReservationStatus.BOOKING;
import static roomescape.reservation.domain.ReservationStatus.WAITING;

class ReservationAcceptanceTest extends AcceptanceTest {

    @Test
    @DisplayName("예약을 추가한다.")
    void createOneReservation() {
        // given
        Member member = createTestMember(MIA_EMAIL, MIA_NAME);
        String token = createTestToken(member.getEmail().getValue());
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
        Member mia = createTestMember(MIA_EMAIL, MIA_NAME);
        Member tommy = createTestMember(TOMMY_EMAIL, TOMMY_NAME);
        String miaToken = createTestToken(mia.getEmail().getValue());
        String tommyToken = createTestToken(tommy.getEmail().getValue());
        Long themeId = createTestTheme();
        Long timeId = createTestReservationTime();

        createTestReservation(MIA_RESERVATION_DATE, timeId, themeId, tommyToken, BOOKING);

        ReservationSaveRequest request = new ReservationSaveRequest(
                MIA_RESERVATION_DATE, timeId, themeId, WAITING.getIdentifier());
        Cookie cookie = new Cookie.Builder("token", miaToken).build();

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
    @DisplayName("동일한 시간대에 예약을 추가한다.")
    void createDuplicatedReservation() {
        // given
        Member member = createTestMember(MIA_EMAIL, MIA_NAME);
        String token = createTestToken(member.getEmail().getValue());
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
    @DisplayName("동시 요청으로 동일한 시간대에 예약을 추가한다.")
    void createDuplicatedReservationInMultiThread() throws InterruptedException {
        // given
        int threadCount = 2;
        List<Cookie> cookies = createCookies(threadCount);
        Long themeId = createTestTheme();
        Long timeId = createTestReservationTime();

        ReservationSaveRequest request = new ReservationSaveRequest(MIA_RESERVATION_DATE, timeId, themeId, BOOKING.getIdentifier());

        // when
        for (int i = 0; i < threadCount; i++) {
            int threadIndex = i;
            new Thread(() -> RestAssured.given()
                    .contentType(ContentType.JSON)
                    .cookie(cookies.get(threadIndex))
                    .body(request).log().all()
                    .when().post("/reservations")
                    .then().log().all()
            ).start();
        }

        // then
        Thread.sleep(1000);
        Member admin = createTestAdmin();
        String adminToken = createTestToken(admin.getEmail().getValue());
        Cookie adminCookie = new Cookie.Builder("token", adminToken).build();

        List<ReservationResponse> reservationResponses = findAllReservations(adminCookie);
        List<ReservationResponse> waitingResponses = findAllWaitingReservations(adminCookie);

        assertSoftly(softly -> {
            softly.assertThat(reservationResponses).hasSize(1);
            softly.assertThat(waitingResponses).hasSize(0);
        });
    }

    private List<Cookie> createCookies(int count) {
        List<Cookie> cookies = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Member member = createTestMember(i + MIA_EMAIL, i + MIA_NAME);
            String token = createTestToken(member.getEmail().getValue());
            Cookie cookie = new Cookie.Builder("token", token).build();
            cookies.add(cookie);
        }
        return cookies;
    }

    @Test
    @DisplayName("동시 요청으로 예약을 삭제할 때 대기 예약을 생성한다.")
    void createWaitingReservationWhenReservationIsBeingDeleted() throws InterruptedException {
        // given
        createTestMember(TOMMY_EMAIL, TOMMY_NAME);
        String tommyToken = createTestToken(TOMMY_EMAIL);
        Long themeId = createTestTheme();
        Long timeId = createTestReservationTime();
        Long reservationId = createTestReservation(MIA_RESERVATION_DATE, timeId, themeId, tommyToken, BOOKING);

        createTestMember(MIA_EMAIL, MIA_NAME);
        String miaToken = createTestToken(MIA_EMAIL);
        createTestAdmin();
        String adminToken = createTestToken(ADMIN_EMAIL);

        Cookie miaCookie = new Cookie.Builder("token", miaToken).build();
        ReservationSaveRequest waitingReservationSaveRequest = new ReservationSaveRequest(
                MIA_RESERVATION_DATE, timeId, themeId, WAITING.getIdentifier());
        Cookie adminCookie = new Cookie.Builder("token", adminToken).build();

        // when
        new Thread(() -> RestAssured.given().log().all()
                .cookie(adminCookie)
                .when().delete("/admin/reservations/" + reservationId)
                .then().log().all()
        ).start();

        new Thread(() -> RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie(miaCookie)
                .body(waitingReservationSaveRequest)
                .when().post("/reservations")
                .then().log().all()
        ).start();

        // then
        Thread.sleep(1000);
        List<ReservationResponse> waitings = findAllWaitingReservations(adminCookie);
        List<ReservationResponse> bookings = findAllReservations(adminCookie);
        assertSoftly(softly -> {
            softly.assertThat(waitings).hasSize(0);
            softly.assertThat(bookings).hasSize(1)
                    .extracting(ReservationResponse::memberName)
                    .contains(MIA_NAME);
        });
    }

    private List<ReservationResponse> findAllReservations(Cookie adminCookie) {
        ExtractableResponse<Response> response = RestAssured.given()
                .cookie(adminCookie)
                .when().get("/admin/reservations")
                .then().extract();
        return Arrays.stream(response.as(ReservationResponse[].class))
                .toList();
    }

    private List<ReservationResponse> findAllWaitingReservations(Cookie adminCookie) {
        ExtractableResponse<Response> response = RestAssured.given()
                .cookie(adminCookie)
                .when().get("/admin/reservations/waiting")
                .then().extract();
        return Arrays.stream(response.as(ReservationResponse[].class))
                .toList();
    }

    @Test
    @DisplayName("동일한 시간에 예약이 없을 때 예약 대기를 추가한다.")
    void createInvalidWaitingReservation() {
        // given
        Member member = createTestMember(MIA_EMAIL, MIA_NAME);
        String token = createTestToken(member.getEmail().getValue());
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
        ReservationResponse reservationResponse = response.as(ReservationResponse.class);

        // then
        assertSoftly(softly -> {
            checkHttpStatusCreated(softly, response);
            softly.assertThat(reservationResponse.id()).isNotNull();
        });
    }

    @Test
    @DisplayName("예약 날짜가 없는 예약을 추가한다.")
    void createInvalidReservation() {
        // given
        Member member = createTestMember(MIA_EMAIL, MIA_NAME);
        String token = createTestToken(member.getEmail().getValue());
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
        Member member = createTestMember(MIA_EMAIL, MIA_NAME);
        String token = createTestToken(member.getEmail().getValue());
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

    @Test
    @DisplayName("사용자의 예약 중, 대기 중 예약 목록을 조회한다.")
    void findMyReservations() {
        // given
        Long themeId = createTestTheme();
        Long timeId = createTestReservationTime();

        Member mia = createTestMember(MIA_EMAIL, MIA_NAME);
        String miaToken = createTestToken(mia.getEmail().getValue());
        Member tommy = createTestMember(TOMMY_EMAIL, TOMMY_NAME);
        String tommyToken = createTestToken(tommy.getEmail().getValue());

        createTestReservation(MIA_RESERVATION_DATE, timeId, themeId, miaToken, BOOKING);
        createTestReservation(TOMMY_RESERVATION_DATE, timeId, themeId, tommyToken, BOOKING);
        createTestReservation(TOMMY_RESERVATION_DATE, timeId, themeId, miaToken, WAITING);

        Cookie cookie = new Cookie.Builder("token", miaToken).build();

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
            softly.assertThat(myReservationResponses).hasSize(2)
                    .extracting(MyReservationResponse::status)
                    .contains("예약", "1번째 예약대기");
        });
    }

    @TestFactory
    @DisplayName("대기 예약을 추가하고 삭제한다.")
    Stream<DynamicTest> createThenDeleteTheme() {
        return Stream.of(
                dynamicTest("대기 예약을 하나 생성한다.", this::createWaitingReservation),
                dynamicTest("대기 예약이 하나 생성된 나의 예약 목록을 조회한다.", () -> findAllWaitingReservationsWithSize(1)),
                dynamicTest("대기 예약을 취소한다.", this::deleteWaitingReservation),
                dynamicTest("대기 예약이 없는 나의 예약 목록을 조회한다.", () -> findAllWaitingReservationsWithSize(0))
        );
    }

    private void findAllWaitingReservationsWithSize(int expectedSize) {
        // given
        String miaToken = createTestToken(MIA_EMAIL);
        Cookie cookie = new Cookie.Builder("token", miaToken).build();

        //  when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .cookie(cookie)
                .when().get("/reservations/mine")
                .then().log().all()
                .extract();
        List<MyReservationResponse> myReservationResponses = Arrays.stream(response.as(MyReservationResponse[].class))
                .toList();

        // then
        assertThat(myReservationResponses).hasSize(expectedSize);
    }

    void deleteWaitingReservation() {
        // given
        String miaToken = createTestToken(MIA_EMAIL);
        Cookie cookie = new Cookie.Builder("token", miaToken).build();

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .cookie(cookie)
                .when().delete("/reservations/2/waiting")
                .then().log().all()
                .extract();

        // then
        checkHttpStatusNoContent(response);
    }
}
