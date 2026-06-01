package roomescape.theme;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import roomescape.theme.exception.ThemeInUseException;

public class ThemeServiceTest {

    private ThemeDao themeDao;
    private ThemeService themeService;

    @BeforeEach
    void setUp() {
        themeDao = mock(ThemeDao.class);
        themeService = new ThemeService(themeDao);
    }

    @Test
    void 존재하지_않는_테마_삭제는_멱등하게_성공한다() {
        when(themeDao.delete(1L))
                .thenReturn(0);

        assertThatCode(() -> themeService.deleteTheme(1L))
                .doesNotThrowAnyException();
        verify(themeDao).delete(1L);
    }

    @Test
    void 예약이_있는_테마는_삭제할_수_없다() {
        when(themeDao.delete(1L))
                .thenThrow(new DataIntegrityViolationException("foreign key violation"));

        assertThatThrownBy(() -> themeService.deleteTheme(1L))
                .isInstanceOf(ThemeInUseException.class);
    }
}
