package roomescape.unit.repository.reservation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.repository.reservation.JpaReservationRepository;
import roomescape.repository.reservationtime.JpaReservationTimeRepository;
import roomescape.repository.theme.JpaThemeRepository;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DataJpaTest
class JpaReservationRepositoryTest {

    @Autowired
    private JpaReservationRepository jpaReservationRepository;
    @Autowired
    private JpaReservationTimeRepository jpaReservationTimeRepository;
    @Autowired
    private JpaThemeRepository jpaThemeRepository;

    @Test
    void 예약시간의_id로_저장된_예약이_있는지_확인할_수_있다() {
        // Given
        LocalDate date = LocalDate.now().plusDays(1);
        ReservationTime reservationTime = jpaReservationTimeRepository.save(new ReservationTime(null, LocalTime.now().plusMinutes(1)));
        Theme theme = jpaThemeRepository.save(new Theme(null, "themeName", "themeDescription", "thumbnailUrl"));
        jpaReservationRepository.save(new Reservation(null, "user", date, reservationTime, theme));

        // When & Then
        assertThat(jpaReservationRepository.existsByTimeId(reservationTime.getId())).isTrue();
    }

    @Test
    void 잘못된_예약시간의_id로는_저장된_예약이_존재하지_않는다는_응답을_받아야_한다() {
        // Given
        LocalDate date = LocalDate.now().plusDays(1);
        ReservationTime reservationTime = jpaReservationTimeRepository.save(new ReservationTime(null, LocalTime.now().plusMinutes(1)));
        Theme theme = jpaThemeRepository.save(new Theme(null, "themeName", "themeDescription", "thumbnailUrl"));
        jpaReservationRepository.save(new Reservation(null, "user", date, reservationTime, theme));

        // When & Then
        assertThat(jpaReservationRepository.existsByTimeId(5000L)).isFalse();
    }

    @Test
    void 날짜와_테마의_id로_저장된_예약을_모두_찾을_수_있다() {
        // Given
        LocalDate date = LocalDate.now().plusDays(1);
        ReservationTime reservationTime1 = jpaReservationTimeRepository.save(new ReservationTime(null, LocalTime.now().plusMinutes(1)));
        ReservationTime reservationTime2 = jpaReservationTimeRepository.save(new ReservationTime(null, LocalTime.now().plusMinutes(5)));
        Theme theme = jpaThemeRepository.save(new Theme(null, "themeName", "themeDescription", "thumbnailUrl"));
        Reservation savedReservation1 = jpaReservationRepository.save(new Reservation(null, "user", date, reservationTime1, theme));
        Reservation savedReservation2 = jpaReservationRepository.save(new Reservation(null, "user", date, reservationTime2, theme));

        // When & Then
        assertThat(jpaReservationRepository.findAllByDateAndThemeId(date, theme.getId())).containsExactlyInAnyOrder(savedReservation1, savedReservation2);
    }

    @Test
    void 두_날짜_사이의_예약을_모두_찾을_수_있다() {
        // Given
        LocalDate date1 = LocalDate.now().plusDays(1);
        LocalDate date2 = LocalDate.now().plusDays(2);
        LocalDate date3 = LocalDate.now().plusDays(3);
        ReservationTime reservationTime = jpaReservationTimeRepository.save(new ReservationTime(null, LocalTime.now().plusMinutes(1)));
        Theme theme = jpaThemeRepository.save(new Theme(null, "themeName", "themeDescription", "thumbnailUrl"));
        Reservation savedReservation1 = jpaReservationRepository.save(new Reservation(null, "user", date1, reservationTime, theme));
        Reservation savedReservation2 = jpaReservationRepository.save(new Reservation(null, "user", date2, reservationTime, theme));
        Reservation savedReservation3 = jpaReservationRepository.save(new Reservation(null, "user", date3, reservationTime, theme));

        // When & Then
        assertThat(jpaReservationRepository.findAllByDateBetween(date1, date3)).containsExactlyInAnyOrder(savedReservation1, savedReservation2, savedReservation3);
    }

    @Test
    void 날짜와_시간_테마에_해당하는_예약이_존재하는지_확인할_수_있다() {
        // Given
        LocalDate date = LocalDate.now().plusDays(1);
        ReservationTime reservationTime = jpaReservationTimeRepository.save(new ReservationTime(null, LocalTime.now().plusMinutes(1)));
        Theme theme = jpaThemeRepository.save(new Theme(null, "themeName", "themeDescription", "thumbnailUrl"));
        jpaReservationRepository.save(new Reservation(null, "user", date, reservationTime, theme));

        LocalDate invalidDate = LocalDate.now().plusDays(3);
        ReservationTime invalidReservationTime = jpaReservationTimeRepository.save(new ReservationTime(null, LocalTime.now().plusMinutes(5)));
        Theme invalidTheme = jpaThemeRepository.save(new Theme(null, "themeName2", "themeDescription", "thumbnailUrl"));

        // When & Then
        assertAll(() -> {
            assertThat(jpaReservationRepository.existsByDateAndTimeAndTheme(date, reservationTime, theme)).isTrue();
            assertThat(jpaReservationRepository.existsByDateAndTimeAndTheme(invalidDate, reservationTime, theme)).isFalse();
            assertThat(jpaReservationRepository.existsByDateAndTimeAndTheme(date, invalidReservationTime, theme)).isFalse();
            assertThat(jpaReservationRepository.existsByDateAndTimeAndTheme(date, reservationTime, invalidTheme)).isFalse();
        });
    }

    @Test
    void 테마의_id로_저장된_예약을_모두_찾을_수_있다() {
        // Given
        LocalDate date = LocalDate.now().plusDays(1);
        ReservationTime reservationTime1 = jpaReservationTimeRepository.save(new ReservationTime(null, LocalTime.now().plusMinutes(1)));
        ReservationTime reservationTime2 = jpaReservationTimeRepository.save(new ReservationTime(null, LocalTime.now().plusMinutes(5)));
        Theme theme = jpaThemeRepository.save(new Theme(null, "themeName", "themeDescription", "thumbnailUrl"));
        jpaReservationRepository.save(new Reservation(null, "user", date, reservationTime1, theme));
        jpaReservationRepository.save(new Reservation(null, "user", date, reservationTime2, theme));

        // When & Then
        assertThat(jpaReservationRepository.existsByThemeId(theme.getId())).isTrue();
    }
}
