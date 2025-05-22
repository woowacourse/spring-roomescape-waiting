package roomescape.integration.fixture;

import org.springframework.stereotype.Component;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeDescription;
import roomescape.domain.theme.ThemeName;
import roomescape.domain.theme.ThemeThumbnail;
import roomescape.repository.ThemeRepository;

@Component
public class ThemeDbFixture {

    private final ThemeRepository themeRepository;

    public ThemeDbFixture(final ThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    public Theme 공포() {
        return createTheme("공포", "공포 테마", "공포.jpg");
    }

    public Theme 로맨스() {
        return createTheme("로멘스", "로멘스 테마", "로멭스.jpg");
    }

    public Theme 커스텀_테마(final String name) {
        return createTheme(name, name + "테마", name + ".jpg");
    }

    public Theme createTheme(
            final String name,
            final String description,
            final String thumbnail
    ) {
        return themeRepository.save(new Theme(
                null,
                new ThemeName(name),
                new ThemeDescription(description),
                new ThemeThumbnail(thumbnail)
        ));
    }
}
