package roomescape.waiting.acceptance;

import static org.hamcrest.core.Is.is;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.helper.TestHelper;
import roomescape.member.entity.Member;
import roomescape.member.entity.RoleType;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.entity.Reservation;
import roomescape.reservation.entity.ReservationSlot;
import roomescape.reservation.entity.ReservationTime;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationSlotRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.theme.entity.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.waiting.dto.request.WaitingCreateRequest;
import roomescape.waiting.entity.Waiting;
import roomescape.waiting.repository.WaitingRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class WaitingAcceptanceTest {

    private static final String DEFAULT_EMAIL = "miso@email.com";
    private static final String DEFAULT_PASSWORD = "miso";
    private static final String DEFAULT_NAME = "미소";

    private static final String DEFAULT_ADMIN_EMAIL = "admin@email.com";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin";
    private static final String DEFAULT_ADMIN_NAME = "어드민";

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private WaitingRepository waitingRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationSlotRepository reservationSlotRepository;

    @BeforeEach
    void init() {
        Member member = new Member(DEFAULT_NAME, DEFAULT_EMAIL, DEFAULT_PASSWORD, RoleType.USER);
        memberRepository.save(member);
        Member admin = new Member(DEFAULT_ADMIN_NAME, DEFAULT_ADMIN_EMAIL, DEFAULT_ADMIN_PASSWORD, RoleType.ADMIN);
        memberRepository.save(admin);
    }

    @DisplayName("로그인을 하지 않으면 예약 대기를 생성할 수 없다.")
    @Test
    void cantWaitingWhenNotLogin() {
        // given
        var date = createTomorrow();
        var theme = createDefaultTheme();
        var time = createDefaultReservationTime();

        var request = new WaitingCreateRequest(date, time.getId(), theme.getId());

        // when & then
        TestHelper.post("/waitings", request)
                .then()
                .assertThat()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @DisplayName("로그인한 사용자는 예약 대기를 생성할 수 있다.")
    @Test
    void createWaitingWhenLogin() {
        // given
        var token = TestHelper.login(DEFAULT_EMAIL, DEFAULT_PASSWORD);

        var date = createTomorrow();
        var theme = createDefaultTheme();
        var time = createDefaultReservationTime();
        var reservationSlot = reservationSlotRepository.save(new ReservationSlot(date, time, theme));
        createOtherMemberReservation(reservationSlot);
        var request = new WaitingCreateRequest(date, time.getId(), theme.getId());

        // when & then
        TestHelper.postWithToken("/waitings", request, token)
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("date", is(date.toString()))
                .body("time", is(time.getStartAt().toString()))
                .body("theme", is(theme.getName()));
    }

    @DisplayName("어드민이 아니라면 예약 대기 목록을 전체 조회할 수 없다.")
    @Test
    void cantGetAllWaitingsNotAdmin() {
        // given
        var token = TestHelper.login(DEFAULT_EMAIL, DEFAULT_PASSWORD);

        var date = createTomorrow();
        var theme = createDefaultTheme();
        var time = createDefaultReservationTime();
        var reservationSlot = reservationSlotRepository.save(new ReservationSlot(date, time, theme));
        createOtherMemberReservation(reservationSlot);

        // when & then
        TestHelper.getWithToken("/waitings", token)
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @DisplayName("어드민은 예약 대기 목록을 전체 조회할 수 있다.")
    @Test
    void getAllWaitings() {
        // given
        var token = TestHelper.login(DEFAULT_ADMIN_EMAIL, DEFAULT_ADMIN_PASSWORD);

        var date = createTomorrow();
        var theme = createDefaultTheme();
        var time = createDefaultReservationTime();
        var reservationSlot = reservationSlotRepository.save(new ReservationSlot(date, time, theme));
        createOtherMemberReservation(reservationSlot);
        createWaiting(reservationSlot);

        // when & then
        TestHelper.getWithToken("/waitings", token)
                .then()
                .log()
                .all()
                .statusCode(HttpStatus.OK.value())
                .body("[0].date", is(date.toString()))
                .body("[0].time", is(time.getStartAt().toString()))
                .body("[0].theme", is(theme.getName()));
    }

    @DisplayName("예약 대기를 승인할 수 있다.")
    @Test
    void acceptWaiting() {
        // given
        var token = TestHelper.login(DEFAULT_ADMIN_EMAIL, DEFAULT_ADMIN_PASSWORD);

        var date = createTomorrow();
        var theme = createDefaultTheme();
        var time = createDefaultReservationTime();
        var reservationSlot = reservationSlotRepository.save(new ReservationSlot(date, time, theme));
        createOtherMemberReservation(reservationSlot);
        var waiting = createWaiting(reservationSlot);

        // when & then
        TestHelper.postWithToken("/waitings/accept/" + waiting.getId(), token)
                .then()
                .statusCode(HttpStatus.OK.value());
    }

    @DisplayName("본인의 예약 대기를 삭제할 수 있다.")
    @Test
    void deleteMyWaiting() {
        // given
        var token = TestHelper.login(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        var date = createTomorrow();
        var theme = createDefaultTheme();
        var time = createDefaultReservationTime();
        var reservationSlot = reservationSlotRepository.save(new ReservationSlot(date, time, theme));
        createOtherMemberReservation(reservationSlot);
        var waiting = createWaiting(reservationSlot);

        // when & then
        TestHelper.deleteWithToken("/waitings/" + waiting.getId(), token)
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    private Waiting createWaiting(ReservationSlot reservationSlot) {
        Member member = memberRepository.findByEmail(DEFAULT_EMAIL)
                .orElse(null);
        Waiting waiting = new Waiting(reservationSlot, member);
        return waitingRepository.save(waiting);
    }

    private void createOtherMemberReservation(ReservationSlot reservationSlot) {
        Member other = new Member("other", "other@email.com", "password", RoleType.USER);
        memberRepository.save(other);
        var reservation = new Reservation(reservationSlot, other);
        reservationRepository.save(reservation);
    }

    private ReservationTime createDefaultReservationTime() {
        ReservationTime reservationTime = new ReservationTime(LocalTime.of(10, 0));
        return reservationTimeRepository.save(reservationTime);
    }

    private LocalDate createTomorrow() {
        return LocalDate.now().plusDays(1);
    }

    private Theme createDefaultTheme() {
        Theme theme = new Theme("테마", "설명", "썸네일");
        return themeRepository.save(theme);
    }
}
