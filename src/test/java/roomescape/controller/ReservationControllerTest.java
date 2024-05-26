package roomescape.controller;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import roomescape.BaseControllerTest;
import roomescape.TestFixture;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Status;
import roomescape.domain.Theme;
import roomescape.service.dto.request.ReservationCreateMemberRequest;

class ReservationControllerTest extends BaseControllerTest {

    @DisplayName("모든 예약 내역을 조회한다.")
    @Test
    void findAllReservations() {
        // given
        reserveAfterSave(TestFixture.getMember1(),
                TestFixture.TOMORROW, TestFixture.getReservationTime10AM(), TestFixture.getTheme1(), Status.CONFIRMED);

        // when & then
        RestAssured.given().log().all()
                .header("cookie", getMember2WithToken())
                .when().get("/reservations")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .body("count", is(1));
    }

    @DisplayName("멤버가 예약을 성공적으로 추가한다.")
    @Test
    void createMemberReservation() {
        // given
        ReservationCreateMemberRequest request = createMemberReservationRequest(TestFixture.TOMORROW,
                TestFixture.getReservationTime10AM(),
                TestFixture.getTheme1());

        // when & then
        RestAssured.given().log().all()
                .header("cookie", getMember1WithToken())
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/reservations")
                .then().log().all().assertThat().statusCode(HttpStatus.CREATED.value());
    }

    @DisplayName("멤버가 예약 대기를 성공적으로 추가한다.")
    @Test
    void createMemberWaiting() {
        // given
        Reservation saved = reserveAfterSave(TestFixture.getMember1(),
                TestFixture.TOMORROW, TestFixture.getReservationTime10AM(), TestFixture.getTheme1(),
                Status.CONFIRMED);
        ReservationCreateMemberRequest request = createMemberReservationRequest(TestFixture.TOMORROW, saved.getTime(),
                saved.getTheme());

        // when & then
        RestAssured.given().log().all()
                .header("cookie", getMember2WithToken())
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/reservations/waiting")
                .then().log().all().assertThat().statusCode(HttpStatus.CREATED.value());
    }

    @DisplayName("멤버의 예약 및 대기 목록을 조회한다.")
    @Test
    void findMyReservationsAndWaiting() {
        // given
        Reservation saved = reserveAfterSave(TestFixture.getMember1(),
                TestFixture.TOMORROW, TestFixture.getReservationTime10AM(), TestFixture.getTheme1(),
                Status.CONFIRMED);
        reserveBySavedMember(saved.getMember(), TestFixture.TOMORROW, TestFixture.getReservationTime11AM(),
                TestFixture.getTheme2(),
                Status.WAITING);
        String accessToken = TestFixture.getTokenAfterLogin(TestFixture.MEMBER1_LOGIN_REQUEST);

        // when & then
        RestAssured.given().log().all()
                .header("cookie", accessToken)
                .contentType(ContentType.JSON)
                .when().get("/reservations/mine")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .body("count", is(2));
    }

    @DisplayName("현재보다 이전 시간으로의 예약 시도 시 400을 응답한다.")
    @Test
    void outdatedReservation() {
        // given
        ReservationCreateMemberRequest request = createMemberReservationRequest(LocalDate.now().minusDays(1),
                TestFixture.getReservationTime10AM(), TestFixture.getTheme1());

        // when & then
        RestAssured.given().log().all()
                .header("cookie", getMember1WithToken())
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/reservations")
                .then().log().all().assertThat().statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @DisplayName("예약을 추가할 때 이미 예약이 존재하는 경우 400을 응답한다.")
    @Test
    void duplicateReservation() {
        // given
        Reservation saved = reserveAfterSave(TestFixture.getMember1(),
                TestFixture.TOMORROW, TestFixture.getReservationTime10AM(), TestFixture.getTheme1(),
                Status.CONFIRMED);
        ReservationCreateMemberRequest request = new ReservationCreateMemberRequest(
                TestFixture.TOMORROW, saved.getTime().getId(),
                saved.getTheme().getId());

        // when & then
        RestAssured.given().log().all()
                .header("cookie", getMember2WithToken())
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/reservations")
                .then().log().all().assertThat().statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @DisplayName("예약과 대기를 동시에 할 수 없다.")
    @Test
    void waitingAfterReserve() {
        // given
        Reservation saved = reserveAfterSave(TestFixture.getMember1(),
                TestFixture.TOMORROW, TestFixture.getReservationTime10AM(), TestFixture.getTheme1(),
                Status.CONFIRMED);
        String accessToken = TestFixture.getTokenAfterLogin(TestFixture.MEMBER1_LOGIN_REQUEST);

        ReservationCreateMemberRequest request = new ReservationCreateMemberRequest(
                TestFixture.TOMORROW, saved.getTime().getId(),
                saved.getTheme().getId());

        // when & then
        RestAssured.given().log().all()
                .header("cookie", accessToken)
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/reservations/waiting")
                .then().log().all().assertThat().statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @DisplayName("멤버가 예약 대기를 취소한다.")
    @Test
    void cancelWaiting() {
        // given
        Reservation saved = reserveAfterSave(TestFixture.getMember1(),
                TestFixture.TOMORROW, TestFixture.getReservationTime10AM(), TestFixture.getTheme1(),
                Status.WAITING);
        String accessToken = TestFixture.getTokenAfterLogin(TestFixture.MEMBER1_LOGIN_REQUEST);

        // when & then
        RestAssured.given().log().all()
                .header("cookie", accessToken)
                .when().delete("/reservations/waiting/{id}", saved.getId())
                .then().log().all().assertThat().statusCode(HttpStatus.NO_CONTENT.value());
    }

    @DisplayName("존재하지 않는 예약 대기를 취소하면 404를 응답한다.")
    @Test
    void cancelNotExistWaiting() {
        // given
        Reservation saved = reserveAfterSave(TestFixture.getMember1(),
                TestFixture.TOMORROW, TestFixture.getReservationTime10AM(), TestFixture.getTheme1(),
                Status.WAITING);
        String accessToken = TestFixture.getTokenAfterLogin(TestFixture.MEMBER1_LOGIN_REQUEST);

        // when & then
        RestAssured.given().log().all()
                .header("cookie", accessToken)
                .when().delete("/reservations/waiting/{id}", saved.getId() + 1)
                .then().log().all()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    private Reservation reserveAfterSave(Member member, LocalDate date, ReservationTime time, Theme theme,
                                         Status status) {
        Member savedMember = memberRepository.save(member);
        ReservationTime savedReservationTime = timeRepository.save(time);
        Theme savedTheme = themeRepository.save(theme);

        return reservationRepository.save(new Reservation(savedMember, date, savedReservationTime, savedTheme, status));
    }

    private Reservation reserveBySavedMember(Member member, LocalDate date, ReservationTime time, Theme theme,
                                             Status status) {
        ReservationTime savedReservationTime = timeRepository.save(time);
        Theme savedTheme = themeRepository.save(theme);

        return reservationRepository.save(new Reservation(member, date, savedReservationTime, savedTheme, status));
    }

    private ReservationCreateMemberRequest createMemberReservationRequest(LocalDate date, ReservationTime time,
                                                                          Theme theme) {
        ReservationTime savedReservationTime = timeRepository.save(time);
        Theme savedTheme = themeRepository.save(theme);

        return new ReservationCreateMemberRequest(date, savedReservationTime.getId(), savedTheme.getId());
    }
}
