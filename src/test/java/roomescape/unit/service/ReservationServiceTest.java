package roomescape.unit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Role;
import roomescape.domain.Theme;
import roomescape.dto.request.ReservationCondition;
import roomescape.dto.response.ReservationResponse;
import roomescape.dto.response.ReservationWithStatusResponse;
import roomescape.exception.ExistedReservationException;
import roomescape.exception.ReservationNotFoundException;
import roomescape.infrastructure.MemberRepository;
import roomescape.infrastructure.ReservationRepository;
import roomescape.infrastructure.ReservationTimeRepository;
import roomescape.infrastructure.ThemeRepository;
import roomescape.service.ReservationService;
import roomescape.unit.fake.FakeMemberRepository;
import roomescape.unit.fake.FakeReservationRepository;
import roomescape.unit.fake.FakeReservationTimeRepository;
import roomescape.unit.fake.FakeThemeRepository;

class ReservationServiceTest {

    private ReservationRepository reservationRepository;
    private ReservationTimeRepository reservationTimeRepository;
    private ThemeRepository themeRepository;
    private MemberRepository memberRepository;
    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        reservationRepository = new FakeReservationRepository();
        reservationTimeRepository = new FakeReservationTimeRepository(reservationRepository);
        themeRepository = new FakeThemeRepository();
        memberRepository = new FakeMemberRepository();
        reservationService = new ReservationService(reservationRepository, reservationTimeRepository, themeRepository,
                memberRepository);
    }

    @Test
    void 예약을_조회할_수_있다() {
        // given
        ReservationTime reservationTime1 = new ReservationTime(1L, LocalTime.of(10, 0));
        reservationTimeRepository.save(reservationTime1);
        Theme theme1 = new Theme(1L, "themeName1", "des", "th");
        themeRepository.save(theme1);
        Member member1 = memberRepository.save(new Member(null, "포라", "email1@domain.com", "password1", Role.MEMBER));
        Member member2 = memberRepository.save(new Member(null, "아마", "email2@domain.com", "password2", Role.MEMBER));
        Reservation reservation1 = Reservation.of(null, member1, LocalDate.of(2025, 7, 25),
                reservationTime1, theme1);
        reservationRepository.save(reservation1);
        Reservation reservation2 = Reservation.of(null, member2, LocalDate.of(2025, 7, 26),
                reservationTime1, theme1);
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
        ReservationTime reservationTime1 = new ReservationTime(1L, LocalTime.of(10, 0));
        reservationTimeRepository.save(reservationTime1);
        Theme theme1 = new Theme(1L, "themeName1", "des", "th");
        themeRepository.save(theme1);
        Member member1 = new Member(1L, "포라", "email1@domain.com", "password1", Role.MEMBER);
        memberRepository.save(member1);
        Reservation reservation1 = Reservation.of(null, member1, LocalDate.of(2025, 7, 25),
                reservationTime1, theme1);
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
        ReservationTime reservationTime1 = new ReservationTime(1L, LocalTime.of(10, 0));
        reservationTimeRepository.save(reservationTime1);
        Theme theme1 = new Theme(1L, "themeName1", "des", "th");
        themeRepository.save(theme1);
        Member member1 = new Member(1L, "name1", "email1@domain.com", "password1", Role.MEMBER);
        memberRepository.save(member1);
        Reservation reservation1 = Reservation.of(null, member1, LocalDate.of(2025, 7, 25),
                reservationTime1, theme1);
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
        ReservationTime reservationTime1 = new ReservationTime(1L, LocalTime.of(10, 0));
        reservationTimeRepository.save(reservationTime1);
        Theme theme1 = new Theme(1L, "themeName1", "des", "th");
        themeRepository.save(theme1);
        Member member1 = new Member(1L, "name1", "email1@domain.com", "password1", Role.MEMBER);
        memberRepository.save(member1);
        Reservation reservation1 = Reservation.of(null, member1, LocalDate.of(2025, 7, 25),
                reservationTime1, theme1);
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
        ReservationTime savedTime = reservationTimeRepository.save(
                new ReservationTime(1L, LocalTime.of(10, 0)));
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
}
