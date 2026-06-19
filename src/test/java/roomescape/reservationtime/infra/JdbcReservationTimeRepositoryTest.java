package roomescape.reservationtime.infra;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.domain.repository.ReservationTimeRepository;
import roomescape.support.TestDataHelper;

@JdbcTest
@Import(JdbcReservationTimeRepository.class)
class JdbcReservationTimeRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ReservationTimeRepository timeRepository;

    private TestDataHelper testHelper;

    @BeforeEach
    void setUp() {
        testHelper = new TestDataHelper(jdbcTemplate);
    }

    @DisplayName("예약 시간 저장을 테스트합니다.")
    @Test
    void save_time() {
        ReservationTime savedTime = timeRepository.save(ReservationTime.builder()
                .startAt(LocalTime.of(9, 0))
                .build());

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(savedTime.getId()).isPositive();
            softly.assertThat(savedTime.getStartAt()).isEqualTo(LocalTime.of(9, 0));
        });
    }

    @DisplayName("id로 예약 시간 조회를 테스트합니다.")
    @Test
    void find_by_id() {
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));

        ReservationTime reservationTime = timeRepository.findById(timeId)
                .orElseThrow();

        assertThat(reservationTime.getStartAt()).isEqualTo(LocalTime.of(9, 0));
    }

    @DisplayName("예약 시간의 시작 시간 오름차순 전체 조회를 테스트합니다.")
    @Test
    void find_all_order_by_start_at() {
        testHelper.insertReservationTime(LocalTime.of(9, 0));
        testHelper.insertReservationTime(LocalTime.of(10, 0));
        testHelper.insertReservationTime(LocalTime.of(11, 0));

        List<ReservationTime> times = timeRepository.findAll();

        assertThat(times)
                .extracting(ReservationTime::getStartAt)
                .containsExactly(
                        LocalTime.of(9, 0),
                        LocalTime.of(10, 0),
                        LocalTime.of(11, 0)
                );
    }

    @DisplayName("예약 시간 삭제를 테스트합니다.")
    @Test
    void delete_time() {
        Long timeId = testHelper.insertReservationTime(LocalTime.of(9, 0));

        Integer deletedCount = timeRepository.delete(timeId);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(deletedCount).isEqualTo(1);
            softly.assertThat(timeRepository.findById(timeId)).isEmpty();
        });
    }

    @DisplayName("같은 시작 시간의 예약 시간 존재 여부 조회를 테스트합니다.")
    @Test
    void exists_by_start_at() {
        testHelper.insertReservationTime(LocalTime.of(9, 0));

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(timeRepository.existsByStartAt(LocalTime.of(9, 0))).isTrue();
            softly.assertThat(timeRepository.existsByStartAt(LocalTime.of(10, 0))).isFalse();
        });
    }
}
