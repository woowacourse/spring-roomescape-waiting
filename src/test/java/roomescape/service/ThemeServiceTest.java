package roomescape.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import roomescape.dao.ThemeDao;
import roomescape.exception.theme.ThemeInUseException;
import roomescape.exception.theme.ThemeNotFoundException;

public class ThemeServiceTest {

    private ThemeDao themeDao;
    private ThemeService themeService;

    @BeforeEach
    void setUp() {
        themeDao = mock(ThemeDao.class);
        themeService = new ThemeService(themeDao);
    }

    @Test
    void 존재하지_않는_테마는_삭제할_수_없다() {
        when(themeDao.delete(1L))
                .thenReturn(0);

        assertThatThrownBy(() -> themeService.deleteTheme(1L))
                .isInstanceOf(ThemeNotFoundException.class);
    }

    @Test
    void 예약이_있는_테마는_삭제할_수_없다() {
        when(themeDao.delete(1L))
                .thenThrow(new DataIntegrityViolationException("foreign key violation"));

        assertThatThrownBy(() -> themeService.deleteTheme(1L))
                .isInstanceOf(ThemeInUseException.class);
    }
}
