package roomescape.infrastructure;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import roomescape.business.model.entity.Theme;
import roomescape.business.model.repository.Themes;
import roomescape.business.model.vo.Id;
import roomescape.test_util.JpaTestUtil;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import({JpaThemes.class, JpaTestUtil.class})
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
        assertThatCode(() -> sut.save(Theme.create("주홍색 연구", "", "")))
                .doesNotThrowAnyException();
    }

    @Test
    void 모든_테마를_찾을_수_있다() {
        String themeId1 = testUtil.insertTheme();
        String themeId2 = testUtil.insertTheme();

        final List<Theme> result = sut.findAll();

        assertThat(result).extracting(r -> r.getId().value())
                .containsExactlyInAnyOrder(themeId1, themeId2);
    }

    @Test
    void 예약이_많은_순으로_테마를_찾을_수_있다() {
        LocalDate date = LocalDate.now();
        String reservationTimeId = testUtil.insertReservationTime(LocalTime.of(10, 0));
        String themeId1 = testUtil.insertTheme();
        String themeId2 = testUtil.insertTheme();
        String themeId3 = testUtil.insertTheme();
        String userId1 = testUtil.insertUser();
        testUtil.insertReservation(date, reservationTimeId, themeId1, userId1);
        testUtil.insertReservation(date, reservationTimeId, themeId2, userId1);
        testUtil.insertReservation(date, reservationTimeId, themeId2, userId1);
        testUtil.insertReservation(date.minusDays(10), reservationTimeId, themeId3, userId1);
        testUtil.insertReservation(date.minusDays(10), reservationTimeId, themeId3, userId1);
        testUtil.insertReservation(date.plusDays(10), reservationTimeId, themeId3, userId1);

        final List<Theme> result = sut.findPopularThemes(date.minusDays(5), date.plusDays(5), 2);

        assertThat(result).extracting(r -> r.getId().value())
                .containsExactly(themeId2, themeId1);
    }

    @Test
    void ID를_기준으로_테마를_찾을_수_있다() {
        String themeId = testUtil.insertTheme();

        final Optional<Theme> result = sut.findById(Id.create(themeId));

        assertThat(result.isPresent()).isTrue();
        assertThat(result.get().getId().value()).isEqualTo(themeId);
    }

    @Test
    void ID를_기준으로_존재하는지_확인할_수_있다() {
        String themeId = testUtil.insertTheme();

        final boolean result = sut.existById(Id.create(themeId));

        assertThat(result).isTrue();
    }

    @Test
    void ID를_기준으로_삭제할_수_있다() {
        String themeId = testUtil.insertTheme();

        sut.deleteById(Id.create(themeId));

        assertThat(testUtil.countTheme()).isZero();
    }
}
