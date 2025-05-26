package roomescape.reservation.acceptance;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.helper.TestHelper;
import roomescape.member.entity.Member;
import roomescape.member.entity.RoleType;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.dto.request.WaitingCreateRequest;
import roomescape.reservation.entity.Reservation;
import roomescape.reservation.entity.ReservationTime;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.theme.entity.Theme;
import roomescape.theme.repository.ThemeRepository;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class WaitingAcceptanceTest {

    private static final String DEFAULT_EMAIL = "miso@email.com";
    private static final String DEFAULT_PASSWORD = "miso";
    private static final String DEFAULT_NAME = "미소";

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @BeforeEach
    void setUp() {
        Member member = new Member(DEFAULT_NAME, DEFAULT_EMAIL, DEFAULT_PASSWORD, RoleType.ADMIN);
        memberRepository.save(member);
        Theme theme = new Theme("테마", "설명", "썸네일");
        themeRepository.save(theme);
        ReservationTime reservationTime = new ReservationTime(LocalTime.of(10, 0));
        reservationTimeRepository.save(reservationTime);
    }

    @Test
    @DisplayName("대기 예약을 생성한다.")
    void createWaiting() {
        // given
        String token = TestHelper.login(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        var otherMember = memberRepository.save(new Member("크루", "crew@email.com", "password", RoleType.USER));
        var theme = themeRepository.findAll().getFirst();
        var time = reservationTimeRepository.findAll().getFirst();
        var date = LocalDate.now().plusDays(1);
        reservationRepository.save(new Reservation(date, time, theme, otherMember));

        var waitingRequest = new WaitingCreateRequest(date, time.getId(), theme.getId());

        // when & then
        TestHelper.postWithToken("/waitings", waitingRequest, token)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("date", equalTo(date.toString()))
                .body("startAt", equalTo("10:00:00"))
                .body("themeName", equalTo("테마"))
                .body("memberName", equalTo(DEFAULT_NAME));
    }

    @Test
    @DisplayName("이미 예약한 사용자가 대기 예약을 생성할 수 없다.")
    void createWaitingByReservedMember() {
        // given
        String token = TestHelper.login(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        var theme = themeRepository.findAll().getFirst();
        var time = reservationTimeRepository.findAll().getFirst();
        var date = LocalDate.now().plusDays(1);
        var member = memberRepository.findByEmail(DEFAULT_EMAIL).get();
        reservationRepository.save(new Reservation(date, time, theme, member));

        var waitingRequest = new WaitingCreateRequest(date, time.getId(), theme.getId());

        // when & then
        TestHelper.postWithToken("/waitings", waitingRequest, token)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body(equalTo("이미 예약한 사용자는 해당 예약에 대기 신청할 수 없습니다."));
    }

    @Test
    @DisplayName("모든 대기 예약을 조회한다.")
    void getAllWaitings() {
        // given
        String token = TestHelper.login(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        var otherMember = memberRepository.save(new Member("크루", "crew@email.com", "password", RoleType.USER));
        var theme = themeRepository.findAll().getFirst();
        var time = reservationTimeRepository.findAll().getFirst();
        var date = LocalDate.now().plusDays(1);
        reservationRepository.save(new Reservation(date, time, theme, otherMember));

        var waitingRequest = new WaitingCreateRequest(date, time.getId(), theme.getId());
        TestHelper.postWithToken("/waitings", waitingRequest, token);

        // when & then
        TestHelper.getWithToken("/admin/waitings", token)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("$", hasSize(1))
                .body("[0].id", equalTo(1))
                .body("[0].date", equalTo(date.toString()))
                .body("[0].startAt", equalTo("10:00:00"))
                .body("[0].memberName", equalTo(DEFAULT_NAME))
                .body("[0].themeName", equalTo("테마"));
    }

    @Test
    @DisplayName("대기 예약을 삭제한다.")
    void deleteWaiting() {
        // given
        String token = TestHelper.login(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        var otherMember = memberRepository.save(new Member("크루", "crew@email.com", "password", RoleType.USER));
        var theme = themeRepository.findAll().getFirst();
        var time = reservationTimeRepository.findAll().getFirst();
        var date = LocalDate.now().plusDays(1);
        reservationRepository.save(new Reservation(date, time, theme, otherMember));

        var waitingRequest = new WaitingCreateRequest(date, time.getId(), theme.getId());
        TestHelper.postWithToken("/waitings", waitingRequest, token);

        // when & then
        TestHelper.deleteWithToken("/waitings/1", token)
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

        TestHelper.getWithToken("/admin/waitings", token)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("$", hasSize(0));
    }

    @Test
    @DisplayName("다른 사용자의 대기 예약을 삭제할 수 없다.")
    void deleteWaitingByOtherMember() {
        // given
        String token = TestHelper.login(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        var otherMember = memberRepository.save(new Member("크루", "crew@email.com", "password", RoleType.USER));
        var theme = themeRepository.findAll().getFirst();
        var time = reservationTimeRepository.findAll().getFirst();
        var date = LocalDate.now().plusDays(1);
        reservationRepository.save(new Reservation(date, time, theme, otherMember));

        var waitingRequest = new WaitingCreateRequest(date, time.getId(), theme.getId());
        TestHelper.postWithToken("/waitings", waitingRequest, token);

        String otherToken = TestHelper.login("crew@email.com", "password");

        // when & then
        TestHelper.deleteWithToken("/waitings/1", otherToken)
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body(equalTo("예약 대기는 본인만 삭제할 수 있습니다."));
    }

    @Test
    @DisplayName("대기 예약을 승인한다.")
    void approveWaiting() {
        // given
        String token = TestHelper.login(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        var otherMember = memberRepository.save(new Member("크루", "crew@email.com", "password", RoleType.USER));
        var theme = themeRepository.findAll().getFirst();
        var time = reservationTimeRepository.findAll().getFirst();
        var date = LocalDate.now().plusDays(1);
        var reservation = reservationRepository.save(new Reservation(date, time, theme, otherMember));

        var waitingRequest = new WaitingCreateRequest(date, time.getId(), theme.getId());
        TestHelper.postWithToken("/waitings", waitingRequest, token);

        // when
        reservationRepository.delete(reservation);

        // then
        TestHelper.postWithToken("/admin/waitings/1/approve", token)
                .then()
                .statusCode(HttpStatus.OK.value());

        TestHelper.getWithToken("/admin/waitings", token)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("$", hasSize(0));
    }

    @Test
    @DisplayName("이미 예약이 있는 상태에서 대기 예약을 승인할 수 없다.")
    void approveWaitingWithExistingReservation() {
        // given
        String token = TestHelper.login(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        var otherMember = memberRepository.save(new Member("크루", "crew@email.com", "password", RoleType.USER));
        var theme = themeRepository.findAll().getFirst();
        var time = reservationTimeRepository.findAll().getFirst();
        var date = LocalDate.now().plusDays(1);
        reservationRepository.save(new Reservation(date, time, theme, otherMember));

        var waitingRequest = new WaitingCreateRequest(date, time.getId(), theme.getId());
        TestHelper.postWithToken("/waitings", waitingRequest, token);

        // when & then
        TestHelper.postWithToken("/admin/waitings/1/approve", token)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body(equalTo("이미 예약이 존재하여 대기자를 승인할 수 없습니다."));
    }
}
