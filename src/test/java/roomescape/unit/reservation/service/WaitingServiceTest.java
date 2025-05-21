package roomescape.unit.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import roomescape.exception.CannotWaitWithoutReservationException;
import roomescape.exception.ExistedReservationException;
import roomescape.exception.ExistedWaitingException;
import roomescape.exception.WaitingNotFoundException;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.infrastructure.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.TimeSlot;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.dto.request.WaitingRequest;
import roomescape.reservation.dto.response.WaitingResponse;
import roomescape.reservation.dto.response.WaitingWithRankResponse;
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

    @Test
    void 회원의_대기를_대기순서와_함께_조회한다() {
        // given
        Theme theme = themeRepository.save(
                Theme.builder()
                        .name("theme1")
                        .description("desc1")
                        .thumbnail("thumb1").build()
        );
        TimeSlot timeSlot = timeSlotRepository.save(
                TimeSlot.builder()
                        .startAt(LocalTime.of(9, 0)).build()
        );
        Member member1 = memberRepository.save(
                Member.builder()
                        .name("name1")
                        .email("email1@domain.com")
                        .password("password1").bulid()
        );
        Member member2 = memberRepository.save(
                Member.builder()
                        .name("name2")
                        .email("email2@domain.com")
                        .password("password2").bulid()
        );
        LocalDate date = LocalDate.of(2025, 1, 1);
        waitingRepository.save(
                Waiting.builder()
                        .date(date)
                        .member(member2)
                        .theme(theme)
                        .timeSlot(timeSlot)
                        .build()
        );
        waitingRepository.save(
                Waiting.builder()
                        .date(date)
                        .member(member1)
                        .theme(theme)
                        .timeSlot(timeSlot)
                        .build()
        );
        // when
        List<WaitingWithRankResponse> waitings = waitingService.findWaitingByMemberId(member1.getId());
        // then
        assertThat(waitings).hasSize(1);
        assertThat(waitings.get(0).getRank()).isEqualTo(2);
    }

    @Test
    void 대기id로_대기를_삭제한다() {
        // given
        Theme theme = themeRepository.save(
                Theme.builder()
                        .name("theme1")
                        .description("desc1")
                        .thumbnail("thumb1").build()
        );
        TimeSlot timeSlot = timeSlotRepository.save(
                TimeSlot.builder()
                        .startAt(LocalTime.of(9, 0)).build()
        );
        Member member1 = memberRepository.save(
                Member.builder()
                        .name("name1")
                        .email("email1@domain.com")
                        .password("password1").bulid()
        );
        Member member2 = memberRepository.save(
                Member.builder()
                        .name("name2")
                        .email("email2@domain.com")
                        .password("password2").bulid()
        );
        LocalDate date = LocalDate.of(2025, 1, 1);
        waitingRepository.save(
                Waiting.builder()
                        .date(date)
                        .member(member2)
                        .theme(theme)
                        .timeSlot(timeSlot)
                        .build()
        );
        Waiting waiting = waitingRepository.save(
                Waiting.builder()
                        .date(date)
                        .member(member1)
                        .theme(theme)
                        .timeSlot(timeSlot)
                        .build()
        );
        // when
        waitingService.deleteWaitingById(member1.getId(), waiting.getId());
        // then
        Optional<Waiting> findWaiting = waitingRepository.findById(waiting.getId());
        assertThat(findWaiting).isEmpty();
    }

    @Test
    void 대기id에_해당하는_대기가_없으면_예외가_발생한다() {
        // given
        Member member1 = memberRepository.save(
                Member.builder()
                        .name("name1")
                        .email("email1@domain.com")
                        .password("password1").bulid()
        );
        // when & then
        assertThatThrownBy(() -> waitingService.deleteWaitingById(member1.getId(), 1L))
                .isInstanceOf(WaitingNotFoundException.class);
    }

    @Test
    void 모든_대기를_조회한다() {
        // given
        Theme theme = Theme.builder()
                .name("theme1")
                .description("desc1")
                .thumbnail("thumb1").build();
        TimeSlot timeSlot = TimeSlot.builder()
                .startAt(LocalTime.of(9, 0)).build();
        Member member = Member.builder()
                .name("name1")
                .email("email1@domain.com")
                .password("password1").bulid();
        waitingRepository.save(
                Waiting.builder()
                        .date(LocalDate.of(2025, 1, 1))
                        .member(member)
                        .theme(theme)
                        .timeSlot(timeSlot).build()
        );
        waitingRepository.save(
                Waiting.builder()
                        .date(LocalDate.of(2025, 1, 2))
                        .member(member)
                        .theme(theme)
                        .timeSlot(timeSlot).build()
        );
        // when
        List<WaitingResponse> waitings = waitingService.findAllWaitings();
        // then
        assertThat(waitings).hasSize(2);
    }
}