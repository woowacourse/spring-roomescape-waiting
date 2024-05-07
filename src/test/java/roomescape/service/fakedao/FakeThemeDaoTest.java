package roomescape.service.fakedao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.model.theme.Theme;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

class FakeThemeDaoTest {

    private static final int INITIAL_THEME_COUNT = 3;
    private static final List<Theme> INITIAL_THEMES = List.of(
            new Theme(1, "n1", "d1", "t1"),
            new Theme(2, "n2", "d2", "t2"),
            new Theme(3, "n3", "d3", "t3")
    );

    private FakeThemeDao fakeThemeDao;

    @BeforeEach
    void setUp() {
        fakeThemeDao = new FakeThemeDao(INITIAL_THEMES);
    }

    @DisplayName("전체 테마를 조회한다.")
    @Test
    void should_find_all_themes() {
        List<Theme> allThemes = fakeThemeDao.findAll();
        assertThat(allThemes).hasSize(INITIAL_THEME_COUNT);
        assertThat(allThemes).isEqualTo(INITIAL_THEMES);
    }

    @DisplayName("테마를 저장한 후 id를 반환한다.")
    @Test
    void should_save_theme() {
        Theme theme = new Theme(4, "n4", "d4", "t4");
        long id = fakeThemeDao.save(theme);
        assertThat(id).isEqualTo(INITIAL_THEME_COUNT + 1);
        assertThat(fakeThemeDao.findAll()).hasSize(INITIAL_THEME_COUNT + 1);
    }

    @DisplayName("특정 id를 가진 테마를 조회한다.")
    @Test
    void should_find_theme_by_id() {
        Optional<Theme> theme = fakeThemeDao.findById(1);
        assertThat(theme).hasValue(new Theme(1, "n1", "d1", "t1"));
    }

    @DisplayName("유효하지 않은 id를 가진 테마를 조회하려는 경우 빈 Optional 을 반환한다.")
    @Test
    void should_return_empty_optional_when_not_exist_id() {
        Optional<Theme> theme = fakeThemeDao.findById(999);
        assertThat(theme).isEmpty();
    }

    @DisplayName("테마를 삭제한다.")
    @Test
    void should_delete_theme() {
        fakeThemeDao.deleteById(1);
        assertThat(fakeThemeDao.findById(1)).isEmpty();
        assertThat(fakeThemeDao.findAll()).hasSize(INITIAL_THEME_COUNT - 1);
    }

    @DisplayName("유효한 id를 가진 테마를 삭제하려는 경우 예외가 발생하지 않는다.")
    @Test
    void should_not_throw_exception_when_exist_id() {
        assertThatCode(() -> fakeThemeDao.deleteById(1))
                .doesNotThrowAnyException();
    }

    @DisplayName("유효하지 않은 id를 가진 테마를 삭제하려는 경우 예외가 발생한다.")
    @Test
    void should_throw_exception_when_not_exist_id() {
        assertThatThrownBy(() -> fakeThemeDao.deleteById(999))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("존재하지 않는 테마입니다.");
    }
}