package roomescape.time.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import roomescape.support.datasource.ReservationTimeDataSource;
import roomescape.support.datasource.BaseRepositoryTest;

class ReservationTimeRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private ReservationTimeDataSource dataSource;

    @BeforeEach
    void setUp() {
        dataSource.clearTable();
        dataSource.clearId();
    }

    @Test
    void 예약_시간을_저장하고_ID로_조회한다() {
        // given
        ReservationTime time = ReservationTime.create(LocalTime.of(10, 0));

        // when
        ReservationTime saved = reservationTimeRepository.save(time);

        // then
        assertThat(saved.getId()).isEqualTo(1L);
        assertThat(saved.getStartAt()).isEqualTo(time.getStartAt());
    }

    @Test
    void 동일한_활성_시간으로_저장하면_DB_제약조건_에러가_발생한다() {
        // given
        reservationTimeRepository.save(ReservationTime.create(LocalTime.of(10, 0)));

        // when & then
        assertThatThrownBy(() -> reservationTimeRepository.save(ReservationTime.create(LocalTime.of(10, 0))))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void 활성_시간_목록을_시간순으로_조회한다() {
        // given
        dataSource.insertTimeByStartToEndWithOneHourRotation(10, 12);

        // when
        List<ReservationTime> times = reservationTimeRepository.findAll(0, 10);

        // then
        assertThat(times).extracting(ReservationTime::getStartAt)
                .containsExactly(LocalTime.of(10, 0), LocalTime.of(11, 0), LocalTime.of(12, 0));
    }
}
