package roomescape.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.exception.DuplicatedException;
import roomescape.exception.NotFoundException;
import roomescape.model.theme.Theme;
import roomescape.repository.ThemeRepository;
import roomescape.repository.dao.ReservationDao;
import roomescape.repository.dao.ThemeDao;
import roomescape.repository.dto.ReservationRowDto;
import roomescape.service.dto.ThemeDto;
import roomescape.service.fakedao.FakeReservationDao;
import roomescape.service.fakedao.FakeThemeDao;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ThemeServiceTest {

    private static final int INITIAL_THEME_COUNT = 2;

    private ThemeService themeService;

    @BeforeEach
    void setUp() {
        ThemeDao themeDao = new FakeThemeDao(new ArrayList<>(List.of(
                new Theme(1, "n1", "d1", "t1"),
                new Theme(2, "n2", "d2", "t2"))));
        ReservationDao reservationDao = new FakeReservationDao(new ArrayList<>(List.of(
                new ReservationRowDto(1, LocalDate.now().minusDays(1), 1L, 1L, 1L),
                new ReservationRowDto(2, LocalDate.now().minusDays(8), 2L, 2L, 2L))));
        themeService = new ThemeService(new ThemeRepository(reservationDao, themeDao));
    }

    @DisplayName("모든 테마를 조회한다.")
    @Test
    void should_find_all_themes() {
        List<Theme> themes = themeService.findAllThemes();
        assertThat(themes).hasSize(INITIAL_THEME_COUNT);
    }

    @DisplayName("테마를 저장한다.")
    @Test
    void should_save_theme() {
        ThemeDto themeDto = new ThemeDto("n3", "d3", "t3");
        themeService.saveTheme(themeDto);
        assertThat(themeService.findAllThemes()).hasSize(INITIAL_THEME_COUNT + 1);
    }

    @DisplayName("중복된 이름의 테마를 저장하려는 경우 예외가 발생한다.")
    @Test
    void should_throw_exception_when_duplicated_name() {
        ThemeDto themeDto = new ThemeDto("n1", "d", "t");
        assertThatThrownBy(() -> themeService.saveTheme(themeDto))
                .isInstanceOf(DuplicatedException.class)
                .hasMessage("[ERROR] 테마의 이름은 중복될 수 없습니다.");
    }

    @DisplayName("테마를 삭제한다.")
    @Test
    void should_delete_theme() {
        themeService.deleteTheme(1L);
        assertThat(themeService.findAllThemes()).hasSize(INITIAL_THEME_COUNT - 1);
    }

    @DisplayName("존재하지 않는 테마를 삭제하려는 경우 예외가 발생한다.")
    @Test
    void should_throw_exception_when_not_exist_id() {
        assertThatThrownBy(() -> themeService.deleteTheme(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("[ERROR] 존재하지 않는 테마입니다.");
    }

    @DisplayName("최근 일주일 간 가장 인기 있는 테마 10개를 조회한다.")
    @Test
    void should_find_popular_theme_of_week() { // TODO: test case 구체화
        List<Theme> popularThemes = themeService.findPopularThemes();
        assertThat(popularThemes).hasSize(2);
    }
}