package roomescape.infrastructure;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import roomescape.business.model.entity.ReservationTime;
import roomescape.business.model.repository.ReservationTimes;
import roomescape.business.model.vo.Id;
import roomescape.infrastructure.jpa.JpaReservationSlots;
import roomescape.infrastructure.jpa.JpaReservationTimes;
import roomescape.test_util.JpaTestUtil;

import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import({JpaReservationTimes.class, JpaTestUtil.class, JpaReservationSlots.class})
class ReservationTimesTest {

    @Autowired
    private ReservationTimes sut;
    @Autowired
    private JpaTestUtil testUtil;

    @AfterEach
    void tearDown() {
        testUtil.deleteAll();
    }

    @Test
    void 예약_시간을_저장할_수_있다() {
        assertThatCode(() -> sut.save(new ReservationTime(LocalTime.of(10, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    void ID_기준으로_예약_시간을_찾을_수_있다() {
        // given
        String timeId = testUtil.insertReservationTime();

        // when
        final Optional<ReservationTime> result = sut.findById(Id.create(timeId));

        // then
        assertThat(result.isPresent()).isTrue();
        assertThat(result.get().getId().value()).isEqualTo(timeId);
    }

    @Test
    void ID_기준으로_존재하는지_확인할_수_있다() {
        // given
        String timeId = testUtil.insertReservationTime();

        // when
        final boolean result = sut.existById(Id.create(timeId));

        // then
        assertThat(result).isTrue();
    }

    @Test
    void 시간_기준으로_존재하는지_확인할_수_있다() {
        // given
        testUtil.insertReservationTime(LocalTime.of(10, 0));

        // when
        final boolean result = sut.existByTime(LocalTime.of(10, 0));

        // then
        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 29})
    void 두_시간_사이에_존재하는지_확인할_수_있다(int minute) {
        // given
        testUtil.insertReservationTime(LocalTime.of(10, minute));

        // when
        final boolean result = sut.existBetween(LocalTime.of(10, 0), LocalTime.of(10, 30));

        // then
        assertThat(result).isTrue();
    }

    @Test
    void ID를_통해_예약_시간을_삭제할_수_있다() {
        // given
        String timeId = testUtil.insertReservationTime();

        // when
        sut.deleteById(Id.create(timeId));

        // then
        assertThat(testUtil.countReservationTime()).isZero();
    }
}
