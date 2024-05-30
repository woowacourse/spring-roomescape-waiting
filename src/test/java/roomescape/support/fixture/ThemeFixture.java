package roomescape.support.fixture;

import org.springframework.stereotype.Component;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.repository.ThemeRepository;

@Component
public class ThemeFixture {

    private final ThemeRepository themeRepository;

    public ThemeFixture(ThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    public Theme createTheme() {
        return themeRepository.save(new Theme("테마명", "설명", "썸네일URL"));
    }
}
