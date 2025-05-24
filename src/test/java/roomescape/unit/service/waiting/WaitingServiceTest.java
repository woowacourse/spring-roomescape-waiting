package roomescape.unit.service.waiting;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.waiting.Waiting;
import roomescape.dto.member.SignupRequestDto;
import roomescape.dto.reservation.AddReservationDto;
import roomescape.dto.reservationtime.AddReservationTimeDto;
import roomescape.dto.theme.AddThemeDto;
import roomescape.dto.waiting.ApplyWaitingRequestDto;
import roomescape.infrastructure.auth.jwt.JwtTokenProvider;
import roomescape.service.member.MemberService;
import roomescape.service.reservation.ReservationService;
import roomescape.service.reservationtime.ReservationTimeService;
import roomescape.service.reserveticket.ReserveTicketService;
import roomescape.service.theme.ThemeService;
import roomescape.service.waiting.WaitingService;
import roomescape.unit.config.ServiceFixture;
import roomescape.unit.repository.member.FakeMemberRepository;
import roomescape.unit.repository.reservation.FakeReservationRepository;
import roomescape.unit.repository.reservation.FakeThemeRepository;
import roomescape.unit.repository.reservationtime.FakeReservationTimeRepository;
import roomescape.unit.repository.reserveticket.FakeReserveTicketRepository;
import roomescape.unit.repository.waiting.FakeWaitingRepository;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class WaitingServiceTest {

    private WaitingService waitingService;
    private MemberService memberService;
    private ReservationService reservationService;
    private ReservationTimeService timeService;
    private ThemeService themeService;

    @BeforeEach
    void setup() {
        JwtTokenProvider jwtTokenProvider = ServiceFixture.jwtTokenProvider();
        FakeMemberRepository fakeMemberRepository = ServiceFixture.fakeMemberRepository();
        BCryptPasswordEncoder bCryptPasswordEncoder = ServiceFixture.passwordEncoder();
        memberService = new MemberService(bCryptPasswordEncoder, fakeMemberRepository, jwtTokenProvider);

        FakeReservationRepository fakeReservationRepository = ServiceFixture.fakeReservationRepository();
        FakeReservationTimeRepository fakeReservationTimeRepository = ServiceFixture.fakeReservationTimeRepository();
        FakeThemeRepository fakeThemeRepository = ServiceFixture.fakeThemeRepository();
        reservationService = new ReservationService(fakeReservationRepository,
                fakeReservationTimeRepository, fakeThemeRepository);
        timeService = new ReservationTimeService(fakeReservationRepository, fakeReservationTimeRepository);
        themeService = new ThemeService(fakeThemeRepository, fakeReservationRepository);

        FakeReserveTicketRepository reservationMemberRepository = ServiceFixture.fakeReserveTicketRepository();
        ReserveTicketService reserveTicketService = new ReserveTicketService(memberService, reservationService,
                reservationMemberRepository);

        FakeWaitingRepository fakeWaitingRepository = ServiceFixture.fakeWaitingRepository();
        waitingService = new WaitingService(fakeWaitingRepository, memberService, timeService, themeService, reserveTicketService);
    }

    @Test
    void 예약대기를_추가할_수_있다() {
        // Given
        LocalDate date = LocalDate.now().plusDays(1);
        Long timeId = timeService.addReservationTime(new AddReservationTimeDto(LocalTime.now().plusMinutes(1)));
        Long themeId = themeService.addTheme(new AddThemeDto("theme", "description", "thumbnail"));
        Long memberId = memberService.signup(new SignupRequestDto("email", "password", "name"));

        // When & Then
        assertThatCode(() -> waitingService.addWaiting(new AddReservationDto(date, timeId, themeId), memberId))
                .doesNotThrowAnyException();
    }

    @Test
    void 동일한_날짜와_시간_테마에는_중복으로_예약대기를_추가할_수_없다() {
        // Given
        LocalDate date = LocalDate.now().plusDays(1);
        Long timeId = timeService.addReservationTime(new AddReservationTimeDto(LocalTime.now().plusMinutes(1)));
        Long themeId = themeService.addTheme(new AddThemeDto("theme", "description", "thumbnail"));
        Long memberId = memberService.signup(new SignupRequestDto("email", "password", "name"));
        waitingService.addWaiting(new AddReservationDto(date, timeId, themeId), memberId);

        // When & Then
        assertThatThrownBy(() -> waitingService.addWaiting(new AddReservationDto(date, timeId, themeId), memberId))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 과거_시간에는_예약대기를_생성할_수_없다() {
        // Given
        LocalDate previousDate = LocalDate.now().minusDays(1);
        Long timeId = timeService.addReservationTime(new AddReservationTimeDto(LocalTime.now().plusMinutes(1)));
        Long themeId = themeService.addTheme(new AddThemeDto("theme", "description", "thumbnail"));
        Long memberId = memberService.signup(new SignupRequestDto("email", "password", "name"));

        // When & Then
        assertThatThrownBy(() -> waitingService.addWaiting(new AddReservationDto(previousDate, timeId, themeId), memberId))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 모든_예약대기를_조회할_수_있다() {
        // Given
        LocalDate date = LocalDate.now().plusDays(1);
        Long timeId = timeService.addReservationTime(new AddReservationTimeDto(LocalTime.now().plusMinutes(1)));
        Long themeId = themeService.addTheme(new AddThemeDto("theme", "description", "thumbnail"));
        Long memberId1 = memberService.signup(new SignupRequestDto("email1", "password", "name1"));
        Long memberId2 = memberService.signup(new SignupRequestDto("email2", "password", "name2"));
        Long memberId3 = memberService.signup(new SignupRequestDto("email3", "password", "name3"));
        Long memberId4 = memberService.signup(new SignupRequestDto("email4", "password", "name4"));
        waitingService.addWaiting(new AddReservationDto(date, timeId, themeId), memberId1);
        waitingService.addWaiting(new AddReservationDto(date, timeId, themeId), memberId2);
        waitingService.addWaiting(new AddReservationDto(date, timeId, themeId), memberId3);
        waitingService.addWaiting(new AddReservationDto(date, timeId, themeId), memberId4);

        // When & Then
        assertThat(waitingService.getAllWaitings()).containsExactlyInAnyOrder(
                new Waiting(1L, date, timeService.getReservationTimeById(timeId), themeService.getThemeById(themeId), memberService.getMemberById(memberId1)),
                new Waiting(2L, date, timeService.getReservationTimeById(timeId), themeService.getThemeById(themeId), memberService.getMemberById(memberId2)),
                new Waiting(3L, date, timeService.getReservationTimeById(timeId), themeService.getThemeById(themeId), memberService.getMemberById(memberId3)),
                new Waiting(4L, date, timeService.getReservationTimeById(timeId), themeService.getThemeById(themeId), memberService.getMemberById(memberId4))
        );
    }

    @Test
    void 예약대기_id에_해당하는_예약대기_엔티티를_조회할_수_있다() {
        // Given
        LocalDate date = LocalDate.now().plusDays(1);
        Long timeId = timeService.addReservationTime(new AddReservationTimeDto(LocalTime.now().plusMinutes(1)));
        Long themeId = themeService.addTheme(new AddThemeDto("theme", "description", "thumbnail"));
        long memberId = memberService.signup(new SignupRequestDto("email", "password", "name"));
        Long savedWaitingId = waitingService.addWaiting(new AddReservationDto(date, timeId, themeId), memberId);

        // When & Then
        assertThat(waitingService.getWaitingById(savedWaitingId)).isEqualTo(new Waiting(savedWaitingId, date, timeService.getReservationTimeById(timeId), themeService.getThemeById(themeId), memberService.getMemberById(memberId)));
    }

    @Test
    void 예약대기_id에_해당하는_예약대기_엔티티가_없다면_예외를_발생시켜야_한다() {
        // Given
        LocalDate date = LocalDate.now().plusDays(1);
        Long timeId = timeService.addReservationTime(new AddReservationTimeDto(LocalTime.now().plusMinutes(1)));
        Long themeId = themeService.addTheme(new AddThemeDto("theme", "description", "thumbnail"));
        Long memberId = memberService.signup(new SignupRequestDto("email", "password", "name"));
        waitingService.addWaiting(new AddReservationDto(date, timeId, themeId), memberId);

        // When & Then
        assertThatThrownBy(() -> waitingService.getWaitingById(5000L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 예약대기_엔티티를_승인하여_예약_엔티티로_등록시킬_수_있다() {
        // Given
        LocalDate date = LocalDate.now().plusDays(1);
        Long timeId = timeService.addReservationTime(new AddReservationTimeDto(LocalTime.now().plusMinutes(1)));
        Long themeId = themeService.addTheme(new AddThemeDto("theme", "description", "thumbnail"));
        long memberId = memberService.signup(new SignupRequestDto("email", "password", "name"));
        Long savedWaitingId = waitingService.addWaiting(new AddReservationDto(date, timeId, themeId), memberId);

        // When
        Long createdReservationId = waitingService.apply(new ApplyWaitingRequestDto(savedWaitingId));

        // Then
        assertAll(() -> {
            assertThat(createdReservationId).isEqualTo(1L);
            assertThatThrownBy(() -> waitingService.getWaitingById(savedWaitingId)).isInstanceOf(IllegalArgumentException.class);
            assertThat(reservationService.getReservationById(createdReservationId)).isEqualTo(new Reservation(createdReservationId, memberService.getMemberById(memberId).getName(), date, timeService.getReservationTimeById(timeId), themeService.getThemeById(themeId)));
        });
    }
}
