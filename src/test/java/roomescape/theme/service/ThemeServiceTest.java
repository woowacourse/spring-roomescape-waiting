package roomescape.theme.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.member.domain.Member;
import roomescape.theme.domain.Theme;
import roomescape.theme.exception.model.ThemeNotFoundException;
import roomescape.theme.repository.FakeThemeRepository;

public class ThemeServiceTest {

    public static final Theme THEME = Theme.of("미르", "미르 방탈출", "썸네일 Url");
    public static final Member MEMBER = Member.of("polla@gmail.com", "polla99");

    private final ThemeService themeService;

    public ThemeServiceTest() {
        this.themeService = new ThemeService(new FakeThemeRepository());
    }

    @Test
    @DisplayName("존재하는 테마가 없을 경우 에러가 발생한다.")
    void notExistThemeReservation() {
        Throwable notExistTheme = assertThrows(ThemeNotFoundException.class,
                () -> themeService.findTheme(1000));
        assertEquals(notExistTheme.getMessage(), new ThemeNotFoundException().getMessage());
    }
}
