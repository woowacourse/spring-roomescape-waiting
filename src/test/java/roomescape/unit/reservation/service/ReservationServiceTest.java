package roomescape.unit.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import roomescape.exception.ExistedReservationException;
import roomescape.exception.ReservationNotFoundException;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.infrastructure.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.TimeSlot;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.dto.request.ReservationCondition;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.dto.response.ReservationWithStatusResponse;
import roomescape.reservation.infrastructure.ReservationRepository;
import roomescape.reservation.infrastructure.ThemeRepository;
import roomescape.reservation.infrastructure.TimeSlotRepository;
import roomescape.reservation.infrastructure.WaitingRepository;
import roomescape.reservation.service.ReservationService;
import roomescape.unit.fake.FakeMemberRepository;
import roomescape.unit.fake.FakeReservationRepository;
import roomescape.unit.fake.FakeThemeRepository;
import roomescape.unit.fake.FakeTimeSlotRepository;
import roomescape.unit.fake.FakeWaitingRepository;

class ReservationServiceTest {

    private ReservationRepository reservationRepository;
    private TimeSlotRepository timeSlotRepository;
    private ThemeRepository themeRepository;
    private MemberRepository memberRepository;
    private ReservationService reservationService;
    private WaitingRepository waitingRepository;

    @BeforeEach
    void setUp() {
        reservationRepository = new FakeReservationRepository();
        timeSlotRepository = new FakeTimeSlotRepository();
        themeRepository = new FakeThemeRepository();
        memberRepository = new FakeMemberRepository();
        waitingRepository = new FakeWaitingRepository();
        reservationService = new ReservationService(
                reservationRepository,
                timeSlotRepository,
                themeRepository,
                memberRepository,
                waitingRepository
        );
    }

    @Test
    void 예약을_조회할_수_있다() {
        // given
        TimeSlot timeSlot = timeSlotRepository.save(
                TimeSlot.builder()
                        .id(1L)
                        .startAt(LocalTime.of(10, 0)).build()
        );
        Theme theme1 = themeRepository.save(
                Theme.builder()
                        .name("theme1")
                        .description("desc1")
                        .thumbnail("thumb1").build()
        );
        themeRepository.save(theme1);
        Member member1 = memberRepository.save(
                Member.builder()
                        .name("포라")
                        .email("email1@domain.com")
                        .password("password1")
                        .role(Role.MEMBER).build()
        );
        reservationRepository.save(
                Reservation.builder()
                        .member(member1)
                        .date(LocalDate.of(2025, 7, 25))
                        .timeSlot(timeSlot)
                        .theme(theme1).build()
        );
        reservationRepository.save(
                Reservation.builder()
                        .member(member1)
                        .date(LocalDate.of(2025, 7, 26))
                        .timeSlot(timeSlot)
                        .theme(theme1).build()
        );
        // when
        List<ReservationResponse> all = reservationService.findReservations(
                new ReservationCondition(
                        theme1.getId(),
                        member1.getId(),
                        LocalDate.of(2025, 7, 25),
                        LocalDate.of(2025, 7, 25)
                )
        );
        // then
        assertThat(all.size()).isEqualTo(1);
        assertThat(all.get(0).memberName()).isEqualTo("포라");
    }

    @Test
    void 샤용자가_예약을_조회할_수_있다() {
        // given
        TimeSlot timeSlot1 = TimeSlot.builder()
                .id(1L)
                .startAt(LocalTime.of(10, 0)).build();
        timeSlotRepository.save(timeSlot1);
        Theme theme1 = Theme.builder()
                .name("theme1")
                .description("desc1")
                .thumbnail("thumb1").build();
        themeRepository.save(theme1);
        Member member1 = Member.builder()
                .id(1L)
                .name("포라")
                .email("email1@domain.com")
                .password("password1")
                .role(Role.MEMBER).build();
        memberRepository.save(member1);
        reservationRepository.save(
                Reservation.builder()
                        .member(member1)
                        .date(LocalDate.of(2025, 7, 26))
                        .timeSlot(timeSlot1)
                        .theme(theme1).build()
        );
        // when
        List<ReservationWithStatusResponse> memberReservations = reservationService.findReservationByMemberId(1L);

        // then
        assertThat(memberReservations.size()).isEqualTo(1);
        assertThat(memberReservations.get(0).memberName()).isEqualTo("포라");
    }

