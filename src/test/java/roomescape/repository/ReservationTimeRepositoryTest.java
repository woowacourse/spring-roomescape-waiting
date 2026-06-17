package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.domain.ReservationTime;

@DataJpaTest
class ReservationTimeRepositoryTest {

    @Autowired
    private ReservationTimeRepository timeDao;

    @Test
    void 예약_시간을_생성한다() {
        // when
        ReservationTime saved = saveTime(10, 0);

        // then
        assertAll(
                () -> assertThat(saved.getId()).isNotNull(),
                () -> assertThat(saved.getStartAt()).isEqualTo(LocalTime.of(10, 0))
        );
    }

    @Test
    void 예약_시간_목록을_조회한다() {
        // given
        saveTime(10, 0);
        saveTime(11, 0);
        saveTime(12, 0);
        saveTime(13, 0);
        saveTime(14, 0);

        // when
        List<ReservationTime> times = timeDao.findAll();

        // then
        assertAll(
                () -> assertThat(times).hasSize(5),
                () -> assertThat(times.get(0).getStartAt()).isEqualTo(LocalTime.of(10, 0)),
                () -> assertThat(times.get(1).getStartAt()).isEqualTo(LocalTime.of(11, 0)),
                () -> assertThat(times.get(2).getStartAt()).isEqualTo(LocalTime.of(12, 0)),
                () -> assertThat(times.get(3).getStartAt()).isEqualTo(LocalTime.of(13, 0)),
                () -> assertThat(times.get(4).getStartAt()).isEqualTo(LocalTime.of(14, 0))
        );
    }

    @Test
    void 아이디에_맞는_예약_시간을_조회한다() {
        // given
        ReservationTime saved = saveTime(10, 0);

        // when
        Optional<ReservationTime> found = timeDao.findById(saved.getId());

        // then
        assertAll(
                () -> assertThat(found).isPresent(),
                () -> assertThat(found.get().getStartAt()).isEqualTo(saved.getStartAt())
        );
    }

    @Test
    void 아이디에_맞는_예약_시간이_존재하지_않으면_빈_값을_반환한다() {
        // when
        Optional<ReservationTime> found = timeDao.findById(0L);

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void 예약_시간을_삭제한다() {
        // given
        ReservationTime saved = saveTime(10, 0);

        // when
        timeDao.deleteById(saved.getId());

        // then
        assertThat(timeDao.findAll()).isEmpty();
    }

    private ReservationTime saveTime(int hour, int minute) {
        return timeDao.save(ReservationTime.createWithoutId(LocalTime.of(hour, minute)));
    }
}
