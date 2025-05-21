package roomescape.unit.reservation.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import roomescape.exception.CannotWaitWithoutReservationException;
import roomescape.exception.ExistedReservationException;
import roomescape.exception.ExistedWaitingException;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.infrastructure.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.TimeSlot;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.dto.request.WaitingRequest;
import roomescape.reservation.infrastructure.ReservationRepository;
import roomescape.reservation.infrastructure.ThemeRepository;
import roomescape.reservation.infrastructure.TimeSlotRepository;
import roomescape.reservation.infrastructure.WaitingRepository;
import roomescape.reservation.service.WaitingService;
import roomescape.unit.fake.FakeMemberRepository;
import roomescape.unit.fake.FakeReservationRepository;
import roomescape.unit.fake.FakeThemeRepository;
import roomescape.unit.fake.FakeTimeSlotRepository;
import roomescape.unit.fake.FakeWaitingRepository;

class WaitingServiceTest {

    private final WaitingService waitingService;
    private final WaitingRepository waitingRepository;
    private final ThemeRepository themeRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final MemberRepository memberRepository;
    private final ReservationRepository reservationRepository;

    public WaitingServiceTest() {
        this.waitingRepository = new FakeWaitingRepository();
        this.themeRepository = new FakeThemeRepository();
        this.timeSlotRepository = new FakeTimeSlotRepository();
        this.memberRepository = new FakeMemberRepository();
        this.reservationRepository = new FakeReservationRepository();
        this.waitingService = new WaitingService(waitingRepository, themeRepository, timeSlotRepository,
                memberRepository, reservationRepository);
    }

    @Test
    void 대기를_생성한다() {
        // given
        Member member1 = Member.builder()
                .name("member1")
                .email("email1@domain.com")
                .password("password1")
                .role(Role.MEMBER).bulid();
        Member member2 = Member.builder()
                .name("member2")
                .email("email2@domain.com")
                .password("password2")
                .role(Role.MEMBER).bulid();
        Theme theme = Theme.builder()
                .name("theme1")
                .description("description")
                .thumbnail("thumbnail").build();
        TimeSlot timeSlot = TimeSlot.builder().startAt(LocalTime.of(9, 0)).build();
        var savedMember1 = memberRepository.save(member1);
        var savedMember2 = memberRepository.save(member2);
        var savedTheme = themeRepository.save(theme);
        var savedTimeSlot = timeSlotRepository.save(timeSlot);
        Reservation reservation = Reservation.builder()
                .date(LocalDate.of(2025, 1, 1))
                .theme(savedTheme)
                .member(savedMember1)
                .timeSlot(savedTimeSlot).build();
        reservationRepository.save(reservation);
        WaitingRequest request = new WaitingRequest(LocalDate.of(2025, 1, 1), savedTimeSlot.getId(),
                savedTheme.getId());
        // when
        assertThatCode(() -> waitingService.createWaiting(savedMember2.getId(), request))
                .doesNotThrowAnyException();
    }

    @Test
    void 예약이_없는데_대기를_생성할_경우_예외가_발생한다() {
        // given
        Member member = Member.builder()
                .name("member1")
                .email("email1@domain.com")
                .password("password1")
                .role(Role.MEMBER).bulid();
        Theme theme = Theme.builder()
                .name("theme1")
                .description("description")
                .thumbnail("thumbnail").build();
        TimeSlot timeSlot = TimeSlot.builder().startAt(LocalTime.of(9, 0)).build();
        var savedMember = memberRepository.save(member);
        var savedTheme = themeRepository.save(theme);
        var savedTimeSlot = timeSlotRepository.save(timeSlot);
        WaitingRequest request = new WaitingRequest(LocalDate.of(2025, 1, 1), savedTimeSlot.getId(),
                savedTheme.getId());
        // when
        assertThatThrownBy(() -> waitingService.createWaiting(savedMember.getId(), request))
                .isInstanceOf(CannotWaitWithoutReservationException.class);
    }

    @Test
    void 본인의_대기가_이미_존재할_경우_예외가_발생한다() {
        // given
        Member member = Member.builder()
                .name("member1")
                .email("email1@domain.com")
                .password("password1")
                .role(Role.MEMBER).bulid();
        Theme theme = Theme.builder()
                .name("theme1")
                .description("description")
                .thumbnail("thumbnail").build();
        TimeSlot timeSlot = TimeSlot.builder().startAt(LocalTime.of(9, 0)).build();
        var savedMember = memberRepository.save(member);
        var savedTheme = themeRepository.save(theme);
        var savedTimeSlot = timeSlotRepository.save(timeSlot);
        Waiting waiting = Waiting.builder()
                .member(savedMember)
                .theme(savedTheme)
                .date(LocalDate.of(2025, 1, 1))
                .timeSlot(savedTimeSlot).build();
        waitingRepository.save(waiting);
        WaitingRequest request = new WaitingRequest(LocalDate.of(2025, 1, 1), savedTimeSlot.getId(),
                savedTheme.getId());
        // when
        assertThatThrownBy(() -> waitingService.createWaiting(savedMember.getId(), request))
                .isInstanceOf(ExistedWaitingException.class);
    }

    @Test
    void 본인의_예약이_이미_존재할_경우_예외가_발생한다() {
        // given
        Member member = Member.builder()
                .name("member1")
                .email("email1@domain.com")
                .password("password1")
                .role(Role.MEMBER).bulid();
        Theme theme = Theme.builder()
                .name("theme1")
                .description("description")
                .thumbnail("thumbnail").build();
        TimeSlot timeSlot = TimeSlot.builder().startAt(LocalTime.of(9, 0)).build();
        var savedMember = memberRepository.save(member);
        var savedTheme = themeRepository.save(theme);
        var savedTimeSlot = timeSlotRepository.save(timeSlot);
        Reservation reservation = Reservation.builder()
                .member(savedMember)
                .theme(savedTheme)
                .date(LocalDate.of(2025, 1, 1))
                .timeSlot(savedTimeSlot).build();
        reservationRepository.save(reservation);
        WaitingRequest request = new WaitingRequest(LocalDate.of(2025, 1, 1), savedTimeSlot.getId(),
                savedTheme.getId());
        // when
        assertThatThrownBy(() -> waitingService.createWaiting(savedMember.getId(), request))
                .isInstanceOf(ExistedReservationException.class);
    }
}