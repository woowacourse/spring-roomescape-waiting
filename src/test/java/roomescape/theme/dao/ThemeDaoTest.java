package roomescape.theme.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.exception.AppException;
import roomescape.theme.domain.Theme;
import roomescape.theme.dto.request.ThemeCreateRequest;

@JdbcTest
class ThemeDaoTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ThemeDao themeDao;

    @BeforeEach
    void setUp() {
        themeDao = new ThemeDao(jdbcTemplate);
    }

    @Nested
    class 테마를_저장한다 {

        @Test
        void 새로운_테마를_저장한다() {
            // given
            Theme theme = Theme.of(1L, "테마이름", "테마설명", "https://image.url");

            // when
            Theme saved = themeDao.insert(new ThemeCreateRequest("테마이름", "테마설명", "https://image.url"));

            // then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getName()).isEqualTo("테마이름");
        }

        @Test
        void 저장_후_ID가_부여된_테마를_반환한다() {
            // given
            Theme theme = Theme.of(1L, "테마이름", "테마설명", "https://image.url");

            // when
            Theme saved = themeDao.insert(new ThemeCreateRequest("테마이름", "테마설명", "https://image.url"));

            // then
            List<Theme> allThemes = themeDao.findAll();
            assertThat(allThemes).hasSize(1);
            assertThat(allThemes.get(0)).isEqualTo(saved);
        }
    }

    @Test
    void 저장된_모든_테마를_조회한다() {
        // given
        themeDao.insert(new ThemeCreateRequest("테마이름1", "테마설명1", "https://image.url1"));
        themeDao.insert(new ThemeCreateRequest("테마이름2", "테마설명2", "https://image.url2"));
        themeDao.insert(new ThemeCreateRequest("테마이름3", "테마설명3", "https://image.url3"));

        // when
        List<Theme> themes = themeDao.findAll();

        // then
        assertThat(themes).hasSize(3);
    }

    @Nested
    class ID로_테마를_조회한다 {

        @Test
        void 존재하는_테마를_조회한다() {
            // given
            Theme saved = themeDao.insert(new ThemeCreateRequest("테마이름", "테마설명", "https://image.url"));

            // when
            Theme found = themeDao.findById(saved.getId());

            // then
            assertThat(found).isEqualTo(saved);
        }

        @Test
        void 존재하지_않는_ID로_조회하면_예외를_던진다() {
            // when // then
            assertThatThrownBy(() -> themeDao.findById(999L))
                    .isInstanceOf(AppException.class)
                    .hasMessageContaining("존재하지 않는 테마");
        }
    }

    @Nested
    class 테마를_삭제한다 {

        @Test
        void ID로_테마를_삭제한다() {
            // given
            Theme saved = themeDao.insert(new ThemeCreateRequest("테마이름", "테마설명", "https://image.url"));

            // when
            boolean deleted = themeDao.delete(saved.getId());

            // then
            assertThat(deleted).isTrue();
            assertThat(themeDao.findAll()).isEmpty();
        }

        @Test
        void 존재하지_않는_ID_삭제_시_false를_반환한다() {
            // when
            boolean deleted = themeDao.delete(999L);

            // then
            assertThat(deleted).isFalse();
        }
    }
}
