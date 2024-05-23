package roomescape.domain.time.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.RepositoryTest;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;


public class AdminReservationTimeRepositoryTest extends RepositoryTest {

    @Autowired
    ReservationTimeRepository reservationTimeRepository;

    @ParameterizedTest
    @CsvSource(value = {"11, true", "12, false", "13, true", "14, false"})
    @DisplayName("해당 시간이 존재하는지 확인할 수 있다.")
    void existsByStartAtTest(int hour, boolean expected) {
        boolean actual = reservationTimeRepository.existsByStartAt(LocalTime.of(hour, 0));

        assertThat(actual).isEqualTo(expected);
    }
}