    @Test
    void 예약을_추가할_수_있다() {
        // given
        TimeSlot timeSlot1 = TimeSlot.builder()
                .id(1L)
                .startAt(LocalTime.of(10, 0)).build();
        timeSlotRepository.save(timeSlot1);
        Theme theme1 = Theme.builder()
                .name("theme1")
                .description("desc1")
                .thumbnail("thumb1").build();
        themeRepository.save(theme1);
        Member member1 = memberRepository.save(
                Member.builder()
                        .name("name1")
                        .email("email1@domain.com")
                        .password("password1")
                        .role(Role.MEMBER).build()
        );
        Reservation reservation1 = Reservation.builder()
                .member(member1)
                .date(LocalDate.of(2025, 7, 25))
                .timeSlot(timeSlot1)
                .theme(theme1).build();
        // when
        reservationRepository.save(reservation1);

        // then
        List<ReservationResponse> filteredReservation = reservationService.findReservations(
                new ReservationCondition(
                        theme1.getId(),
                        member1.getId(),
                        LocalDate.of(2025, 7, 25),
                        LocalDate.of(2025, 7, 25)
                )
        );
        assertThat(filteredReservation.size()).isEqualTo(1);
        assertThat(filteredReservation.getLast().memberName()).isEqualTo("name1");
    }

    @Test
    void 예약을_삭제할_수_있다() {
        // given
        TimeSlot timeSlot1 = TimeSlot.builder()
                .id(1L)
                .startAt(LocalTime.of(10, 0)).build();
        timeSlotRepository.save(timeSlot1);
        Theme theme1 = Theme.builder()
                .name("theme1")
                .description("desc1")
                .thumbnail("thumb1").build();
        themeRepository.save(theme1);
        Member member1 = memberRepository.save(
                Member.builder()
                        .name("name1")
                        .email("email1@domain.com")
                        .password("password1")
                        .role(Role.MEMBER).build()
        );
        Reservation reservation1 = Reservation.builder()
                .member(member1)
                .date(LocalDate.of(2025, 7, 26))
                .timeSlot(timeSlot1)
                .theme(theme1).build();
        Reservation savedReservation = reservationRepository.save(reservation1);
        // when
        reservationService.cancelReservationAndPromoteWait(savedReservation.getId());

        // then
        List<ReservationResponse> all = reservationService.findReservations(
                new ReservationCondition(null, null, null, null));
        assertThat(all.size()).isEqualTo(0);
    }

    @Test
    void id에_대한_예약이_없을_경우_예외가_발생한다() {
        // when & then
        Assertions.assertThatThrownBy(() -> reservationService.cancelReservationAndPromoteWait(10L))
                .isInstanceOf(ReservationNotFoundException.class);
    }

    @Test
    void 중복_예약하면_예외가_발생한다() {
        // given
        TimeSlot timeSlot1 = timeSlotRepository.save(
                TimeSlot.builder()
                        .id(1L)
                        .startAt(LocalTime.of(10, 0)).build()
        );
        Theme theme1 = themeRepository.save(
                Theme.builder()
                        .name("themeName1")
                        .description("des")
                        .thumbnail("th").build()
        );
        Member member1 = memberRepository.save(
                Member.builder()
                        .name("name1")
                        .email("email1@domain.com")
                        .password("password1")
                        .role(Role.MEMBER).build()
        );
        Reservation reservation1 = Reservation.builder()
                .member(member1)
                .date(LocalDate.of(2025, 7, 25))
                .timeSlot(timeSlot1)
                .theme(theme1).build();
        reservationRepository.save(reservation1);

        // when & then
        assertThatThrownBy(
                () -> reservationService.createReservation(1L, timeSlot1.getId(), theme1.getId(),
                        LocalDate.of(2025, 7, 25)))
                .isInstanceOf(ExistedReservationException.class);
    }

    @Test
    void 예약을_삭제하면_가장_빠른_대기가_예약으로_전환된다() {
        // given
        TimeSlot timeSlot1 = timeSlotRepository.save(
                TimeSlot.builder()
                        .id(1L)
                        .startAt(LocalTime.of(10, 0)).build()
        );
        Theme theme1 = themeRepository.save(
                Theme.builder()
                        .name("theme1")
                        .description("desc1")
                        .thumbnail("thumb1").build()
        );
        Member member1 = memberRepository.save(
                Member.builder()
                        .name("name1")
                        .email("email1@domain.com")
                        .password("password1")
                        .role(Role.MEMBER).build()
        );
        Member member2 = memberRepository.save(
                Member.builder()
                        .name("name1")
                        .email("email1@domain.com")
                        .password("password1")
                        .role(Role.MEMBER).build()
        );
        Reservation reservation = reservationRepository.save(
                Reservation.builder()
                        .member(member1)
                        .date(LocalDate.of(2025, 7, 25))
                        .timeSlot(timeSlot1)
                        .theme(theme1).build()
        );
        Waiting waiting = waitingRepository.save(
                Waiting.builder()
                        .member(member2)
                        .date(LocalDate.of(2025, 7, 25))
                        .timeSlot(timeSlot1)
                        .theme(theme1).build()
        );
        // when
        reservationService.cancelReservationAndPromoteWait(reservation.getId());

        // then
        List<Reservation> reservations = reservationRepository.findAll();
        assertThat(reservations.size()).isEqualTo(1);
        assertThat(reservations.get(0).getMember().getId()).isEqualTo(member2.getId());
    }
}
