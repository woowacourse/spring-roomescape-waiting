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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
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
        assertThatCode(() -> sut.save(ReservationTime.create(LocalTime.of(10, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    void 모든_예약_시간을_찾을_수_있다() {
        // given
        String timeId1 = testUtil.insertReservationTime();
        String timeId2 = testUtil.insertReservationTime();

        // when
        final List<ReservationTime> result = sut.findAll();

        // then
        assertThat(result).extracting(r -> r.getId().value())
                .containsExactlyInAnyOrder(timeId1, timeId2);
    }

    @Test
    void 해당_날짜와_테마에_예약되지_않은_모든_예약_시간을_찾을_수_있다() {
        // given
        String timeId1 = testUtil.insertReservationTime();
        String timeId2 = testUtil.insertReservationTime();
        String timeId3 = testUtil.insertReservationTime();
        String themeId = testUtil.insertTheme();
        String userId = testUtil.insertUser();
        String slotId = testUtil.insertSlot(LocalDate.now().plusDays(10), timeId1, themeId);
        testUtil.insertReservation(slotId, userId);

        // when
        final List<ReservationTime> result = sut.findAvailableByDateAndThemeId(LocalDate.now().plusDays(10), Id.create(themeId));

        // then
        assertThat(result).extracting(r -> r.getId().value())
                .containsExactlyInAnyOrder(timeId2, timeId3);
    }

    @Test
    void 해당_날짜와_테마에_예약된_모든_예약_시간을_찾을_수_있다() {
        // given
        String timeId1 = testUtil.insertReservationTime();
        testUtil.insertReservationTime();
        testUtil.insertReservationTime();
        String themeId = testUtil.insertTheme();
        String userId = testUtil.insertUser();
        String slotId = testUtil.insertSlot(LocalDate.now().plusDays(10), timeId1, themeId);
        testUtil.insertReservation(slotId, userId);

        // when
        final List<ReservationTime> result = sut.findNotAvailableByDateAndThemeId(LocalDate.now().plusDays(10), Id.create(themeId));

        // then
        assertThat(result).extracting(r -> r.getId().value())
                .containsExactlyInAnyOrder(timeId1);
    }

    @Test
    void 예약된_시간이_없을_때_빈_리스트를_반환한다() {
        // given
        testUtil.insertReservationTime();
        testUtil.insertReservationTime();
        String themeId = testUtil.insertTheme();
        testUtil.insertUser();

        // when
        final List<ReservationTime> result = sut.findNotAvailableByDateAndThemeId(LocalDate.now().plusDays(10), Id.create(themeId));

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void 다른_날짜의_예약은_포함하지_않는다() {
        // given
        String timeId1 = testUtil.insertReservationTime();
        testUtil.insertReservationTime();
        String themeId = testUtil.insertTheme();
        String slotId = testUtil.insertSlot(LocalDate.now().plusDays(5), timeId1, themeId);
        String userId = testUtil.insertUser();
        testUtil.insertReservation(slotId, userId);

        // when
        final List<ReservationTime> result = sut.findNotAvailableByDateAndThemeId(LocalDate.now().plusDays(10), Id.create(themeId));

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void 다른_테마의_예약은_포함하지_않는다() {
        // given
        String timeId1 = testUtil.insertReservationTime();
        testUtil.insertReservationTime();
        String themeId1 = testUtil.insertTheme();
        String themeId2 = testUtil.insertTheme();
        String userId = testUtil.insertUser();
        String slotId = testUtil.insertSlot(LocalDate.now().plusDays(10), timeId1, themeId1);
        testUtil.insertReservation(slotId, userId);

        // when
        final List<ReservationTime> result = sut.findNotAvailableByDateAndThemeId(LocalDate.now().plusDays(10), Id.create(themeId2));

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void 여러_예약이_있을_때_모든_예약된_시간을_찾을_수_있다() {
        // given
        String timeId1 = testUtil.insertReservationTime();
        testUtil.insertReservationTime();
        String timeId3 = testUtil.insertReservationTime();
        testUtil.insertReservationTime();
        String themeId = testUtil.insertTheme();
        String userId = testUtil.insertUser();
        String slotId1 = testUtil.insertSlot(LocalDate.now().plusDays(10), timeId1, themeId);
        String slotId2 = testUtil.insertSlot(LocalDate.now().plusDays(10), timeId3, themeId);
        testUtil.insertReservation(slotId1, userId);
        testUtil.insertReservation(slotId2, userId);

        // when
        final List<ReservationTime> result = sut.findNotAvailableByDateAndThemeId(LocalDate.now().plusDays(10), Id.create(themeId));

        // then
        assertThat(result).extracting(r -> r.getId().value())
                .containsExactlyInAnyOrder(timeId1, timeId3);
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
