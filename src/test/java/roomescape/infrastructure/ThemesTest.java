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
