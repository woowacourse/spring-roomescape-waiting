package roomescape.time.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.time.fixture.DateTimeFixture.TIME_10_00;
import static roomescape.time.fixture.DateTimeFixture.TIME_11_00;
import static roomescape.time.fixture.DateTimeFixture.TIME_12_00;
import static roomescape.time.fixture.DateTimeFixture.TIME_13_00;
import static roomescape.time.fixture.DateTimeFixture.TOMORROW;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.time.domain.ReservationTime;
import roomescape.time.domain.ReservationTimeRepository;
import roomescape.time.dto.ReservationTimeWithBookStatusResponse;

// 테스트 초기 데이터
// 시간1: {ID=1, TIME=10:00}
// 시간2: {ID=2, TIME=11:00}
// 시간3: {ID=3, TIME=12:00}

// 내일 10:00 테마1 예약 존재
// 내일 11:00 테마1 예약 존재
// 내일 10:00 테마2 예약 존재
@JdbcTest
@Sql(scripts = "/test_data.sql", executionPhase = ExecutionPhase.BEFORE_TEST_CLASS)
class JdbcReservationTimeRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ReservationTimeRepository reservationTimeRepository;

    @BeforeEach
    void setUp() {
        reservationTimeRepository = new JdbcReservationTimeRepository(jdbcTemplate);
    }

    @DisplayName("예약시간 모두를 불러올 수 있다")
    @Test
    void should_find_all() {
        assertThat(reservationTimeRepository.findAll()).hasSize(3);
    }

    @DisplayName("원하는 ID의 예약시간을 불러올 수 있다")
    @Test
    void should_find_reservation_time_by_id() {
        assertThat(reservationTimeRepository.findById(1L)).isPresent();
    }

    @DisplayName("동일한 시간이 존재하는지 확인할 수 있다")
    @Test
    void should_return_true_when_same_time_is_already_exist() {
        assertThat(reservationTimeRepository.existByStartAt(TIME_11_00)).isTrue();
    }

    @DisplayName("동일한 시간의 예약시간이 존재하지 않는 경우를 확인할 수 있다")
    @Test
    void should_return_false_when_same_time_is_no_exist() {
        assertThat(reservationTimeRepository.existByStartAt(TIME_13_00)).isFalse();
    }

    @DisplayName("예약시간을 추가할 수 있다")
    @Test
    void should_get_reservation_time_after_insert() {
        ReservationTime requestReservationTime = new ReservationTime(null, TIME_12_00);
        assertThat(reservationTimeRepository.save(requestReservationTime).getId()).isNotNull();
    }

    @DisplayName("원하는 ID의 예약시간을 삭제할 수 있다")
    @Test
    void should_deleteById() {
        reservationTimeRepository.deleteById(3L);
        assertThat(reservationTimeRepository.findAll()).hasSize(2);
    }

    @DisplayName("테마와 날짜를 기반으로 예약 가능 상태를 포함한 시간을 조회할 수 있다")
    @Test
    void should_get_reservation_times_with_book_status() {
        ReservationTimeWithBookStatusResponse timeStatus1 = new ReservationTimeWithBookStatusResponse(1L, TIME_10_00,
                true);
        ReservationTimeWithBookStatusResponse timeStatus2 = new ReservationTimeWithBookStatusResponse(
                2L, TIME_11_00, true);
        ReservationTimeWithBookStatusResponse timeStatus3 = new ReservationTimeWithBookStatusResponse(
                3L, TIME_12_00, false);

        List<ReservationTimeWithBookStatusResponse> timesWithBookStatus = reservationTimeRepository.findByDateAndThemeIdWithBookStatus(
                TOMORROW, 1L);

        assertThat(timesWithBookStatus)
                .contains(timeStatus1, timeStatus2, timeStatus3);
    }
}
