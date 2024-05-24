package roomescape.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import roomescape.domain.ReservationTime;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ReservationTimeRepositoryTest {

    @Autowired
    ReservationTimeRepository timeRepository;

    @Test
    @DisplayName("모든 예약 시간 목록을 조회한다.")
    void findAll() {
        // given
        final List<ReservationTime> expected = List.of(
                new ReservationTime(1L, null),
                new ReservationTime(2L, null),
                new ReservationTime(3L, null),
                new ReservationTime(4L, null),
                new ReservationTime(5L, null)
        );

        assertThat(timeRepository.findAll()).isEqualTo(expected);
    }

    @Test
    @DisplayName("예약 시간 존재 여부를 확인한다.")
    void existsTrue() {
        assertThat(timeRepository.existsByStartAt(LocalTime.of(18, 0))).isTrue();
        assertThat(timeRepository.existsByStartAt(LocalTime.of(0, 0))).isFalse();
    }
}
