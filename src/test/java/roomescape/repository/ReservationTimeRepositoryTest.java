package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Slot;
import roomescape.domain.Theme;

@DataJpaTest
public class ReservationTimeRepositoryTest {

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Test
    void saveTest() {
        ReservationTime reservationTimeWithoutId = new ReservationTime(LocalTime.of(10, 0));
        ReservationTime reservationTime = reservationTimeRepository.save(reservationTimeWithoutId);

        assertThat(reservationTime.getId()).isNotNull();
    }

    @Test
    void findByIdTest() {
        ReservationTime saved = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));

        Optional<ReservationTime> reservationTime = reservationTimeRepository.findById(saved.getId());

        assertThat(reservationTime.orElseThrow().getId()).isEqualTo(saved.getId());
    }

    @Test
    void findAllTest() {
        reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        reservationTimeRepository.save(new ReservationTime(LocalTime.of(11, 0)));

        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
        assertThat(reservationTimes.size()).isEqualTo(2);
    }

    @Test
    void findReservedTimesByDateAndThemeIdTest() {
        ReservationTime time1 = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        ReservationTime time2 = reservationTimeRepository.save(new ReservationTime(LocalTime.of(11, 0)));
        reservationTimeRepository.save(new ReservationTime(LocalTime.of(12, 0)));

        Theme theme = themeRepository.save(new Theme("방탈출1", "방탈출1 설명", "url.jpg"));

        reservationRepository.save(new Reservation("fizz", new Slot(LocalDate.of(2026, 5, 2), time1, theme)));
        reservationRepository.save(new Reservation("fizz", new Slot(LocalDate.of(2026, 5, 2), time2, theme)));

        List<ReservationTime> reservedTimes = reservationTimeRepository.findReservedTimesByDateAndTheme_Id(
                LocalDate.of(2026, 5, 2), theme.getId());

        assertThat(reservedTimes.get(0)).isEqualTo(time1);
        assertThat(reservedTimes.get(1)).isEqualTo(time2);
    }

    @Test
    void deleteTest() {
        ReservationTime saved = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));

        reservationTimeRepository.deleteById(saved.getId());

        assertThat(reservationTimeRepository.count()).isEqualTo(0);
    }

    @Test
    void existsByStartAt() {
        reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));

        assertThat(reservationTimeRepository.existsByStartAt(LocalTime.of(10, 0))).isTrue();
        assertThat(reservationTimeRepository.existsByStartAt(LocalTime.of(11, 0))).isFalse();
    }
}
