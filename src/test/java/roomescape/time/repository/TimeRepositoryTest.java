package roomescape.time.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.test.RepositoryTest;
import roomescape.time.domain.ReservationTime;

class TimeRepositoryTest extends RepositoryTest {
    private static final int COUNT_OF_TIME = 3;

    @Autowired
    private TimeRepository timeRepository;

    @DisplayName("전체 예약 시간을 조회할 수 있다.")
    @Test
    void findAllTest() {
        List<ReservationTime> actual = timeRepository.findAll();

        assertThat(actual).hasSize(COUNT_OF_TIME);
    }

    @DisplayName("id로 예약 시간을 조회할 수 있다.")
    @Test
    void findByIdTest() {
        Optional<ReservationTime> actual = timeRepository.findById(1L);

        assertThat(actual.get().getId()).isEqualTo(1L);
    }

    @DisplayName("예약 가능한 예약 시간을 조회할 수 있다.")
    @Test
    void findTimesExistsReservationDateAndThemeIdTest() {
        List<ReservationTime> expected = List.of(
                new ReservationTime(2L, LocalTime.of(19, 0)));

        List<ReservationTime> actual = timeRepository.findTimesExistsReservationDateAndThemeId(
                LocalDate.of(2022, 5, 5), 1L);

        assertThat(actual).isEqualTo(expected);
    }

    @DisplayName("예약 시간을 저장할 수 있다.")
    @Test
    void saveTest() {
        timeRepository.save(new ReservationTime(LocalTime.of(23, 0)));

        Optional<ReservationTime> savedTime = timeRepository.findById(COUNT_OF_TIME + 1L);
        assertThat(savedTime).isNotEmpty();
    }

    @DisplayName("예약 시간을 삭제할 수 있다.")
    @Test
    void deleteByIdTest() {
        timeRepository.deleteById(3L);

        Optional<ReservationTime> savedTime = timeRepository.findById(3L);
        assertThat(savedTime).isEmpty();
    }
}
