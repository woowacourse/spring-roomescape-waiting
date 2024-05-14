package roomescape.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.domain.ReservationTime;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
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
}
