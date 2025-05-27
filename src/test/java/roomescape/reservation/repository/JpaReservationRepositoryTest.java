package roomescape.reservation.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.repository.JpaMemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.repository.reservation.JpaReservationRepository;
import roomescape.reservation.repository.time.JpaReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class JpaReservationRepositoryTest {

    @Autowired
    private JpaReservationRepository jpaReservationRepository;
    @Autowired
    private JpaReservationTimeRepository jpaReservationTimeRepository;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private JpaMemberRepository jpaMemberRepository;

    @Test
    void 예약_전체_조회() {
        // when & then
        assertThat(jpaReservationRepository.findAll()).hasSize(0);
    }

    @Test
    void 예약_저장() {
        // given
        final ReservationTime reservationTime = new ReservationTime(LocalTime.of(20, 0));
        jpaReservationTimeRepository.save(reservationTime);
        final Theme theme = new Theme("name", "description", "thumbnail");
        themeRepository.save(theme);
        final Member member = new Member("name", "email", "password", Role.USER);
        jpaMemberRepository.save(member);

        final Reservation reservation = new Reservation(member, LocalDate.of(2025, 12, 25), reservationTime, theme);

        // when
        jpaReservationRepository.save(reservation);

        //then
        assertThat(jpaReservationRepository.findAll()).hasSize(1);
    }

    @Test
    void 아이디를_기준으로_예약_조회() {
        // given
        final ReservationTime reservationTime = new ReservationTime(LocalTime.of(20, 0));
        jpaReservationTimeRepository.save(reservationTime);
        final Theme theme = new Theme("name", "description", "thumbnail");
        themeRepository.save(theme);
        final Member member = new Member("name", "email", "password", Role.USER);
        jpaMemberRepository.save(member);

        final Reservation reservation = new Reservation(member, LocalDate.of(2025, 12, 25), reservationTime, theme);
        final Reservation savedReservation = jpaReservationRepository.save(reservation);

        // when
        final Reservation foundReservation = jpaReservationRepository.findById(savedReservation.getId())
                .orElseThrow(IllegalArgumentException::new);

        // then
        assertThat(foundReservation.getId()).isEqualTo(savedReservation.getId());
    }

    @Test
    void 아이디를_기준으로_예약_삭제() {
        // given
        final ReservationTime reservationTime = new ReservationTime(LocalTime.of(20, 0));
        jpaReservationTimeRepository.save(reservationTime);
        final Theme theme = new Theme("name", "description", "thumbnail");
        themeRepository.save(theme);
        final Member member = new Member("name", "email", "password", Role.USER);
        jpaMemberRepository.save(member);

        final Reservation reservation = new Reservation(member, LocalDate.of(2025, 12, 25), reservationTime, theme);
        final Reservation savedReservation = jpaReservationRepository.save(reservation);

        // when
        jpaReservationRepository.deleteById(savedReservation.getId());

        // then
        assertThat(jpaReservationRepository.findById(savedReservation.getId())).isEmpty();
    }

    @Test
    void 날짜와_시간과_테마가_일치하는_예약_존재여부_확인() {
        // given
        final ReservationTime reservationTime = new ReservationTime(LocalTime.of(20, 0));
        jpaReservationTimeRepository.save(reservationTime);
        final Theme theme = new Theme("name", "description", "thumbnail");
        themeRepository.save(theme);
        final Member member = new Member("name", "email", "password", Role.USER);
        jpaMemberRepository.save(member);

        final Reservation reservation = new Reservation(member, LocalDate.of(2025, 12, 25), reservationTime, theme);
        jpaReservationRepository.save(reservation);

        // when
        final boolean exists = jpaReservationRepository.existsByDateAndTimeAndTheme(
                LocalDate.of(2025, 12, 25), reservationTime, theme);

        // then
        assertThat(exists).isTrue();
    }

    @Test
    void 날짜가_불일치하는_예약_존재여부_확인() {
        // given
        final ReservationTime reservationTime = new ReservationTime(LocalTime.of(20, 0));
        jpaReservationTimeRepository.save(reservationTime);
        final Theme theme = new Theme("name", "description", "thumbnail");
        themeRepository.save(theme);
        final Member member = new Member("name", "email", "password", Role.USER);
        jpaMemberRepository.save(member);

        final Reservation reservation = new Reservation(member, LocalDate.of(2025, 12, 25), reservationTime, theme);
        jpaReservationRepository.save(reservation);

        // when
        final boolean exists = jpaReservationRepository.existsByDateAndTimeAndTheme(
                LocalDate.of(2025, 11, 25), reservationTime, theme);

        // then
        assertThat(exists).isFalse();
    }

    @Test
    void 테마와_멤버와_날짜사이에_있는_예약_조회() {
        // given
        final ReservationTime reservationTime = new ReservationTime(LocalTime.of(20, 0));
        jpaReservationTimeRepository.save(reservationTime);
        final Theme theme = new Theme("name", "description", "thumbnail");
        themeRepository.save(theme);
        final Member member = new Member("name", "email", "password", Role.USER);
        jpaMemberRepository.save(member);

        final Reservation reservation1 = new Reservation(member, LocalDate.of(2025, 11, 25), reservationTime, theme);
        final Reservation reservation2 = new Reservation(member, LocalDate.of(2025, 12, 26), reservationTime, theme);
        jpaReservationRepository.save(reservation1);
        jpaReservationRepository.save(reservation2);

        // when
        final List<Reservation> reservations = jpaReservationRepository.findByThemeAndMemberAndDateBetween(
                theme, member, LocalDate.of(2025, 12, 24), LocalDate.of(2025, 12, 27)
        );

        // then
        assertThat(reservations).containsExactly(reservation2);
    }

    @Test
    void 일주일_이내의_인기있는_예약_테마_정보_조회() {
        // given
        final ReservationTime reservationTime = new ReservationTime(LocalTime.of(20, 0));
        jpaReservationTimeRepository.save(reservationTime);
        final Theme theme1 = new Theme("name1", "description", "thumbnail");
        final Theme theme2 = new Theme("name2", "description", "thumbnail");
        themeRepository.save(theme1);
        themeRepository.save(theme2);
        final Member member = new Member("name", "email", "password", Role.USER);
        jpaMemberRepository.save(member);

        final Reservation reservation1 = new Reservation(member, LocalDate.of(2025, 12, 15), reservationTime, theme1);
        final Reservation reservation2 = new Reservation(member, LocalDate.of(2025, 12, 18), reservationTime, theme1);
        final Reservation reservation3 = new Reservation(member, LocalDate.of(2025, 12, 23), reservationTime, theme1);
        final Reservation reservation4 = new Reservation(member, LocalDate.of(2025, 12, 22), reservationTime, theme2);
        final Reservation reservation5 = new Reservation(member, LocalDate.of(2025, 12, 24), reservationTime, theme2);
        jpaReservationRepository.save(reservation1);
        jpaReservationRepository.save(reservation2);
        jpaReservationRepository.save(reservation3);
        jpaReservationRepository.save(reservation4);
        jpaReservationRepository.save(reservation5);

        // when
        final List<Theme> popularThemes = jpaReservationRepository.findPopularThemesByReservationBetween(
                LocalDate.of(2025, 12, 19), LocalDate.of(2025, 12, 26), PageRequest.of(0, 1));

        // then
        assertThat(popularThemes).containsExactly(theme2);
    }

    @Test
    void 멤버_기준으로_예약_조회() {
        // given
        final ReservationTime reservationTime = new ReservationTime(LocalTime.of(20, 0));
        jpaReservationTimeRepository.save(reservationTime);
        final Theme theme = new Theme("name", "description", "thumbnail");
        themeRepository.save(theme);
        final Member member = new Member("name", "email", "password", Role.USER);
        jpaMemberRepository.save(member);

        final Reservation reservation1 = new Reservation(member, LocalDate.of(2025, 12, 25), reservationTime, theme);
        final Reservation reservation2 = new Reservation(member, LocalDate.of(2025, 12, 26), reservationTime, theme);
        jpaReservationRepository.save(reservation1);
        jpaReservationRepository.save(reservation2);

        // when
        final List<Reservation> reservations = jpaReservationRepository.findByMember(member);

        // then
        assertThat(reservations).containsExactlyInAnyOrder(reservation1, reservation2);
    }

}
