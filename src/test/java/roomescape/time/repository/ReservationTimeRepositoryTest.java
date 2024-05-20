package roomescape.time.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.time.domain.ReservationTime;
import roomescape.time.domain.ReservationTimeRepository;
import roomescape.time.fixture.DateTimeFixture;

// 10:00
// 11:00
// 12:00

// 내일, 테마 1, 10시 예약있음
// 내일, 테마 1, 11시 예약있음
// 내일, 테마 2, 10시 예약있음

@DataJpaTest
@Sql(scripts = "/test_data.sql", executionPhase = ExecutionPhase.BEFORE_TEST_CLASS)
class ReservationTimeRepositoryTest {

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @DisplayName("날짜와 테마를 기반으로 예약된 시간을 가져올 수 있다")
    @Test
    void should_find_reserved_time_by_date_and_theme() {
        List<ReservationTime> reservedTime = reservationTimeRepository.findReservedTime(DateTimeFixture.TOMORROW, 1L);
        assertThat(reservedTime).hasSize(2);
    }

    @DisplayName("시간값이 겹치는 로우가 있는 지 확인할 수 있다")
    @Test
    void should_check_same_start_at_exist() {
        assertAll(
                () -> assertThat(reservationTimeRepository.existsByStartAt(DateTimeFixture.TIME_10_00)).isTrue(),
                () -> assertThat(reservationTimeRepository.existsByStartAt(DateTimeFixture.TIME_13_00)).isFalse()
        );
    }
}
