package roomescape.infrastructure;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import roomescape.business.model.entity.Theme;
import roomescape.business.model.repository.Themes;
import roomescape.business.model.vo.Id;
import roomescape.infrastructure.jpa.JpaReservationSlots;
import roomescape.infrastructure.jpa.JpaThemes;
import roomescape.test_util.JpaTestUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import({JpaThemes.class, JpaTestUtil.class, JpaReservationSlots.class})
class ThemesTest {

    @Autowired
    private Themes sut;
    @Autowired
    private JpaTestUtil testUtil;

    @AfterEach
    void tearDown() {
        testUtil.deleteAll();
    }

    @Test
    void 테마를_저장할_수_있다() {
        assertThatCode(() -> sut.save(new Theme("주홍색 연구", "", "")))
                .doesNotThrowAnyException();
    }

    @Test
    void 모든_테마를_찾을_수_있다() {
        // given
        String themeId1 = testUtil.insertTheme();
        String themeId2 = testUtil.insertTheme();

        // when
        final List<Theme> result = sut.findAll();

        // then
        assertThat(result).extracting(r -> r.getId().value())
                .containsExactlyInAnyOrder(themeId1, themeId2);
    }

    @Test
    void 예약이_많은_순으로_테마를_찾을_수_있다() {
        // given
        LocalDate date = LocalDate.now();
        String reservationTimeId = testUtil.insertReservationTime();
        String themeId1 = testUtil.insertTheme();
        String themeId2 = testUtil.insertTheme();
        String themeId3 = testUtil.insertTheme();
        String slotId1 = testUtil.insertSlot(date, reservationTimeId, themeId1);
        String slotId2_1 = testUtil.insertSlot(date, reservationTimeId, themeId2);
        String slotId2_2 = testUtil.insertSlot(date, reservationTimeId, themeId2);
        String slotId3_1 = testUtil.insertSlot(date.plusDays(5), reservationTimeId, themeId3);
        String slotId3_2 = testUtil.insertSlot(date.plusDays(5), reservationTimeId, themeId3);
        String slotId3_3 = testUtil.insertSlot(date.plusDays(5), reservationTimeId, themeId3);
        String userId1 = testUtil.insertUser();
        testUtil.insertReservation(slotId1, userId1);
        testUtil.insertReservation(slotId2_1, userId1);
        testUtil.insertReservation(slotId2_2, userId1);
        testUtil.insertReservation(slotId3_1, userId1);
        testUtil.insertReservation(slotId3_2, userId1);
        testUtil.insertReservation(slotId3_3, userId1);

        // when
        final List<Theme> result = sut.findPopularThemes(date.minusDays(5), date.plusDays(3), 2);

        // then
        assertThat(result).extracting(r -> r.getId().value())
                .containsExactly(themeId2, themeId1);
    }

    @Test
    void ID를_기준으로_테마를_찾을_수_있다() {
        // given
        String themeId = testUtil.insertTheme();

        // when
        final Optional<Theme> result = sut.findById(Id.create(themeId));

        // then
        assertThat(result.isPresent()).isTrue();
        assertThat(result.get().getId().value()).isEqualTo(themeId);
    }

    @Test
    void ID를_기준으로_존재하는지_확인할_수_있다() {
        // given
        String themeId = testUtil.insertTheme();

        // when
        final boolean result = sut.existById(Id.create(themeId));

        // then
        assertThat(result).isTrue();
    }

    @Test
    void ID를_기준으로_삭제할_수_있다() {
        // given
        String themeId = testUtil.insertTheme();

        // when
        sut.deleteById(Id.create(themeId));

        // then
        assertThat(testUtil.countTheme()).isZero();
    }
}
