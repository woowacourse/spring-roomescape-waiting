package roomescape.reservation.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private MemberRepository memberRepository;

    @Test
    void 예약_전체_조회() {
        // when & then
        assertThat(reservationRepository.findAll()).hasSize(0);
    }

    @Test
    void 예약_저장() {
        // given
        ReservationTime reservationTime = new ReservationTime(LocalTime.of(20, 0));
        reservationTimeRepository.save(reservationTime);
        Theme theme = new Theme("name", "description", "thumbnail");
        themeRepository.save(theme);
        Member member = new Member("name", "email", "password", Role.USER);
        memberRepository.save(member);

        Reservation reservation = new Reservation(member, LocalDate.of(2025, 12, 25), reservationTime, theme);

        // when
        reservationRepository.save(reservation);

        //then
        assertThat(reservationRepository.findAll()).hasSize(1);
    }

    @Test
    void 아이디를_기준으로_예약_조회() {
        // given
        ReservationTime reservationTime = new ReservationTime(LocalTime.of(20, 0));
        reservationTimeRepository.save(reservationTime);
        Theme theme = new Theme("name", "description", "thumbnail");
        themeRepository.save(theme);
        Member member = new Member("name", "email", "password", Role.USER);
        memberRepository.save(member);

        Reservation reservation = new Reservation(member, LocalDate.of(2025, 12, 25), reservationTime, theme);
        Reservation savedReservation = reservationRepository.save(reservation);

        // when
        Reservation foundReservation = reservationRepository.findById(savedReservation.getId())
                .orElseThrow(IllegalArgumentException::new);

        // then
        assertThat(foundReservation.getId()).isEqualTo(savedReservation.getId());
    }

    @Test
    void 아이디를_기준으로_예약_삭제() {
        // given
        ReservationTime reservationTime = new ReservationTime(LocalTime.of(20, 0));
        reservationTimeRepository.save(reservationTime);
        Theme theme = new Theme("name", "description", "thumbnail");
        themeRepository.save(theme);
        Member member = new Member("name", "email", "password", Role.USER);
        memberRepository.save(member);

        Reservation reservation = new Reservation(member, LocalDate.of(2025, 12, 25), reservationTime, theme);
        Reservation savedReservation = reservationRepository.save(reservation);

        // when
        reservationRepository.deleteById(savedReservation.getId());

        // then
        assertThat(reservationRepository.findById(savedReservation.getId())).isEmpty();
    }

    @Test
    void 날짜와_시간과_테마가_일치하는_예약_존재여부_확인() {
        // given
        ReservationTime reservationTime = new ReservationTime(LocalTime.of(20, 0));
        reservationTimeRepository.save(reservationTime);
        Theme theme = new Theme("name", "description", "thumbnail");
        themeRepository.save(theme);
        Member member = new Member("name", "email", "password", Role.USER);
        memberRepository.save(member);

        Reservation reservation = new Reservation(member, LocalDate.of(2025, 12, 25), reservationTime, theme);
        reservationRepository.save(reservation);

        // when
        boolean exists = reservationRepository.existsByDateAndTimeAndTheme(
                LocalDate.of(2025, 12, 25), reservationTime, theme);

        // then
        assertThat(exists).isTrue();
    }

    @Test
    void 날짜가_불일치하는_예약_존재여부_확인() {
        // given
        ReservationTime reservationTime = new ReservationTime(LocalTime.of(20, 0));
        reservationTimeRepository.save(reservationTime);
        Theme theme = new Theme("name", "description", "thumbnail");
        themeRepository.save(theme);
        Member member = new Member("name", "email", "password", Role.USER);
        memberRepository.save(member);

        Reservation reservation = new Reservation(member, LocalDate.of(2025, 12, 25), reservationTime, theme);
        reservationRepository.save(reservation);

        // when
        boolean exists = reservationRepository.existsByDateAndTimeAndTheme(
                LocalDate.of(2025, 11, 25), reservationTime, theme);

        // then
        assertThat(exists).isFalse();
    }

    @Test
    void 테마와_멤버와_날짜사이에_있는_예약_조회() {
        // given
        ReservationTime reservationTime = new ReservationTime(LocalTime.of(20, 0));
        reservationTimeRepository.save(reservationTime);
        Theme theme = new Theme("name", "description", "thumbnail");
        themeRepository.save(theme);
        Member member = new Member("name", "email", "password", Role.USER);
        memberRepository.save(member);

        Reservation reservation1 = new Reservation(member, LocalDate.of(2025, 11, 25), reservationTime, theme);
        Reservation reservation2 = new Reservation(member, LocalDate.of(2025, 12, 26), reservationTime, theme);
        reservationRepository.save(reservation1);
        reservationRepository.save(reservation2);

        // when
        List<Reservation> reservations = reservationRepository.findByThemeAndMemberAndDateBetween(
                theme, member, LocalDate.of(2025, 12, 24), LocalDate.of(2025, 12, 27)
        );

        // then
        assertThat(reservations).containsExactly(reservation2);
    }
}
