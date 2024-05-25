package roomescape.controller.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static roomescape.TestFixture.ADMIN;
import static roomescape.TestFixture.ADMIN_LOGIN_REQUEST;
import static roomescape.TestFixture.MEMBER1;
import static roomescape.TestFixture.RESERVATION_TIME_10AM;
import static roomescape.TestFixture.THEME1;
import static roomescape.TestFixture.TOMORROW;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import roomescape.TestFixture;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Status;
import roomescape.domain.Theme;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.dto.request.ReservationCreateRequest;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class AdminReservationControllerTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationTimeRepository timeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @AfterEach
    void tearDown() {
        reservationRepository.deleteAllInBatch();
        timeRepository.deleteAllInBatch();
        themeRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @DisplayName("어드민이 예약을 추가한다.")
    @Test
    void createReservation() {
        // given
        memberRepository.save(ADMIN);
        String accessToken = TestFixture.getTokenAfterLogin(ADMIN_LOGIN_REQUEST);

        ReservationCreateRequest request = createReservationRequest(MEMBER1, TOMORROW, RESERVATION_TIME_10AM, THEME1);

        // then
        RestAssured.given().log().all()
                .header("cookie", accessToken)
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/admin/reservations")
                .then().log().all()
                .assertThat().statusCode(HttpStatus.CREATED.value());
    }

    @DisplayName("예약을 삭제한다.")
    @Test
    void deleteReservationSuccess() {
        // given
        Reservation saved = reserveAfterSave(MEMBER1, TOMORROW, RESERVATION_TIME_10AM, THEME1, Status.CONFIRMED);

        memberRepository.save(ADMIN);
        String accessToken = TestFixture.getTokenAfterLogin(ADMIN_LOGIN_REQUEST);

        // when & then
        RestAssured.given().log().all()
                .header("cookie", accessToken)
                .when().delete("/admin/reservations/" + saved.getId())
                .then().log().all()
                .assertThat().statusCode(HttpStatus.NO_CONTENT.value());
    }

    @DisplayName("모든 대기 중인 예약을 조회한다.")
    @Test
    void findAllWaiting() {
        // given
        reserveAfterSave(MEMBER1, TOMORROW, RESERVATION_TIME_10AM, THEME1, Status.WAITING);

        memberRepository.save(ADMIN);
        String accessToken = TestFixture.getTokenAfterLogin(ADMIN_LOGIN_REQUEST);

        // when & then
        RestAssured.given().log().all()
                .header("cookie", accessToken)
                .when().get("/admin/reservations/waiting")
                .then().log().all()
                .body("count", is(1));
    }

    @DisplayName("대기 중인 예약을 승인한다.")
    @Test
    void approveWaiting() {
        // given
        Reservation waiting = reserveAfterSave(MEMBER1, TOMORROW, RESERVATION_TIME_10AM, THEME1, Status.WAITING);

        memberRepository.save(ADMIN);
        String accessToken = TestFixture.getTokenAfterLogin(ADMIN_LOGIN_REQUEST);

        // when
        RestAssured.given().log().all()
                .header("cookie", accessToken)
                .when().post("/admin/reservations/waiting/{id}/approve", waiting.getId())
                .then().log().all()
                .statusCode(HttpStatus.OK.value());

        // then
        reservationRepository.findById(waiting.getId())
                .ifPresentOrElse(r -> assertThat(r.getStatus()).isEqualTo(Status.CONFIRMED), AssertionError::new);
    }

    @DisplayName("대기 중인 예약을 거절한다.")
    @Test
    void denyWaiting() {
        // given
        Reservation waiting = reserveAfterSave(MEMBER1, TOMORROW, RESERVATION_TIME_10AM, THEME1, Status.WAITING);
        memberRepository.save(ADMIN);
        String accessToken = TestFixture.getTokenAfterLogin(ADMIN_LOGIN_REQUEST);

        // when
        RestAssured.given().log().all()
                .header("cookie", accessToken)
                .when().post("/admin/reservations/waiting/{id}/deny", waiting.getId())
                .then().log().all()
                .statusCode(HttpStatus.NO_CONTENT.value());

        // then
        assertThat(reservationRepository.findById(waiting.getId())).isEmpty();
    }

    private Reservation reserveAfterSave(Member member, LocalDate date, ReservationTime time, Theme theme,
                                         Status status) {
        Member savedMember = memberRepository.save(member);
        ReservationTime savedReservationTime = timeRepository.save(time);
        Theme savedTheme = themeRepository.save(theme);

        return reservationRepository.save(new Reservation(savedMember, date, savedReservationTime, savedTheme, status));
    }

    private ReservationCreateRequest createReservationRequest(Member member, LocalDate date, ReservationTime time,
                                                              Theme theme) {
        Member savedMember = memberRepository.save(member);
        ReservationTime savedReservationTime = timeRepository.save(time);
        Theme savedTheme = themeRepository.save(theme);

        return new ReservationCreateRequest(savedMember.getId(), date, savedReservationTime.getId(),
                savedTheme.getId());
    }
}
