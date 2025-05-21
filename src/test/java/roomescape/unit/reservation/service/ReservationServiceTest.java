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
        TimeSlot timeSlot1 = new TimeSlot(1L, LocalTime.of(10, 0));
        timeSlotRepository.save(timeSlot1);
        Theme theme1 = new Theme(1L, "themeName1", "des", "th");
        themeRepository.save(theme1);
        Member member1 = memberRepository.save(new Member(null, "포라", "email1@domain.com", "password1", Role.MEMBER));
        Member member2 = memberRepository.save(new Member(null, "아마", "email2@domain.com", "password2", Role.MEMBER));
        Reservation reservation1 = Reservation.of(null, member1, LocalDate.of(2025, 7, 25),
                timeSlot1, theme1);
        reservationRepository.save(reservation1);
        Reservation reservation2 = Reservation.of(null, member2, LocalDate.of(2025, 7, 26),
                timeSlot1, theme1);
        reservationRepository.save(reservation2);
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
        TimeSlot timeSlot1 = new TimeSlot(1L, LocalTime.of(10, 0));
        timeSlotRepository.save(timeSlot1);
        Theme theme1 = new Theme(1L, "themeName1", "des", "th");
        themeRepository.save(theme1);
        Member member1 = new Member(1L, "포라", "email1@domain.com", "password1", Role.MEMBER);
        memberRepository.save(member1);
        Reservation reservation1 = Reservation.of(null, member1, LocalDate.of(2025, 7, 25),
                timeSlot1, theme1);
        reservationRepository.save(reservation1);
        // when
        List<ReservationWithStatusResponse> memberReservations = reservationService.findReservationByMemberId(1L);

        // then
        assertThat(memberReservations.size()).isEqualTo(1);
        assertThat(memberReservations.get(0).memberName()).isEqualTo("포라");
    }

    @Test
    void 예약을_추가할_수_있다() {
        // given
        TimeSlot timeSlot1 = new TimeSlot(1L, LocalTime.of(10, 0));
        timeSlotRepository.save(timeSlot1);
        Theme theme1 = new Theme(1L, "themeName1", "des", "th");
        themeRepository.save(theme1);
        Member member1 = new Member(1L, "name1", "email1@domain.com", "password1", Role.MEMBER);
        memberRepository.save(member1);
        Reservation reservation1 = Reservation.of(null, member1, LocalDate.of(2025, 7, 25),
                timeSlot1, theme1);
        // when
        reservationRepository.save(reservation1);

        // then
        List<ReservationResponse> all = reservationService.findReservations(
                new ReservationCondition(
                        theme1.getId(),
                        member1.getId(),
                        LocalDate.of(2025, 7, 25),
                        LocalDate.of(2025, 7, 25)
                )
        );
        assertThat(all.size()).isEqualTo(1);
        assertThat(all.getLast().memberName()).isEqualTo("name1");
    }

    @Test
    void 예약을_삭제할_수_있다() {
        // given
        TimeSlot timeSlot1 = new TimeSlot(1L, LocalTime.of(10, 0));
        timeSlotRepository.save(timeSlot1);
        Theme theme1 = new Theme(1L, "themeName1", "des", "th");
        themeRepository.save(theme1);
        Member member1 = new Member(1L, "name1", "email1@domain.com", "password1", Role.MEMBER);
        memberRepository.save(member1);
        Reservation reservation1 = Reservation.of(null, member1, LocalDate.of(2025, 7, 25),
                timeSlot1, theme1);
        Reservation savedReservation = reservationRepository.save(reservation1);
        // when
        reservationService.deleteReservationById(savedReservation.getId());

        // then
        List<ReservationResponse> all = reservationService.findReservations(
                new ReservationCondition(null, null, null, null));
        assertThat(all.size()).isEqualTo(0);
    }

    @Test
    void id에_대한_예약이_없을_경우_예외가_발생한다() {
        // when & then
        Assertions.assertThatThrownBy(() -> reservationService.deleteReservationById(10L))
                .isInstanceOf(ReservationNotFoundException.class);
    }

    @Test
    void 중복_예약하면_예외가_발생한다() {
        // given
        TimeSlot savedTime = timeSlotRepository.save(
                new TimeSlot(1L, LocalTime.of(10, 0)));
        Theme savedTheme = themeRepository.save(new Theme(null, "themeName1", "des", "th"));
        Member savedMember = memberRepository.save(
                new Member(1L, "name1", "email1@domain.com", "password1", Role.MEMBER));
        Reservation reservation1 = Reservation.of(null, savedMember, LocalDate.of(2025, 7, 25),
                savedTime, savedTheme);
        reservationRepository.save(reservation1);

        // when & then
        assertThatThrownBy(
                () -> reservationService.createReservation(1L, savedTime.getId(), savedTheme.getId(),
                        LocalDate.of(2025, 7, 25)))
                .isInstanceOf(ExistedReservationException.class);
    }

    @Test
    void 예약을_삭제하면_가장_빠른_대기가_예약으로_전환된다() {
        // given
        TimeSlot timeSlot1 = timeSlotRepository.save(new TimeSlot(1L, LocalTime.of(10, 0)));
        Theme theme1 = themeRepository.save(new Theme(1L, "themeName1", "des", "th"));
        Member member1 = memberRepository.save(
                new Member(null, "name1", "email1@domain.com", "password1", Role.MEMBER));
        Member member2 = memberRepository.save(
                new Member(null, "name2", "email2@domain.com", "password2", Role.MEMBER));
        Reservation reservation = reservationRepository.save(
                Reservation.of(null, member1, LocalDate.of(2025, 7, 25), timeSlot1, theme1)
        );
        Waiting waiting = waitingRepository.save(
                Waiting.of(null, member2, LocalDate.of(2025, 7, 25), timeSlot1, theme1)
        );
        // when
        reservationService.deleteReservationById(reservation.getId());

        // then
        List<Reservation> reservations = reservationRepository.findAll();
        assertThat(reservations.size()).isEqualTo(1);
        assertThat(reservations.get(0).getMember().getId()).isEqualTo(member2.getId());
    }
}
